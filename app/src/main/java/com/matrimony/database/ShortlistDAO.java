package com.matrimony.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.matrimony.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShortlistDAO {
    
    private DatabaseHelper dbHelper;
    private DatabaseReference firebaseRef;

    public ShortlistDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.firebaseRef = FirebaseDatabase.getInstance().getReference("shortlists");
    }
    
    public ShortlistDAO(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.firebaseRef = FirebaseDatabase.getInstance().getReference("shortlists");
    }
    
    // Add to Shortlist
    public long addToShortlist(int userId, int shortlistedUserId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SHORTLIST_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_SHORTLIST_SHORTLISTED_USER_ID, shortlistedUserId);
        values.put(DatabaseHelper.COLUMN_SHORTLIST_CREATED_AT, System.currentTimeMillis());
        
        long result = db.insert(DatabaseHelper.TABLE_SHORTLIST, null, values);

        // Sync to Firebase if successful
        if (result > 0) {
            syncShortlistToFirebase(userId, shortlistedUserId, true);
        }

        return result;
    }
    
    // Remove from Shortlist
    public int removeFromShortlist(int userId, int shortlistedUserId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(DatabaseHelper.TABLE_SHORTLIST,
                DatabaseHelper.COLUMN_SHORTLIST_USER_ID + " = ? AND " +
                        DatabaseHelper.COLUMN_SHORTLIST_SHORTLISTED_USER_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(shortlistedUserId)});

        // Remove from Firebase if successful
        if (result > 0) {
            syncShortlistToFirebase(userId, shortlistedUserId, false);
        }

        return result;
    }
    
    // Get Shortlisted User IDs
    public List<Integer> getShortlistedUserIds(int userId) {
        List<Integer> shortlistedIds = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SHORTLIST,
                new String[]{DatabaseHelper.COLUMN_SHORTLIST_SHORTLISTED_USER_ID},
                DatabaseHelper.COLUMN_SHORTLIST_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                shortlistedIds.add(cursor.getInt(0));
            }
            cursor.close();
        }
        return shortlistedIds;
    }
    
    // Check if Shortlisted
    public boolean isShortlisted(int userId, int shortlistedUserId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + DatabaseHelper.TABLE_SHORTLIST +
                " WHERE " + DatabaseHelper.COLUMN_SHORTLIST_USER_ID + " = ? AND " +
                DatabaseHelper.COLUMN_SHORTLIST_SHORTLISTED_USER_ID + " = ? LIMIT 1",
                new String[]{String.valueOf(userId), String.valueOf(shortlistedUserId)});
        
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();
        return exists;
    }
    
    // Get Shortlist Count
    public int getShortlistCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SHORTLIST +
                " WHERE " + DatabaseHelper.COLUMN_SHORTLIST_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // Sync shortlist action to Firebase
    private void syncShortlistToFirebase(int userId, int shortlistedUserId, boolean isAdd) {
        try {
            // Get users' Firebase UIDs
            UserDAO userDAO = new UserDAO(dbHelper);
            User user = userDAO.getUserById(userId);
            User shortlistedUser = userDAO.getUserById(shortlistedUserId);

            if (user == null || user.getFirebaseUid() == null || user.getFirebaseUid().isEmpty()) {
                android.util.Log.w("ShortlistDAO", "Cannot sync: No Firebase UID for userId: " + userId);
                return;
            }

            if (shortlistedUser == null || shortlistedUser.getFirebaseUid() == null || shortlistedUser.getFirebaseUid().isEmpty()) {
                android.util.Log.w("ShortlistDAO", "Cannot sync: No Firebase UID for shortlistedUserId: " + shortlistedUserId);
                return;
            }

            String firebaseUid = user.getFirebaseUid();
            String shortlistedFirebaseUid = shortlistedUser.getFirebaseUid();

            if (isAdd) {
                Map<String, Object> shortlistData = new HashMap<>();
                shortlistData.put("userFirebaseUid", firebaseUid);
                shortlistData.put("shortlistedFirebaseUid", shortlistedFirebaseUid);
                shortlistData.put("timestamp", System.currentTimeMillis());

                firebaseRef.child(firebaseUid)
                    .child(shortlistedFirebaseUid)
                    .setValue(shortlistData)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("ShortlistDAO", "Shortlist synced to Firebase with UIDs");
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("ShortlistDAO", "Failed to sync shortlist: " + e.getMessage());
                    });
            } else {
                firebaseRef.child(firebaseUid)
                    .child(shortlistedFirebaseUid)
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("ShortlistDAO", "Shortlist removed from Firebase");
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("ShortlistDAO", "Failed to remove shortlist: " + e.getMessage());
                    });
            }
        } catch (Exception e) {
            android.util.Log.e("ShortlistDAO", "Error syncing shortlist to Firebase: " + e.getMessage());
        }
    }
}
