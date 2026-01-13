package com.matrimony.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.matrimony.R;
import com.matrimony.database.BiodataDAO;
import com.matrimony.database.ContactRequestDAO;
import com.matrimony.database.ShortlistDAO;
import com.matrimony.database.UserDAO;
import com.matrimony.database.DatabaseHelper;
import com.matrimony.model.Biodata;
import com.matrimony.model.ContactRequest;
import com.matrimony.model.User;
import com.matrimony.util.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView nameText;
    private TextView ageGenderText;
    private TextView locationText;
    
    // Personal Info
    private TextView heightText;
    private TextView weightText;
    private TextView maritalStatusText;
    private TextView religionText;
    private TextView motherTongueText;
    private TextView complexionText;
    private TextView bloodGroupText;
    
    // Education & Career
    private TextView educationText;
    private TextView occupationText;
    private TextView incomeText;
    private TextView companyText;
    
    // Family
    private TextView fatherNameText;
    private TextView motherNameText;
    private TextView siblingsText;
    private TextView familyTypeText;
    
    // About
    private TextView aboutMeText;
    private TextView hobbiesText;
    
    // Partner Preferences
    private TextView partnerAgeText;
    private TextView partnerReligionText;
    private TextView partnerEducationText;
    private TextView partnerExpectationsText;

    private Button shortlistButton;
    private Button sendRequestButton;
    private ProgressBar progressBar;

    // Contact Details
    private View contactCard;
    private TextView phoneText;
    private TextView emailText;

    private int profileUserId;
    private SessionManager sessionManager;
    private UserDAO userDAO;
    private BiodataDAO biodataDAO;
    private ShortlistDAO shortlistDAO;
    private ContactRequestDAO contactRequestDAO;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        sessionManager = new SessionManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);
        biodataDAO = new BiodataDAO(dbHelper);
        shortlistDAO = new ShortlistDAO(dbHelper);
        contactRequestDAO = new ContactRequestDAO(dbHelper);
        executorService = Executors.newSingleThreadExecutor();

        profileUserId = getIntent().getIntExtra("userId", -1);
        if (profileUserId == -1) {
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        initViews();
        setupClickListeners();
        loadProfile();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("View Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        profileImageView = findViewById(R.id.profileImage);
        nameText = findViewById(R.id.nameText);
        ageGenderText = findViewById(R.id.ageLocationText); // Using ageLocationText as ageGenderText
        locationText = findViewById(R.id.locationText); // May or may not exist

        heightText = findViewById(R.id.heightText);
        weightText = findViewById(R.id.weightText);
        maritalStatusText = findViewById(R.id.maritalStatusText);
        religionText = findViewById(R.id.religionText);
        // motherTongueText = findViewById(R.id.motherTongueText);  // doesn't exist
        // complexionText = findViewById(R.id.complexionText);  // doesn't exist
        // bloodGroupText = findViewById(R.id.bloodGroupText);  // doesn't exist
        
        educationText = findViewById(R.id.educationText);
        occupationText = findViewById(R.id.occupationText);
        // incomeText = findViewById(R.id.incomeText);  // doesn't exist
        // companyText = findViewById(R.id.companyText);  // doesn't exist
        
        // fatherNameText = findViewById(R.id.fatherNameText);  // doesn't exist
        // motherNameText = findViewById(R.id.motherNameText);  // doesn't exist
        // siblingsText = findViewById(R.id.siblingsText);  // doesn't exist
        // familyTypeText = findViewById(R.id.familyTypeText);  // doesn't exist
        
        // aboutMeText = findViewById(R.id.aboutMeText);  // doesn't exist
        // hobbiesText = findViewById(R.id.hobbiesText);  // doesn't exist
        
        // partnerAgeText = findViewById(R.id.partnerAgeText);  // doesn't exist
        // partnerReligionText = findViewById(R.id.partnerReligionText);  // doesn't exist
        // partnerEducationText = findViewById(R.id.partnerEducationText);  // doesn't exist
        // partnerExpectationsText = findViewById(R.id.partnerExpectationsText);  // doesn't exist

        shortlistButton = findViewById(R.id.shortlistButton);
        sendRequestButton = findViewById(R.id.requestContactButton);
        progressBar = findViewById(R.id.progressBar);

        // Contact Details
        contactCard = findViewById(R.id.contactCard);
        phoneText = findViewById(R.id.phoneText);
        emailText = findViewById(R.id.emailText);
    }

    private void setupClickListeners() {
        shortlistButton.setOnClickListener(v -> toggleShortlist());
        sendRequestButton.setOnClickListener(v -> sendContactRequest());
    }

    private void loadProfile() {
        showLoading(true);
        
        executorService.execute(() -> {
            User user = userDAO.getUserById(profileUserId);
            Biodata biodata = biodataDAO.getBiodataByUserId(profileUserId);
            
            int currentUserId = sessionManager.getUserId();
            boolean isShortlisted = shortlistDAO.isShortlisted(currentUserId, profileUserId);

            // Get request status between users (one-way system)
            String requestStatus = contactRequestDAO.getRequestStatusBetweenUsers(currentUserId, profileUserId);

            // Check if connected (request accepted by either party)
            boolean isConnected = "accepted".equals(requestStatus);

            runOnUiThread(() -> {
                showLoading(false);
                
                if (user != null) {
                    displayProfile(user, biodata, isConnected);
                    updateButtons(isShortlisted, requestStatus);
                } else {
                    Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void displayProfile(User user, Biodata biodata, boolean isConnected) {
        nameText.setText(user.getName());
        
        // Load profile photo if available
        if (user.getProfilePhotoUri() != null && !user.getProfilePhotoUri().isEmpty()) {
            try {
                Uri photoUri = Uri.parse(user.getProfilePhotoUri());
                profileImageView.setImageURI(photoUri);
            } catch (Exception e) {
                // If loading fails, keep default image
            }
        }

        // Show/hide contact details based on connection status
        if (isConnected) {
            if (contactCard != null) contactCard.setVisibility(View.VISIBLE);
            if (phoneText != null) phoneText.setText(user.getPhone() != null ? user.getPhone() : "Not provided");
            if (emailText != null) emailText.setText(user.getEmail() != null ? user.getEmail() : "Not provided");
        } else {
            if (contactCard != null) contactCard.setVisibility(View.GONE);
        }

        if (biodata != null) {
            String ageGender = (biodata.getAge() > 0 ? biodata.getAge() + " years, " : "") + user.getGender();
            if (ageGenderText != null) ageGenderText.setText(ageGender);

            String location = "";
            if (biodata.getCity() != null && !biodata.getCity().isEmpty()) location += biodata.getCity();
            if (biodata.getState() != null && !biodata.getState().isEmpty()) location += (location.isEmpty() ? "" : ", ") + biodata.getState();
            if (biodata.getCountry() != null && !biodata.getCountry().isEmpty()) location += (location.isEmpty() ? "" : ", ") + biodata.getCountry();
            if (locationText != null) locationText.setText(location.isEmpty() ? "Not specified" : location);

            // Personal Info - only set if views exist
            if (heightText != null) heightText.setText(getDisplayValue(biodata.getHeight()));
            if (weightText != null) weightText.setText(getDisplayValue(biodata.getWeight()));
            if (maritalStatusText != null) maritalStatusText.setText(getDisplayValue(biodata.getMaritalStatus()));
            if (religionText != null) religionText.setText(getDisplayValue(biodata.getReligion()));
            // motherTongueText, complexionText, bloodGroupText - don't exist in layout

            // Education & Career
            if (educationText != null) educationText.setText(getDisplayValue(biodata.getEducation()));
            if (occupationText != null) occupationText.setText(getDisplayValue(biodata.getOccupation()));
            // incomeText, companyText - don't exist in layout

            // Family - don't exist in layout
            // fatherNameText, motherNameText, siblingsText, familyTypeText

            // About - don't exist in layout
            // aboutMeText, hobbiesText

            // Partner Preferences - don't exist in layout
            // partnerAgeText, partnerReligionText, partnerEducationText, partnerExpectationsText
        } else {
            if (ageGenderText != null) ageGenderText.setText(user.getGender());
            if (locationText != null) locationText.setText("Not specified");
        }
    }

    private String getDisplayValue(String value) {
        return (value == null || value.isEmpty()) ? "Not specified" : value;
    }

    private void updateButtons(boolean isShortlisted, String requestStatus) {
        shortlistButton.setText(isShortlisted ? "Remove from Shortlist" : "Add to Shortlist");

        // Handle different request states (one-way system like Facebook)
        switch (requestStatus) {
            case "accepted":
                // Request accepted - show "Accepted" (disabled)
                sendRequestButton.setText("Accepted");
                sendRequestButton.setEnabled(false);
                break;
            case "sent":
                // You sent a pending request - show "Request Sent" (disabled)
                sendRequestButton.setText("Request Sent");
                sendRequestButton.setEnabled(false);
                break;
            case "received":
                // You received a pending request - show "Respond to Request" (disabled)
                // User should go to Contact Requests to accept/reject
                sendRequestButton.setText("Respond to Request");
                sendRequestButton.setEnabled(false); // Can't send when you have pending received request
                break;
            default: // "none"
                // No request exists - show "Send Contact Request" (enabled)
                sendRequestButton.setText("Send Contact Request");
                sendRequestButton.setEnabled(true);
                break;
        }
    }

    private void toggleShortlist() {
        int userId = sessionManager.getUserId();
        
        executorService.execute(() -> {
            boolean isShortlisted = shortlistDAO.isShortlisted(userId, profileUserId);
            
            if (isShortlisted) {
                shortlistDAO.removeFromShortlist(userId, profileUserId);
                runOnUiThread(() -> {
                    shortlistButton.setText("Add to Shortlist");
                    Toast.makeText(this, "Removed from shortlist", Toast.LENGTH_SHORT).show();
                });
            } else {
                shortlistDAO.addToShortlist(userId, profileUserId);
                runOnUiThread(() -> {
                    shortlistButton.setText("Remove from Shortlist");
                    Toast.makeText(this, "Added to shortlist", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void sendContactRequest() {
        int userId = sessionManager.getUserId();
        
        executorService.execute(() -> {
            // Check if any request exists between users in either direction
            boolean anyRequestExists = contactRequestDAO.anyRequestExistsBetweenUsers(userId, profileUserId);

            if (!anyRequestExists) {
                // No request exists - create new request
                ContactRequest request = new ContactRequest(userId, profileUserId, "");
                contactRequestDAO.insertRequest(request);
                runOnUiThread(() -> {
                    sendRequestButton.setText("Request Sent");
                    sendRequestButton.setEnabled(false);
                    Toast.makeText(this, "Contact request sent!", Toast.LENGTH_SHORT).show();
                });
            } else {
                // Request already exists (shouldn't happen if button logic is correct)
                runOnUiThread(() -> {
                    Toast.makeText(this, "Request already exists between you and this user", Toast.LENGTH_SHORT).show();
                });
            }
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
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
