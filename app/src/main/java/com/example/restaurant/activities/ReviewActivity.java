package com.example.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.restaurant.R;
import com.example.restaurant.models.Restaurant;
import com.example.restaurant.models.Review;
import com.example.restaurant.models.User;
import com.example.restaurant.utils.DatabaseHelper;
import com.google.android.material.slider.Slider;

import java.util.Date;

public class ReviewActivity extends AppCompatActivity {
    private Restaurant restaurant;
    private User currentUser;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Get restaurant and user from intent
        restaurant = (Restaurant) getIntent().getSerializableExtra("restaurant");
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        TextView tvRestaurantName = findViewById(R.id.tvRestaurantName);
        TextView tvRestaurantPostcode = findViewById(R.id.tvRestaurantPostcode);
        EditText etComment = findViewById(R.id.etComment);
        Slider sliderService = findViewById(R.id.sliderService);
        Slider sliderValue = findViewById(R.id.sliderValue);
        Slider sliderFood = findViewById(R.id.sliderFood);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        // Set restaurant info
        tvRestaurantName.setText(restaurant.getName());
        tvRestaurantPostcode.setText(restaurant.getPostcode());

        btnSubmit.setOnClickListener(v -> {
            String comment = etComment.getText().toString().trim();
            float serviceScore = sliderService.getValue();
            float valueScore = sliderValue.getValue();
            float foodScore = sliderFood.getValue();

            if (comment.isEmpty()) {
                Toast.makeText(this, "Please enter your comment", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create new review
            Review review = new Review();
            review.setRestaurantId(restaurant.getId());
            review.setUserId(currentUser.getId());
            review.setComment(comment);
            review.setServiceScore(serviceScore);
            review.setValueScore(valueScore);
            review.setFoodScore(foodScore);
            review.setReviewDate(new Date());

            // Add to database
            long id = databaseHelper.addReview(review);
            if (id != -1) {
                Toast.makeText(this, "Review submitted successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show();
            }
        });
    }
}