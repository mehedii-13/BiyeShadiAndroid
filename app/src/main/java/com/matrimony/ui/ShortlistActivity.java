package com.matrimony.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.matrimony.R;
import com.matrimony.adapter.ShortlistAdapter;
import com.matrimony.database.BiodataDAO;
import com.matrimony.database.ShortlistDAO;
import com.matrimony.database.UserDAO;
import com.matrimony.database.DatabaseHelper;
import com.matrimony.model.Biodata;
import com.matrimony.model.MatchProfile;
import com.matrimony.model.User;
import com.matrimony.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShortlistActivity extends AppCompatActivity implements ShortlistAdapter.OnShortlistItemClickListener {

    private RecyclerView recyclerView;
    private TextView noResultsText;
    private ProgressBar progressBar;

    private ShortlistAdapter adapter;
    private List<MatchProfile> shortlistedProfiles = new ArrayList<>();

    private SessionManager sessionManager;
    private UserDAO userDAO;
    private BiodataDAO biodataDAO;
    private ShortlistDAO shortlistDAO;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortlist);

        sessionManager = new SessionManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);
        biodataDAO = new BiodataDAO(dbHelper);
        shortlistDAO = new ShortlistDAO(dbHelper);
        executorService = Executors.newSingleThreadExecutor();

        setupToolbar();
        initViews();
        setupRecyclerView();
        loadShortlist();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Shortlist");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        noResultsText = findViewById(R.id.noResultsText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new ShortlistAdapter(shortlistedProfiles, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadShortlist() {
        showLoading(true);
        int userId = sessionManager.getUserId();

        executorService.execute(() -> {
            List<Integer> shortlistItems = shortlistDAO.getShortlistedUserIds(userId);
            List<MatchProfile> profiles = new ArrayList<>();

            for (Integer shortlistedUserId : shortlistItems) {
                User user = userDAO.getUserById(shortlistedUserId);
                if (user != null) {
                    Biodata biodata = biodataDAO.getBiodataByUserId(user.getId());
                    
                    MatchProfile profile = new MatchProfile(
                        user.getId(),
                        user.getName(),
                        biodata != null ? biodata.getAge() : 0,
                        biodata != null ? biodata.getHeight() : "",
                        biodata != null ? biodata.getReligion() : "",
                        biodata != null ? biodata.getMaritalStatus() : "",
                        biodata != null ? biodata.getEducation() : "",
                        biodata != null ? biodata.getOccupation() : "",
                        biodata != null ? biodata.getAnnualIncome() : "",
                        biodata != null ? biodata.getCity() : "",
                        biodata != null ? biodata.getState() : "",
                        biodata != null ? biodata.getCountry() : "",
                        biodata != null ? biodata.getAboutMe() : "",
                        user.getProfilePhotoUri() != null ? user.getProfilePhotoUri() : "",
                        user.getGender()
                    );
                    profiles.add(profile);
                }
            }

            runOnUiThread(() -> {
                showLoading(false);
                shortlistedProfiles.clear();
                shortlistedProfiles.addAll(profiles);
                adapter.notifyDataSetChanged();

                if (profiles.isEmpty()) {
                    noResultsText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    noResultsText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onViewProfileClick(MatchProfile profile) {
        Intent intent = new Intent(this, ViewProfileActivity.class);
        intent.putExtra("userId", profile.getUserId());
        startActivity(intent);
    }

    @Override
    public void onRemoveClick(MatchProfile profile) {
        int userId = sessionManager.getUserId();
        
        executorService.execute(() -> {
            shortlistDAO.removeFromShortlist(userId, profile.getUserId());
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Removed from shortlist", Toast.LENGTH_SHORT).show();
                loadShortlist();
            });
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
    protected void onResume() {
        super.onResume();
        loadShortlist();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
