package com.matrimony.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.matrimony.model.Biodata;
import com.matrimony.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiodataDAO {
    
    private DatabaseHelper dbHelper;
    private DatabaseReference firebaseRef;

    public BiodataDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.firebaseRef = FirebaseDatabase.getInstance().getReference("biodatas");
    }
    
    public BiodataDAO(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.firebaseRef = FirebaseDatabase.getInstance().getReference("biodatas");
    }
    
    // Insert Biodata
    public long insertBiodata(Biodata biodata) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = getBiodataContentValues(biodata);
        
        long result = db.insert(DatabaseHelper.TABLE_BIODATA, null, values);

        // Sync to Firebase if insert successful
        if (result > 0) {
            syncToFirebase(biodata.getUserId(), biodata);
        }

        return result;
    }
    
    // Update Biodata
    public int updateBiodata(Biodata biodata) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = getBiodataContentValues(biodata);
        
        int result = db.update(DatabaseHelper.TABLE_BIODATA, values,
                DatabaseHelper.COLUMN_BIODATA_USER_ID + " = ?",
                new String[]{String.valueOf(biodata.getUserId())});

        // Sync to Firebase if update successful
        if (result > 0) {
            syncToFirebase(biodata.getUserId(), biodata);
        }

        return result;
    }
    
    // Get Biodata by User ID
    public Biodata getBiodataByUserId(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BIODATA,
                null,
                DatabaseHelper.COLUMN_BIODATA_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        
        Biodata biodata = null;
        if (cursor != null && cursor.moveToFirst()) {
            biodata = cursorToBiodata(cursor);
            cursor.close();
        }
        return biodata;
    }
    
    // Get Biodata by ID
    public Biodata getBiodataById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BIODATA,
                null,
                DatabaseHelper.COLUMN_BIODATA_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);
        
        Biodata biodata = null;
        if (cursor != null && cursor.moveToFirst()) {
            biodata = cursorToBiodata(cursor);
            cursor.close();
        }
        return biodata;
    }
    
    // Get All Biodata
    public List<Biodata> getAllBiodata() {
        List<Biodata> biodataList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BIODATA, null, null, null, null, null, null);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                biodataList.add(cursorToBiodata(cursor));
            }
            cursor.close();
        }
        return biodataList;
    }
    
    // Search Biodata
    public List<Biodata> searchBiodata(int currentUserId, Integer minAge, Integer maxAge,
                                       String religion, String maritalStatus, String education, String city) {
        List<Biodata> biodataList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        StringBuilder query = new StringBuilder("SELECT * FROM " + DatabaseHelper.TABLE_BIODATA + " WHERE " +
                DatabaseHelper.COLUMN_BIODATA_USER_ID + " != ?");
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(currentUserId));
        
        if (minAge != null) {
            query.append(" AND ").append(DatabaseHelper.COLUMN_BIODATA_AGE).append(" >= ?");
            selectionArgs.add(String.valueOf(minAge));
        }
        if (maxAge != null) {
            query.append(" AND ").append(DatabaseHelper.COLUMN_BIODATA_AGE).append(" <= ?");
            selectionArgs.add(String.valueOf(maxAge));
        }
        if (religion != null && !religion.equals("Any") && !religion.isEmpty()) {
            query.append(" AND ").append(DatabaseHelper.COLUMN_BIODATA_RELIGION).append(" = ?");
            selectionArgs.add(religion);
        }
        if (maritalStatus != null && !maritalStatus.equals("Any") && !maritalStatus.isEmpty()) {
            query.append(" AND ").append(DatabaseHelper.COLUMN_BIODATA_MARITAL_STATUS).append(" = ?");
            selectionArgs.add(maritalStatus);
        }
        if (education != null && !education.equals("Any") && !education.isEmpty()) {
            query.append(" AND ").append(DatabaseHelper.COLUMN_BIODATA_EDUCATION).append(" = ?");
            selectionArgs.add(education);
        }
        if (city != null && !city.isEmpty()) {
            query.append(" AND ").append(DatabaseHelper.COLUMN_BIODATA_CITY).append(" LIKE ?");
            selectionArgs.add("%" + city + "%");
        }
        
        Cursor cursor = db.rawQuery(query.toString(), selectionArgs.toArray(new String[0]));
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                biodataList.add(cursorToBiodata(cursor));
            }
            cursor.close();
        }
        return biodataList;
    }
    
    // Search Biodata - Overloaded method for simple search
    public List<Biodata> searchBiodata(String religion, String education, String city, String maritalStatus, String occupation) {
        List<Biodata> biodataList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        StringBuilder query = new StringBuilder("SELECT * FROM " + DatabaseHelper.TABLE_BIODATA + " WHERE 1=1");
        List<String> selectionArgs = new ArrayList<>();
        
        if (religion != null && !religion.equals("Any") && !religion.isEmpty()) {
            query.append(" AND ").append(DatabaseHelper.COLUMN_BIODATA_RELIGION).append(" = ?");
            selectionArgs.add(religion);
        }
        if (education != null && !education.equals("Any") && !education.isEmpty()) {
            query.append(" AND ").append(DatabaseHelper.COLUMN_BIODATA_EDUCATION).append(" = ?");
            selectionArgs.add(education);
        }
        if (city != null && !city.isEmpty()) {
            query.append(" AND ").append(DatabaseHelper.COLUMN_BIODATA_CITY).append(" LIKE ?");
            selectionArgs.add("%" + city + "%");
        }
        if (maritalStatus != null && !maritalStatus.equals("Any") && !maritalStatus.isEmpty()) {
            query.append(" AND ").append(DatabaseHelper.COLUMN_BIODATA_MARITAL_STATUS).append(" = ?");
            selectionArgs.add(maritalStatus);
        }
        if (occupation != null && !occupation.isEmpty()) {
            query.append(" AND ").append(DatabaseHelper.COLUMN_BIODATA_OCCUPATION).append(" LIKE ?");
            selectionArgs.add("%" + occupation + "%");
        }
        
        Cursor cursor = db.rawQuery(query.toString(), selectionArgs.toArray(new String[0]));
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                biodataList.add(cursorToBiodata(cursor));
            }
            cursor.close();
        }
        return biodataList;
    }
    
    // Get All Completed Profiles
    public List<Biodata> getAllCompletedProfiles(int currentUserId) {
        List<Biodata> biodataList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BIODATA,
                null,
                DatabaseHelper.COLUMN_BIODATA_USER_ID + " != ? AND " + DatabaseHelper.COLUMN_BIODATA_PROFILE_COMPLETED + " = ?",
                new String[]{String.valueOf(currentUserId), "1"},
                null, null, null);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                biodataList.add(cursorToBiodata(cursor));
            }
            cursor.close();
        }
        return biodataList;
    }
    
    // Helper: Create ContentValues from Biodata
    private ContentValues getBiodataContentValues(Biodata biodata) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_BIODATA_USER_ID, biodata.getUserId());
        values.put(DatabaseHelper.COLUMN_BIODATA_DOB, biodata.getDateOfBirth());
        values.put(DatabaseHelper.COLUMN_BIODATA_AGE, biodata.getAge());
        values.put(DatabaseHelper.COLUMN_BIODATA_HEIGHT, biodata.getHeight());
        values.put(DatabaseHelper.COLUMN_BIODATA_WEIGHT, biodata.getWeight());
        values.put(DatabaseHelper.COLUMN_BIODATA_MARITAL_STATUS, biodata.getMaritalStatus());
        values.put(DatabaseHelper.COLUMN_BIODATA_RELIGION, biodata.getReligion());
        values.put(DatabaseHelper.COLUMN_BIODATA_EDUCATION, biodata.getEducation());
        values.put(DatabaseHelper.COLUMN_BIODATA_OCCUPATION, biodata.getOccupation());
        values.put(DatabaseHelper.COLUMN_BIODATA_CITY, biodata.getCity());
        values.put(DatabaseHelper.COLUMN_BIODATA_PRESENT_ADDRESS, biodata.getPresentAddress());
        values.put(DatabaseHelper.COLUMN_BIODATA_PERMANENT_ADDRESS, biodata.getPermanentAddress());
        values.put(DatabaseHelper.COLUMN_BIODATA_ABOUT, biodata.getAbout());
        values.put(DatabaseHelper.COLUMN_BIODATA_PARTNER_PREF, biodata.getPartnerPreferences());
        values.put(DatabaseHelper.COLUMN_BIODATA_PROFILE_COMPLETED, biodata.isProfileCompleted() ? 1 : 0);
        return values;
    }
    
    // Helper: Convert Cursor to Biodata
    private Biodata cursorToBiodata(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_ID));
        int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_USER_ID));
        String dob = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_DOB));
        int age = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_AGE));
        String height = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_HEIGHT));
        String weight = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_WEIGHT));
        String maritalStatus = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_MARITAL_STATUS));
        String religion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_RELIGION));
        String education = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_EDUCATION));
        String occupation = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_OCCUPATION));
        String city = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_CITY));
        String presentAddress = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_PRESENT_ADDRESS));
        String permanentAddress = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_PERMANENT_ADDRESS));
        String about = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_ABOUT));
        String partnerPref = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_PARTNER_PREF));
        boolean profileCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIODATA_PROFILE_COMPLETED)) == 1;
        
        return new Biodata(id, userId, dob, age, height, weight, maritalStatus, religion,
                education, occupation, city, presentAddress, permanentAddress, about, partnerPref, profileCompleted);
    }

    // Sync biodata to Firebase Realtime Database
    private void syncToFirebase(int userId, Biodata biodata) {
        try {
            // Get user's Firebase UID
            UserDAO userDAO = new UserDAO(dbHelper);
            User user = userDAO.getUserById(userId);

            if (user == null || user.getFirebaseUid() == null || user.getFirebaseUid().isEmpty()) {
                android.util.Log.w("BiodataDAO", "Cannot sync biodata: User not found or no Firebase UID for userId: " + userId);
                return;
            }

            String firebaseUid = user.getFirebaseUid();
            Map<String, Object> biodataMap = biodataToMap(biodata);

            firebaseRef.child(firebaseUid).setValue(biodataMap)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("BiodataDAO", "Biodata synced to Firebase with UID: " + firebaseUid + " (userId: " + userId + ")");
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("BiodataDAO", "Failed to sync biodata to Firebase: " + e.getMessage());
                });
        } catch (Exception e) {
            android.util.Log.e("BiodataDAO", "Error syncing to Firebase: " + e.getMessage());
        }
    }

    // Convert Biodata object to Map for Firebase
    private Map<String, Object> biodataToMap(Biodata biodata) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", biodata.getUserId());
        map.put("dateOfBirth", biodata.getDateOfBirth());
        map.put("age", biodata.getAge());
        map.put("height", biodata.getHeight());
        map.put("weight", biodata.getWeight());
        map.put("maritalStatus", biodata.getMaritalStatus());
        map.put("religion", biodata.getReligion());
        map.put("education", biodata.getEducation());
        map.put("occupation", biodata.getOccupation());
        map.put("city", biodata.getCity());
        map.put("presentAddress", biodata.getPresentAddress());
        map.put("permanentAddress", biodata.getPermanentAddress());
        map.put("about", biodata.getAbout());
        map.put("partnerPreferences", biodata.getPartnerPreferences());
        map.put("profileCompleted", biodata.isProfileCompleted());
        map.put("lastUpdated", System.currentTimeMillis());
        return map;
    }
}
