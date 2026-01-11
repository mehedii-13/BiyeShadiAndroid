package com.matrimony.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.matrimony.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class to dump all database contents to Logcat
 * This helps debug what data is actually stored in the database
 */
public class DatabaseDumper {
    
    private static final String TAG = "DatabaseDumper";
    
    public static void dumpAllData(Context context) {
        Log.d(TAG, "");
        Log.d(TAG, "╔════════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║          DATABASE DUMP - ALL TABLES AND DATA                   ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════════╝");
        Log.d(TAG, "");
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Database info
        Log.d(TAG, "📁 Database: " + db.getPath());
        Log.d(TAG, "📊 Version: " + db.getVersion());
        Log.d(TAG, "🔓 Writable: " + !db.isReadOnly());
        Log.d(TAG, "");
        
        // List all tables
        Cursor tablesCursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'android_%' AND name NOT LIKE 'sqlite_%'", 
            null);
        
        Log.d(TAG, "📋 Tables in database:");
        while (tablesCursor.moveToNext()) {
            String tableName = tablesCursor.getString(0);
            Log.d(TAG, "   - " + tableName);
        }
        tablesCursor.close();
        Log.d(TAG, "");
        
        // Dump Users Table
        dumpUsersTable(db);
        
        // Dump Biodata Table
        dumpBiodataTable(db);
        
        // Dump Shortlist Table
        dumpShortlistTable(db);
        
        // Dump Contact Requests Table
        dumpContactRequestsTable(db);
        
        Log.d(TAG, "");
        Log.d(TAG, "═══════════════════ END OF DATABASE DUMP ═══════════════════");
        Log.d(TAG, "");
    }
    
    private static void dumpUsersTable(SQLiteDatabase db) {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║                      USERS TABLE                               ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════════╝");
        
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERS, null);
        
        int count = cursor.getCount();
        Log.d(TAG, "📊 Total Users: " + count);
        Log.d(TAG, "");
        
        if (count == 0) {
            Log.d(TAG, "⚠️  NO USERS IN DATABASE!");
        } else {
            int rowNum = 1;
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                String gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));
                
                Log.d(TAG, "─────────────────────────────────────────────────────────");
                Log.d(TAG, "👤 User #" + rowNum + ":");
                Log.d(TAG, "   ID:       " + id);
                Log.d(TAG, "   Name:     " + name);
                Log.d(TAG, "   Email:    [" + email + "]");
                Log.d(TAG, "   Password: [" + password + "] (length: " + password.length() + ")");
                Log.d(TAG, "   Phone:    " + phone);
                Log.d(TAG, "   Gender:   " + gender);
                Log.d(TAG, "   Role:     " + role);
                Log.d(TAG, "   Created:  " + formatTimestamp(createdAt));
                rowNum++;
            }
        }
        cursor.close();
        Log.d(TAG, "");
    }
    
    private static void dumpBiodataTable(SQLiteDatabase db) {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║                    BIODATA TABLE                               ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════════╝");
        
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_BIODATA, null);
        
        int count = cursor.getCount();
        Log.d(TAG, "📊 Total Biodata Records: " + count);
        Log.d(TAG, "");
        
        if (count == 0) {
            Log.d(TAG, "⚠️  NO BIODATA IN DATABASE!");
        } else {
            int rowNum = 1;
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String dob = cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth"));
                int age = cursor.getInt(cursor.getColumnIndexOrThrow("age"));
                String height = cursor.getString(cursor.getColumnIndexOrThrow("height"));
                String weight = cursor.getString(cursor.getColumnIndexOrThrow("weight"));
                String maritalStatus = cursor.getString(cursor.getColumnIndexOrThrow("marital_status"));
                String religion = cursor.getString(cursor.getColumnIndexOrThrow("religion"));
                String education = cursor.getString(cursor.getColumnIndexOrThrow("education"));
                String occupation = cursor.getString(cursor.getColumnIndexOrThrow("occupation"));
                String city = cursor.getString(cursor.getColumnIndexOrThrow("city"));
                int profileCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("profile_completed"));
                
                Log.d(TAG, "─────────────────────────────────────────────────────────");
                Log.d(TAG, "📋 Biodata #" + rowNum + ":");
                Log.d(TAG, "   ID:             " + id);
                Log.d(TAG, "   User ID:        " + userId);
                Log.d(TAG, "   Age:            " + age);
                Log.d(TAG, "   Height:         " + height);
                Log.d(TAG, "   Weight:         " + weight);
                Log.d(TAG, "   Marital Status: " + maritalStatus);
                Log.d(TAG, "   Religion:       " + religion);
                Log.d(TAG, "   Education:      " + education);
                Log.d(TAG, "   Occupation:     " + occupation);
                Log.d(TAG, "   City:           " + city);
                Log.d(TAG, "   Completed:      " + (profileCompleted == 1 ? "Yes" : "No"));
                rowNum++;
            }
        }
        cursor.close();
        Log.d(TAG, "");
    }
    
    private static void dumpShortlistTable(SQLiteDatabase db) {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║                   SHORTLIST TABLE                              ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════════╝");
        
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_SHORTLIST, null);
        
        int count = cursor.getCount();
        Log.d(TAG, "📊 Total Shortlist Entries: " + count);
        
        if (count == 0) {
            Log.d(TAG, "⚠️  NO SHORTLIST ENTRIES!");
        } else {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                int shortlistedUserId = cursor.getInt(cursor.getColumnIndexOrThrow("shortlisted_user_id"));
                long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));
                
                Log.d(TAG, "   User " + userId + " shortlisted User " + shortlistedUserId + " at " + formatTimestamp(createdAt));
            }
        }
        cursor.close();
        Log.d(TAG, "");
    }
    
    private static void dumpContactRequestsTable(SQLiteDatabase db) {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║                CONTACT REQUESTS TABLE                          ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════════╝");
        
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_CONTACT_REQUESTS, null);
        
        int count = cursor.getCount();
        Log.d(TAG, "📊 Total Contact Requests: " + count);
        
        if (count == 0) {
            Log.d(TAG, "⚠️  NO CONTACT REQUESTS!");
        } else {
            int rowNum = 1;
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int senderId = cursor.getInt(cursor.getColumnIndexOrThrow("sender_id"));
                int receiverId = cursor.getInt(cursor.getColumnIndexOrThrow("receiver_id"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
                long requestDate = cursor.getLong(cursor.getColumnIndexOrThrow("request_date"));
                
                Log.d(TAG, "─────────────────────────────────────────────────────────");
                Log.d(TAG, "✉️  Request #" + rowNum + ":");
                Log.d(TAG, "   From User:  " + senderId);
                Log.d(TAG, "   To User:    " + receiverId);
                Log.d(TAG, "   Status:     " + status);
                Log.d(TAG, "   Message:    " + message);
                Log.d(TAG, "   Date:       " + formatTimestamp(requestDate));
                rowNum++;
            }
        }
        cursor.close();
        Log.d(TAG, "");
    }
    
    private static String formatTimestamp(long timestamp) {
        if (timestamp == 0) return "Not set";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            return "Invalid timestamp";
        }
    }
    
    /**
     * Quick method to show only users table
     */
    public static void dumpUsersOnly(Context context) {
        Log.d(TAG, "");
        Log.d(TAG, "═══════════ USERS TABLE DUMP ═══════════");
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        dumpUsersTable(db);
        Log.d(TAG, "═══════════════════════════════════════");
        Log.d(TAG, "");
    }
}
