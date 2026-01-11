package com.matrimony.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.matrimony.R;
import com.matrimony.model.MatchProfile;

import java.util.List;

public class MatchProfileAdapter extends RecyclerView.Adapter<MatchProfileAdapter.ViewHolder> {

    private List<MatchProfile> profiles;
    private OnProfileClickListener listener;

    public interface OnProfileClickListener {
        void onProfileClick(MatchProfile profile);
        void onShortlistClick(MatchProfile profile);
        void onSendRequestClick(MatchProfile profile);
    }

    public MatchProfileAdapter(List<MatchProfile> profiles, OnProfileClickListener listener) {
        this.profiles = profiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_profile, parent, false);
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
        TextView nameText;
        TextView ageGenderText;
        TextView locationText;
        TextView religionText;
        TextView educationText;
        TextView occupationText;
        Button viewProfileButton;
        Button shortlistButton;
        Button sendRequestButton;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            ageGenderText = itemView.findViewById(R.id.ageGenderText);
            locationText = itemView.findViewById(R.id.locationText);
            religionText = itemView.findViewById(R.id.religionText);
            educationText = itemView.findViewById(R.id.educationText);
            occupationText = itemView.findViewById(R.id.occupationText);
            viewProfileButton = itemView.findViewById(R.id.viewProfileButton);
            shortlistButton = itemView.findViewById(R.id.shortlistButton);
            sendRequestButton = itemView.findViewById(R.id.sendRequestButton);
        }

        void bind(MatchProfile profile) {
            nameText.setText(profile.getFullName());
            
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
            if (profile.getState() != null && !profile.getState().isEmpty()) {
                location += (location.isEmpty() ? "" : ", ") + profile.getState();
            }
            locationText.setText(location.isEmpty() ? "Location not specified" : location);
            
            religionText.setText(profile.getReligion() != null && !profile.getReligion().isEmpty() 
                ? profile.getReligion() : "Religion not specified");
            educationText.setText(profile.getEducation() != null && !profile.getEducation().isEmpty() 
                ? profile.getEducation() : "Education not specified");
            occupationText.setText(profile.getOccupation() != null && !profile.getOccupation().isEmpty() 
                ? profile.getOccupation() : "Occupation not specified");

            viewProfileButton.setOnClickListener(v -> {
                if (listener != null) listener.onProfileClick(profile);
            });

            shortlistButton.setOnClickListener(v -> {
                if (listener != null) listener.onShortlistClick(profile);
            });

            sendRequestButton.setOnClickListener(v -> {
                if (listener != null) listener.onSendRequestClick(profile);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onProfileClick(profile);
            });
        }
    }
}
