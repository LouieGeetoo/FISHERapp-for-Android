package com.sjfc.fisherapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjfc.fisherapp.FisherappDatabase.directoryPeople;

public class DirectoryDetailsActivity extends DirectoryActivity {
	
	public static final String KEY_PERSON_ID = "com.sjfc.fisherapp.person_id";
	private static String fullName = "";
	private static String firstName = "";
	private static String middle = "";
	private static String lastName = "";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	long personId = getIntent().getLongExtra(KEY_PERSON_ID, 0);
    	Log.d("Fisherapp", "Got id " + personId);
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directory_details);

        /** Set yellow bar title and status text */
        TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.directory_details);
        TextView txtUpdateStatus = (TextView)findViewById(R.id.txtUpdateStatus);
        txtUpdateStatus.setText(R.string.blank);

    	/** Listen for logo push */
        ImageView fisherappLogo = (ImageView) findViewById(R.id.imgFISHERappLogo);
        fisherappLogo.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		/** TODO: Go back to main menu? */
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
			TextView t = (TextView)findViewById(R.id.txtGroup);
			if(data.equals("FACULTY"))
				t.setText("Faculty");
			else if(data.equals("STAFF"))
				t.setText("Staff");
			else
				t.setText(data);
		}
		if (columnName.equals(directoryPeople.JOB_TITLE)) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtJobTitle);
			t.setText(data);
		}
		if (columnName.equals(directoryPeople.DEPARTMENT)) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtDepartment);
			t.setText(data);
		}
		if (columnName.equals(directoryPeople.OFFICE)) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtOffice);
			t.setText(data);
			/*
			t.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					String uri = "geo:43.117278648239015,-77.51273989677429";  
					startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri))); 
				}
			});
			*/
		}
		if (columnName.equals(directoryPeople.PHONE_NUMBER)) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtPhone);
			
			String number = "";
			for (int i = 0; i < data.length(); i++) {
				number = number + data.charAt(i);
				if (i == 2 || i == 5) {
					number = number + "-";
				}
			}
			t.setText(number);
		}
		if (columnName.equals(directoryPeople.EMAIL)) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtEmail);
			t.setText(data);
		}
    }
}