package com.matrimony.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.matrimony.R;
import com.matrimony.database.BiodataDAO;
import com.matrimony.database.UserDAO;
import com.matrimony.database.DatabaseHelper;
import com.matrimony.model.Biodata;
import com.matrimony.model.User;
import com.matrimony.util.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyProfileActivity extends AppCompatActivity {

    // User Info
    private ImageView profileImageView;
    private TextView nameText;
    private TextView emailText;
    private TextView phoneText;
    private TextView genderText;
    
    private ProgressBar progressBar;
    private Button logoutButton;
    private Button editProfileButton;

    private SessionManager sessionManager;
    private UserDAO userDAO;
    private BiodataDAO biodataDAO;
    private ExecutorService executorService;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        sessionManager = new SessionManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);
        biodataDAO = new BiodataDAO(dbHelper);
        executorService = Executors.newSingleThreadExecutor();
        mAuth = FirebaseAuth.getInstance();

        setupToolbar();
        initViews();
        setupClickListeners();
        loadProfile();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        profileImageView = findViewById(R.id.profileImage);
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        phoneText = findViewById(R.id.phoneText);
        genderText = findViewById(R.id.genderText);
        
        TextView biodataActionButton = findViewById(R.id.biodataActionButton);
        progressBar = findViewById(R.id.progressBar);
        logoutButton = findViewById(R.id.logoutButton);
        editProfileButton = findViewById(R.id.editProfileButton);

        biodataActionButton.setOnClickListener(v -> {
            startActivity(new Intent(this, BiodataFormActivity.class));
        });
    }

    private void setupClickListeners() {
        // Logout button
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());

        // Edit profile button
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> performLogout())
            .setNegativeButton("No", null)
            .show();
    }

    private void performLogout() {
        // Sign out from Firebase
        mAuth.signOut();

        // Clear local session
        sessionManager.logoutUser();

        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void loadProfile() {
        showLoading(true);
        int userId = sessionManager.getUserId();

        executorService.execute(() -> {
            User user = userDAO.getUserById(userId);
            Biodata biodata = biodataDAO.getBiodataByUserId(userId);

            runOnUiThread(() -> {
                showLoading(false);
                
                if (user != null) {
                    nameText.setText(user.getName());
                    emailText.setText(user.getEmail());
                    phoneText.setText(user.getPhone());
                    genderText.setText(user.getGender());

                    // Load profile photo if available
                    android.util.Log.d("MyProfile", "User photo URI: " + user.getProfilePhotoUri());
                    if (user.getProfilePhotoUri() != null && !user.getProfilePhotoUri().isEmpty()) {
                        try {
                            Uri photoUri = Uri.parse(user.getProfilePhotoUri());
                            profileImageView.setImageURI(photoUri);
                            android.util.Log.d("MyProfile", "Photo set successfully from URI: " + photoUri);
                        } catch (Exception e) {
                            android.util.Log.e("MyProfile", "Error loading photo: " + e.getMessage());
                            // If loading fails, keep default image
                        }
                    } else {
                        android.util.Log.d("MyProfile", "No photo URI available, using default");
                    }
                }
                
                // Update biodata status text
                TextView biodataStatusText = findViewById(R.id.biodataStatusText);
                TextView biodataActionButton = findViewById(R.id.biodataActionButton);

                if (biodata != null) {
                    biodataStatusText.setText("Created");
                    biodataStatusText.setTextColor(getResources().getColor(R.color.success_green));
                    biodataActionButton.setText("Edit Biodata");
                } else {
                    biodataStatusText.setText("Not Created");
                    biodataStatusText.setTextColor(getResources().getColor(R.color.warning_orange));
                    biodataActionButton.setText("Create Biodata");
                }
            });
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
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
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
