package com.matrimony.model;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String gender;
    private String role;
    private String firebaseUid;  // Firebase Auth UID
    private String profilePhotoUri;  // Profile photo URI

    public User() {
    }
    
    public User(int id, String name, String email, String password, String phone, String gender, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.role = role;
    }
    
    public User(int id, String name, String email, String password, String phone, String gender, String role, String firebaseUid) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.role = role;
        this.firebaseUid = firebaseUid;
    }

    public User(String name, String email, String password, String phone, String gender) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.role = "user";
    }
    
    public User(String name, String email, String password, String phone, String gender, String firebaseUid) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.role = "user";
        this.firebaseUid = firebaseUid;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public String getProfilePhotoUri() { return profilePhotoUri; }
    public void setProfilePhotoUri(String profilePhotoUri) { this.profilePhotoUri = profilePhotoUri; }
}
