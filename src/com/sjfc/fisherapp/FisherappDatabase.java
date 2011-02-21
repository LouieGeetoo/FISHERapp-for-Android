package com.sjfc.fisherapp;

import android.provider.BaseColumns;
import com.sjfc.fisherapp.R;

public final class FisherappDatabase {
	
	private FisherappDatabase() {}
	
	/** DIRECTORY_PEOPLE schema */
	public static final class directoryPeople implements BaseColumns {
		private directoryPeople() {}
		public static final String PEOPLE_TABLE = "table_directory_people";
		public static final String TEMP_TABLE = "table_directory_temp";
		public static final String FAC_STAFF_DIR = "FAC_STAFF_DIR";
		public static final String LAST_NAME = "LAST_NAME";
		public static final String FIRST_NAME = "FIRST_NAME";
		public static final String MIDDLE_NAME = "MIDDLE_NAME";
		public static final String JOB_TITLE = "JOB_TITLE";
		public static final String DEPARTMENT = "DEPARTMENT";
		public static final String OFFICE = "OFFICE";
		public static final String PHONE_NUMBER = "PHONE_NUMBER";
		public static final String EMAIL = "EMAIL";
		public static final String DEFAULT_SORT_ORDER = "LAST_NAME ASC";
	}
}