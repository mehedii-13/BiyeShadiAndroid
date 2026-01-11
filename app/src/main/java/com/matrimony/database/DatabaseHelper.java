package com.matrimony.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "biyeshadi.db";
    private static final int DATABASE_VERSION = 3;  // Incremented for profile_photo_uri column

    // Users Table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_PHONE = "phone";
    public static final String COLUMN_USER_GENDER = "gender";
    public static final String COLUMN_USER_ROLE = "role";
    public static final String COLUMN_USER_FIREBASE_UID = "firebase_uid";
    public static final String COLUMN_USER_PROFILE_PHOTO = "profile_photo_uri";
    public static final String COLUMN_USER_CREATED_AT = "created_at";
    
    // Biodata Table
    public static final String TABLE_BIODATA = "biodata";
    public static final String COLUMN_BIODATA_ID = "id";
    public static final String COLUMN_BIODATA_USER_ID = "user_id";
    public static final String COLUMN_BIODATA_DOB = "date_of_birth";
    public static final String COLUMN_BIODATA_AGE = "age";
    public static final String COLUMN_BIODATA_HEIGHT = "height";
    public static final String COLUMN_BIODATA_WEIGHT = "weight";
    public static final String COLUMN_BIODATA_MARITAL_STATUS = "marital_status";
    public static final String COLUMN_BIODATA_RELIGION = "religion";
    public static final String COLUMN_BIODATA_EDUCATION = "education";
    public static final String COLUMN_BIODATA_OCCUPATION = "occupation";
    public static final String COLUMN_BIODATA_CITY = "city";
    public static final String COLUMN_BIODATA_PRESENT_ADDRESS = "present_address";
    public static final String COLUMN_BIODATA_PERMANENT_ADDRESS = "permanent_address";
    public static final String COLUMN_BIODATA_ABOUT = "about";
    public static final String COLUMN_BIODATA_PARTNER_PREF = "partner_preferences";
    public static final String COLUMN_BIODATA_PROFILE_COMPLETED = "profile_completed";
    
    // Shortlist Table
    public static final String TABLE_SHORTLIST = "shortlist";
    public static final String COLUMN_SHORTLIST_ID = "id";
    public static final String COLUMN_SHORTLIST_USER_ID = "user_id";
    public static final String COLUMN_SHORTLIST_SHORTLISTED_USER_ID = "shortlisted_user_id";
    public static final String COLUMN_SHORTLIST_CREATED_AT = "created_at";
    
    // Contact Requests Table
    public static final String TABLE_CONTACT_REQUESTS = "contact_requests";
    public static final String COLUMN_REQUEST_ID = "id";
    public static final String COLUMN_REQUEST_SENDER_ID = "sender_id";
    public static final String COLUMN_REQUEST_RECEIVER_ID = "receiver_id";
    public static final String COLUMN_REQUEST_STATUS = "status";
    public static final String COLUMN_REQUEST_MESSAGE = "message";
    public static final String COLUMN_REQUEST_CREATED_AT = "request_date";
    public static final String COLUMN_REQUEST_RESPONSE_DATE = "response_date";
    
    private static DatabaseHelper instance;
    
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_NAME + " TEXT NOT NULL, " +
                COLUMN_USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COLUMN_USER_PASSWORD + " TEXT NOT NULL, " +
                COLUMN_USER_PHONE + " TEXT, " +
                COLUMN_USER_GENDER + " TEXT, " +
                COLUMN_USER_ROLE + " TEXT DEFAULT 'user', " +
                COLUMN_USER_FIREBASE_UID + " TEXT UNIQUE, " +
                COLUMN_USER_PROFILE_PHOTO + " TEXT, " +
                COLUMN_USER_CREATED_AT + " INTEGER DEFAULT 0)";
        db.execSQL(createUsersTable);
        
        // Create Biodata Table
        String createBiodataTable = "CREATE TABLE " + TABLE_BIODATA + " (" +
                COLUMN_BIODATA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BIODATA_USER_ID + " INTEGER NOT NULL UNIQUE, " +
                COLUMN_BIODATA_DOB + " TEXT, " +
                COLUMN_BIODATA_AGE + " INTEGER DEFAULT 0, " +
                COLUMN_BIODATA_HEIGHT + " TEXT, " +
                COLUMN_BIODATA_WEIGHT + " TEXT, " +
                COLUMN_BIODATA_MARITAL_STATUS + " TEXT, " +
                COLUMN_BIODATA_RELIGION + " TEXT, " +
                COLUMN_BIODATA_EDUCATION + " TEXT, " +
                COLUMN_BIODATA_OCCUPATION + " TEXT, " +
                COLUMN_BIODATA_CITY + " TEXT, " +
                COLUMN_BIODATA_PRESENT_ADDRESS + " TEXT, " +
                COLUMN_BIODATA_PERMANENT_ADDRESS + " TEXT, " +
                COLUMN_BIODATA_ABOUT + " TEXT, " +
                COLUMN_BIODATA_PARTNER_PREF + " TEXT, " +
                COLUMN_BIODATA_PROFILE_COMPLETED + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + COLUMN_BIODATA_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE)";
        db.execSQL(createBiodataTable);
        
        // Create Shortlist Table
        String createShortlistTable = "CREATE TABLE " + TABLE_SHORTLIST + " (" +
                COLUMN_SHORTLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SHORTLIST_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_SHORTLIST_SHORTLISTED_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_SHORTLIST_CREATED_AT + " INTEGER DEFAULT 0, " +
                "UNIQUE(" + COLUMN_SHORTLIST_USER_ID + ", " + COLUMN_SHORTLIST_SHORTLISTED_USER_ID + "), " +
                "FOREIGN KEY(" + COLUMN_SHORTLIST_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + COLUMN_SHORTLIST_SHORTLISTED_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE)";
        db.execSQL(createShortlistTable);
        
        // Create Contact Requests Table
        String createContactRequestsTable = "CREATE TABLE " + TABLE_CONTACT_REQUESTS + " (" +
                COLUMN_REQUEST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_REQUEST_SENDER_ID + " INTEGER NOT NULL, " +
                COLUMN_REQUEST_RECEIVER_ID + " INTEGER NOT NULL, " +
                COLUMN_REQUEST_STATUS + " TEXT DEFAULT 'pending', " +
                COLUMN_REQUEST_MESSAGE + " TEXT, " +
                COLUMN_REQUEST_CREATED_AT + " INTEGER DEFAULT 0, " +
                COLUMN_REQUEST_RESPONSE_DATE + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + COLUMN_REQUEST_SENDER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + COLUMN_REQUEST_RECEIVER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE)";
        db.execSQL(createContactRequestsTable);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        android.util.Log.d("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Handle migration from version 1 to version 2
        if (oldVersion < 2) {
            // Add firebase_uid column to existing users table
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " +
                          COLUMN_USER_FIREBASE_UID + " TEXT");
                android.util.Log.d("DatabaseHelper", "Successfully added firebase_uid column");
            } catch (Exception e) {
                android.util.Log.e("DatabaseHelper", "Error adding firebase_uid column: " + e.getMessage());
            }
        }

        // Handle migration from version 2 to version 3
        if (oldVersion < 3) {
            // Add profile_photo_uri column to existing users table
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " +
                          COLUMN_USER_PROFILE_PHOTO + " TEXT");
                android.util.Log.d("DatabaseHelper", "Successfully added profile_photo_uri column");
            } catch (Exception e) {
                android.util.Log.e("DatabaseHelper", "Error adding profile_photo_uri column: " + e.getMessage());
            }
        }

        // Add more migrations here as needed
        // if (oldVersion < 4) { ... }
    }
    
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
