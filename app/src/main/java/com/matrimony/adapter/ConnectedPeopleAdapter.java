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
import com.matrimony.model.MatchProfile;
import com.matrimony.util.ImageUtils;

import java.util.List;

public class ConnectedPeopleAdapter extends RecyclerView.Adapter<ConnectedPeopleAdapter.ViewHolder> {

    private List<MatchProfile> profiles;
    private OnConnectedPersonClickListener listener;

    public interface OnConnectedPersonClickListener {
        void onViewProfileClick(MatchProfile profile);
    }

    public ConnectedPeopleAdapter(List<MatchProfile> profiles, OnConnectedPersonClickListener listener) {
        this.profiles = profiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_connected_person, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MatchProfile profile = profiles.get(position);
        holder.bind(profile);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView profilePlaceholder;
        TextView nameText;
        TextView ageGenderText;
        TextView locationText;
        TextView statusText;
        Button viewProfileButton;

        ViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            profilePlaceholder = itemView.findViewById(R.id.profilePlaceholder);
            nameText = itemView.findViewById(R.id.nameText);
            ageGenderText = itemView.findViewById(R.id.ageGenderText);
            locationText = itemView.findViewById(R.id.locationText);
            statusText = itemView.findViewById(R.id.statusText);
            viewProfileButton = itemView.findViewById(R.id.viewProfileButton);
        }

        void bind(MatchProfile profile) {
            nameText.setText(profile.getFullName());
            
            // Load profile image
            if (profile.getProfilePhotoUri() != null && !profile.getProfilePhotoUri().isEmpty()) {
                ImageUtils.loadProfileImage(itemView.getContext(), profile.getProfilePhotoUri(),
                    profileImage, profilePlaceholder);
            } else {
                profileImage.setImageResource(R.drawable.ic_default_avatar);
                if (profilePlaceholder != null) {
                    profilePlaceholder.setVisibility(View.GONE);
                }
            }

            String ageGender = "";
            if (profile.getAge() > 0) {
                ageGender = profile.getAge() + " years";
            }
            if (profile.getGender() != null && !profile.getGender().isEmpty()) {
                ageGender += (ageGender.isEmpty() ? "" : ", ") + profile.getGender();
            }
            ageGenderText.setText(ageGender.isEmpty() ? "Age not specified" : ageGender);
            
            String location = "";
            if (profile.getCity() != null && !profile.getCity().isEmpty()) {
                location = profile.getCity();
            }
            locationText.setText(location.isEmpty() ? "Location not specified" : location);
            
            statusText.setText("Connected ✓");
            statusText.setTextColor(itemView.getContext().getResources().getColor(R.color.success_green));

            viewProfileButton.setOnClickListener(v -> {
                if (listener != null) listener.onViewProfileClick(profile);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onViewProfileClick(profile);
            });
        }
    }
}
