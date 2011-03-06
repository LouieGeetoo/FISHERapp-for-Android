package com.sjfc.fisherapp;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjfc.fisherapp.FisherappDatabase.directoryPeople;

public class DirectoryDetailsActivity extends DirectoryActivity {
	
	public static final String KEY_PERSON_ID = "com.sjfc.fisherapp.person_id";
	
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
    	Log.d("Fisherapp", columnName + ": " + data);
    	if (data == null)
    		data = "";
		if (columnName.compareTo(directoryPeople.LAST_NAME) == 0) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtLastName);
			t.setText(data);
		}
		if (columnName.compareTo(directoryPeople.FIRST_NAME) == 0) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtFirstName);
			t.setText(data + " ");
		}
		if (columnName.compareTo(directoryPeople.MIDDLE_NAME) == 0) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtMiddle);
			t.setText(data + " ");
		}
		if (columnName.compareTo(directoryPeople.FAC_STAFF_DIR) == 0) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtGroup);
			t.setText(data);
		}
		if (columnName.compareTo(directoryPeople.JOB_TITLE) == 0) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtJobTitle);
			t.setText(data);
		}
		if (columnName.compareTo(directoryPeople.DEPARTMENT) == 0) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtDepartment);
			t.setText(data);
		}
		if (columnName.compareTo(directoryPeople.OFFICE) == 0) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtOffice);
			t.setText(data);
		}
		if (columnName.compareTo(directoryPeople.PHONE_NUMBER) == 0) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtPhone);
			t.setText(data);
		}
		if (columnName.compareTo(directoryPeople.EMAIL) == 0) {
			Log.d("Fisherapp", columnName + ": " + data);
			TextView t = (TextView)findViewById(R.id.txtEmail);
			t.setText(data);
		}
    }
}