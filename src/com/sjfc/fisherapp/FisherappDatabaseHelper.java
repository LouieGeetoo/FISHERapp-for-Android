package com.sjfc.fisherapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sjfc.fisherapp.FisherappDatabase.directoryPeople;

/**
 * Implements the local MySQL database for holding F/S directory entries.
 */
class FisherappDatabaseHelper extends SQLiteOpenHelper {
	/** The local database's name and filename. */
	private static final String DATABASE_NAME = "fisherapp.db";
	/** Version of the database. If the database schema changes, this is implemented. Used by the onUpgrade method.
	 */
	private static final int DATABASE_VERSION = 1;
	
	FisherappDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	/**
	 * Runs when the database doesn't already exist.
	 * Creates two tables: PEOPLE_TABLE for permanent entry storage and TEMP_TABLE for temporary storage during network XML parsing.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " +
			directoryPeople.PEOPLE_TABLE + " ("
			+ directoryPeople._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
			+ directoryPeople.LAST_NAME + " TEXT,"
			+ directoryPeople.FIRST_NAME + " TEXT,"
			+ directoryPeople.MIDDLE_NAME + " TEXT,"
			+ directoryPeople.FAC_STAFF_DIR + " TEXT,"
			+ directoryPeople.JOB_TITLE + " TEXT,"
			+ directoryPeople.DEPARTMENT + " TEXT,"
			+ directoryPeople.OFFICE + " TEXT,"
			+ directoryPeople.PHONE_NUMBER + " TEXT,"
			+ directoryPeople.EMAIL + " TEXT"
			+ ");");
		db.execSQL("CREATE TABLE " +
			directoryPeople.TEMP_TABLE + " ("
			+ directoryPeople._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
			+ directoryPeople.LAST_NAME + " TEXT,"
			+ directoryPeople.FIRST_NAME + " TEXT,"
			+ directoryPeople.MIDDLE_NAME + " TEXT,"
			+ directoryPeople.FAC_STAFF_DIR + " TEXT,"
			+ directoryPeople.JOB_TITLE + " TEXT,"
			+ directoryPeople.DEPARTMENT + " TEXT,"
			+ directoryPeople.OFFICE + " TEXT,"
			+ directoryPeople.PHONE_NUMBER + " TEXT,"
			+ directoryPeople.EMAIL + " TEXT"
			+ ");");
	}
	
	/**
	 * Runs if the DATABASE_VERSION of the app code if higher than that of the stored database.
	 * Can move around data, rename fields, or whatever is necessary to ensure compatibility while saving existing data.
	 * Since this is the first version of the database, it does nothing.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
	
	public void getAllSuggestedValues(String partialValue) {
		
	}

}