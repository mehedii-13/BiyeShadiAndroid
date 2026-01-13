package com.matrimony.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.matrimony.R;
import com.matrimony.util.ImageUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RequestsSentAdapter extends RecyclerView.Adapter<RequestsSentAdapter.ViewHolder> {

    public static class RequestSentItem {
        private int requestId;
        private int receiverId;
        private String receiverName;
        private String receiverDetails;
        private String status;
        private long requestDate;
        private String photoUri;

        public RequestSentItem(int requestId, int receiverId, String receiverName,
                              String receiverDetails, String status, long requestDate) {
            this.requestId = requestId;
            this.receiverId = receiverId;
            this.receiverName = receiverName;
            this.receiverDetails = receiverDetails;
            this.status = status;
            this.requestDate = requestDate;
        }

        public RequestSentItem(int requestId, int receiverId, String receiverName,
                              String receiverDetails, String status, long requestDate, String photoUri) {
            this.requestId = requestId;
            this.receiverId = receiverId;
            this.receiverName = receiverName;
            this.receiverDetails = receiverDetails;
            this.status = status;
            this.requestDate = requestDate;
            this.photoUri = photoUri;
        }

        public int getRequestId() { return requestId; }
        public int getReceiverId() { return receiverId; }
        public String getReceiverName() { return receiverName; }
        public String getReceiverDetails() { return receiverDetails; }
        public String getStatus() { return status; }
        public long getRequestDate() { return requestDate; }
        public String getPhotoUri() { return photoUri; }
    }

    public interface OnRequestSentClickListener {
        void onViewProfile(int userId);
        void onCancelRequest(int requestId);
    }

    private List<RequestSentItem> requestItems;
    private OnRequestSentClickListener listener;

    public RequestsSentAdapter(List<RequestSentItem> requestItems, OnRequestSentClickListener listener) {
        this.requestItems = requestItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request_sent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RequestSentItem item = requestItems.get(position);

        holder.nameText.setText(item.getReceiverName());
        holder.detailsText.setText(item.getReceiverDetails());

        // Load profile photo
        ImageUtils.loadProfileImage(holder.itemView.getContext(), item.getPhotoUri(),
                holder.profileImage, holder.profilePlaceholder);

        // Format status
        String statusText = "Status: " + formatStatus(item.getStatus());
        holder.statusText.setText(statusText);

        // Set status color
        int statusColor = getStatusColor(holder.itemView.getContext(), item.getStatus());
        holder.statusText.setTextColor(statusColor);

        // Format date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String dateText = "Sent on: " + dateFormat.format(new Date(item.getRequestDate()));
        holder.dateText.setText(dateText);

        // View Profile button
        holder.viewProfileButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewProfile(item.getReceiverId());
            }
        });

        // Cancel button - only show for pending requests
        if ("pending".equals(item.getStatus())) {
            holder.cancelRequestButton.setVisibility(View.VISIBLE);
            holder.cancelRequestButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelRequest(item.getRequestId());
                }
            });
        } else {
            holder.cancelRequestButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return requestItems.size();
    }

    private String formatStatus(String status) {
        if (status == null) return "Pending";
        switch (status.toLowerCase()) {
            case "pending":
                return "Pending";
            case "accepted":
                return "Accepted";
            case "rejected":
                return "Rejected";
            default:
                return status;
        }
    }

    private int getStatusColor(android.content.Context context, String status) {
        if (status == null) status = "pending";
        switch (status.toLowerCase()) {
            case "accepted":
                return context.getResources().getColor(R.color.success_green);
            case "rejected":
                return context.getResources().getColor(R.color.error_red);
            case "pending":
            default:
                return context.getResources().getColor(R.color.warning_orange);
        }
    }

    public void removeItem(int position) {
        requestItems.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView profilePlaceholder;
        TextView nameText;
        TextView detailsText;
        TextView statusText;
        TextView dateText;
        MaterialButton viewProfileButton;
        MaterialButton cancelRequestButton;

        ViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            profilePlaceholder = itemView.findViewById(R.id.profilePlaceholder);
            nameText = itemView.findViewById(R.id.nameText);
            detailsText = itemView.findViewById(R.id.detailsText);
            statusText = itemView.findViewById(R.id.statusText);
            dateText = itemView.findViewById(R.id.dateText);
            viewProfileButton = itemView.findViewById(R.id.viewProfileButton);
            cancelRequestButton = itemView.findViewById(R.id.cancelRequestButton);
        }
    }
}

