package com.matrimony.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.matrimony.R;
import com.matrimony.database.UserDAO;
import com.matrimony.database.DatabaseHelper;
import com.matrimony.model.User;
import com.matrimony.util.DatabaseDumper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText fullNameField;
    private EditText emailField;
    private EditText phoneField;
    private Spinner genderSpinner;
    private EditText passwordField;
    private EditText confirmPasswordField;
    private Button createAccountButton;
    private TextView backToLoginLink;
    private ProgressBar progressBar;

    private UserDAO userDAO;
    private ExecutorService executorService;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);
        executorService = Executors.newSingleThreadExecutor();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupSpinner();
        setupClickListeners();
    }

    private void initViews() {
        fullNameField = findViewById(R.id.etFullName);
        emailField = findViewById(R.id.etEmail);
        phoneField = findViewById(R.id.etPhone);
        genderSpinner = findViewById(R.id.spinnerGender);
        passwordField = findViewById(R.id.etPassword);
        confirmPasswordField = findViewById(R.id.etConfirmPassword);
        createAccountButton = findViewById(R.id.btnCreateAccount);
        backToLoginLink = findViewById(R.id.tvBackToLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinner() {
        String[] genders = {"Select Gender", "Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            R.layout.spinner_item, genders);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
    }

    private void setupClickListeners() {
        createAccountButton.setOnClickListener(v -> handleCreateAccount());
        
        backToLoginLink.setOnClickListener(v -> {
            finish();
        });
    }

    private void handleCreateAccount() {
        String fullName = fullNameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(fullName)) {
            fullNameField.setError("Full name is required");
            fullNameField.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailField.setError("Email is required");
            emailField.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Please enter a valid email");
            emailField.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneField.setError("Phone number is required");
            phoneField.requestFocus();
            return;
        }

        if (phone.length() < 10) {
            phoneField.setError("Phone number must be at least 10 digits");
            phoneField.requestFocus();
            return;
        }

        if (gender.equals("Select Gender")) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Password is required");
            passwordField.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordField.setError("Password must be at least 6 characters");
            passwordField.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordField.setError("Passwords do not match");
            confirmPasswordField.requestFocus();
            return;
        }

        // Show progress
        showLoading(true);

        // First, create Firebase Authentication account
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Firebase account created successfully
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    String firebaseUid = null;
                    if (firebaseUser != null) {
                        firebaseUid = firebaseUser.getUid();
                        android.util.Log.d("CreateAccount", "Firebase account created with UID: " + firebaseUid);
                    }

                    // Now save to local database with Firebase UID
                    final String finalFirebaseUid = firebaseUid;
                    executorService.execute(() -> saveUserToLocalDatabase(fullName, email, phone, gender, password, finalFirebaseUid));

                } else {
                    // Firebase account creation failed
                    showLoading(false);
                    String errorMessage = task.getException() != null ?
                        task.getException().getMessage() : "Unknown error";

                    android.util.Log.e("CreateAccount", "Firebase auth failed: " + errorMessage);

                    Toast.makeText(this, "Account creation failed: " + errorMessage,
                        Toast.LENGTH_LONG).show();
                }
            });
    }

    private void saveUserToLocalDatabase(String fullName, String email, String phone, String gender, String password, String firebaseUid) {
            User existingUser = userDAO.getUserByEmail(email);
            
            if (existingUser != null) {
                runOnUiThread(() -> {
                    showLoading(false);
                    emailField.setError("Email already registered");
                    emailField.requestFocus();
                    Toast.makeText(this, "This email is already registered", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            // Create new user with Firebase UID
            User newUser = new User(fullName, email, password, phone, gender, firebaseUid);

            // Log what we're about to insert
            android.util.Log.d("CreateAccount", "Creating user with Firebase UID: " + firebaseUid + ", Email: " + email);

            long userId = userDAO.insertUser(newUser);
            
            // Dump database to see what was actually saved
            android.util.Log.d("CreateAccount", "=== DATABASE AFTER INSERT ===");
            com.matrimony.util.DatabaseDumper.dumpUsersOnly(CreateAccountActivity.this);
            
            // Verify the user was created by trying to read it back
            User verifyUser = null;
            if (userId > 0) {
                verifyUser = userDAO.getUserByEmail(email);
            }

            final User finalVerifyUser = verifyUser;
            final long finalUserId = userId;
            
            runOnUiThread(() -> {
                showLoading(false);
                
                if (finalUserId > 0 && finalVerifyUser != null) {
                    // Show detailed success message with verification
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setTitle("Account Created Successfully!");
                    builder.setMessage(
                            "User ID: " + finalUserId + "\n" +
                            "Name: " + finalVerifyUser.getName() + "\n" +
                            "Email: " + finalVerifyUser.getEmail() + "\n" +
                            "Phone: " + finalVerifyUser.getPhone() + "\n" +
                            "Gender: " + finalVerifyUser.getGender() + "\n" +
                            "Password Length: " + finalVerifyUser.getPassword().length() + " chars\n\n" +
                            "You can now login with your email and password."
                    );
                    builder.setPositiveButton("Go to Login", (dialog, which) -> {
                        Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                        intent.putExtra("registered_email", email);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });
                    builder.setCancelable(false);
                    builder.show();
                } else if (finalUserId > 0) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setTitle("Warning: Verification Failed");
                    builder.setMessage("Account was created with ID: " + finalUserId + "\n\nBut we couldn't verify it by reading it back from the database.\n\nThis might be a database issue. Try logging in anyway.");
                    builder.setPositiveButton("Try Login", (dialog, which) -> finish());
                    builder.show();
                } else {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setTitle("Account Creation Failed");
                    builder.setMessage("Failed to create account.\n\nReturn code: " + finalUserId + "\n\nThis usually means:\n- Database write error\n- Email already exists\n- Database permission issue\n\nCheck Logcat for details (tag: UserDAO)");
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
            });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        createAccountButton.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
