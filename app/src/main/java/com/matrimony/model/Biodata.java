package com.matrimony.model;

public class Biodata {
    private int id;
    private int userId;
    private String dateOfBirth;
    private int age;
    private String height;
    private String weight;
    private String maritalStatus;
    private String religion;
    private String caste;
    private String motherTongue;
    private String complexion;
    private String bloodGroup;
    private String education;
    private String occupation;
    private String annualIncome;
    private String companyName;
    private String fatherName;
    private String fatherOccupation;
    private String motherName;
    private String motherOccupation;
    private String siblings;
    private String familyType;
    private String familyStatus;
    private String address;
    private String city;
    private String state;
    private String country;
    private String aboutMe;
    private String hobbies;
    private int partnerAgeFrom;
    private int partnerAgeTo;
    private String partnerReligion;
    private String partnerEducation;
    private String partnerMaritalStatus;
    private String partnerExpectations;
    private String presentAddress;
    private String permanentAddress;
    private String about;
    private String partnerPreferences;
    private boolean profileCompleted;
    
    public Biodata() {
    }
    
    public Biodata(int userId, String dateOfBirth, int age, String height, String weight,
                   String maritalStatus, String religion, String education, String occupation,
                   String address, String city, String state, String country,
                   String fatherName, String motherName, String aboutMe,
                   String partnerExpectations) {
        this.userId = userId;
        this.dateOfBirth = dateOfBirth;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.maritalStatus = maritalStatus;
        this.religion = religion;
        this.education = education;
        this.occupation = occupation;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.aboutMe = aboutMe;
        this.partnerExpectations = partnerExpectations;
        this.profileCompleted = false;
    }
    
    public Biodata(int id, int userId, String dateOfBirth, int age, String height, String weight,
                   String maritalStatus, String religion, String education, String occupation,
                   String city, String presentAddress, String permanentAddress, String about,
                   String partnerPreferences, boolean profileCompleted) {
        this.id = id;
        this.userId = userId;
        this.dateOfBirth = dateOfBirth;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.maritalStatus = maritalStatus;
        this.religion = religion;
        this.education = education;
        this.occupation = occupation;
        this.city = city;
        this.presentAddress = presentAddress;
        this.permanentAddress = permanentAddress;
        this.about = about;
        this.partnerPreferences = partnerPreferences;
        this.profileCompleted = profileCompleted;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getHeight() { return height; }
    public void setHeight(String height) { this.height = height; }
    
    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }
    
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    
    public String getReligion() { return religion; }
    public void setReligion(String religion) { this.religion = religion; }
    
    public String getCaste() { return caste; }
    public void setCaste(String caste) { this.caste = caste; }
    
    public String getMotherTongue() { return motherTongue; }
    public void setMotherTongue(String motherTongue) { this.motherTongue = motherTongue; }
    
    public String getComplexion() { return complexion; }
    public void setComplexion(String complexion) { this.complexion = complexion; }
    
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    
    public String getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(String annualIncome) { this.annualIncome = annualIncome; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    
    public String getFatherOccupation() { return fatherOccupation; }
    public void setFatherOccupation(String fatherOccupation) { this.fatherOccupation = fatherOccupation; }
    
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    
    public String getMotherOccupation() { return motherOccupation; }
    public void setMotherOccupation(String motherOccupation) { this.motherOccupation = motherOccupation; }
    
    public String getSiblings() { return siblings; }
    public void setSiblings(String siblings) { this.siblings = siblings; }
    
    public String getFamilyType() { return familyType; }
    public void setFamilyType(String familyType) { this.familyType = familyType; }
    
    public String getFamilyStatus() { return familyStatus; }
    public void setFamilyStatus(String familyStatus) { this.familyStatus = familyStatus; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getAboutMe() { return aboutMe; }
    public void setAboutMe(String aboutMe) { this.aboutMe = aboutMe; }
    
    public String getHobbies() { return hobbies; }
    public void setHobbies(String hobbies) { this.hobbies = hobbies; }
    
    public int getPartnerAgeFrom() { return partnerAgeFrom; }
    public void setPartnerAgeFrom(int partnerAgeFrom) { this.partnerAgeFrom = partnerAgeFrom; }
    
    public int getPartnerAgeTo() { return partnerAgeTo; }
    public void setPartnerAgeTo(int partnerAgeTo) { this.partnerAgeTo = partnerAgeTo; }
    
    public String getPartnerReligion() { return partnerReligion; }
    public void setPartnerReligion(String partnerReligion) { this.partnerReligion = partnerReligion; }
    
    public String getPartnerEducation() { return partnerEducation; }
    public void setPartnerEducation(String partnerEducation) { this.partnerEducation = partnerEducation; }
    
    public String getPartnerMaritalStatus() { return partnerMaritalStatus; }
    public void setPartnerMaritalStatus(String partnerMaritalStatus) { this.partnerMaritalStatus = partnerMaritalStatus; }
    
    public String getPartnerExpectations() { return partnerExpectations; }
    public void setPartnerExpectations(String partnerExpectations) { this.partnerExpectations = partnerExpectations; }
    
    public String getPresentAddress() { return presentAddress; }
    public void setPresentAddress(String presentAddress) { this.presentAddress = presentAddress; }
    
    public String getPermanentAddress() { return permanentAddress; }
    public void setPermanentAddress(String permanentAddress) { this.permanentAddress = permanentAddress; }
    
    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }
    
    public String getPartnerPreferences() { return partnerPreferences; }
    public void setPartnerPreferences(String partnerPreferences) { this.partnerPreferences = partnerPreferences; }
    
    public boolean isProfileCompleted() { return profileCompleted; }
    public void setProfileCompleted(boolean profileCompleted) { this.profileCompleted = profileCompleted; }
}
