package com.matrimony.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.matrimony.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContactRequestAdapter extends RecyclerView.Adapter<ContactRequestAdapter.ViewHolder> {

    private List<RequestItem> requests;
    private OnRequestClickListener listener;

    public static class RequestItem {
        public int requestId;
        public int senderId;
        public String senderName;
        public String senderEmail;
        public String status;
        public long requestDate;

        public RequestItem(int requestId, int senderId, String senderName, String senderEmail, String status, long requestDate) {
            this.requestId = requestId;
            this.senderId = senderId;
            this.senderName = senderName;
            this.senderEmail = senderEmail;
            this.status = status;
            this.requestDate = requestDate;
        }
    }

    public interface OnRequestClickListener {
        void onViewProfileClick(int userId);
        void onAcceptClick(int requestId);
        void onRejectClick(int requestId);
    }

    public ContactRequestAdapter(List<RequestItem> requests, OnRequestClickListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RequestItem request = requests.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView emailText;
        TextView dateText;
        Button viewProfileButton;
        Button acceptButton;
        Button rejectButton;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            emailText = itemView.findViewById(R.id.emailText);
            dateText = itemView.findViewById(R.id.dateText);
            viewProfileButton = itemView.findViewById(R.id.viewProfileButton);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }

        void bind(RequestItem request) {
            nameText.setText(request.senderName);
            emailText.setText(request.senderEmail);
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            dateText.setText("Received: " + sdf.format(new Date(request.requestDate)));

            viewProfileButton.setOnClickListener(v -> {
                if (listener != null) listener.onViewProfileClick(request.senderId);
            });

            acceptButton.setOnClickListener(v -> {
                if (listener != null) listener.onAcceptClick(request.requestId);
            });

            rejectButton.setOnClickListener(v -> {
                if (listener != null) listener.onRejectClick(request.requestId);
            });
        }
    }
}
