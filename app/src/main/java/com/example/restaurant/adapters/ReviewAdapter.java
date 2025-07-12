package com.example.restaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.R;
import com.example.restaurant.models.Review;
import com.example.restaurant.utils.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private Context context;
    private List<Review> reviewList;
    private DatabaseHelper databaseHelper;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = new ArrayList<>(reviewList); // Create a new list to avoid direct reference
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        String username = databaseHelper.getUsernameById(review.getUserId());

        holder.tvUsername.setText(username != null ? username : "Anonymous");
        holder.tvComment.setText(review.getComment());
        holder.tvServiceScore.setText(String.format(Locale.getDefault(), "Service: %.1f", review.getServiceScore()));
        holder.tvValueScore.setText(String.format(Locale.getDefault(), "Value: %.1f", review.getValueScore()));
        holder.tvFoodScore.setText(String.format(Locale.getDefault(), "Food: %.1f", review.getFoodScore()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        holder.tvReviewDate.setText(dateFormat.format(review.getReviewDate()));
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void updateReviews(List<Review> newReviews) {
        this.reviewList.clear();
        this.reviewList.addAll(newReviews);
        notifyDataSetChanged();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvComment, tvServiceScore, tvValueScore, tvFoodScore, tvReviewDate;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvServiceScore = itemView.findViewById(R.id.tvServiceScore);
            tvValueScore = itemView.findViewById(R.id.tvValueScore);
            tvFoodScore = itemView.findViewById(R.id.tvFoodScore);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
        }
    }
}