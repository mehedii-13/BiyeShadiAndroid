package com.matrimony.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.matrimony.R;
import com.matrimony.adapter.RequestsSentAdapter;
import com.matrimony.database.BiodataDAO;
import com.matrimony.database.ContactRequestDAO;
import com.matrimony.database.UserDAO;
import com.matrimony.database.DatabaseHelper;
import com.matrimony.model.Biodata;
import com.matrimony.model.ContactRequest;
import com.matrimony.model.User;
import com.matrimony.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestsSentActivity extends AppCompatActivity implements RequestsSentAdapter.OnRequestSentClickListener {

    private RecyclerView recyclerView;
    private TextView noResultsText;
    private ProgressBar progressBar;

    private RequestsSentAdapter adapter;
    private List<RequestsSentAdapter.RequestSentItem> requestItems = new ArrayList<>();

    private SessionManager sessionManager;
    private UserDAO userDAO;
    private BiodataDAO biodataDAO;
    private ContactRequestDAO contactRequestDAO;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests_sent);

        sessionManager = new SessionManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);
        biodataDAO = new BiodataDAO(dbHelper);
        contactRequestDAO = new ContactRequestDAO(dbHelper);
        executorService = Executors.newSingleThreadExecutor();

        setupToolbar();
        initViews();
        setupRecyclerView();
        loadRequestsSent();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Requests Sent");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        noResultsText = findViewById(R.id.noResultsText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new RequestsSentAdapter(requestItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadRequestsSent() {
        showLoading(true);
        int userId = sessionManager.getUserId();

        executorService.execute(() -> {
            List<ContactRequest> sentRequests = contactRequestDAO.getSentRequests(userId);
            List<RequestsSentAdapter.RequestSentItem> items = new ArrayList<>();

            for (ContactRequest request : sentRequests) {
                User receiver = userDAO.getUserById(request.getReceiverId());
                if (receiver != null) {
                    Biodata biodata = biodataDAO.getBiodataByUserId(receiver.getId());

                    String details = receiver.getGender();
                    if (biodata != null) {
                        if (biodata.getAge() > 0) {
                            details += ", " + biodata.getAge() + " years";
                        }
                        if (biodata.getCity() != null && !biodata.getCity().isEmpty()) {
                            details += ", " + biodata.getCity();
                        }
                    }

                    items.add(new RequestsSentAdapter.RequestSentItem(
                        request.getId(),
                        request.getReceiverId(),
                        receiver.getName(),
                        details,
                        request.getStatus(),
                        request.getRequestDate()
                    ));
                }
            }

            runOnUiThread(() -> {
                showLoading(false);
                requestItems.clear();
                requestItems.addAll(items);
                adapter.notifyDataSetChanged();

                if (requestItems.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    noResultsText.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    noResultsText.setVisibility(View.GONE);
                }
            });
        });
    }

    @Override
    public void onViewProfile(int userId) {
        Intent intent = new Intent(this, ViewProfileActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    @Override
    public void onCancelRequest(int requestId) {
        new AlertDialog.Builder(this)
            .setTitle("Cancel Request")
            .setMessage("Are you sure you want to cancel this contact request?")
            .setPositiveButton("Yes", (dialog, which) -> cancelRequest(requestId))
            .setNegativeButton("No", null)
            .show();
    }

    private void cancelRequest(int requestId) {
        executorService.execute(() -> {
            // Delete the request from database
            int result = contactRequestDAO.deleteRequest(requestId);

            runOnUiThread(() -> {
                if (result > 0) {
                    Toast.makeText(this, "Request cancelled successfully", Toast.LENGTH_SHORT).show();
                    loadRequestsSent(); // Reload the list
                } else {
                    Toast.makeText(this, "Failed to cancel request", Toast.LENGTH_SHORT).show();
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
        loadRequestsSent(); // Reload when returning to this activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}

