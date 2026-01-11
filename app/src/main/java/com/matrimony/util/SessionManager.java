package com.matrimony.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "BiyeShadiSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_GENDER = "userGender";
    
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }
    
    public void createLoginSession(int userId, String name, String email, String phone, String gender) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_PHONE, phone);
        editor.putString(KEY_USER_GENDER, gender);
        editor.apply();
    }
    
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }
    
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }
    
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }
    
    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, "");
    }
    
    public String getUserGender() {
        return prefs.getString(KEY_USER_GENDER, "");
    }
    
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}
