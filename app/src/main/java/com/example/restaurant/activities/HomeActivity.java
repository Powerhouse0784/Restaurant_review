package com.example.restaurant.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.R;
import com.example.restaurant.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get current user
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        if (currentUser == null) {
            // Get from shared prefs if app was restarted
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            long userId = prefs.getLong("userId", -1);
            String username = prefs.getString("username", null);

            if (userId != -1 && username != null) {
                currentUser = new User(userId, username, false);
            } else {
                // Not logged in, go back to main
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return;
            }
        }

        // Display user info
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Welcome, " + currentUser.getUsername());

        // Setup restaurant list (same as your original MainActivity)
        RecyclerView recyclerView = findViewById(R.id.restaurantRecyclerView);
        // ... setup RecyclerView ...

        FloatingActionButton fab = findViewById(R.id.fabAddRestaurant);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, NewRestaurantActivity.class);
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            // Clear login state
            getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().clear().apply();

            // Go back to main
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}