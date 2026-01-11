package com.matrimony.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.matrimony.R;

import java.util.List;

public class SuggestionProfileAdapter extends RecyclerView.Adapter<SuggestionProfileAdapter.ViewHolder> {

    public static class SuggestionProfile {
        private int userId;
        private String name;
        private int age;
        private String location;
        private int matchPercentage;

        public SuggestionProfile(int userId, String name, int age, String location, int matchPercentage) {
            this.userId = userId;
            this.name = name;
            this.age = age;
            this.location = location;
            this.matchPercentage = matchPercentage;
        }

        public int getUserId() { return userId; }
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getLocation() { return location; }
        public int getMatchPercentage() { return matchPercentage; }
    }

    public interface OnSuggestionClickListener {
        void onSuggestionClick(int userId);
    }

    private List<SuggestionProfile> suggestions;
    private OnSuggestionClickListener listener;

    public SuggestionProfileAdapter(List<SuggestionProfile> suggestions, OnSuggestionClickListener listener) {
        this.suggestions = suggestions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggestion_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SuggestionProfile profile = suggestions.get(position);

        holder.nameText.setText(profile.getName());
        holder.ageText.setText(profile.getAge() + " years");
        holder.locationText.setText(profile.getLocation());
        holder.matchText.setText(profile.getMatchPercentage() + "% Match");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionClick(profile.getUserId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView ageText;
        TextView locationText;
        TextView matchText;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.suggestionNameText);
            ageText = itemView.findViewById(R.id.suggestionAgeText);
            locationText = itemView.findViewById(R.id.suggestionLocationText);
            matchText = itemView.findViewById(R.id.suggestionMatchText);
        }
    }
}

