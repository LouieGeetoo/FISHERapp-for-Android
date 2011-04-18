package com.sjfc.fisherapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;

import com.sjfc.fisherapp.FisherappDatabase.directoryPeople;

/** Serves as a base class for {@link #DirectoryListActivity.class DirectoryListActivity} and {@link #DirectoryDetailsActivity.class DirectoryDetailsActivity} classes;
 * provides global variables and methods for querying the local database.*/
public class DirectoryActivity extends Activity {

	protected FisherappDatabaseHelper mDatabase = null; 
	protected Cursor mCursor = null;
	protected SQLiteDatabase mDB = null;
	
	public static final String PREFS_NAME = "FisherappPrefs";
	public static final String PREF_FIRST_LAUNCH = "firstLaunch";
	public static final String PREF_SYNCING = "isSyncing";
	public static final String PREF_LAST_SYNCED = "lastSynced";
	public static final String PREF_ENTRY_COUNT = "entryCount";
	public static final String PREF_DIRECTORY_URL = "directoryUrl";
	/** The XML update URL that will be used if one is not found in Preferences. https://genesee2.sjfc.edu:8910/pls/PROD/sjfc_android_app.employees_xml */
	public static final String DEFAULT_DIRECTORY_URL = "https://genesee2.sjfc.edu:8910/pls/PROD/sjfc_android_app.employees_xml";

	/** Runs on Activity creation; initializes the local database. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDatabase = new FisherappDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getWritableDatabase();
		
	}

	/** Runs on Activity destruction; saves Preferences and cleanly closes the database. */
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

	/**
	 * Queries the database and returns a Cursor containing a full or partial list depending on the filter passed in.
	 * @param constraint String to filter the list by, if any.
	 * @return Cursor
	 */
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
				+ directoryPeople.FAC_STAFF_DIR + "," +
				directoryPeople.PEOPLE_TABLE + "."
				+ directoryPeople.DEPARTMENT + "," +
				directoryPeople.PEOPLE_TABLE + "."
				+ directoryPeople._ID
		};
		
		if (constraint == null  ||  constraint.length () == 0)  {
			//  Return the full list
			return queryBuilder.query(mDB, asColumnsToReturn, null, null,
					null, null, directoryPeople.DEFAULT_SORT_ORDER);
		}  else  {
			// Return a filtered list
			String startsWith = constraint.toString().trim()+"%";
			String contains = "%"+startsWith;
				return mDB.query(directoryPeople.PEOPLE_TABLE, asColumnsToReturn,
						"LAST_NAME like ? OR FIRST_NAME like ? OR MIDDLE_NAME like ?" +
						" OR FAC_STAFF_DIR like ? OR DEPARTMENT like ? OR JOB_TITLE like ?",
						new String[]{startsWith,startsWith,startsWith,startsWith,contains,contains}, null, null, null);
		}
	}
}
