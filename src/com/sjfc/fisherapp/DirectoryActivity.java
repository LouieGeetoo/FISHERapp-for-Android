package com.sjfc.fisherapp;

import com.sjfc.fisherapp.FisherappDatabase.directoryPeople;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.view.View;

public class DirectoryActivity extends Activity {

	protected FisherappDatabaseHelper mDatabase = null; 
	protected Cursor mCursor = null;
	protected SQLiteDatabase mDB = null;
	
	public static final String PREFS_NAME = "FisherappPrefs";
	public static final String PREF_SYNCING = "isSyncing";
	public static final String PREF_LAST_SYNCED = "lastSynced";
	public static final String PREF_DIRECTORY_URL = "directoryUrl";
	public static final String DEFAULT_DIRECTORY_URL = "https://genesee2.sjfc.edu:8910/pls/PROD/sjfc_android_app.employees_xml";

		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDatabase = new FisherappDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getWritableDatabase();
		
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		boolean syncing = settings.getBoolean(PREF_SYNCING, false);
		if(mDB != null && syncing == false)
		{
			mDB.close();
		}
		
		if(mDatabase != null && syncing == false)
		{
			mDatabase.close();
		}
	}
	
	public Cursor getDirectoryList (CharSequence constraint)  {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    	queryBuilder.setTables(
    		directoryPeople.PEOPLE_TABLE
    	);
    	
    	String asColumnsToReturn[] = { 
    			directoryPeople.PEOPLE_TABLE + "."
    			+ directoryPeople.LAST_NAME + "," +
    			directoryPeople.PEOPLE_TABLE + "."
    			+ directoryPeople.FIRST_NAME + "," +
    			directoryPeople.PEOPLE_TABLE + "."
    			+ directoryPeople.MIDDLE_NAME + "," +
    			directoryPeople.PEOPLE_TABLE + "."
    			+ directoryPeople.JOB_TITLE + "," +
    			directoryPeople.PEOPLE_TABLE + "."
    			+ directoryPeople._ID
    	};
    	
	    if (constraint == null  ||  constraint.length () == 0)  {
	        //  Return the full list
	    	return queryBuilder.query(mDB, asColumnsToReturn, null, null,
	    			null, null, directoryPeople.DEFAULT_SORT_ORDER);
	    }  else  {
	        return mDB.query(directoryPeople.PEOPLE_TABLE, asColumnsToReturn, "LAST_NAME like '%'" + 
	        	constraint.toString() + "'%'", null, null, null,
	        	"CASE WHEN LAST_NAME like '" + constraint.toString() +
	        	"%' THEN 0 ELSE 1 END, LAST_NAME");
	    }
	}

}