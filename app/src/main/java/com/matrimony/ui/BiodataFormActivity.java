package com.matrimony.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.matrimony.R;
import com.matrimony.database.BiodataDAO;
import com.matrimony.database.DatabaseHelper;
import com.matrimony.model.Biodata;
import com.matrimony.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BiodataFormActivity extends AppCompatActivity {

    // Personal Information
    private EditText dateOfBirthField;
    private EditText ageField;
    private Spinner heightSpinner;
    private EditText weightField;
    private Spinner maritalStatusSpinner;
    private Spinner religionSpinner;
    private EditText casteField;
    private EditText motherTongueField;
    private Spinner complexionSpinner;
    private Spinner bloodGroupSpinner;

    // Education & Career
    private Spinner educationSpinner;
    private EditText occupationField;
    private Spinner incomeSpinner;
    private EditText companyField;

    // Family Details
    private EditText fatherNameField;
    private EditText fatherOccupationField;
    private EditText motherNameField;
    private EditText motherOccupationField;
    private EditText siblingsField;
    private Spinner familyTypeSpinner;
    private Spinner familyStatusSpinner;

    // Location
    private EditText addressField;
    private EditText cityField;
    private EditText stateField;
    private EditText countryField;

    // About
    private EditText aboutMeField;
    private EditText hobbiesField;

    // Partner Preferences
    private EditText partnerAgeFromField;
    private EditText partnerAgeToField;
    private Spinner partnerReligionSpinner;
    private Spinner partnerEducationSpinner;
    private Spinner partnerMaritalStatusSpinner;
    private EditText partnerExpectationsField;

    private Button saveButton;
    private ProgressBar progressBar;
    private ScrollView scrollView;

    private SessionManager sessionManager;
    private BiodataDAO biodataDAO;
    private ExecutorService executorService;
    private Biodata existingBiodata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biodata_form);

        sessionManager = new SessionManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        biodataDAO = new BiodataDAO(dbHelper);
        executorService = Executors.newSingleThreadExecutor();

        setupToolbar();
        initViews();
        setupSpinners();
        setupClickListeners();
        loadExistingBiodata();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Biodata");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        // Personal Information
        dateOfBirthField = findViewById(R.id.dateOfBirthField);
        ageField = findViewById(R.id.ageField);
        heightSpinner = findViewById(R.id.heightSpinner);
        weightField = findViewById(R.id.weightField);
        maritalStatusSpinner = findViewById(R.id.maritalStatusSpinner);
        religionSpinner = findViewById(R.id.religionSpinner);
        casteField = findViewById(R.id.casteField);
        motherTongueField = findViewById(R.id.motherTongueField);
        complexionSpinner = findViewById(R.id.complexionSpinner);
        bloodGroupSpinner = findViewById(R.id.bloodGroupSpinner);

        // Education & Career
        educationSpinner = findViewById(R.id.educationSpinner);
        occupationField = findViewById(R.id.occupationField);
        incomeSpinner = findViewById(R.id.incomeSpinner);
        companyField = findViewById(R.id.companyField);

        // Family Details
        fatherNameField = findViewById(R.id.fatherNameField);
        fatherOccupationField = findViewById(R.id.fatherOccupationField);
        motherNameField = findViewById(R.id.motherNameField);
        motherOccupationField = findViewById(R.id.motherOccupationField);
        siblingsField = findViewById(R.id.siblingsField);
        familyTypeSpinner = findViewById(R.id.familyTypeSpinner);
        familyStatusSpinner = findViewById(R.id.familyStatusSpinner);

        // Location
        addressField = findViewById(R.id.addressField);
        cityField = findViewById(R.id.cityField);
        stateField = findViewById(R.id.stateField);
        countryField = findViewById(R.id.countryField);

        // About
        aboutMeField = findViewById(R.id.aboutMeField);
        hobbiesField = findViewById(R.id.hobbiesField);

        // Partner Preferences
        partnerAgeFromField = findViewById(R.id.partnerAgeFromField);
        partnerAgeToField = findViewById(R.id.partnerAgeToField);
        partnerReligionSpinner = findViewById(R.id.partnerReligionSpinner);
        partnerEducationSpinner = findViewById(R.id.partnerEducationSpinner);
        partnerMaritalStatusSpinner = findViewById(R.id.partnerMaritalStatusSpinner);
        partnerExpectationsField = findViewById(R.id.partnerExpectationsField);

        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);
        scrollView = findViewById(R.id.scrollView);
    }

    private void setupSpinners() {
        // Height
        String[] heights = {"Select Height", "4'0\"", "4'1\"", "4'2\"", "4'3\"", "4'4\"", "4'5\"", "4'6\"", "4'7\"", "4'8\"", "4'9\"", "4'10\"", "4'11\"",
            "5'0\"", "5'1\"", "5'2\"", "5'3\"", "5'4\"", "5'5\"", "5'6\"", "5'7\"", "5'8\"", "5'9\"", "5'10\"", "5'11\"",
            "6'0\"", "6'1\"", "6'2\"", "6'3\"", "6'4\"", "6'5\"", "6'6\""};
        setupSpinner(heightSpinner, heights);

        // Marital Status
        String[] maritalStatuses = {"Select Status", "Never Married", "Divorced", "Widowed", "Awaiting Divorce"};
        setupSpinner(maritalStatusSpinner, maritalStatuses);
        setupSpinner(partnerMaritalStatusSpinner, new String[]{"Any", "Never Married", "Divorced", "Widowed", "Awaiting Divorce"});

        // Religion
        String[] religions = {"Select Religion", "Islam", "Hinduism", "Buddhism", "Christianity", "Others"};
        setupSpinner(religionSpinner, religions);
        setupSpinner(partnerReligionSpinner, new String[]{"Any", "Islam", "Hinduism", "Buddhism", "Christianity", "Others"});

        // Complexion
        String[] complexions = {"Select Complexion", "Very Fair", "Fair", "Wheatish", "Wheatish Brown", "Dark"};
        setupSpinner(complexionSpinner, complexions);

        // Blood Group
        String[] bloodGroups = {"Select Blood Group", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        setupSpinner(bloodGroupSpinner, bloodGroups);

        // Education
        String[] educations = {"Select Education", "High School", "Diploma", "Bachelor's Degree", "Master's Degree", "PhD", "Professional Degree"};
        setupSpinner(educationSpinner, educations);
        setupSpinner(partnerEducationSpinner, new String[]{"Any", "High School", "Diploma", "Bachelor's Degree", "Master's Degree", "PhD", "Professional Degree"});

        // Income
        String[] incomes = {"Select Income", "Below 2 Lakh", "2-5 Lakh", "5-10 Lakh", "10-20 Lakh", "20-50 Lakh", "50 Lakh - 1 Crore", "Above 1 Crore"};
        setupSpinner(incomeSpinner, incomes);

        // Family Type
        String[] familyTypes = {"Select Family Type", "Joint Family", "Nuclear Family"};
        setupSpinner(familyTypeSpinner, familyTypes);

        // Family Status
        String[] familyStatuses = {"Select Family Status", "Middle Class", "Upper Middle Class", "Rich", "Affluent"};
        setupSpinner(familyStatusSpinner, familyStatuses);
    }

    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveBiodata());

        // Setup date picker for date of birth
        dateOfBirthField.setFocusable(false);
        dateOfBirthField.setClickable(true);
        dateOfBirthField.setOnClickListener(v -> showDatePicker());

        // Disable age field - will be auto-calculated
        ageField.setFocusable(false);
        ageField.setEnabled(false);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // Parse existing date if available
        String currentDate = dateOfBirthField.getText().toString();
        if (!currentDate.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                calendar.setTime(sdf.parse(currentDate));
            } catch (Exception e) {
                // Use current date if parsing fails
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format date as dd/MM/yyyy
                    String selectedDate = String.format(Locale.getDefault(),
                            "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    dateOfBirthField.setText(selectedDate);

                    // Calculate and set age
                    int age = calculateAge(selectedYear, selectedMonth, selectedDay);
                    ageField.setText(String.valueOf(age));
                },
                year, month, day
        );

        // Set max date to today (can't be born in future)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Set min date to 150 years ago (reasonable limit)
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -150);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private int calculateAge(int birthYear, int birthMonth, int birthDay) {
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birthYear;

        // Check if birthday hasn't occurred this year
        int currentMonth = today.get(Calendar.MONTH);
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
            age--;
        }

        return age;
    }

    private void loadExistingBiodata() {
        int userId = sessionManager.getUserId();
        
        executorService.execute(() -> {
            existingBiodata = biodataDAO.getBiodataByUserId(userId);
            
            runOnUiThread(() -> {
                if (existingBiodata != null) {
                    populateFields(existingBiodata);
                } else {
                    countryField.setText("Bangladesh");
                }
            });
        });
    }

    private void populateFields(Biodata biodata) {
        dateOfBirthField.setText(biodata.getDateOfBirth() != null ? biodata.getDateOfBirth() : "");

        // Calculate age from date of birth if available
        if (biodata.getDateOfBirth() != null && !biodata.getDateOfBirth().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Calendar birthDate = Calendar.getInstance();
                birthDate.setTime(sdf.parse(biodata.getDateOfBirth()));
                int age = calculateAge(birthDate.get(Calendar.YEAR),
                                     birthDate.get(Calendar.MONTH),
                                     birthDate.get(Calendar.DAY_OF_MONTH));
                ageField.setText(String.valueOf(age));
            } catch (Exception e) {
                // If parsing fails, use stored age
                ageField.setText(biodata.getAge() > 0 ? String.valueOf(biodata.getAge()) : "");
            }
        } else {
            ageField.setText(biodata.getAge() > 0 ? String.valueOf(biodata.getAge()) : "");
        }

        setSpinnerValue(heightSpinner, biodata.getHeight());
        weightField.setText(biodata.getWeight() != null ? biodata.getWeight() : "");
        setSpinnerValue(maritalStatusSpinner, biodata.getMaritalStatus());
        setSpinnerValue(religionSpinner, biodata.getReligion());
        casteField.setText(biodata.getCaste() != null ? biodata.getCaste() : "");
        motherTongueField.setText(biodata.getMotherTongue() != null ? biodata.getMotherTongue() : "");
        setSpinnerValue(complexionSpinner, biodata.getComplexion());
        setSpinnerValue(bloodGroupSpinner, biodata.getBloodGroup());

        setSpinnerValue(educationSpinner, biodata.getEducation());
        occupationField.setText(biodata.getOccupation() != null ? biodata.getOccupation() : "");
        setSpinnerValue(incomeSpinner, biodata.getAnnualIncome());
        companyField.setText(biodata.getCompanyName() != null ? biodata.getCompanyName() : "");

        fatherNameField.setText(biodata.getFatherName() != null ? biodata.getFatherName() : "");
        fatherOccupationField.setText(biodata.getFatherOccupation() != null ? biodata.getFatherOccupation() : "");
        motherNameField.setText(biodata.getMotherName() != null ? biodata.getMotherName() : "");
        motherOccupationField.setText(biodata.getMotherOccupation() != null ? biodata.getMotherOccupation() : "");
        siblingsField.setText(biodata.getSiblings() != null ? biodata.getSiblings() : "");
        setSpinnerValue(familyTypeSpinner, biodata.getFamilyType());
        setSpinnerValue(familyStatusSpinner, biodata.getFamilyStatus());

        addressField.setText(biodata.getAddress() != null ? biodata.getAddress() : "");
        cityField.setText(biodata.getCity() != null ? biodata.getCity() : "");
        stateField.setText(biodata.getState() != null ? biodata.getState() : "");
        countryField.setText((biodata.getCountry() == null || biodata.getCountry().isEmpty()) ? "Bangladesh" : biodata.getCountry());

        aboutMeField.setText(biodata.getAboutMe() != null ? biodata.getAboutMe() : "");
        hobbiesField.setText(biodata.getHobbies() != null ? biodata.getHobbies() : "");

        partnerAgeFromField.setText(biodata.getPartnerAgeFrom() > 0 ? String.valueOf(biodata.getPartnerAgeFrom()) : "");
        partnerAgeToField.setText(biodata.getPartnerAgeTo() > 0 ? String.valueOf(biodata.getPartnerAgeTo()) : "");
        setSpinnerValue(partnerReligionSpinner, biodata.getPartnerReligion());
        setSpinnerValue(partnerEducationSpinner, biodata.getPartnerEducation());
        setSpinnerValue(partnerMaritalStatusSpinner, biodata.getPartnerMaritalStatus());
        partnerExpectationsField.setText(biodata.getPartnerExpectations() != null ? biodata.getPartnerExpectations() : "");
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null || value.isEmpty()) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private String getSpinnerValue(Spinner spinner) {
        String value = spinner.getSelectedItem().toString();
        if (value.startsWith("Select") || value.equals("Any")) {
            return "";
        }
        return value;
    }

    private void saveBiodata() {
        showLoading(true);

        int userId = sessionManager.getUserId();
        int biodataId = existingBiodata != null ? existingBiodata.getId() : 0;

        int age = 0;
        try {
            age = Integer.parseInt(ageField.getText().toString().trim());
        } catch (NumberFormatException e) {
            // ignore
        }

        int partnerAgeFrom = 18;
        int partnerAgeTo = 40;
        try {
            partnerAgeFrom = Integer.parseInt(partnerAgeFromField.getText().toString().trim());
            partnerAgeTo = Integer.parseInt(partnerAgeToField.getText().toString().trim());
        } catch (NumberFormatException e) {
            // use defaults
        }

        Biodata biodata;
        if (existingBiodata != null) {
            // Update existing
            biodata = existingBiodata;
            biodata.setDateOfBirth(dateOfBirthField.getText().toString().trim());
            biodata.setAge(age);
            biodata.setHeight(getSpinnerValue(heightSpinner));
            biodata.setWeight(weightField.getText().toString().trim());
            biodata.setMaritalStatus(getSpinnerValue(maritalStatusSpinner));
            biodata.setReligion(getSpinnerValue(religionSpinner));
            biodata.setCaste(casteField.getText().toString().trim());
            biodata.setMotherTongue(motherTongueField.getText().toString().trim());
            biodata.setComplexion(getSpinnerValue(complexionSpinner));
            biodata.setBloodGroup(getSpinnerValue(bloodGroupSpinner));
            biodata.setEducation(getSpinnerValue(educationSpinner));
            biodata.setOccupation(occupationField.getText().toString().trim());
            biodata.setAnnualIncome(getSpinnerValue(incomeSpinner));
            biodata.setCompanyName(companyField.getText().toString().trim());
            biodata.setFatherName(fatherNameField.getText().toString().trim());
            biodata.setFatherOccupation(fatherOccupationField.getText().toString().trim());
            biodata.setMotherName(motherNameField.getText().toString().trim());
            biodata.setMotherOccupation(motherOccupationField.getText().toString().trim());
            biodata.setSiblings(siblingsField.getText().toString().trim());
            biodata.setFamilyType(getSpinnerValue(familyTypeSpinner));
            biodata.setFamilyStatus(getSpinnerValue(familyStatusSpinner));
            biodata.setAddress(addressField.getText().toString().trim());
            biodata.setCity(cityField.getText().toString().trim());
            biodata.setState(stateField.getText().toString().trim());
            biodata.setCountry(countryField.getText().toString().trim());
            biodata.setAboutMe(aboutMeField.getText().toString().trim());
            biodata.setHobbies(hobbiesField.getText().toString().trim());
            biodata.setPartnerAgeFrom(partnerAgeFrom);
            biodata.setPartnerAgeTo(partnerAgeTo);
            biodata.setPartnerReligion(getSpinnerValue(partnerReligionSpinner));
            biodata.setPartnerEducation(getSpinnerValue(partnerEducationSpinner));
            biodata.setPartnerMaritalStatus(getSpinnerValue(partnerMaritalStatusSpinner));
            biodata.setPartnerExpectations(partnerExpectationsField.getText().toString().trim());
            biodata.setProfileCompleted(true);
        } else {
            // Create new
            biodata = new Biodata(
                userId,
                dateOfBirthField.getText().toString().trim(),
                age,
                getSpinnerValue(heightSpinner),
                weightField.getText().toString().trim(),
                getSpinnerValue(maritalStatusSpinner),
                getSpinnerValue(religionSpinner),
                getSpinnerValue(educationSpinner),
                occupationField.getText().toString().trim(),
                addressField.getText().toString().trim(),
                cityField.getText().toString().trim(),
                stateField.getText().toString().trim(),
                countryField.getText().toString().trim(),
                fatherNameField.getText().toString().trim(),
                motherNameField.getText().toString().trim(),
                aboutMeField.getText().toString().trim(),
                partnerExpectationsField.getText().toString().trim()
            );
            biodata.setCaste(casteField.getText().toString().trim());
            biodata.setMotherTongue(motherTongueField.getText().toString().trim());
            biodata.setComplexion(getSpinnerValue(complexionSpinner));
            biodata.setBloodGroup(getSpinnerValue(bloodGroupSpinner));
            biodata.setAnnualIncome(getSpinnerValue(incomeSpinner));
            biodata.setCompanyName(companyField.getText().toString().trim());
            biodata.setFatherOccupation(fatherOccupationField.getText().toString().trim());
            biodata.setMotherOccupation(motherOccupationField.getText().toString().trim());
            biodata.setSiblings(siblingsField.getText().toString().trim());
            biodata.setFamilyType(getSpinnerValue(familyTypeSpinner));
            biodata.setFamilyStatus(getSpinnerValue(familyStatusSpinner));
            biodata.setHobbies(hobbiesField.getText().toString().trim());
            biodata.setPartnerAgeFrom(partnerAgeFrom);
            biodata.setPartnerAgeTo(partnerAgeTo);
            biodata.setPartnerReligion(getSpinnerValue(partnerReligionSpinner));
            biodata.setPartnerEducation(getSpinnerValue(partnerEducationSpinner));
            biodata.setPartnerMaritalStatus(getSpinnerValue(partnerMaritalStatusSpinner));
            biodata.setProfileCompleted(true);
        }

        Biodata finalBiodata = biodata;
        executorService.execute(() -> {
            if (existingBiodata != null) {
                biodataDAO.updateBiodata(finalBiodata);
            } else {
                biodataDAO.insertBiodata(finalBiodata);
            }

            runOnUiThread(() -> {
                showLoading(false);
                Toast.makeText(this, "Biodata saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!show);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
