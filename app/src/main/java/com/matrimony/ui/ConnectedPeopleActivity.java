package com.matrimony.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.matrimony.R;
import com.matrimony.adapter.ConnectedPeopleAdapter;
import com.matrimony.database.BiodataDAO;
import com.matrimony.database.ContactRequestDAO;
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

public class ConnectedPeopleActivity extends AppCompatActivity implements ConnectedPeopleAdapter.OnConnectedPersonClickListener {

    private RecyclerView recyclerView;
    private TextView noResultsText;
    private ProgressBar progressBar;

    private ConnectedPeopleAdapter adapter;
    private List<MatchProfile> connectedProfiles = new ArrayList<>();

    private SessionManager sessionManager;
    private UserDAO userDAO;
    private BiodataDAO biodataDAO;
    private ContactRequestDAO contactRequestDAO;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_people);

        sessionManager = new SessionManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);
        biodataDAO = new BiodataDAO(dbHelper);
        contactRequestDAO = new ContactRequestDAO(dbHelper);
        executorService = Executors.newSingleThreadExecutor();

        setupToolbar();
        initViews();
        setupRecyclerView();
        loadConnectedPeople();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Connected People");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        noResultsText = findViewById(R.id.noResultsText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new ConnectedPeopleAdapter(connectedProfiles, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadConnectedPeople() {
        showLoading(true);
        int userId = sessionManager.getUserId();

        executorService.execute(() -> {
            List<ContactRequest> connections = contactRequestDAO.getAcceptedConnections(userId);
            List<MatchProfile> profiles = new ArrayList<>();

            for (ContactRequest connection : connections) {
                int connectedUserId = connection.getSenderId() == userId ? connection.getReceiverId() : connection.getSenderId();
                User user = userDAO.getUserById(connectedUserId);
                
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
                        "",
                        user.getGender()
                    );
                    profiles.add(profile);
                }
            }

            runOnUiThread(() -> {
                showLoading(false);
                connectedProfiles.clear();
                connectedProfiles.addAll(profiles);
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
        loadConnectedPeople();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
