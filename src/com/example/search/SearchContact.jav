package com.example.search;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;


public class SearchContact
{
	
	private static SearchContact mSearchContact;
	private static Context mContext;
	private List<ContactInfo> mContactList = new ArrayList<ContactInfo>();
	
	private SearchContact()
	{
		ContentResolver cr = mContext.getContentResolver();
		Cursor cur = cr.query( ContactsContract.Contacts.CONTENT_URI , null , null , null , null );
		if( cur.getCount() > 0 )
		{
			while( cur.moveToNext() )
			{
				String id = cur.getString( cur.getColumnIndex( ContactsContract.Contacts._ID ) );
				String name = cur.getString( cur.getColumnIndex( ContactsContract.Contacts.DISPLAY_NAME ) );
				if( Integer.parseInt( cur.getString( cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER ) ) ) > 0 )
				{
					Cursor pCur = cr.query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI , null , ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?" , new String[]{ id } , null );
					while( pCur.moveToNext() )
					{
						String phoneNo = pCur.getString( pCur.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER ) );
						InputStream is = openPhoto( Long.valueOf( id ) );
						Bitmap photo = BitmapFactory.decodeStream( is );
						mContactList.add( new ContactInfo( Long.valueOf( id ) , name , phoneNo , photo ) );
					}
					pCur.close();
				}
			}
		}
	}
	
	public static SearchContact getInstance(
			Context c )
	{
		if( mSearchContact == null )
		{
			mContext = c;
			mSearchContact = new SearchContact();
		}
		return mSearchContact;
	}
	
	public List<Object> search(
			String str )
	{
		List<Object> resultList = new ArrayList<Object>();
		for( ContactInfo contactInfo : mContactList )
		{
			String name = contactInfo.getName();
			if( name.toLowerCase().contains( str.toLowerCase() ) )
			{
				resultList.add( contactInfo );
			}
		}
		return resultList;
	}
	
	private InputStream openPhoto(
			long contactId )
	{
		Uri contactUri = ContentUris.withAppendedId( Contacts.CONTENT_URI , contactId );
		Uri photoUri = Uri.withAppendedPath( contactUri , Contacts.Photo.CONTENT_DIRECTORY );
		Cursor cursor = mContext.getContentResolver().query( photoUri , new String[]{ Contacts.Photo.PHOTO } , null , null , null );
		if( cursor == null )
		{
			return null;
		}
		try
		{
			if( cursor.moveToFirst() )
			{
				byte[] data = cursor.getBlob( 0 );
				if( data != null )
				{
					return new ByteArrayInputStream( data );
				}
			}
		}
		finally
		{
			cursor.close();
		}
		return null;
	}
}
