package com.matrimony.model;

public class MatchProfile {
    private int id;
    private int userId;
    private String fullName;
    private int age;
    private String height;
    private String religion;
    private String maritalStatus;
    private String education;
    private String occupation;
    private String annualIncome;
    private String city;
    private String state;
    private String country;
    private String aboutMe;
    private String photoUrl;
    private String gender;
    private String email;
    private String phone;
    
    public MatchProfile() {
    }
    
    public MatchProfile(int userId, String fullName, int age, String height, String religion,
                        String maritalStatus, String education, String occupation, String annualIncome,
                        String city, String state, String country, String aboutMe,
                        String photoUrl, String gender) {
        this.userId = userId;
        this.fullName = fullName;
        this.age = age;
        this.height = height;
        this.religion = religion;
        this.maritalStatus = maritalStatus;
        this.education = education;
        this.occupation = occupation;
        this.annualIncome = annualIncome;
        this.city = city;
        this.state = state;
        this.country = country;
        this.aboutMe = aboutMe;
        this.photoUrl = photoUrl;
        this.gender = gender;
    }
    
    public MatchProfile(int id, int userId, String fullName, int age, String gender,
                        String religion, String education, String occupation,
                        String city, String email, String phone) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.religion = religion;
        this.education = education;
        this.occupation = occupation;
        this.city = city;
        this.email = email;
        this.phone = phone;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getHeight() { return height; }
    public void setHeight(String height) { this.height = height; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getReligion() { return religion; }
    public void setReligion(String religion) { this.religion = religion; }
    
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    
    public String getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(String annualIncome) { this.annualIncome = annualIncome; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getAboutMe() { return aboutMe; }
    public void setAboutMe(String aboutMe) { this.aboutMe = aboutMe; }
    
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    
    // Alias method for backward compatibility
    public String getProfilePhotoUri() { return photoUrl; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
