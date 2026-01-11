package com.matrimony.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.matrimony.R;
import com.matrimony.database.UserDAO;
import com.matrimony.database.DatabaseHelper;
import com.matrimony.model.User;
import com.matrimony.util.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private FloatingActionButton changePhotoButton;
    private TextInputEditText nameField;
    private TextInputEditText emailField;
    private TextInputEditText phoneField;
    private TextInputEditText genderField;
    private MaterialButton saveButton;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private UserDAO userDAO;
    private ExecutorService executorService;
    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);
        executorService = Executors.newSingleThreadExecutor();

        setupToolbar();
        initViews();
        setupImagePicker();
        setupClickListeners();
        loadUserData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        profileImageView = findViewById(R.id.profileImageView);
        changePhotoButton = findViewById(R.id.changePhotoButton);
        nameField = findViewById(R.id.nameField);
        emailField = findViewById(R.id.emailField);
        phoneField = findViewById(R.id.phoneField);
        genderField = findViewById(R.id.genderField);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();

                    // Take persistent URI permission so we can access it later
                    if (selectedImageUri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(
                                selectedImageUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                            android.util.Log.d("EditProfile", "Persistent permission granted for URI");
                        } catch (SecurityException e) {
                            android.util.Log.e("EditProfile", "Failed to take persistent permission: " + e.getMessage());
                        }
                    }

                    profileImageView.setImageURI(selectedImageUri);
                    Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void setupClickListeners() {
        changePhotoButton.setOnClickListener(v -> openImagePicker());
        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        // Request persistent permission flags
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                       Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    private void loadUserData() {
        showLoading(true);
        int userId = sessionManager.getUserId();

        executorService.execute(() -> {
            User user = userDAO.getUserById(userId);

            runOnUiThread(() -> {
                showLoading(false);
                if (user != null) {
                    nameField.setText(user.getName());
                    emailField.setText(user.getEmail());
                    phoneField.setText(user.getPhone());
                    genderField.setText(user.getGender());

                    // Load existing profile photo if available
                    android.util.Log.d("EditProfile", "Loading user with photo URI: " + user.getProfilePhotoUri());
                    if (user.getProfilePhotoUri() != null && !user.getProfilePhotoUri().isEmpty()) {
                        try {
                            Uri photoUri = Uri.parse(user.getProfilePhotoUri());
                            profileImageView.setImageURI(photoUri);
                            selectedImageUri = photoUri; // Set so it doesn't get lost
                            android.util.Log.d("EditProfile", "Photo loaded successfully from URI: " + photoUri);
                        } catch (Exception e) {
                            android.util.Log.e("EditProfile", "Error loading photo: " + e.getMessage());
                            // If parsing fails, just use default
                        }
                    } else {
                        android.util.Log.d("EditProfile", "No existing photo URI");
                    }
                } else {
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void saveProfile() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            nameField.setError("Name is required");
            nameField.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailField.setError("Email is required");
            emailField.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Invalid email format");
            emailField.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            phoneField.setError("Phone is required");
            phoneField.requestFocus();
            return;
        }

        showLoading(true);
        int userId = sessionManager.getUserId();

        executorService.execute(() -> {
            User user = userDAO.getUserById(userId);

            if (user != null) {
                user.setName(name);
                user.setEmail(email);
                user.setPhone(phone);

                // Save profile photo URI if a new photo was selected
                // Note: If no new photo selected, the existing URI is already in the user object
                if (selectedImageUri != null) {
                    user.setProfilePhotoUri(selectedImageUri.toString());
                    android.util.Log.d("EditProfile", "Saving new photo URI: " + selectedImageUri.toString());
                } else {
                    android.util.Log.d("EditProfile", "No new photo selected, keeping existing: " + user.getProfilePhotoUri());
                }

                int result = userDAO.updateUser(user);

                runOnUiThread(() -> {
                    showLoading(false);
                    if (result > 0) {
                        // Update session
                        sessionManager.createLoginSession(user.getId(), user.getName(),
                            user.getEmail(), user.getPhone(), user.getGender());

                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
                });
            }
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

