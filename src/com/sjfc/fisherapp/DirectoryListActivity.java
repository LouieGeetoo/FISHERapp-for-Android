package com.sjfc.fisherapp;

import java.net.URL;
import java.util.Calendar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sjfc.fisherapp.FisherappDatabase.directoryPeople;

/** F.D.1 DirectoryListActivity */
public class DirectoryListActivity extends DirectoryActivity {
	
	/** F.D.1.V global variables */
	public static String directoryUrl;
	private static  Handler mHandler = new Handler();
	private static Thread parseThread;
	private static SimpleCursorAdapter adapter;
	private static boolean syncing = false;
	private static boolean cancelSync;
	private static boolean masterSyncSetting;
	private static boolean firstLaunch;
	private static int entryCount;
	private ProgressBar mProgress;

	/** F.D.1.1 onCreate */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		/** Restore preferences */
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		//SharedPreferences.Editor prefsEditor = settings.edit();
		syncing = settings.getBoolean(PREF_SYNCING, false);
		directoryUrl = settings.getString(PREF_DIRECTORY_URL, DEFAULT_DIRECTORY_URL);
		firstLaunch = settings.getBoolean(PREF_FIRST_LAUNCH, true);
		entryCount = settings.getInt(PREF_ENTRY_COUNT, 634);
		
		/* Check whether the user has system-wide background syncing enabled */
		ConnectivityManager mgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		getContentResolver();
		/* Check OS "Auto-sync" setting */
		masterSyncSetting = ContentResolver.getMasterSyncAutomatically();
		/* Check OS "Background data" setting */
		if(!mgr.getBackgroundDataSetting()) {
			masterSyncSetting = false;
		}
		Log.d("Fisherapp", "onCreate: syncing = " + syncing + " | directoryUrl = " + directoryUrl);
		Log.d("Fisherapp", "masterSyncSetting = " + masterSyncSetting);
		
		setContentView(R.layout.directory_list);
		updateSyncIndicator(syncing);
		updateFirstSyncMessage(firstLaunch);
		
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		mProgress.setMax(entryCount);

		/** Set yellow bar title and status text */
		TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
		txtTitle.setText(R.string.directory);

		/** Listen for logo click (for future "home menu" access) */
		ImageView fisherappLogo = (ImageView) findViewById(R.id.imgFISHERappLogo);
		fisherappLogo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

			}
		});
		
		fillPeopleListView();
		startSync(true);
	}
	
	/** NEW METHOD startSync */
	public void startSync(boolean auto) {
		if(!syncing) {
			if(auto) {
				if(firstLaunch) {
					if (isTimeForSync()) {
						startXMLParseThread();
					}
				} else if(masterSyncSetting) {
					if (isTimeForSync()) {
						startXMLParseThread();
					}
				}
			} else {
				startXMLParseThread();
			}
		}	
	}
	
	/** F.D.1.2 isTimeForSync */
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
	
	/** F.D.1.3 fillPeopleListView */
	private void fillPeopleListView() {
		mCursor = getDirectoryList(null);
		
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
		av.setFastScrollEnabled(true);
		av.setTextFilterEnabled(true);
		
		EditText etext=(EditText)findViewById(R.id.search_box);
		etext.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void afterTextChanged(Editable s) {
				adapter.getFilter().filter(s.toString());
			}
		});
		
		adapter.setFilterQueryProvider(new FilterQueryProvider() {
			public Cursor runQuery(CharSequence constraint) {
				return getDirectoryList(constraint);
			}
		});
		
		/* Listen for list item click */
		av.setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {						
						Intent intent = new Intent(DirectoryListActivity.this,
								DirectoryDetailsActivity.class);
						intent.putExtra(DirectoryDetailsActivity.KEY_PERSON_ID, id);
						startActivity(intent);
					}
				});
	}
	
	/** F.D.1.4 startXMLParseThread */
	private void startXMLParseThread() {
		if (!syncing) {
			Toast.makeText(getApplicationContext(),
					"Updating directory...", Toast.LENGTH_SHORT).show();
			
			parseThread = new Thread () {
				
				boolean success = false;
				
				Handler hUpdateStatus = new Handler(){
					public void handleMessage(Message msg) {
						updateSyncIndicator(syncing);
						updateFirstSyncMessage(firstLaunch);
					}
					};
					
				Handler hUpdateProgressBar = new Handler(){
					public void handleMessage(Message msg) {
						mProgress.setProgress(entryCount);
					}
					};
					
				Handler hNotifySyncFailed = new Handler(){
					public void handleMessage(Message msg) {
						Toast.makeText(getApplicationContext(),
								"Sync failed. Is there a data connection?", Toast.LENGTH_LONG).show();
					}
					};
					
					Handler hNotifySyncCancelled = new Handler(){
						public void handleMessage(Message msg) {
							Toast.makeText(getApplicationContext(),
									"Directory sync cancelled. (Press Home next time to let it run" +
									" in the background.)", Toast.LENGTH_LONG).show();							
						}
						};
				
				public void run() {
					try {
						if (!syncing) {
							syncing = true;
							cancelSync = false;
							hUpdateStatus.sendEmptyMessage(0);
							
							if(!firstLaunch)
								sleep(5000);
							
							SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
							SharedPreferences.Editor prefsEditor = settings.edit();
							prefsEditor.commit();
							
							mDB.delete(directoryPeople.TEMP_TABLE, null, null);
							
							XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
							XmlPullParser parser = parserCreator.newPullParser();
							
							URL feed = new URL(directoryUrl);
							parser.setInput(feed.openStream(), null);
							
							ContentValues entry = new ContentValues();
							int parserEvent = parser.getEventType();
							String tag = "";
							String value = "";
							entryCount = 0;
							
							while (parserEvent != XmlPullParser.END_DOCUMENT) {
								if(interrupted()) {
									syncing = false;
									success = false;
									cancelSync = true;
									hNotifySyncCancelled.sendEmptyMessage(0);
									break;
								}
								
								if(parserEvent == XmlPullParser.START_TAG) {
									tag = parser.getName();
									if (isPeopleField(tag)) {
										parserEvent = parser.next();
										value =  parser.getText();
										entry.put(tag, value.replaceAll("\n", ""));
									}
								}
								if(parserEvent == XmlPullParser.END_TAG && parser.getName().compareTo("ROW") == 0) {
									mDB.insert(directoryPeople.TEMP_TABLE, null, entry);
									entryCount++;
									Log.d("Fisherapp", "Entry " + entryCount + " added to table_temp.");
									hUpdateProgressBar.sendEmptyMessage(0);
									entry.clear();
								}
							 	parserEvent = parser.next();
							}
							
							if(!cancelSync) {
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
								firstLaunch = false;
								
								hUpdateStatus.sendEmptyMessage(0);

								Calendar cal = Calendar.getInstance();
								int weekNow = cal.get(Calendar.WEEK_OF_YEAR);
								int yearNow = cal.get(Calendar.YEAR);
								Log.d("Fisherapp", "Sync finished: Week " + weekNow + " of " + yearNow);
								String thisWeek = yearNow + "." + weekNow;
								
								prefsEditor.putString(PREF_LAST_SYNCED, thisWeek);
								prefsEditor.putBoolean(PREF_FIRST_LAUNCH, firstLaunch);
								prefsEditor.putInt(PREF_ENTRY_COUNT, entryCount);
								prefsEditor.commit();
							}
						}
					} catch (Exception e) {
						Log.e("Fisherapp", "XML Parse Error: " + e.toString());
						success = false;
						syncing = false;
						if (firstLaunch) {
							hNotifySyncFailed.sendEmptyMessage(0);
						}
						hUpdateStatus.sendEmptyMessage(0);
						SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
						SharedPreferences.Editor prefsEditor = settings.edit();
						prefsEditor.commit();
					}

					mHandler.post(new Runnable() {
						public void run() {
							if (!syncing) {
								if (success) {
									Toast.makeText(getApplicationContext(),
											"Directory updated!", Toast.LENGTH_SHORT).show();
								} else {
								}
								adapter.getCursor().requery();
								Log.d("Fisherapp", "ListView Cursor refreshed.");
								hUpdateStatus.sendEmptyMessage(0);
							}
						}
					});
				}
			};
			parseThread.start();
		}		
	}
	
	/** F.D.1.5 updateFirstSyncMessage */
	private void updateFirstSyncMessage(boolean first) {
		View firstSyncMessage = (View) findViewById(R.id.emptyBox);
		View filterBox = (View) findViewById(R.id.search_box);
		if (first) {
			firstSyncMessage.setVisibility(View.VISIBLE);
			filterBox.setVisibility(View.GONE);
		} else {
			firstSyncMessage.setVisibility(View.GONE);
			filterBox.setVisibility(View.VISIBLE);
		}
	}
	
	/** F.D.1.6 isPeopleField */
	public boolean isPeopleField(String tag) {
		if (tag.equals(directoryPeople.LAST_NAME))
			return true;
		if (tag.equals(directoryPeople.FIRST_NAME))
			return true;
		if (tag.equals(directoryPeople.MIDDLE_NAME))
			return true;
		if (tag.equals(directoryPeople.FAC_STAFF_DIR))
			return true;
		if (tag.equals(directoryPeople.JOB_TITLE))
			return true;
		if (tag.equals(directoryPeople.DEPARTMENT))
			return true;
		if (tag.equals(directoryPeople.OFFICE))
			return true;
		if (tag.equals(directoryPeople.PHONE_NUMBER))
			return true;
		if (tag.equals(directoryPeople.EMAIL))
			return true;
		return false;
	}
	
	/** F.D.1.7 updateSyncIndicator */
	void updateSyncIndicator(boolean visible) {
		View indicator = (View) findViewById(R.id.progSyncStatus);
		int visibility = indicator.getVisibility();
		if(visible && visibility == View.GONE)
			indicator.setVisibility(View.VISIBLE);
		if (!visible && visibility == View.VISIBLE)
			indicator.setVisibility(View.GONE);
	}
	
	/** NEW METHOD onDestroy */
	public void onDestroy() {
		super.onDestroy();
		parseThread.interrupt();
	}

	/** NEW METHOD onCreateOptionsMenu */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if(syncing && !firstLaunch) {
			menu.add(0, 0, 0, "Cancel sync")
				.setIcon(R.drawable.ic_menu_stop);
		} else {
			menu.add(0, 0, 0, "Sync now")
				.setIcon(R.drawable.ic_menu_refresh);
		}
		return true;
	}
	
	/** NEW METHOD onPrepareOptionsMenu */
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.removeItem(0);
		if(syncing && !firstLaunch) {
			menu.add(0, 0, 0, "Cancel sync")
				.setIcon(R.drawable.ic_menu_stop);
		} else {
			menu.add(0, 0, 0, "Sync now")
				.setIcon(R.drawable.ic_menu_refresh);
		}
		return true;
	}
	
	/** NEW METHOD onOptionsItemSelected */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case 0:
				if(syncing && !firstLaunch) {
					parseThread.interrupt();
				} else {
					startSync(false);
				}
			default:
				//invalidateOptionsMenu(); /* Needed for Android 3.0 (tablets) */
				return true;
		}
	}
}
