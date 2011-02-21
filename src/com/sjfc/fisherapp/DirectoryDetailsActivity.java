package com.sjfc.fisherapp;

import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentValues;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.sjfc.fisherapp.FisherappDatabase.directoryPeople;

public class DirectoryDetailsActivity extends DirectoryActivity {
	
	private SimpleCursorAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.directory_details);

        /** Set yellow bar title and status text */
        TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.directory);
        TextView txtUpdateStatus = (TextView)findViewById(R.id.txtUpdateStatus);
        txtUpdateStatus.setText(R.string.blank);

    	/** Listen for logo push */
        ImageView fisherappLogo = (ImageView) findViewById(R.id.imgFISHERappLogo);
        fisherappLogo.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		// TODO: Return to Directory list
        	}
        });
        
        fillPeopleListView();
        
        
    }
    
    private void fillPeopleListView() {
        // Populate the ListView
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
    	
    	mCursor = queryBuilder.query(mDB, asColumnsToReturn, null, null,
    			null, null, directoryPeople.DEFAULT_SORT_ORDER);
    	
    	updateFirstSyncMessage();
    	
    	startManagingCursor(mCursor);
    	
    	adapter = new SimpleCursorAdapter(this,
    			R.layout.directory_people_item, mCursor,
    			new String[]{
    				directoryPeople.LAST_NAME,
    				directoryPeople.FIRST_NAME,
    				directoryPeople.MIDDLE_NAME,
    				directoryPeople.JOB_TITLE},
    			new int[]{
    				R.id.txtLastName,
    				R.id.txtFirstName,
    				R.id.txtMiddle,
    				R.id.txtTitle} 
    	); 
    	
    	ListView av = (ListView)findViewById(R.id.listPeople);
    	av.setAdapter(adapter);
    }
    

    
    private void updateFirstSyncMessage() {
    	/*
    	View firstSyncMessage = (View) findViewById(R.id.emptyBox);
    	if (mCursor.getCount() == 0) {
    		firstSyncMessage.setVisibility(View.VISIBLE);
    	} else {
    		firstSyncMessage.setVisibility(View.GONE);
    	}
    	*/
    }
    
	public boolean isPeopleField(String tag) {
		if (tag.compareTo(directoryPeople.LAST_NAME) == 0)
			return true;
		if (tag.compareTo(directoryPeople.FIRST_NAME) == 0)
			return true;
		if (tag.compareTo(directoryPeople.MIDDLE_NAME) == 0)
			return true;
		if (tag.compareTo(directoryPeople.FAC_STAFF_DIR) == 0)
			return true;
		if (tag.compareTo(directoryPeople.JOB_TITLE) == 0)
			return true;
		if (tag.compareTo(directoryPeople.DEPARTMENT) == 0)
			return true;
		if (tag.compareTo(directoryPeople.OFFICE) == 0)
			return true;
		if (tag.compareTo(directoryPeople.PHONE_NUMBER) == 0)
			return true;
		if (tag.compareTo(directoryPeople.EMAIL) == 0)
			return true;
		return false;
	}
}