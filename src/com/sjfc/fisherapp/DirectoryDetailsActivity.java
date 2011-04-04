package com.sjfc.fisherapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sjfc.fisherapp.FisherappDatabase.directoryPeople;

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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		long personId = getIntent().getLongExtra(KEY_PERSON_ID, 0);
		Log.d("Fisherapp", "Got id " + personId);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory_details);

		/** Set yellow bar title and status text */
		TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
		txtTitle.setText(R.string.directory_details);

		/** Listen for logo push */
		ImageView fisherappLogo = (ImageView) findViewById(R.id.imgFISHERappLogo);
		fisherappLogo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finishActivity(0);
			}
		});
		
		fillPeopleDetailsView(personId);
	}
	
	private void fillPeopleDetailsView(long personId) {
		Log.d("Fisherapp", "fillDetails method: personId = " + personId);
		Cursor c = mDB.query(directoryPeople.PEOPLE_TABLE, null, "_id=" + personId, null, null, null, null);
		startManagingCursor(c);
		
		Log.d("Fisherapp", "Cursor created");
		
		/** Fill in the details fields one-by-one */
		if (c.moveToFirst()) {
			for (int i = 0; i < c.getColumnCount(); i++) {
				//Log.d("Fisherapp", "Cursor: " + c.getColumnName(i) + " " + c.getString(i));
				updateAppropriateView(c.getColumnName(i), c.getString(i));
			}
		}
	}
	
	private void updateAppropriateView(String columnName, String data) {
		//Log.d("Fisherapp", columnName + ": " + data);
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
				t.setTextColor(getResources().getColor(getResources().getColor(R.color.sub_gray_light)));
			} else {
				t.setText(data);
			}
		}
	}

	/** NEW METHOD onCreateOptionsMenu */
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
	
	/** NEW METHOD onOptionsItemSelected */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("Fisherapp", "Menu item id: " + item.getItemId());
		switch(item.getItemId()) {		
			case 0: /* Dial */
				startActivity(new Intent(android.content.Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
				break;
			case 1: /* Send Email */
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				emailIntent.setType("text/email");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{emailAddress});
				startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.send_email_header)));
				break;
			case 2:	/* Share */
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
				
				break;
		}
		return true;
	}
}