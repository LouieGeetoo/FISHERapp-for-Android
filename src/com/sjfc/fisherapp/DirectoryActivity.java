package com.sjfc.fisherapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

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
	
	

}