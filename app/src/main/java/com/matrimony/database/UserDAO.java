package com.matrimony.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.matrimony.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDAO {
    
    private static final String TAG = "UserDAO";
    private DatabaseHelper dbHelper;
    private DatabaseReference firebaseRef;

    public UserDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.firebaseRef = FirebaseDatabase.getInstance().getReference("users");
    }
    
    public UserDAO(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.firebaseRef = FirebaseDatabase.getInstance().getReference("users");
    }
    
    // Insert User
    public synchronized long insertUser(User user) {
        SQLiteDatabase db = null;
        long userId = -1;
        
        try {
            db = dbHelper.getWritableDatabase();
            
            // Trim all string values before inserting
            String name = user.getName() != null ? user.getName().trim() : "";
            String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";
            String password = user.getPassword() != null ? user.getPassword().trim() : "";
            String phone = user.getPhone() != null ? user.getPhone().trim() : "";
            String gender = user.getGender() != null ? user.getGender().trim() : "";
            String role = user.getRole() != null ? user.getRole().trim() : "user";
            
            Log.d(TAG, "=== INSERT USER START ===");
            Log.d(TAG, "Name: " + name);
            Log.d(TAG, "Email: [" + email + "]");
            Log.d(TAG, "Password: [" + password + "], Length: " + password.length());
            Log.d(TAG, "Phone: " + phone);
            Log.d(TAG, "Gender: " + gender);
            
            // Start transaction for atomic operation
            db.beginTransaction();
            
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USER_NAME, name);
            values.put(DatabaseHelper.COLUMN_USER_EMAIL, email);
            values.put(DatabaseHelper.COLUMN_USER_PASSWORD, password);
            values.put(DatabaseHelper.COLUMN_USER_PHONE, phone);
            values.put(DatabaseHelper.COLUMN_USER_GENDER, gender);
            values.put(DatabaseHelper.COLUMN_USER_ROLE, role);
            values.put(DatabaseHelper.COLUMN_USER_FIREBASE_UID, user.getFirebaseUid());
            values.put(DatabaseHelper.COLUMN_USER_CREATED_AT, System.currentTimeMillis());
            
            userId = db.insertOrThrow(DatabaseHelper.TABLE_USERS, null, values);
            
            // Mark transaction successful
            db.setTransactionSuccessful();
            
            Log.d(TAG, "✓ User inserted with ID: " + userId);
            Log.d(TAG, "=== INSERT USER END ===");
            
        } catch (Exception e) {
            Log.e(TAG, "✗ ERROR inserting user: " + e.getMessage(), e);
            userId = -1;
        } finally {
            if (db != null && db.inTransaction()) {
                db.endTransaction();
            }
        }
        
        // Verify insertion AFTER transaction is complete
        if (userId > 0) {
            try {
                Thread.sleep(100); // Small delay to ensure write is flushed
                User verifyUser = getUserById((int) userId);
                if (verifyUser != null) {
                    Log.d(TAG, "✓ VERIFICATION: User found - Email: [" + verifyUser.getEmail() + "]");
                    // Sync to Firebase after successful verification
                    syncUserToFirebase(verifyUser);
                } else {
                    Log.e(TAG, "✗ VERIFICATION FAILED: User NOT found after insert!");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during verification: " + e.getMessage());
            }
        }
        
        return userId;
    }
    
    // Update User
    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_NAME, user.getName());
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COLUMN_USER_PHONE, user.getPhone());
        values.put(DatabaseHelper.COLUMN_USER_GENDER, user.getGender());
        
        // Always include profile photo URI (even if null) to ensure it's saved
        values.put(DatabaseHelper.COLUMN_USER_PROFILE_PHOTO, user.getProfilePhotoUri());

        Log.d(TAG, "Updating user " + user.getId() + " with photo URI: " + user.getProfilePhotoUri());

        int result = db.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(user.getId())});

        Log.d(TAG, "Update result: " + result + " rows affected");

        // Sync to Firebase if update successful
        if (result > 0) {
            syncUserToFirebase(user);
        }

        return result;
    }
    
    // Authenticate User
    public synchronized User authenticateUser(String email, String password) {
        // Trim and lowercase email, trim password
        email = email != null ? email.trim().toLowerCase() : "";
        password = password != null ? password.trim() : "";
        
        Log.d(TAG, "=== AUTHENTICATION ATTEMPT ===");
        Log.d(TAG, "Input Email: [" + email + "]");
        Log.d(TAG, "Input Password: [" + password + "], Length: " + password.length());
        
        // Get user by email
        User userByEmail = getUserByEmail(email);
        
        if (userByEmail != null) {
            Log.d(TAG, "✓ User found by email!");
            Log.d(TAG, "DB Email: [" + userByEmail.getEmail() + "]");
            Log.d(TAG, "DB Password: [" + userByEmail.getPassword() + "], Length: " + userByEmail.getPassword().length());
            
            // Direct password comparison
            if (password.equals(userByEmail.getPassword())) {
                Log.d(TAG, "✓ Password matches! Authentication SUCCESS!");
                return userByEmail;
            } else {
                Log.d(TAG, "✗ Password mismatch!");
                Log.d(TAG, "Expected: [" + userByEmail.getPassword() + "]");
                Log.d(TAG, "Got:      [" + password + "]");
                
                // Character by character comparison for debugging
                String dbPass = userByEmail.getPassword();
                for (int i = 0; i < Math.max(password.length(), dbPass.length()); i++) {
                    char inputChar = i < password.length() ? password.charAt(i) : '?';
                    char dbChar = i < dbPass.length() ? dbPass.charAt(i) : '?';
                    if (inputChar != dbChar) {
                        Log.d(TAG, "  Diff at [" + i + "]: input='" + inputChar + "' (" + (int)inputChar + ") vs db='" + dbChar + "' (" + (int)dbChar + ")");
                    }
                }
            }
        } else {
            Log.d(TAG, "✗ No user found with email: [" + email + "]");
            
            // List all users for debugging
            Log.d(TAG, "Listing all users in database:");
            List<User> allUsers = getAllUsersDebug();
            if (allUsers.isEmpty()) {
                Log.d(TAG, "  Database is EMPTY - no users found!");
            } else {
                for (User u : allUsers) {
                    Log.d(TAG, "  - ID: " + u.getId() + ", Email: [" + u.getEmail() + "], Password: [" + u.getPassword() + "]");
                }
            }
        }
        
        Log.d(TAG, "=== AUTHENTICATION FAILED ===");
        return null;
    }
    
    // Debug method to get all users without filter
    private List<User> getAllUsersDebug() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, null, null, null, null, null);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                users.add(cursorToUser(cursor));
            }
            cursor.close();
        }
        return users;
    }
    
    // Get User by Email
    public synchronized User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Trim and lowercase email to ensure consistency
        email = email != null ? email.trim().toLowerCase() : "";
        
        Log.d(TAG, "=== GET USER BY EMAIL ===");
        Log.d(TAG, "Looking for email: [" + email + "]");
        
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_USER_EMAIL + " = ? COLLATE NOCASE",
                new String[]{email},
                null, null, null);
        
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            Log.d(TAG, "✓ Found user: " + user.getName() + " with email: [" + user.getEmail() + "]");
            cursor.close();
        } else {
            Log.d(TAG, "✗ No user found with email: [" + email + "]");
            
            // Debug: Show all emails in database
            Cursor allCursor = db.query(DatabaseHelper.TABLE_USERS, 
                new String[]{DatabaseHelper.COLUMN_USER_EMAIL}, 
                null, null, null, null, null);
            if (allCursor != null) {
                Log.d(TAG, "All emails in database:");
                while (allCursor.moveToNext()) {
                    String dbEmail = allCursor.getString(0);
                    Log.d(TAG, "  - [" + dbEmail + "]");
                }
                allCursor.close();
            }
            if (cursor != null) cursor.close();
        }
        return user;
    }
    
    // Get User by ID
    public User getUserById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }
    
    // Check if Email Exists
    public boolean emailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + DatabaseHelper.TABLE_USERS +
                " WHERE " + DatabaseHelper.COLUMN_USER_EMAIL + " = ? LIMIT 1",
                new String[]{email});
        
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();
        return exists;
    }
    
    // Get All Users
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_USER_ROLE + " = ?",
                new String[]{"user"},
                null, null, null);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                users.add(cursorToUser(cursor));
            }
            cursor.close();
        }
        return users;
    }
    
    // Get Potential Matches
    public List<User> getPotentialMatches(int currentUserId, String currentGender) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_USER_ROLE + " = ? AND " +
                        DatabaseHelper.COLUMN_USER_ID + " != ? AND " +
                        DatabaseHelper.COLUMN_USER_GENDER + " != ?",
                new String[]{"user", String.valueOf(currentUserId), currentGender},
                null, null, null);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                users.add(cursorToUser(cursor));
            }
            cursor.close();
        }
        return users;
    }
    
    // Get User Count
    public int getUserCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS +
                " WHERE " + DatabaseHelper.COLUMN_USER_ROLE + " = ?",
                new String[]{"user"});
        
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }
    
    // Helper method to convert Cursor to User
    private User cursorToUser(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL));
        String password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PHONE));
        String gender = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_GENDER));
        String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ROLE));
        
        // Get firebase_uid (may be null for existing users)
        int firebaseUidIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_FIREBASE_UID);
        String firebaseUid = firebaseUidIndex >= 0 ? cursor.getString(firebaseUidIndex) : null;

        // Get profile_photo_uri (may be null)
        int profilePhotoIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_PROFILE_PHOTO);
        String profilePhotoUri = profilePhotoIndex >= 0 ? cursor.getString(profilePhotoIndex) : null;

        Log.d(TAG, "Loading user " + id + " with photo URI: " + profilePhotoUri + " (column index: " + profilePhotoIndex + ")");

        User user = new User(id, name, email, password, phone, gender, role, firebaseUid);
        user.setProfilePhotoUri(profilePhotoUri);
        return user;
    }

    // Sync user profile to Firebase Realtime Database
    private void syncUserToFirebase(User user) {
        try {
            // Use Firebase UID as the key, not local database ID
            String firebaseUid = user.getFirebaseUid();
            if (firebaseUid == null || firebaseUid.isEmpty()) {
                Log.w(TAG, "Cannot sync user to Firebase: No Firebase UID for user " + user.getEmail());
                return;
            }

            Map<String, Object> userMap = userToMap(user);
            firebaseRef.child(firebaseUid).setValue(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✓ User synced to Firebase with UID: " + firebaseUid + " (" + user.getEmail() + ")");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "✗ Failed to sync user to Firebase: " + e.getMessage());
                });
        } catch (Exception e) {
            Log.e(TAG, "Error syncing user to Firebase: " + e.getMessage());
        }
    }

    // Convert User object to Map for Firebase (excluding password for security)
    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        // DO NOT sync password to Firebase for security reasons
        map.put("phone", user.getPhone());
        map.put("gender", user.getGender());
        map.put("role", user.getRole());
        map.put("firebaseUid", user.getFirebaseUid());
        map.put("lastUpdated", System.currentTimeMillis());
        return map;
    }
}
