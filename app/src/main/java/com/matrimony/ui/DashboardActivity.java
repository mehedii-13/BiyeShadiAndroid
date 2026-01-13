package com.matrimony.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.matrimony.R;
import com.matrimony.adapter.SuggestionProfileAdapter;
import com.matrimony.database.BiodataDAO;
import com.matrimony.database.ContactRequestDAO;
import com.matrimony.database.ShortlistDAO;
import com.matrimony.database.UserDAO;
import com.matrimony.database.DatabaseHelper;
import com.matrimony.model.Biodata;
import com.matrimony.model.User;
import com.matrimony.util.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    private TextView welcomeText;
    private TextView profileCompletionText;
    private ProgressBar profileProgressBar;
    private TextView profileStatusMessage;
    
    // Statistics
    private TextView shortlistCount;
    private TextView pendingRequestCount;
    private TextView contactRequestCount;
    private TextView connectedPeopleCount;
    
    // Quick Actions
    private CardView cardSearchMatches;
    private CardView cardMyProfile;
    private CardView cardShortlist;
    private CardView cardContactRequests;
    private CardView cardConnectedPeople;
    private CardView cardEditBiodata;
    private CardView cardRequestsSent;

    // Suggestions
    private RecyclerView suggestionsRecyclerView;
    private MaterialButton seeMoreSuggestionsButton;
    private TextView viewAllSuggestionsText;
    private SuggestionProfileAdapter suggestionsAdapter;
    private List<SuggestionProfileAdapter.SuggestionProfile> suggestionsList = new ArrayList<>();
    private int currentAgeTolerance = 4; // Start with 4 years tolerance

    // User Info
    private TextView userNameInfo;
    private TextView userEmailInfo;
    private TextView userPhoneInfo;
    private TextView userGenderInfo;

    private SessionManager sessionManager;
    private UserDAO userDAO;
    private BiodataDAO biodataDAO;
    private ShortlistDAO shortlistDAO;
    private ContactRequestDAO contactRequestDAO;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);
        biodataDAO = new BiodataDAO(dbHelper);
        shortlistDAO = new ShortlistDAO(dbHelper);
        contactRequestDAO = new ContactRequestDAO(dbHelper);
        executorService = Executors.newSingleThreadExecutor();

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        setupToolbar();
        initViews();
        setupClickListeners();
        setupSuggestionsRecyclerView();
        loadUserData();
        loadSuggestions(currentAgeTolerance);
        loadStatistics();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dashboard");
        }
    }

    private void initViews() {
        welcomeText = findViewById(R.id.welcomeText);
        profileCompletionText = findViewById(R.id.profileCompletionText);
        profileProgressBar = findViewById(R.id.profileProgressBar);
        profileStatusMessage = findViewById(R.id.profileStatusMessage);
        
        // Statistics
        shortlistCount = findViewById(R.id.shortlistCount);
        pendingRequestCount = findViewById(R.id.pendingRequestCount);
        contactRequestCount = findViewById(R.id.contactRequestCount);
        connectedPeopleCount = findViewById(R.id.connectedPeopleCount);
        
        // Quick Action Cards
        cardSearchMatches = findViewById(R.id.cardSearchMatches);
        cardMyProfile = findViewById(R.id.cardMyProfile);
        cardShortlist = findViewById(R.id.cardShortlist);
        cardContactRequests = findViewById(R.id.cardContactRequests);
        cardConnectedPeople = findViewById(R.id.cardConnectedPeople);
        cardEditBiodata = findViewById(R.id.cardEditBiodata);
        cardRequestsSent = findViewById(R.id.cardRequestsSent);

        // Suggestions
        suggestionsRecyclerView = findViewById(R.id.suggestionsRecyclerView);
        seeMoreSuggestionsButton = findViewById(R.id.seeMoreSuggestionsButton);
        viewAllSuggestionsText = findViewById(R.id.viewAllSuggestionsText);

        // User Info
        userNameInfo = findViewById(R.id.userNameInfo);
        userEmailInfo = findViewById(R.id.userEmailInfo);
        userPhoneInfo = findViewById(R.id.userPhoneInfo);
        userGenderInfo = findViewById(R.id.userGenderInfo);
    }

    private void setupClickListeners() {
        cardSearchMatches.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchMatchesActivity.class));
        });

        cardMyProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, MyProfileActivity.class));
        });

        cardShortlist.setOnClickListener(v -> {
            startActivity(new Intent(this, ShortlistActivity.class));
        });

        cardContactRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, ContactRequestsActivity.class));
        });

        cardConnectedPeople.setOnClickListener(v -> {
            startActivity(new Intent(this, ConnectedPeopleActivity.class));
        });

        cardEditBiodata.setOnClickListener(v -> {
            startActivity(new Intent(this, BiodataFormActivity.class));
        });

        cardRequestsSent.setOnClickListener(v -> {
            startActivity(new Intent(this, RequestsSentActivity.class));
        });

        seeMoreSuggestionsButton.setOnClickListener(v -> {
            currentAgeTolerance = 5; // Expand criteria
            loadSuggestions(currentAgeTolerance);
        });

        viewAllSuggestionsText.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchMatchesActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        String userName = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();
        String userPhone = sessionManager.getUserPhone();
        String userGender = sessionManager.getUserGender();

        welcomeText.setText("Welcome, " + userName + "!");
        userNameInfo.setText(userName);
        userEmailInfo.setText(userEmail);
        userPhoneInfo.setText(userPhone);
        userGenderInfo.setText(userGender);

        // Load profile completion
        loadProfileCompletion();
    }

    private void loadProfileCompletion() {
        int userId = sessionManager.getUserId();
        
        executorService.execute(() -> {
            Biodata biodata = biodataDAO.getBiodataByUserId(userId);
            
            runOnUiThread(() -> {
                int completion = calculateProfileCompletion(biodata);
                profileProgressBar.setProgress(completion);
                profileCompletionText.setText(completion + "%");
                
                if (completion == 100) {
                    profileStatusMessage.setText("Your profile is complete! Great job!");
                    profileStatusMessage.setTextColor(getResources().getColor(R.color.success_green));
                } else if (completion >= 60) {
                    profileStatusMessage.setText("Almost there! Complete your biodata.");
                    profileStatusMessage.setTextColor(getResources().getColor(R.color.warning_orange));
                } else {
                    profileStatusMessage.setText("Please complete your biodata to find matches.");
                    profileStatusMessage.setTextColor(getResources().getColor(R.color.error_red));
                }
            });
        });
    }

    private int calculateProfileCompletion(Biodata biodata) {
        if (biodata == null) return 20; // Basic account info only
        
        int totalFields = 15;
        int completedFields = 4; // name, email, phone, gender from registration
        
        if (biodata.getDateOfBirth() != null && !biodata.getDateOfBirth().isEmpty()) completedFields++;
        if (biodata.getHeight() != null && !biodata.getHeight().isEmpty()) completedFields++;
        if (biodata.getMaritalStatus() != null && !biodata.getMaritalStatus().isEmpty()) completedFields++;
        if (biodata.getReligion() != null && !biodata.getReligion().isEmpty()) completedFields++;
        if (biodata.getEducation() != null && !biodata.getEducation().isEmpty()) completedFields++;
        if (biodata.getOccupation() != null && !biodata.getOccupation().isEmpty()) completedFields++;
        if (biodata.getCity() != null && !biodata.getCity().isEmpty()) completedFields++;
        if (biodata.getAboutMe() != null && !biodata.getAboutMe().isEmpty()) completedFields++;
        if (biodata.getFatherName() != null && !biodata.getFatherName().isEmpty()) completedFields++;
        if (biodata.getMotherName() != null && !biodata.getMotherName().isEmpty()) completedFields++;
        if (biodata.getAge() > 0) completedFields++;
        
        return (completedFields * 100) / totalFields;
    }

    private void loadStatistics() {
        int userId = sessionManager.getUserId();
        
        executorService.execute(() -> {
            try {
                int shortlist = shortlistDAO.getShortlistCount(userId);
                int pending = contactRequestDAO.getPendingRequestCount(userId);
                int sent = contactRequestDAO.getSentRequestsCount(userId); // Only pending sent requests
                int connected = contactRequestDAO.getConnectedCount(userId);

                runOnUiThread(() -> {
                    shortlistCount.setText(String.valueOf(shortlist));
                    pendingRequestCount.setText(String.valueOf(pending));
                    contactRequestCount.setText(String.valueOf(sent));
                    connectedPeopleCount.setText(String.valueOf(connected));
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    shortlistCount.setText("0");
                    pendingRequestCount.setText("0");
                    contactRequestCount.setText("0");
                    connectedPeopleCount.setText("0");
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> {
                sessionManager.logoutUser();
                navigateToLogin();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupSuggestionsRecyclerView() {
        suggestionsAdapter = new SuggestionProfileAdapter(suggestionsList, userId -> {
            Intent intent = new Intent(this, ViewProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        suggestionsRecyclerView.setLayoutManager(layoutManager);
        suggestionsRecyclerView.setAdapter(suggestionsAdapter);
    }

    private void loadSuggestions(int ageTolerance) {
        int currentUserId = sessionManager.getUserId();
        String currentUserGender = sessionManager.getUserGender();

        executorService.execute(() -> {
            Biodata currentUserBiodata = biodataDAO.getBiodataByUserId(currentUserId);

            if (currentUserBiodata == null) {
                runOnUiThread(() -> {
                    suggestionsRecyclerView.setVisibility(View.GONE);
                });
                return;
            }

            List<SuggestionProfileAdapter.SuggestionProfile> suggestions = new ArrayList<>();
            List<Biodata> allBiodata = biodataDAO.getAllBiodata();

            int currentUserAge = currentUserBiodata.getAge();
            String currentUserCity = currentUserBiodata.getCity();
            String currentUserIncome = currentUserBiodata.getAnnualIncome();

            for (Biodata biodata : allBiodata) {
                if (biodata.getUserId() == currentUserId) continue;

                User user = userDAO.getUserById(biodata.getUserId());
                if (user == null || currentUserGender.equals(user.getGender())) continue;

                // Skip if already connected
                boolean isConnected = contactRequestDAO.areUsersConnected(currentUserId, biodata.getUserId());
                if (isConnected) continue;

                // Calculate match percentage
                int matchScore = calculateMatchScore(currentUserBiodata, biodata, ageTolerance);

                if (matchScore > 50) { // Only show if more than 50% match
                    String location = biodata.getCity() != null ? biodata.getCity() : "Not specified";
                    suggestions.add(new SuggestionProfileAdapter.SuggestionProfile(
                        user.getId(),
                        user.getName(),
                        biodata.getAge(),
                        location,
                        matchScore,
                        user.getProfilePhotoUri()
                    ));
                }
            }

            // Sort by match percentage
            Collections.sort(suggestions, (a, b) -> Integer.compare(b.getMatchPercentage(), a.getMatchPercentage()));

            // Limit to top 10
            if (suggestions.size() > 10) {
                suggestions = suggestions.subList(0, 10);
            }

            List<SuggestionProfileAdapter.SuggestionProfile> finalSuggestions = suggestions;
            runOnUiThread(() -> {
                suggestionsList.clear();
                suggestionsList.addAll(finalSuggestions);
                suggestionsAdapter.notifyDataSetChanged();

                if (suggestionsList.isEmpty()) {
                    suggestionsRecyclerView.setVisibility(View.GONE);
                    seeMoreSuggestionsButton.setVisibility(View.GONE);
                } else {
                    suggestionsRecyclerView.setVisibility(View.VISIBLE);
                    // Show "See More" button if tolerance is still at 4
                    seeMoreSuggestionsButton.setVisibility(ageTolerance == 4 && suggestionsList.size() >= 5 ? View.VISIBLE : View.GONE);
                }
            });
        });
    }

    private int calculateMatchScore(Biodata currentUser, Biodata otherUser, int ageTolerance) {
        int score = 0;
        int totalCriteria = 0;

        // Age compatibility (30 points)
        totalCriteria++;
        int ageDiff = Math.abs(currentUser.getAge() - otherUser.getAge());
        if (ageDiff <= ageTolerance) {
            score += 30;
        } else if (ageDiff <= ageTolerance + 2) {
            score += 15;
        }

        // Location proximity (25 points)
        totalCriteria++;
        if (currentUser.getCity() != null && otherUser.getCity() != null) {
            if (currentUser.getCity().equalsIgnoreCase(otherUser.getCity())) {
                score += 25;
            } else if (currentUser.getState() != null && otherUser.getState() != null &&
                      currentUser.getState().equalsIgnoreCase(otherUser.getState())) {
                score += 12;
            }
        }

        // Income compatibility (20 points)
        totalCriteria++;
        if (isIncomeCompatible(currentUser.getAnnualIncome(), otherUser.getAnnualIncome())) {
            score += 20;
        }

        // Education compatibility (15 points)
        totalCriteria++;
        if (currentUser.getEducation() != null && otherUser.getEducation() != null &&
            currentUser.getEducation().equalsIgnoreCase(otherUser.getEducation())) {
            score += 15;
        }

        // Religion match (10 points)
        totalCriteria++;
        if (currentUser.getReligion() != null && otherUser.getReligion() != null &&
            currentUser.getReligion().equalsIgnoreCase(otherUser.getReligion())) {
            score += 10;
        }

        return score;
    }

    private boolean isIncomeCompatible(String income1, String income2) {
        if (income1 == null || income2 == null || income1.isEmpty() || income2.isEmpty()) {
            return false;
        }

        // Define income brackets
        String[] incomeBrackets = {
            "Below 2 Lakh",
            "2-5 Lakh",
            "5-10 Lakh",
            "10-20 Lakh",
            "20-50 Lakh",
            "50 Lakh - 1 Crore",
            "Above 1 Crore"
        };

        int bracket1 = -1, bracket2 = -1;
        for (int i = 0; i < incomeBrackets.length; i++) {
            if (incomeBrackets[i].equals(income1)) bracket1 = i;
            if (incomeBrackets[i].equals(income2)) bracket2 = i;
        }

        if (bracket1 == -1 || bracket2 == -1) return false;

        // Compatible if within 1 bracket
        return Math.abs(bracket1 - bracket2) <= 1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileCompletion();
        loadStatistics();
        loadSuggestions(currentAgeTolerance);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
