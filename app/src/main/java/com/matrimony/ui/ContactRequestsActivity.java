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
import com.matrimony.adapter.ContactRequestAdapter;
import com.matrimony.database.ContactRequestDAO;
import com.matrimony.database.UserDAO;
import com.matrimony.database.DatabaseHelper;
import com.matrimony.model.ContactRequest;
import com.matrimony.model.User;
import com.matrimony.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactRequestsActivity extends AppCompatActivity implements ContactRequestAdapter.OnRequestClickListener {

    private RecyclerView recyclerView;
    private TextView noResultsText;
    private ProgressBar progressBar;

    private ContactRequestAdapter adapter;
    private List<ContactRequestAdapter.RequestItem> requestItems = new ArrayList<>();

    private SessionManager sessionManager;
    private UserDAO userDAO;
    private ContactRequestDAO contactRequestDAO;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_requests);

        sessionManager = new SessionManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);
        contactRequestDAO = new ContactRequestDAO(dbHelper);
        executorService = Executors.newSingleThreadExecutor();

        setupToolbar();
        initViews();
        setupRecyclerView();
        loadRequests();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Contact Requests");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        noResultsText = findViewById(R.id.noResultsText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new ContactRequestAdapter(requestItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadRequests() {
        showLoading(true);
        int userId = sessionManager.getUserId();

        executorService.execute(() -> {
            List<ContactRequest> pendingRequests = contactRequestDAO.getPendingRequestsForUser(userId);
            List<ContactRequestAdapter.RequestItem> items = new ArrayList<>();

            for (ContactRequest request : pendingRequests) {
                User sender = userDAO.getUserById(request.getSenderId());
                if (sender != null) {
                    items.add(new ContactRequestAdapter.RequestItem(
                        request.getId(),
                        request.getSenderId(),
                        sender.getName(),
                        sender.getEmail(),
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

                if (items.isEmpty()) {
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
    public void onViewProfileClick(int userId) {
        Intent intent = new Intent(this, ViewProfileActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    @Override
    public void onAcceptClick(int requestId) {
        executorService.execute(() -> {
            contactRequestDAO.updateRequestStatus(requestId, "accepted", System.currentTimeMillis());
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Request accepted!", Toast.LENGTH_SHORT).show();
                loadRequests();
            });
        });
    }

    @Override
    public void onRejectClick(int requestId) {
        executorService.execute(() -> {
            contactRequestDAO.updateRequestStatus(requestId, "rejected", System.currentTimeMillis());
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show();
                loadRequests();
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
        loadRequests();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
