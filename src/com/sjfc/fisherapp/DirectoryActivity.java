package com.sjfc.fisherapp;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.sjfc.fisherapp.R;

public class DirectoryActivity extends Activity {

	protected FisherappDatabaseHelper mDatabase = null; 
	protected Cursor mCursor = null;
	protected SQLiteDatabase mDB = null;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDatabase = new FisherappDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getWritableDatabase();
		
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mDB != null)
		{
			mDB.close();
		}
		
		if(mDatabase != null)
		{
			mDatabase.close();
		}
	}
	
	

}