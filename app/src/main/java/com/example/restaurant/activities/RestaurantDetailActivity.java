package com.example.restaurant.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.R;
import com.example.restaurant.adapters.ReviewAdapter;
import com.example.restaurant.models.Restaurant;
import com.example.restaurant.models.Review;
import com.example.restaurant.models.User;
import com.example.restaurant.utils.DatabaseHelper;
import com.example.restaurant.utils.MarkdownUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class RestaurantDetailActivity extends AppCompatActivity {
    private Restaurant restaurant;
    private User currentUser;
    private DatabaseHelper databaseHelper;
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        // Get restaurant and user from intent
        restaurant = (Restaurant) getIntent().getSerializableExtra("restaurant");
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        TextView tvName = findViewById(R.id.tvName);
        TextView tvPostcode = findViewById(R.id.tvPostcode);
        ImageView ivRestaurant = findViewById(R.id.ivRestaurant);
        TextView tvFoodType = findViewById(R.id.tvFoodType);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvAddedDate = findViewById(R.id.tvAddedDate);
        TextView tvAvgService = findViewById(R.id.tvAvgService);
        TextView tvAvgValue = findViewById(R.id.tvAvgValue);
        TextView tvAvgFood = findViewById(R.id.tvAvgFood);
        TextView tvAvgOverall = findViewById(R.id.tvAvgOverall);
        Button btnLeaveReview = findViewById(R.id.btnLeaveReview);
        RecyclerView rvReviews = findViewById(R.id.rvReviews);

        // Set restaurant details
        tvName.setText(restaurant.getName());
        tvPostcode.setText(restaurant.getPostcode());

        // Load restaurant image using Picasso
        if (restaurant.getImagePath() != null && !restaurant.getImagePath().isEmpty()) {
            Picasso.get()
                    .load(new File(restaurant.getImagePath()))
                    .placeholder(R.drawable.placeholder_restaurant)
                    .error(R.drawable.placeholder_restaurant)
                    .fit()
                    .centerCrop()
                    .into(ivRestaurant);
        } else {
            ivRestaurant.setImageResource(R.drawable.placeholder_restaurant);
        }

        tvFoodType.setText(restaurant.getFoodType());
        tvDescription.setText(MarkdownUtils.markdownToHtml(restaurant.getDescription()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        tvAddedDate.setText(dateFormat.format(restaurant.getAddedDate()));

        // Set average ratings
        if (restaurant.getAverageRating() != null) {
            Restaurant.AverageRatings avgRatings = restaurant.getAverageRating();
            tvAvgService.setText(String.format(Locale.getDefault(), "%.1f", avgRatings.getService()));
            tvAvgValue.setText(String.format(Locale.getDefault(), "%.1f", avgRatings.getValue()));
            tvAvgFood.setText(String.format(Locale.getDefault(), "%.1f", avgRatings.getFood()));
            tvAvgOverall.setText(String.format(Locale.getDefault(), "%.1f", avgRatings.getOverall()));
        } else {
            tvAvgService.setText("-");
            tvAvgValue.setText("-");
            tvAvgFood.setText("-");
            tvAvgOverall.setText("-");
        }

        // Set up reviews RecyclerView
        reviewAdapter = new ReviewAdapter(this, restaurant.getReviews());
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);

        // Handle leave review button
        if (currentUser == null || databaseHelper.hasUserReviewedRestaurant(currentUser.getId(), restaurant.getId())) {
            btnLeaveReview.setVisibility(View.GONE);
        } else {
            btnLeaveReview.setOnClickListener(v -> {
                Intent intent = new Intent(RestaurantDetailActivity.this, ReviewActivity.class);
                intent.putExtra("restaurant", restaurant);
                intent.putExtra("currentUser", currentUser);
                startActivityForResult(intent, 1);
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Refresh reviews after adding a new one
            restaurant.setReviews(databaseHelper.getReviewsForRestaurant(restaurant.getId()));
            reviewAdapter.updateReviews(restaurant.getReviews());

            // Update average ratings
            restaurant.setAverageRating(databaseHelper.getAverageRatings(restaurant.getId()));

            // Update the rating displays without recreating the activity
            TextView tvAvgService = findViewById(R.id.tvAvgService);
            TextView tvAvgValue = findViewById(R.id.tvAvgValue);
            TextView tvAvgFood = findViewById(R.id.tvAvgFood);
            TextView tvAvgOverall = findViewById(R.id.tvAvgOverall);

            Restaurant.AverageRatings avgRatings = restaurant.getAverageRating();
            tvAvgService.setText(String.format(Locale.getDefault(), "%.1f", avgRatings.getService()));
            tvAvgValue.setText(String.format(Locale.getDefault(), "%.1f", avgRatings.getValue()));
            tvAvgFood.setText(String.format(Locale.getDefault(), "%.1f", avgRatings.getFood()));
            tvAvgOverall.setText(String.format(Locale.getDefault(), "%.1f", avgRatings.getOverall()));

            // Hide the review button if user has now reviewed
            Button btnLeaveReview = findViewById(R.id.btnLeaveReview);
            if (databaseHelper.hasUserReviewedRestaurant(currentUser.getId(), restaurant.getId())) {
                btnLeaveReview.setVisibility(View.GONE);
            }
        }
    }
}