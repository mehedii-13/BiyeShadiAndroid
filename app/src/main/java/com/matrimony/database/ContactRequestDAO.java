package com.matrimony.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.matrimony.model.ContactRequest;
import com.matrimony.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactRequestDAO {
    
    private DatabaseHelper dbHelper;
    private DatabaseReference firebaseRef;

    public ContactRequestDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.firebaseRef = FirebaseDatabase.getInstance().getReference("contactRequests");
    }
    
    public ContactRequestDAO(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.firebaseRef = FirebaseDatabase.getInstance().getReference("contactRequests");
    }
    
    // Insert Contact Request
    public long insertRequest(ContactRequest request) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_REQUEST_SENDER_ID, request.getSenderId());
        values.put(DatabaseHelper.COLUMN_REQUEST_RECEIVER_ID, request.getReceiverId());
        values.put(DatabaseHelper.COLUMN_REQUEST_STATUS, request.getStatus());
        values.put(DatabaseHelper.COLUMN_REQUEST_MESSAGE, request.getMessage());
        values.put(DatabaseHelper.COLUMN_REQUEST_CREATED_AT, request.getRequestDate());
        values.put(DatabaseHelper.COLUMN_REQUEST_RESPONSE_DATE, request.getResponseDate());
        
        long result = db.insert(DatabaseHelper.TABLE_CONTACT_REQUESTS, null, values);

        // Sync to Firebase if successful
        if (result > 0) {
            request.setId((int) result);
            syncRequestToFirebase(request);
        }

        return result;
    }
    
    // Update Request Status
    public int updateRequestStatus(int requestId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_REQUEST_STATUS, status);
        values.put(DatabaseHelper.COLUMN_REQUEST_RESPONSE_DATE, System.currentTimeMillis());
        
        int result = db.update(DatabaseHelper.TABLE_CONTACT_REQUESTS, values,
                DatabaseHelper.COLUMN_REQUEST_ID + " = ?",
                new String[]{String.valueOf(requestId)});

        // Sync to Firebase if successful
        if (result > 0) {
            ContactRequest request = getRequestById(requestId);
            if (request != null) {
                syncRequestToFirebase(request);
            }
        }

        return result;
    }
    
    // Get Pending Requests for User
    public List<ContactRequest> getPendingRequestsForUser(int userId) {
        List<ContactRequest> requests = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CONTACT_REQUESTS,
                null,
                DatabaseHelper.COLUMN_REQUEST_RECEIVER_ID + " = ? AND " + DatabaseHelper.COLUMN_REQUEST_STATUS + " = ?",
                new String[]{String.valueOf(userId), "pending"},
                null, null, null);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                requests.add(cursorToContactRequest(cursor));
            }
            cursor.close();
        }
        return requests;
    }
    
    // Get Sent Requests by User
    public List<ContactRequest> getSentRequestsByUser(int userId) {
        List<ContactRequest> requests = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CONTACT_REQUESTS,
                null,
                DatabaseHelper.COLUMN_REQUEST_SENDER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                requests.add(cursorToContactRequest(cursor));
            }
            cursor.close();
        }
        return requests;
    }
    
    // Alias for getSentRequestsByUser
    public List<ContactRequest> getSentRequests(int userId) {
        return getSentRequestsByUser(userId);
    }
    
    // Update Request Status
    public int updateRequestStatus(int requestId, String status, long responseDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_REQUEST_STATUS, status);
        values.put(DatabaseHelper.COLUMN_REQUEST_RESPONSE_DATE, responseDate);
        
        return db.update(DatabaseHelper.TABLE_CONTACT_REQUESTS, values,
                DatabaseHelper.COLUMN_REQUEST_ID + " = ?",
                new String[]{String.valueOf(requestId)});
    }
    
    // Get Accepted Connections
    public List<ContactRequest> getAcceptedConnections(int userId) {
        List<ContactRequest> requests = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CONTACT_REQUESTS,
                null,
                "(" + DatabaseHelper.COLUMN_REQUEST_SENDER_ID + " = ? OR " +
                        DatabaseHelper.COLUMN_REQUEST_RECEIVER_ID + " = ?) AND " +
                        DatabaseHelper.COLUMN_REQUEST_STATUS + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(userId), "accepted"},
                null, null, null);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                requests.add(cursorToContactRequest(cursor));
            }
            cursor.close();
        }
        return requests;
    }
    
    // Check if Request Exists
    public boolean requestExists(int senderId, int receiverId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + DatabaseHelper.TABLE_CONTACT_REQUESTS +
                " WHERE " + DatabaseHelper.COLUMN_REQUEST_SENDER_ID + " = ? AND " +
                DatabaseHelper.COLUMN_REQUEST_RECEIVER_ID + " = ? LIMIT 1",
                new String[]{String.valueOf(senderId), String.valueOf(receiverId)});
        
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();
        return exists;
    }
    
    // Check if Two Users Are Connected (request accepted)
    public boolean areUsersConnected(int userId1, int userId2) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + DatabaseHelper.TABLE_CONTACT_REQUESTS +
                " WHERE ((" + DatabaseHelper.COLUMN_REQUEST_SENDER_ID + " = ? AND " +
                DatabaseHelper.COLUMN_REQUEST_RECEIVER_ID + " = ?) OR (" +
                DatabaseHelper.COLUMN_REQUEST_SENDER_ID + " = ? AND " +
                DatabaseHelper.COLUMN_REQUEST_RECEIVER_ID + " = ?)) AND " +
                DatabaseHelper.COLUMN_REQUEST_STATUS + " = ? LIMIT 1",
                new String[]{String.valueOf(userId1), String.valueOf(userId2),
                        String.valueOf(userId2), String.valueOf(userId1), "accepted"});

        boolean connected = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();
        return connected;
    }

    // Get Pending Request Count
    public int getPendingRequestCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CONTACT_REQUESTS +
                " WHERE " + DatabaseHelper.COLUMN_REQUEST_RECEIVER_ID + " = ? AND " +
                DatabaseHelper.COLUMN_REQUEST_STATUS + " = ?",
                new String[]{String.valueOf(userId), "pending"});

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // Get Sent Requests Count (only pending)
    public int getSentRequestsCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CONTACT_REQUESTS +
                " WHERE " + DatabaseHelper.COLUMN_REQUEST_SENDER_ID + " = ? AND " +
                DatabaseHelper.COLUMN_REQUEST_STATUS + " = ?",
                new String[]{String.valueOf(userId), "pending"});

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }
    
    // Get Connected Count
    public int getConnectedCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CONTACT_REQUESTS +
                " WHERE (" + DatabaseHelper.COLUMN_REQUEST_SENDER_ID + " = ? OR " +
                DatabaseHelper.COLUMN_REQUEST_RECEIVER_ID + " = ?) AND " +
                DatabaseHelper.COLUMN_REQUEST_STATUS + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(userId), "accepted"});
        
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }
    
    // Delete Contact Request
    public int deleteRequest(int requestId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_CONTACT_REQUESTS,
                DatabaseHelper.COLUMN_REQUEST_ID + " = ?",
                new String[]{String.valueOf(requestId)});
    }

    // Helper: Convert Cursor to ContactRequest
    private ContactRequest cursorToContactRequest(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUEST_ID));
        int senderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUEST_SENDER_ID));
        int receiverId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUEST_RECEIVER_ID));
        String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUEST_STATUS));
        String message = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUEST_MESSAGE));
        long requestDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUEST_CREATED_AT));
        long responseDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUEST_RESPONSE_DATE));
        
        return new ContactRequest(id, senderId, receiverId, status, message, requestDate, responseDate);
    }

    // Get Request by ID
    private ContactRequest getRequestById(int requestId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CONTACT_REQUESTS,
                null,
                DatabaseHelper.COLUMN_REQUEST_ID + " = ?",
                new String[]{String.valueOf(requestId)},
                null, null, null);

        ContactRequest request = null;
        if (cursor != null && cursor.moveToFirst()) {
            request = cursorToContactRequest(cursor);
            cursor.close();
        }
        return request;
    }

    // Sync contact request to Firebase
    private void syncRequestToFirebase(ContactRequest request) {
        try {
            // Get sender and receiver Firebase UIDs
            UserDAO userDAO = new UserDAO(dbHelper);
            User sender = userDAO.getUserById(request.getSenderId());
            User receiver = userDAO.getUserById(request.getReceiverId());

            if (sender == null || sender.getFirebaseUid() == null || sender.getFirebaseUid().isEmpty()) {
                android.util.Log.w("ContactRequestDAO", "Cannot sync: No Firebase UID for senderId: " + request.getSenderId());
                return;
            }

            if (receiver == null || receiver.getFirebaseUid() == null || receiver.getFirebaseUid().isEmpty()) {
                android.util.Log.w("ContactRequestDAO", "Cannot sync: No Firebase UID for receiverId: " + request.getReceiverId());
                return;
            }

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("id", request.getId());
            requestMap.put("senderFirebaseUid", sender.getFirebaseUid());
            requestMap.put("receiverFirebaseUid", receiver.getFirebaseUid());
            requestMap.put("status", request.getStatus());
            requestMap.put("message", request.getMessage());
            requestMap.put("requestDate", request.getRequestDate());
            requestMap.put("responseDate", request.getResponseDate());
            requestMap.put("lastUpdated", System.currentTimeMillis());

            // Store under both sender and receiver for easy querying
            String requestKey = "request_" + request.getId();

            firebaseRef.child(sender.getFirebaseUid()).child("sent").child(requestKey).setValue(requestMap)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("ContactRequestDAO", "Contact request synced to sender's Firebase");
                });

            firebaseRef.child(receiver.getFirebaseUid()).child("received").child(requestKey).setValue(requestMap)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("ContactRequestDAO", "Contact request synced to receiver's Firebase");
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ContactRequestDAO", "Failed to sync contact request: " + e.getMessage());
                });
        } catch (Exception e) {
            android.util.Log.e("ContactRequestDAO", "Error syncing contact request to Firebase: " + e.getMessage());
        }
    }
}
