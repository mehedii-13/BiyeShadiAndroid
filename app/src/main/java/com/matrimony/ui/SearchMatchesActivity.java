package com.matrimony.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.matrimony.R;
import com.matrimony.adapter.MatchProfileAdapter;
import com.matrimony.database.BiodataDAO;
import com.matrimony.database.ContactRequestDAO;
import com.matrimony.database.ShortlistDAO;
import com.matrimony.database.UserDAO;
import com.matrimony.database.DatabaseHelper;
import com.matrimony.model.Biodata;
import com.matrimony.model.ContactRequest;
import com.matrimony.model.MatchProfile;
import com.matrimony.model.User;
import com.matrimony.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchMatchesActivity extends AppCompatActivity implements MatchProfileAdapter.OnProfileClickListener {

    private EditText searchField;
    private TextView clearSearchButton;
    private Spinner minAgeSpinner;
    private Spinner maxAgeSpinner;
    private Spinner maritalStatusSpinner;
    private Spinner religionSpinner;
    private Spinner educationSpinner;
    private EditText cityField;
    private Button searchButton;
    private Button clearButton;
    private RecyclerView recyclerView;
    private TextView resultsLabel;
    private TextView noResultsText;
    private ProgressBar progressBar;

    private MatchProfileAdapter adapter;
    private List<MatchProfile> matchProfiles = new ArrayList<>();
    private List<MatchProfile> allProfiles = new ArrayList<>(); // Store all profiles for filtering
    private String currentSearchQuery = ""; // Store current search query

    private SessionManager sessionManager;
    private UserDAO userDAO;
    private BiodataDAO biodataDAO;
    private ShortlistDAO shortlistDAO;
    private ContactRequestDAO contactRequestDAO;
    private ExecutorService executorService;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_matches);

        sessionManager = new SessionManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);
        biodataDAO = new BiodataDAO(dbHelper);
        shortlistDAO = new ShortlistDAO(dbHelper);
        contactRequestDAO = new ContactRequestDAO(dbHelper);
        executorService = Executors.newSingleThreadExecutor();

        setupToolbar();
        initViews();
        setupSpinners();
        setupRecyclerView();
        setupClickListeners();
        loadAllMatches();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Search Matches");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        searchField = findViewById(R.id.searchField);
        clearSearchButton = findViewById(R.id.clearSearchButton);
        minAgeSpinner = findViewById(R.id.minAgeSpinner);
        maxAgeSpinner = findViewById(R.id.maxAgeSpinner);
        maritalStatusSpinner = findViewById(R.id.maritalStatusSpinner);
        religionSpinner = findViewById(R.id.religionSpinner);
        educationSpinner = findViewById(R.id.educationSpinner);
        cityField = findViewById(R.id.cityField);
        searchButton = findViewById(R.id.searchButton);
        clearButton = findViewById(R.id.clearButton);
        recyclerView = findViewById(R.id.recyclerView);
        resultsLabel = findViewById(R.id.resultsLabel);
        noResultsText = findViewById(R.id.noResultsText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinners() {
        // Age spinners
        List<String> ages = new ArrayList<>();
        ages.add("Any");
        for (int i = 18; i <= 70; i++) {
            ages.add(String.valueOf(i));
        }
        ArrayAdapter<String> ageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ages);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minAgeSpinner.setAdapter(ageAdapter);
        maxAgeSpinner.setAdapter(ageAdapter);

        // Marital Status
        String[] maritalStatuses = {"Any", "Never Married", "Divorced", "Widowed", "Awaiting Divorce"};
        setupSpinner(maritalStatusSpinner, maritalStatuses);

        // Religion
        String[] religions = {"Any", "Islam", "Hinduism", "Buddhism", "Christianity", "Others"};
        setupSpinner(religionSpinner, religions);

        // Education
        String[] educations = {"Any", "High School", "Diploma", "Bachelor's Degree", "Master's Degree", "PhD", "Professional Degree"};
        setupSpinner(educationSpinner, educations);
    }

    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        adapter = new MatchProfileAdapter(matchProfiles, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        searchButton.setOnClickListener(v -> performSearch());
        clearButton.setOnClickListener(v -> clearFilters());

        // Clear search button
        clearSearchButton.setOnClickListener(v -> {
            searchField.setText("");
            clearSearchButton.setVisibility(View.GONE);
        });

        // Real-time search with debounce (wait 300ms after user stops typing)
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show/hide clear button
                clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Schedule new search after 300ms delay
                currentSearchQuery = s.toString().trim();
                searchRunnable = () -> filterProfilesByName(currentSearchQuery);
                searchHandler.postDelayed(searchRunnable, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProfilesByName(String query) {
        if (query.isEmpty()) {
            // If search is empty, show all profiles or apply filters
            performSearch();
        } else {
            // Filter profiles by name
            List<MatchProfile> filtered = new ArrayList<>();
            for (MatchProfile profile : allProfiles) {
                if (profile.getFullName().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(profile);
                }
            }

            runOnUiThread(() -> {
                matchProfiles.clear();
                matchProfiles.addAll(filtered);
                adapter.notifyDataSetChanged();

                if (matchProfiles.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    noResultsText.setVisibility(View.VISIBLE);
                    noResultsText.setText("No matches found for \"" + query + "\"");
                    resultsLabel.setText("No results");
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    noResultsText.setVisibility(View.GONE);
                    resultsLabel.setText("Found " + filtered.size() + " match" + (filtered.size() > 1 ? "es" : ""));
                }
            });
        }
    }

    private void loadAllMatches() {
        showLoading(true);
        int userId = sessionManager.getUserId();
        String userGender = sessionManager.getUserGender();

        executorService.execute(() -> {
            List<MatchProfile> profiles = new ArrayList<>();
            
            // Get all biodata except current user
            List<Biodata> biodataList = biodataDAO.getAllBiodata();
            
            for (Biodata biodata : biodataList) {
                if (biodata.getUserId() == userId) continue; // Skip own profile
                
                User user = userDAO.getUserById(biodata.getUserId());
                if (user == null) continue;
                
                // Filter by opposite gender
                if (userGender.equals(user.getGender())) continue;
                
                MatchProfile profile = new MatchProfile(
                    user.getId(),
                    user.getName(),
                    biodata.getAge(),
                    biodata.getHeight() != null ? biodata.getHeight() : "",
                    biodata.getReligion() != null ? biodata.getReligion() : "",
                    biodata.getMaritalStatus() != null ? biodata.getMaritalStatus() : "",
                    biodata.getEducation() != null ? biodata.getEducation() : "",
                    biodata.getOccupation() != null ? biodata.getOccupation() : "",
                    biodata.getAnnualIncome() != null ? biodata.getAnnualIncome() : "",
                    biodata.getCity() != null ? biodata.getCity() : "",
                    biodata.getState() != null ? biodata.getState() : "",
                    biodata.getCountry() != null ? biodata.getCountry() : "",
                    biodata.getAboutMe() != null ? biodata.getAboutMe() : "",
                    "",
                    user.getGender()
                );
                profiles.add(profile);
            }

            runOnUiThread(() -> {
                showLoading(false);
                allProfiles.clear();
                allProfiles.addAll(profiles);

                // Apply search filter if there's a query
                if (!currentSearchQuery.isEmpty()) {
                    filterProfilesByName(currentSearchQuery);
                } else {
                    displayResults(profiles);
                }
            });
        });
    }

    private void performSearch() {
        showLoading(true);
        
        int userId = sessionManager.getUserId();
        String userGender = sessionManager.getUserGender();
        
        String minAgeStr = minAgeSpinner.getSelectedItem().toString();
        String maxAgeStr = maxAgeSpinner.getSelectedItem().toString();
        String maritalStatus = maritalStatusSpinner.getSelectedItem().toString();
        String religion = religionSpinner.getSelectedItem().toString();
        String education = educationSpinner.getSelectedItem().toString();
        String city = cityField.getText().toString().trim();

        Integer minAge = minAgeStr.equals("Any") ? null : Integer.parseInt(minAgeStr);
        Integer maxAge = maxAgeStr.equals("Any") ? null : Integer.parseInt(maxAgeStr);

        executorService.execute(() -> {
            List<MatchProfile> profiles = new ArrayList<>();
            
            List<Biodata> biodataList = biodataDAO.getAllBiodata();
            
            for (Biodata biodata : biodataList) {
                if (biodata.getUserId() == userId) continue;
                
                User user = userDAO.getUserById(biodata.getUserId());
                if (user == null || userGender.equals(user.getGender())) continue;
                
                // Apply filters
                if (minAge != null && biodata.getAge() < minAge) continue;
                if (maxAge != null && biodata.getAge() > maxAge) continue;
                if (!maritalStatus.equals("Any") && (biodata.getMaritalStatus() == null || !biodata.getMaritalStatus().equals(maritalStatus))) continue;
                if (!religion.equals("Any") && (biodata.getReligion() == null || !biodata.getReligion().equals(religion))) continue;
                if (!education.equals("Any") && (biodata.getEducation() == null || !biodata.getEducation().equals(education))) continue;
                if (!city.isEmpty() && (biodata.getCity() == null || !biodata.getCity().toLowerCase().contains(city.toLowerCase()))) continue;
                
                MatchProfile profile = new MatchProfile(
                    user.getId(),
                    user.getName(),
                    biodata.getAge(),
                    biodata.getHeight() != null ? biodata.getHeight() : "",
                    biodata.getReligion() != null ? biodata.getReligion() : "",
                    biodata.getMaritalStatus() != null ? biodata.getMaritalStatus() : "",
                    biodata.getEducation() != null ? biodata.getEducation() : "",
                    biodata.getOccupation() != null ? biodata.getOccupation() : "",
                    biodata.getAnnualIncome() != null ? biodata.getAnnualIncome() : "",
                    biodata.getCity() != null ? biodata.getCity() : "",
                    biodata.getState() != null ? biodata.getState() : "",
                    biodata.getCountry() != null ? biodata.getCountry() : "",
                    biodata.getAboutMe() != null ? biodata.getAboutMe() : "",
                    "",
                    user.getGender()
                );
                profiles.add(profile);
            }

            runOnUiThread(() -> {
                showLoading(false);
                allProfiles.clear();
                allProfiles.addAll(profiles);

                // Apply name search filter if there's a query
                if (!currentSearchQuery.isEmpty()) {
                    filterProfilesByName(currentSearchQuery);
                } else {
                    displayResults(profiles);
                }
            });
        });
    }

    private void clearFilters() {
        minAgeSpinner.setSelection(0);
        maxAgeSpinner.setSelection(0);
        maritalStatusSpinner.setSelection(0);
        religionSpinner.setSelection(0);
        educationSpinner.setSelection(0);
        cityField.setText("");
        searchField.setText("");
        currentSearchQuery = "";
        loadAllMatches();
    }

    private void displayResults(List<MatchProfile> profiles) {
        matchProfiles.clear();
        matchProfiles.addAll(profiles);
        adapter.notifyDataSetChanged();

        if (profiles.isEmpty()) {
            resultsLabel.setText("No matches found");
            noResultsText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            resultsLabel.setText("Found " + profiles.size() + " match" + (profiles.size() > 1 ? "es" : ""));
            noResultsText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onProfileClick(MatchProfile profile) {
        Intent intent = new Intent(this, ViewProfileActivity.class);
        intent.putExtra("userId", profile.getUserId());
        startActivity(intent);
    }

    @Override
    public void onShortlistClick(MatchProfile profile) {
        int userId = sessionManager.getUserId();
        
        executorService.execute(() -> {
            boolean isShortlisted = shortlistDAO.isShortlisted(userId, profile.getUserId());
            
            if (isShortlisted) {
                shortlistDAO.removeFromShortlist(userId, profile.getUserId());
                runOnUiThread(() -> Toast.makeText(this, "Removed from shortlist", Toast.LENGTH_SHORT).show());
            } else {
                shortlistDAO.addToShortlist(userId, profile.getUserId());
                runOnUiThread(() -> Toast.makeText(this, "Added to shortlist", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onSendRequestClick(MatchProfile profile) {
        int userId = sessionManager.getUserId();
        
        executorService.execute(() -> {
            boolean requestExists = contactRequestDAO.requestExists(userId, profile.getUserId());
            
            if (requestExists) {
                runOnUiThread(() -> Toast.makeText(this, "Request already sent", Toast.LENGTH_SHORT).show());
            } else {
                ContactRequest request = new ContactRequest(userId, profile.getUserId(), "");
                contactRequestDAO.insertRequest(request);
                runOnUiThread(() -> Toast.makeText(this, "Contact request sent!", Toast.LENGTH_SHORT).show());
            }
        });
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
