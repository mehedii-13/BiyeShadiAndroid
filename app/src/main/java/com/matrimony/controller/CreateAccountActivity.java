package com.matrimony.controller;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.matrimony.R;

/**
 * CreateAccountActivity - Handles user registration
 * This is the Android equivalent of CreateAccountController from JavaFX
 */
public class CreateAccountActivity extends AppCompatActivity {

    // UI Components
    private EditText etFullName;
    private EditText etEmail;
    private EditText etPhone;
    private Spinner spinnerGender;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private MaterialButton btnCreateAccount;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialize views
        initializeViews();

        // Setup gender spinner
        setupGenderSpinner();

        // Setup click listeners
        setupClickListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        spinnerGender = findViewById(R.id.spinnerGender);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    /**
     * Setup the gender spinner with options
     */
    private void setupGenderSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    /**
     * Setup click listeners for buttons and links
     */
    private void setupClickListeners() {
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCreateAccount();
            }
        });

        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBackToLogin();
            }
        });
    }

    /**
     * Handle create account button click
     * Equivalent to handleCreateAccount() in JavaFX controller
     */
    private void handleCreateAccount() {
        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validate inputs
        if (!validateInputs(fullName, email, phone, gender, password, confirmPassword)) {
            return;
        }

        // TODO: Implement account creation logic
        // This should connect to your backend/database
        createAccount(fullName, email, phone, gender, password);
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs(String fullName, String email, String phone, 
                                   String gender, String password, String confirmPassword) {
        // Check if any field is empty
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Please enter your full name");
            etFullName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return false;
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Please enter your phone number");
            etPhone.requestFocus();
            return false;
        }

        // Validate phone format (basic validation)
        if (phone.length() < 10) {
            etPhone.setError("Please enter a valid phone number");
            etPhone.requestFocus();
            return false;
        }

        if (gender.equals("Select your gender")) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please create a password");
            etPassword.requestFocus();
            return false;
        }

        // Validate password strength
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return false;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Create account with validated data
     */
    private void createAccount(String fullName, String email, String phone, 
                              String gender, String password) {
        // TODO: Implement your account creation logic here
        // This could involve:
        // 1. Making an API call to your backend
        // 2. Storing data in local database
        // 3. Using Firebase Authentication
        
        // For now, show a success message
        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();
        
        // Navigate to login or main screen
        // finish(); // Close this activity
    }

    /**
     * Handle back to login link click
     * Equivalent to handleBackToLogin() in JavaFX controller
     */
    private void handleBackToLogin() {
        // Navigate back to login screen
        finish(); // Close this activity and return to previous screen
        
        // Or if you need to start a specific activity:
        // Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
        // startActivity(intent);
        // finish();
    }
}

