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

public class ShortlistAdapter extends RecyclerView.Adapter<ShortlistAdapter.ViewHolder> {

    private List<MatchProfile> profiles;
    private OnShortlistItemClickListener listener;

    public interface OnShortlistItemClickListener {
        void onViewProfileClick(MatchProfile profile);
        void onRemoveClick(MatchProfile profile);
    }

    public ShortlistAdapter(List<MatchProfile> profiles, OnShortlistItemClickListener listener) {
        this.profiles = profiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shortlist, parent, false);
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
        Button viewProfileButton;
        Button removeButton;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            ageGenderText = itemView.findViewById(R.id.ageGenderText);
            locationText = itemView.findViewById(R.id.locationText);
            religionText = itemView.findViewById(R.id.religionText);
            viewProfileButton = itemView.findViewById(R.id.viewProfileButton);
            removeButton = itemView.findViewById(R.id.removeButton);
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
            locationText.setText(location.isEmpty() ? "Location not specified" : location);
            
            religionText.setText(profile.getReligion() != null && !profile.getReligion().isEmpty() 
                ? profile.getReligion() : "Religion not specified");

            viewProfileButton.setOnClickListener(v -> {
                if (listener != null) listener.onViewProfileClick(profile);
            });

            removeButton.setOnClickListener(v -> {
                if (listener != null) listener.onRemoveClick(profile);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onViewProfileClick(profile);
            });
        }
    }
}
