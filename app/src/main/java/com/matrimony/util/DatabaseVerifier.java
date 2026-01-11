package com.matrimony.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.matrimony.database.DatabaseHelper;
import com.matrimony.database.UserDAO;
import com.matrimony.model.User;

public class DatabaseVerifier {
    
    private static final String TAG = "DatabaseVerifier";
    
    public static boolean verifyDatabase(Context context) {
        Log.d(TAG, "========== DATABASE VERIFICATION START ==========");
        
        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            
            // 1. Check if database is open
            if (!db.isOpen()) {
                Log.e(TAG, "ERROR: Database is NOT open!");
                return false;
            }
            Log.d(TAG, "✓ Database is open");
            
            // 2. Check if database is writable
            if (!db.isReadOnly()) {
                Log.d(TAG, "✓ Database is writable");
            } else {
                Log.e(TAG, "ERROR: Database is READ ONLY!");
                return false;
            }
            
            // 3. Check database path
            String dbPath = db.getPath();
            Log.d(TAG, "Database path: " + dbPath);
            
            // 4. Check if users table exists
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", 
                new String[]{"users"});
            if (cursor != null && cursor.moveToFirst()) {
                Log.d(TAG, "✓ Table 'users' exists");
                cursor.close();
            } else {
                Log.e(TAG, "ERROR: Table 'users' does NOT exist!");
                if (cursor != null) cursor.close();
                return false;
            }
            
            // 5. List all tables
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            if (cursor != null) {
                Log.d(TAG, "All tables in database:");
                while (cursor.moveToNext()) {
                    Log.d(TAG, "  - " + cursor.getString(0));
                }
                cursor.close();
            }
            
            // 6. Check table structure
            cursor = db.rawQuery("PRAGMA table_info(users)", null);
            if (cursor != null) {
                Log.d(TAG, "Users table structure:");
                while (cursor.moveToNext()) {
                    String colName = cursor.getString(1);
                    String colType = cursor.getString(2);
                    Log.d(TAG, "  - " + colName + " (" + colType + ")");
                }
                cursor.close();
            }
            
            // 7. Count existing users
            cursor = db.rawQuery("SELECT COUNT(*) FROM users", null);
            if (cursor != null && cursor.moveToFirst()) {
                int userCount = cursor.getInt(0);
                Log.d(TAG, "Current user count: " + userCount);
                cursor.close();
            }
            
            // 8. Try to insert a test user
            Log.d(TAG, "Testing insert operation...");
            UserDAO userDAO = new UserDAO(dbHelper);
            
            String testEmail = "test_" + System.currentTimeMillis() + "@verify.com";
            User testUser = new User("Test User", testEmail, "password123", "1234567890", "Male");
            
            long testId = userDAO.insertUser(testUser);
            if (testId > 0) {
                Log.d(TAG, "✓ Test user inserted successfully with ID: " + testId);
                
                // Verify we can read it back
                User verifyUser = userDAO.getUserByEmail(testEmail);
                if (verifyUser != null) {
                    Log.d(TAG, "✓ Test user retrieved successfully");
                    Log.d(TAG, "  Name: " + verifyUser.getName());
                    Log.d(TAG, "  Email: " + verifyUser.getEmail());
                    Log.d(TAG, "  Password: " + verifyUser.getPassword());
                    
                    // Clean up test user
                    db.delete("users", "id = ?", new String[]{String.valueOf(testId)});
                    Log.d(TAG, "✓ Test user deleted");
                } else {
                    Log.e(TAG, "ERROR: Could not retrieve test user!");
                    return false;
                }
            } else {
                Log.e(TAG, "ERROR: Failed to insert test user! (returned " + testId + ")");
                return false;
            }
            
            Log.d(TAG, "========== DATABASE VERIFICATION PASSED ==========");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "ERROR during database verification: " + e.getMessage(), e);
            Log.d(TAG, "========== DATABASE VERIFICATION FAILED ==========");
            return false;
        }
    }
}
