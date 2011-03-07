package com.sjfc.fisherapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sjfc.fisherapp.FisherappDatabase.directoryPeople;

class FisherappDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "fisherapp.db";
	private static final int DATABASE_VERSION = 1;
	
	FisherappDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
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
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This is where things are moved around
		// or new tables and columns are added
		// for the first start after a update to the app
		// with a new database structure.
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}