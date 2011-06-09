package com.sjfc.fisherapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.sjfc.fisherapp.FisherappDatabase.directoryPeople;

/** Displays details for one Faculty or Staff member as chosen in {@link #DirectoryListActivity.class DirectoryListActivity}.
 * Shows full name, job title, department, faculty/staff status, office location, office phone number, and Fisher email address, with some extra options and functionality.
 */
@SuppressWarnings("deprecation")
public class DirectoryDetailsActivity extends DirectoryActivity {
	
	public static final String KEY_PERSON_ID = "com.sjfc.fisherapp.person_id";
	private static String fullName = "";
	private static String firstName = "";
	private static String middle = "";
	private static String lastName = "";
	private static String group = "";
	private static String jobTitle = "";
	private static String department = "";
	private static String office = "";
	private static String phoneNumber = "";
	private static String emailAddress = "";
	
	private static String building = "";
	
	/** 
	 * Initializes and displays the Directory Details View.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		long personId = getIntent().getLongExtra(KEY_PERSON_ID, 0);
		Log.d("Fisherapp", "Got id " + personId);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory_details);

		/* Set yellow bar title and status text */
		TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
		txtTitle.setText(R.string.directory_details);

		/* Listen for logo push */
		ImageView fisherappLogo = (ImageView) findViewById(R.id.imgFISHERappLogo);
		fisherappLogo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				/* Do nothing. */
			}
		});
		
		fillPeopleDetailsView(personId);
		
		View phoneButton1 = (View)findViewById(R.id.phoneOuter);
		View phoneButton2 = (View)findViewById(R.id.txtPhone);
		phoneButton1.setOnClickListener(phoneListener);
		phoneButton2.setOnClickListener(phoneListener);
		View emailButton1 = (View)findViewById(R.id.emailOuter);
		View emailButton2 = (View)findViewById(R.id.txtEmail);
		emailButton1.setOnClickListener(emailListener);
		emailButton2.setOnClickListener(emailListener);
	}
	
	/**
	 * Queries the database and fills the TextViews with the appropriate information.
	 * 
	 * @param personId The ID of the database entry as passed in by {@link #DirectoryListActivity.class DirectoryListActivity}.
	 */
	private void fillPeopleDetailsView(long personId) {
		Log.d("Fisherapp", "fillDetails method: personId = " + personId);
		Cursor c = mDB.query(directoryPeople.PEOPLE_TABLE, null, "_id=" + personId, null, null, null, null);
		startManagingCursor(c);
		
		Log.d("Fisherapp", "Cursor created");
		
		/* Fill in the details fields one-by-one */
		if (c.moveToFirst()) {
			for (int i = 0; i < c.getColumnCount(); i++) {
				updateAppropriateView(c.getColumnName(i), c.getString(i));
			}
		}
		
		/* Start Google Analytics tracking */
		if (true) { // TODO: Make tracking only occur if the user has agreed/opted-in (use Preferences)
			tracker = GoogleAnalyticsTracker.getInstance();
			tracker.start(this.getString(R.string.analytics_api_key), 10, getApplicationContext());
			tracker.setDebug(true); //TODO: Remove this DEBUG line for deployment
			tracker.setDryRun(true);  //TODO: Remove this DEBUG line for deployment
			
			/* Record a Details activity view in Google Analytics */
			tracker.trackPageView("/" + this.getLocalClassName());
			/* Record some info about who's actually being viewed */
			tracker.setCustomVar(1, "FacStaff Title", jobTitle, 3);
			tracker.setCustomVar(2, "FacStaff Group", group, 3);
			tracker.setCustomVar(3, "FacStaff Department", department, 3);
			tracker.setCustomVar(4, "FacStaff Building", building, 3);
		}
	}
	
	/**
	 * Updates the appropriate TextView for the data passed in.
	 * 
	 * @param columnName The name of the database table column (e.g. "LAST_NAME")
	 * @param data The value of the field passed in (e.g. "Bain")
	 */
	private void updateAppropriateView(String columnName, String data) {
		if (data == null)
			data = "";
		if (columnName.equals(directoryPeople.LAST_NAME)) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtFullName);
			lastName = data;
			fullName = firstName + middle + lastName;
			t.setText(fullName);
		}
		if (columnName.equals(directoryPeople.FIRST_NAME)) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtFullName);
			if(data.equals("")) {
				firstName = "";
				fullName = firstName + middle + lastName;
				t.setText(fullName);
			} else {
				firstName = data + " ";
				fullName = firstName + middle + lastName;
				t.setText(fullName);
			}
		}
		if (columnName.equals(directoryPeople.MIDDLE_NAME)) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtFullName);
			if(data.equals("")) {
				middle = "";
				fullName = firstName + middle + lastName;
				t.setText(fullName);
			} else {
				middle = data + " ";
				fullName = firstName + middle + lastName;
				t.setText(fullName);
			}
		}
		if (columnName.equals(directoryPeople.FAC_STAFF_DIR)) {
			Log.d("Fisherapp", columnName + ": " + data);
			group = data;
			TextView t = (TextView)findViewById(R.id.txtGroup);
			if(data.length() <= 0) {
				t.setText(R.string.no_group);
				t.setTextColor(getResources().getColor(R.color.sub_gray_light));
			} else {
				t.setText(data);
			}
		}
		if (columnName.equals(directoryPeople.JOB_TITLE)) {
			Log.d("Fisherapp", columnName + ": " + data);
			jobTitle = data;
			TextView t = (TextView)findViewById(R.id.txtJobTitle);
			if(data.length() <= 0) {
				t.setText(R.string.no_title);
				t.setTextColor(getResources().getColor(R.color.sub_gray_light));
			} else {
				t.setText(data);
			}
		}
		if (columnName.equals(directoryPeople.DEPARTMENT)) {
			Log.d("Fisherapp", columnName + ": " + data);
			department = data;
			TextView t = (TextView)findViewById(R.id.txtDepartment);
			if(data.length() <= 0) {
				t.setText(R.string.no_department);
				t.setTextColor(getResources().getColor(R.color.sub_gray_light));
			} else {
				t.setText(data);
			}
		}
		if (columnName.equals(directoryPeople.OFFICE)) {
			Log.d("Fisherapp", columnName + ": " + data);
			office = data;
			
			/* Perform a simply String manipulation to try to get just the building */
			int spaceIndex =  office.indexOf(' ');
			if(spaceIndex == -1) {
				building = office;
			} else {
				building = office.substring(0,spaceIndex);
			}

			TextView t = (TextView)findViewById(R.id.txtOffice);
			if(data.length() <= 0) {
				t.setText(R.string.no_office);
				t.setTextColor(getResources().getColor(R.color.sub_gray_light));
			} else {
				t.setText(data);
			}
		}
		if (columnName.equals(directoryPeople.PHONE_NUMBER)) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtPhone);
			phoneNumber = "";
			if(data.length() <= 0) {
				t.setText(R.string.no_phone);
				t.setTextColor(getResources().getColor(R.color.sub_gray_light));
			} else {
				for (int i = 0; i < data.length(); i++) {
					phoneNumber = phoneNumber + data.charAt(i);
					if (i == 2 || i == 5) {
						phoneNumber = phoneNumber + "-";
					}
				}
				t.setText(phoneNumber);
			}
		}
		if (columnName.equals(directoryPeople.EMAIL)) {
			Log.d("Fisherapp", columnName + ": " + data);
			emailAddress = data;
			TextView t = (TextView)findViewById(R.id.txtEmail);
			if(data.length() <= 0) {
				t.setText(R.string.no_email);
				t.setTextColor(getResources().getColor(R.color.sub_gray_light));
			} else {
				t.setText(data);
			}
		}
	}

	private OnClickListener phoneListener = new OnClickListener() {
	    public void onClick(View v) {
	    	if(true) { // TODO
				tracker.trackEvent("ui_interaction",				// category
									"call_phone_facstaff",			// action
									"Button",					// label
									0);								// value
			}
	      callPhone();
	    }
	};

	private OnClickListener emailListener = new OnClickListener() {
	    public void onClick(View v) {
	    	if(true) { // TODO
				tracker.trackEvent("ui_interaction",				// category
									"send_email_facstaff",			// action
									"Button",					// label
									0);						// value
			}
	      sendEmail();
	    }
	};
	
	/**
	 * Initializes the Options Menu that comes up when the user presses the Menu button on their device.
	 * 
	 * Options are Call Phone, Send Email, Share, and Add to Contacts.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, R.string.call_phone)
			.setIcon(R.drawable.ic_menu_call)
			.setEnabled(phoneNumber.length() > 0);
		menu.add(0, 1, 0, R.string.send_email)
			.setIcon(R.drawable.ic_menu_send)
			.setEnabled(emailAddress.length() > 0);
		menu.add(0, 2, 0, R.string.share)
			.setIcon(R.drawable.ic_menu_share);
		menu.add(0, 3, 0, R.string.add_to_contacts)
			.setIcon(R.drawable.ic_menu_invite);
		return true;
	}
	
	/**
	 * Implements the actual functionality of the Options Menu items when they are clicked.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("Fisherapp", "Menu item id: " + item.getItemId());
		switch(item.getItemId()) {		
			case 0: /* Dial */
				/* Record this interaction */
				if(true) { // TODO
					tracker.trackEvent("ui_interaction",				// category
										"call_phone_facstaff",			// action
										"Options_Menu",					// label
										0);								// value
				}
				callPhone();
				break;
			case 1: /* Send Email */
				/* Record this interaction */
				if(true) { // TODO
					tracker.trackEvent("ui_interaction",				// category
										"send_email_facstaff",			// action
										"Options_Menu",					// label
										0);						// value
				}
				sendEmail();
				break;
			case 2:	/* Share */
				if(true) { // TODO
					tracker.trackEvent("ui_interaction",				// category
										"share_info_facstaff",			// action
										"Options_Menu",					// label
										0);						// value
				}
				String shareString =
					fullName + "\n" +
					jobTitle + "\n" +
					department + "\n" +
					group + "\n" +
					office + "\n" +
					phoneNumber + "\n" +
					emailAddress;
				Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(Intent.EXTRA_TEXT, shareString);
				startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_header)));
				break;
			case 3: /* Add to Contacts */
				if(true) { // TODO
					tracker.trackEvent("ui_interaction",				// category
										"add_to_contacts_facstaff",		// action
										"Options_Menu",					// label
										0);						// value
				}
				addLocalContact();
				break;
		}
		return true;
	}
	
	private void callPhone() {
		startActivity(new Intent(android.content.Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
	}
	
	private void sendEmail() {
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + emailAddress));
		startActivity(emailIntent);
	}
	
	/** This Google-contact-adding method was used for compatibility with pre-2.0 Android phones.
	 * It may be re-implemented in the future, but probably not as only a very small percentage of users still use such devices.
	 */
	private void addGoogleContact() {
		ContentValues values = new ContentValues();
		/* Create contact with name */
		values.put(Contacts.People.NAME, fullName);
		Uri uri = People.createPersonInMyContactsGroup(getContentResolver(), values);
		values.clear();
		/* Add phone number */
		Uri phoneUri = Uri.withAppendedPath(uri, Contacts.People.Phones.CONTENT_DIRECTORY);
		values.put(Contacts.Phones.NUMBER, phoneNumber);
		values.put(Contacts.Phones.TYPE, Contacts.Phones.TYPE_WORK);
		getContentResolver().insert(phoneUri, values);
		values.clear();
		/* Add email address */
		Uri emailUri = Uri.withAppendedPath(uri, People.ContactMethods.CONTENT_DIRECTORY);
		values.put(People.ContactMethods.KIND, Contacts.KIND_EMAIL);
		values.put(People.ContactMethods.DATA, emailAddress);
		values.put(People.ContactMethods.TYPE, People.ContactMethods.TYPE_WORK);
		getContentResolver().insert(emailUri, values);   
		Toast.makeText(getApplicationContext(),
				fullName + " " + getResources().getString(R.string.added_to_contacts), Toast.LENGTH_SHORT).show();		
	}
	
	/** This method uses the new and improved method of contact-adding, but it requires the app to be Android 2.0 or above only.
	 * 
	 */
	private void addLocalContact() { /* NOTE: This will require Android 2.0 or later on the device... */
		Intent intent = new Intent(Intent.ACTION_INSERT);
		intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
		intent.putExtra(ContactsContract.Intents.Insert.NAME, fullName);
		intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
		intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
		intent.putExtra(ContactsContract.Intents.Insert.EMAIL, emailAddress);
		intent.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
		startActivityForResult(intent, 0);
	}
}