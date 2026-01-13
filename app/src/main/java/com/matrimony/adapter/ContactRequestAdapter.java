package com.matrimony.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.matrimony.R;
import com.matrimony.util.ImageUtils;

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
        public String profilePhotoUri;
        public String status;
        public long requestDate;

        public RequestItem(int requestId, int senderId, String senderName, String senderEmail, String profilePhotoUri, String status, long requestDate) {
            this.requestId = requestId;
            this.senderId = senderId;
            this.senderName = senderName;
            this.senderEmail = senderEmail;
            this.profilePhotoUri = profilePhotoUri;
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
        ImageView profileImage;
        TextView profilePlaceholder;
        TextView nameText;
        TextView emailText;
        TextView dateText;
        Button viewProfileButton;
        Button acceptButton;
        Button rejectButton;

        ViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            profilePlaceholder = itemView.findViewById(R.id.profilePlaceholder);
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
            
            // Load profile image
            if (request.profilePhotoUri != null && !request.profilePhotoUri.isEmpty()) {
                ImageUtils.loadProfileImage(itemView.getContext(), request.profilePhotoUri,
                    profileImage, profilePlaceholder);
            } else {
                profileImage.setImageResource(R.drawable.ic_default_avatar);
                if (profilePlaceholder != null) {
                    profilePlaceholder.setVisibility(View.GONE);
                }
            }

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
