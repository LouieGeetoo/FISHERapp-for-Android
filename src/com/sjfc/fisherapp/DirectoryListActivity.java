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
import android.net.NetworkInfo;
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

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.sjfc.fisherapp.FisherappDatabase.directoryPeople;

/** Displays full directory list with filter box; manages directory updates.
 *  */
public class DirectoryListActivity extends DirectoryActivity {
	/** The URL for the XML that the directory data is pulled from during an update. Defaults to https://genesee2.sjfc.edu:8910/pls/PROD/sjfc_android_app.employees_xml */
	public static String directoryUrl;
	/** The thread that will be used to perform network updates. */
	private static Thread parseThread;
	/** The handler that the update thread will use to communicate with the UI thread. */
	private static  Handler mHandler = new Handler();
	/** The adapter that binds the local database to the ListView. */
	private static SimpleCursorAdapter adapter;
	/** Whether an update parse thread is currently running. */
	private static boolean syncing = false;
	/** Whether the parse thread has encountered the XmlPullParser zero results bug. */
	private static boolean bugged = false;
	/** Whether the running update was intentionally interrupted. */
	private static boolean cancelSync;
	/** Reflects the system-wide Background Data and Auto-Sync settings */
	private static boolean masterSyncSetting;
	/** Whether this is the first time the user has launched the app. */
	private static boolean firstLaunch;
	/** Data connectivity info, checked in onCreate. */ 
	NetworkInfo mNetworkInfo;
	/** The number of people entries parsed in the current/just-run update. */
	private static int entryCount;
	/** The ProgressBar for the first-launch message and progress display. */
	private ProgressBar mProgress;

	/** 
	 * Initializes and displays the Directory List View.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		/* Restore preferences */
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		//SharedPreferences.Editor prefsEditor = settings.edit();
		syncing = settings.getBoolean(PREF_SYNCING, false);
		directoryUrl = settings.getString(PREF_DIRECTORY_URL, DEFAULT_DIRECTORY_URL);
		firstLaunch = settings.getBoolean(PREF_FIRST_LAUNCH, true);
		entryCount = settings.getInt(PREF_ENTRY_COUNT, 634);
		
		/* Start Google Analytics tracking */
		if (true) { // TODO: Make tracking only occur if the user has agreed/opted-in (use Preferences)
			tracker = GoogleAnalyticsTracker.getInstance();
			tracker.start(this.getString(R.string.analytics_api_key), 10, getApplicationContext());
			tracker.setDebug(true); //TODO: Remove this DEBUG line for deployment
			tracker.setDryRun(true);  //TODO: Remove this DEBUG line for deployment
		}
		
		/* Check whether the user has system-wide background syncing enabled */
		ConnectivityManager mgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		getContentResolver();
		/* Check OS "Auto-sync" setting */
		masterSyncSetting = ContentResolver.getMasterSyncAutomatically();
		/* Check OS "Background data" setting */
		if(!mgr.getBackgroundDataSetting()) {
			masterSyncSetting = false;
		}
		/* Check whether there is even an active data connection */
		mNetworkInfo = mgr.getActiveNetworkInfo();
			
		Log.d("Fisherapp", "onCreate: syncing = " + syncing + " | directoryUrl = " + directoryUrl);
		Log.d("Fisherapp", "masterSyncSetting = " + masterSyncSetting);
		Log.d("Fisherapp", "Network connectivity: " + mNetworkInfo);
		
		setContentView(R.layout.directory_list);
		updateSyncIndicator(syncing);
		updateFirstSyncMessage(firstLaunch);
		
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		mProgress.setMax(entryCount);

		/* Set yellow bar title and status text */
		TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
		txtTitle.setText(R.string.directory);

		/* Listen for logo click (for future "home menu" access) */
		ImageView fisherappLogo = (ImageView) findViewById(R.id.imgFISHERappLogo);
		fisherappLogo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				/* Do nothing. */
			}
		});
		
		/* Record a List activity view in Google Analytics */
		tracker.trackPageView("/" + this.getLocalClassName());
		
		fillPeopleListView();
		startSync(true);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		/* Record the fact that the user returned to the list */
		if(true) { //TODO
			tracker.trackEvent("ui_interaction",				// category
								"return_to_directory_list",		// action
								this.getLocalClassName(),		// label
								0);								// value
		}
	}
	
	/** Helper class to start the update parse thread.
	 * Helps distinguish between auto-sync, manual sync, and first-launch sync.
	 * @param auto	Whether this update was initiated automatically (as opposed to through direct user action).
	 * */
	public void startSync(boolean auto) {
		cancelSync = false;
		if(!syncing && mNetworkInfo.isConnected()) {
			if(auto) {
				if(firstLaunch) {
					if (isTimeForSync()) {
						if(true) { // TODO
							tracker.trackEvent("sync",				// category
												"auto_sync",			// action
												"first_launch",					// label
												0);						// value
						}
						Toast.makeText(getApplicationContext(),
								getResources().getString(R.string.syncing), Toast.LENGTH_SHORT).show();
						startXMLParseThread();
					}
				} else if(masterSyncSetting) {
					if (isTimeForSync()) {
						if(true) { // TODO
							tracker.trackEvent("sync",				// category
												"auto_sync",			// action
												"is_time_for_sync",					// label
												0);						// value
						}
						startXMLParseThread();
					}
				}
			} else {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.syncing), Toast.LENGTH_SHORT).show();
				startXMLParseThread();
			}
		}
		else if (!mNetworkInfo.isConnected() && (isTimeForSync() || !auto)) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
		}
	}
	
	/** Checks whether it is time to start an auto-update.
	 * Compares current week-of-the-year with the stored week-of-the-year of the last successful sync.
	 * @return boolean
	 * */
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
	
	/** Fills the ListView with local database data.
	 * Queries the local MySQL database for full or filtered list and displays the results. */
	private void fillPeopleListView() {
		
		mCursor = getDirectoryList(null);
		startManagingCursor(mCursor);
		
		adapter = new SimpleCursorAdapter(this,
				R.layout.directory_list_item, mCursor,
				new String[]{
					directoryPeople.LAST_NAME,
					directoryPeople.FIRST_NAME,
					directoryPeople.MIDDLE_NAME,
					directoryPeople.JOB_TITLE,
					directoryPeople.FAC_STAFF_DIR,
					directoryPeople.DEPARTMENT},
				new int[]{
					R.id.txtLastName,
					R.id.txtFirstName,
					R.id.txtMiddle,
					R.id.txtTitle,
					R.id.txtGroup,
					R.id.txtDepartment} 
		); 
		
		ListView av = (ListView)findViewById(R.id.listPeople);
		av.setAdapter(adapter);
		av.setFastScrollEnabled(true);
		av.setTextFilterEnabled(true);
		
		EditText etext=(EditText)findViewById(R.id.search_box);
		etext.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) { }
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
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
						startActivityForResult(intent, 0);
					}
				});
	}
	
	/** Starts a thread to update the local database with the server's data.
	 * Does not start a new parse thread if one already exists.
	 * Thread parses the XML file found at the URL provided by the Office of Information Technology.
	 * Parses into a temporary MySQL table and copies it into the permanent one upon completion.
	 * If the parse results in 0 entries, it is run again to workaround an XmlPullParser bug.
	 * Then updates the ListView. */
	private void startXMLParseThread() {
		if (!syncing) {

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
								getResources().getString(R.string.sync_failed), Toast.LENGTH_LONG).show();
					}
					};
					
				Handler hNotifySyncCancelled = new Handler(){
					public void handleMessage(Message msg) {
						Toast.makeText(getApplicationContext(),
								getResources().getString(R.string.sync_canceled), Toast.LENGTH_LONG).show();							
					}
					};
				
				public void run() {
					while(!success && !cancelSync) {
						try {
							if (!syncing) {
								syncing = true;
								cancelSync = false;
								hUpdateStatus.sendEmptyMessage(0);
								
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
											entry.put(tag, value.replaceAll("\n", "")
													.replaceAll("STAFF", "Staff")
													.replaceAll("FACULTY", "Faculty"));
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
								parserEvent = parser.next();
								
								if (cancelSync) {
									bugged = false;
								} else if (entryCount <= 0) {
									bugged = true;
								} else {
									bugged = false;
								}
								
								if(!cancelSync && !bugged) {
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
									bugged = false;
									firstLaunch = false;
									syncing = false;
									
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
								syncing = false;
							}
						} catch (Exception e) {
							Log.e("Fisherapp", "XML Parse Error: " + e.toString());
							success = false;
							syncing = false;
							bugged = false;
							if (firstLaunch) {
								hNotifySyncFailed.sendEmptyMessage(0);
							}
							hUpdateStatus.sendEmptyMessage(0);
							SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
							SharedPreferences.Editor prefsEditor = settings.edit();
							prefsEditor.commit();
						}
					}
					

					mHandler.post(new Runnable() {
						public void run() {
							if (!syncing && !bugged) {
								if (success) {
									Toast.makeText(getApplicationContext(),
											getResources().getString(R.string.synced), Toast.LENGTH_SHORT).show();
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
	
	/** Shows or hides the message and progress bar for the user to see upon their first launch of the app (when the list is empty and unusable). */
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
	
	/** The parse thread uses this to decide whether its current tag is an entry field or not.
	 * @return boolean */
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
	
	/** Shows or hides the indeterminate progress indicator circle in the title bar.
	 *  @param visible Whether the indicator should be shown or hidden.*/
	void updateSyncIndicator(boolean visible) {
		View indicator = (View) findViewById(R.id.progSyncStatus);
		int visibility = indicator.getVisibility();
		if(visible && visibility == View.GONE)
			indicator.setVisibility(View.VISIBLE);
		if (!visible && visibility == View.VISIBLE)
			indicator.setVisibility(View.GONE);
	}
	
	/** Runs on Activity destruction; cleanly interrupts any running parse thread. */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (parseThread != null) {
			parseThread.interrupt();
		}
	}
	
	/** Sets up what is displayed when the device's Menu button is pressed: "Update now" or "Stop update". */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.removeItem(0);
		if(syncing && !firstLaunch) {
			menu.add(0, 0, 0, R.string.stop_sync)
				.setIcon(R.drawable.ic_menu_stop);
		} else {
			menu.add(0, 0, 0, R.string.start_sync)
				.setIcon(R.drawable.ic_menu_refresh);
		}
		return true;
	}
	
	/** Handles Option Menu choice to start or stop a manual update. */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case 0:
				if(syncing && !firstLaunch) {
					parseThread.interrupt();
				} else {
					startSync(false);
				}
			default:
				return true;
		}
	}
}
