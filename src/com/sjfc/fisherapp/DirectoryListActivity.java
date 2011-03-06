package com.sjfc.fisherapp;

import java.net.URL;
import java.util.Calendar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sjfc.fisherapp.FisherappDatabase.directoryPeople;

/** F.1 DirectoryListActivity */
public class DirectoryListActivity extends DirectoryActivity {
	
	/** F.1.V global variables */
	public static final String PREFS_NAME = "FisherappPrefs";
	public static final String PREF_SYNCING = "isSyncing";
	public static final String PREF_LAST_SYNCED = "lastSynced";
	public static final String PREF_DIRECTORY_URL = "directoryUrl";
	public static final String DEFAULT_DIRECTORY_URL = "https://genesee2.sjfc.edu:8910/pls/PROD/sjfc_android_app.employees_xml";

	public static String directoryUrl;
	public static final Handler mHandler = new Handler();
	private static SimpleCursorAdapter adapter;
	private static boolean syncing = false;
	
	/** F.1.1 onCreate */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        /** Restore preferences */
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	//SharedPreferences.Editor prefsEditor = settings.edit();
        syncing = settings.getBoolean(PREF_SYNCING, false);
        directoryUrl = settings.getString(PREF_DIRECTORY_URL, DEFAULT_DIRECTORY_URL);
        
        setContentView(R.layout.directory_list);

        /** Set yellow bar title and status text */
        TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.directory);
        TextView txtUpdateStatus = (TextView)findViewById(R.id.txtUpdateStatus);
        if (syncing) {
        	txtUpdateStatus.setText(R.string.syncing);
        } else {
        	txtUpdateStatus.setText(R.string.blank);
        }

    	/** Listen for logo click -> manual sync */
        ImageView fisherappLogo = (ImageView) findViewById(R.id.imgFISHERappLogo);
        fisherappLogo.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		/** Refresh! */
        		startXMLParseThread();
        	}
        });
        
        fillPeopleListView();
        
        if (isTimeForSync()) {
        	startXMLParseThread();
        }

    }
    
    /** F.1.2 isTimeForSync */
    private boolean isTimeForSync() {
    	Calendar cal = Calendar.getInstance();
    	int weekNow = cal.get(Calendar.WEEK_OF_YEAR);
    	int yearNow = cal.get(Calendar.YEAR);
    	Log.d("Fisherapp", "Currently it is Week " + weekNow + " of " + yearNow);
    	String thisWeek = yearNow + "." + weekNow;
    	
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String lastSync = settings.getString(PREF_LAST_SYNCED, "0.0");
    	
        if (thisWeek.compareTo(lastSync) != 0) {
        	Log.d("Fisherapp", "Time for sync. This week is: " + thisWeek + ", Last Sync: " + lastSync);
        	return true;
        } else {
        	Log.d("Fisherapp", "No sync needed. This week is: " + thisWeek + ", Last Sync: " + lastSync);
        	return false;
        }
    }
    
    /** F.1.3 fillPeopleListView */
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
    	
    	/** Listen for list item click */
        av.setOnItemClickListener(
        		new AdapterView.OnItemClickListener() {
    	    		public void onItemClick(AdapterView<?> parent, View view,
    						int position, long id) {	    			
    	    			Toast.makeText(getApplicationContext(),
    	    					"Clicked id = " + id, Toast.LENGTH_SHORT).show();
    	    			
    	    			Intent intent = new Intent(DirectoryListActivity.this,
    	    					DirectoryDetailsActivity.class);
    	    			intent.putExtra(DirectoryDetailsActivity.KEY_PERSON_ID, id);
    	    			startActivity(intent);
    				}
        		});
    }
    
    /** F.1.4 startXMLParseThread */
    private void startXMLParseThread() {
    	if (!syncing) {
    		TextView txtUpdateStatus = (TextView)findViewById(R.id.txtUpdateStatus);
            txtUpdateStatus.setText(R.string.syncing);
            
            new Thread () {
        		
        		boolean success = false;
        		
        		public void run() {
        			try {
        				if (!syncing) {
        					syncing = true;
        					SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        					SharedPreferences.Editor prefsEditor = settings.edit();
							prefsEditor.putBoolean(PREF_SYNCING, syncing);
							prefsEditor.commit();
            				
        		    		mDB.delete(directoryPeople.TEMP_TABLE, null, null);
        		    		
        		    	    XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
        		    	    XmlPullParser parser = parserCreator.newPullParser();
        		    	    
        		    	    //String XMLaddress = DEFAULT_DIRECTORY_URL;
        	    	        URL feed = new URL(directoryUrl);
        		    	    parser.setInput(feed.openStream(), null);
        		    	    
        		    	    ContentValues entry = new ContentValues();
        		    	    int parserEvent = parser.getEventType();
        		    	    String tag = "";
        		    	    String value = "";
        		    	    
        		    	    while (parserEvent != XmlPullParser.END_DOCUMENT) {
        		    	    	if(parserEvent == XmlPullParser.START_TAG) {
        		    	    		tag = parser.getName();
        		    	    		if (isPeopleField(tag)) {
        		    	    			parserEvent = parser.next();
        		    	    			value =  parser.getText();
        		    		    		entry.put(tag, value);
        		    		    		//Log.d("Fisherapp", "Parsed " + tag + ": " + value);
        		    	    		}
        		    	    	}
        		    	    	if(parserEvent == XmlPullParser.END_TAG && parser.getName().compareTo("ROW") == 0) {
        		    	    		mDB.insert(directoryPeople.TEMP_TABLE, null, entry);
        		    	    		Log.d("Fisherapp", "Entry added to table_temp.");
        		    				entry.clear();
        		    	    	}
        		    	     	parserEvent = parser.next();
        		    	    }
        		    	    Log.d("Fisherapp", "Finished parsing People XML.");
        		    	    
        		    	    /** Copy temp table to permanent table */
        		        	mDB.delete(directoryPeople.PEOPLE_TABLE, null, null);
        		        	
        		        	String copyQuery = "INSERT INTO " + directoryPeople.PEOPLE_TABLE
        		        		+ " SELECT * FROM " + directoryPeople.TEMP_TABLE;
        		        	
        		        	mDB.execSQL(copyQuery);
        		        	
        		        	/** Delete contents of temp table */
        		        	mDB.delete(directoryPeople.TEMP_TABLE, null, null);
        		        	Log.d("Fisherapp", "Data copied to table_people.");
        		        	success = true;
        		        	syncing = false;
        		        	
        		        	Calendar cal = Calendar.getInstance();
        		        	int weekNow = cal.get(Calendar.WEEK_OF_YEAR);
        		        	int yearNow = cal.get(Calendar.YEAR);
        		        	Log.d("Fisherapp", "Sync finished: Week " + weekNow + " of " + yearNow);
        		        	String thisWeek = yearNow + "." + weekNow;
        		        	
        		        	prefsEditor.putString(PREF_LAST_SYNCED, thisWeek);
        		        	prefsEditor.putBoolean(PREF_SYNCING, syncing);
							prefsEditor.commit();
        				}
    		        	
        			} catch (Exception e) {
        				Log.e("Fisherapp", "XML Parse Error: " + e.toString());
        				success = false;
        				syncing = false;
        				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        				SharedPreferences.Editor prefsEditor = settings.edit();
        				prefsEditor.putBoolean("isSyncing", syncing);
						prefsEditor.commit();
        			}
        			
        			mHandler.post(new Runnable() {
        				public void run() {
        					if (!syncing) {
        						TextView txtUpdateStatus = (TextView)findViewById(R.id.txtUpdateStatus);
            		        	if (success) {
            			        	txtUpdateStatus.setText(R.string.synced);
            		        	} else {
            		            	txtUpdateStatus.setText(R.string.sync_failed);
            		        	}
            		        	// Refresh the ListView
            		        	adapter.getCursor().requery();
            		        	//adapter.notifyDataSetChanged();
            		        	Log.d("Fisherapp", "ListView Cursor refreshed.");
            		        	updateFirstSyncMessage();
        					}
        				}
        			});
        		}
        	}.start();
    	}    	
    }
    
    /** F.1.5 updateFirstSyncMessage */
    private void updateFirstSyncMessage() {
    	View firstSyncMessage = (View) findViewById(R.id.emptyBox);
    	if (mCursor.getCount() == 0) {
    		firstSyncMessage.setVisibility(View.VISIBLE);
    	} else {
    		firstSyncMessage.setVisibility(View.GONE);
    	}
    }
    
    /** F.1.6 isPeopleField */
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