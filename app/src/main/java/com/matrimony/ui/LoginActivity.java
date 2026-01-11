package com.matrimony.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.matrimony.R;
import com.matrimony.database.UserDAO;
import com.matrimony.database.DatabaseHelper;
import com.matrimony.model.User;
import com.matrimony.util.SessionManager;
import com.matrimony.util.DatabaseVerifier;
import com.matrimony.util.DatabaseDumper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField;
    private EditText passwordField;
    private Button loginButton;
    private TextView createAccountLink;
    private ProgressBar progressBar;
    
    private UserDAO userDAO;
    private SessionManager sessionManager;
    private ExecutorService executorService;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // CRITICAL: Verify database is working FIRST
        android.util.Log.d("LoginActivity", "Verifying database...");
        boolean dbOk = DatabaseVerifier.verifyDatabase(this);
        if (!dbOk) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Database Error");
            builder.setMessage("Database verification failed! The app may not work correctly.\n\nCheck Logcat for details (tag: DatabaseVerifier)");
            builder.setPositiveButton("Continue Anyway", (dialog, which) -> {});
            builder.show();
        }
        
        // Dump current database contents for debugging
        android.util.Log.d("LoginActivity", "Dumping database contents...");
        DatabaseDumper.dumpAllData(this);

        // Initialize database and session manager
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);
        sessionManager = new SessionManager(this);
        executorService = Executors.newSingleThreadExecutor();
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard();
            return;
        }

        // Initialize views
        initViews();
        setupClickListeners();
        
        // Pre-fill email if coming from registration
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("registered_email")) {
            String email = intent.getStringExtra("registered_email");
            emailField.setText(email);
            passwordField.requestFocus();
        }
    }

    private void initViews() {
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);
        createAccountLink = findViewById(R.id.createAccountLink);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> handleLogin());
        
        createAccountLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // Validation
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

        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Password is required");
            passwordField.requestFocus();
            return;
        }

        // Show progress
        showLoading(true);

        // Authenticate with Firebase first
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Firebase authentication successful
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    String firebaseUid = null;
                    if (firebaseUser != null) {
                        firebaseUid = firebaseUser.getUid();
                        android.util.Log.d("LoginActivity", "Firebase login successful with UID: " + firebaseUid);
                    }

                    // Now load user data from local database and update Firebase UID if needed
                    final String finalFirebaseUid = firebaseUid;
                    executorService.execute(() -> loadUserFromLocalDatabase(email, password, finalFirebaseUid));

                } else {
                    // Firebase authentication failed
                    showLoading(false);
                    String errorMessage = task.getException() != null ?
                        task.getException().getMessage() : "Unknown error";

                    android.util.Log.e("LoginActivity", "Firebase login failed: " + errorMessage);

                    Toast.makeText(this, "Login failed: " + errorMessage,
                        Toast.LENGTH_LONG).show();
                }
            });
    }

    private void loadUserFromLocalDatabase(String email, String password, String firebaseUid) {
            android.util.Log.d("LoginActivity", "Attempting login - Email: " + email + ", Firebase UID: " + firebaseUid);

            User user = userDAO.authenticateUser(email, password);

            // If user exists but doesn't have Firebase UID, update it
            if (user != null && firebaseUid != null && user.getFirebaseUid() == null) {
                android.util.Log.d("LoginActivity", "Updating user with Firebase UID: " + firebaseUid);
                user.setFirebaseUid(firebaseUid);
                userDAO.updateUser(user);
            }

            runOnUiThread(() -> {
                showLoading(false);
                
                if (user != null) {
                    // Save session
                    sessionManager.createLoginSession(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getGender()
                    );
                    
                    Toast.makeText(this, "Welcome, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                } else {
                    // Check if email exists to give better error message
                    executorService.execute(() -> {
                        User checkUser = userDAO.getUserByEmail(email);
                        runOnUiThread(() -> {
                            if (checkUser != null) {
                                Toast.makeText(this, "Wrong password for " + email, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "No account found with email: " + email, Toast.LENGTH_LONG).show();
                            }
                        });
                    });
                }
            });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
