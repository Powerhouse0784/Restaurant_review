package com.example.restaurant.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.R;
import com.example.restaurant.adapters.RestaurantAdapter;
import com.example.restaurant.models.Restaurant;
import com.example.restaurant.models.User;
import com.example.restaurant.utils.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;
    private List<Restaurant> restaurantList;
    private DatabaseHelper databaseHelper;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize DatabaseHelper first
        databaseHelper = new DatabaseHelper(this);

        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", null);

        if (username == null) {
            // User not logged in - show login/register screen
            showLoginRegisterScreen();
        } else {
            // User is logged in - show home screen
            showHomeScreen(username);
        }
    }

    private void showLoginRegisterScreen() {
        setContentView(R.layout.activity_main);

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });
    }

    private void showHomeScreen(String username) {
        setContentView(R.layout.activity_home);

        // Get current user from database
        currentUser = databaseHelper.getUserByUsername(username);
        if (currentUser == null) {
            // User not found in database (shouldn't happen) - go back to login
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().clear().apply();
            showLoginRegisterScreen();
            return;
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.restaurantRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadRestaurants();

        // Setup FAB
        FloatingActionButton fab = findViewById(R.id.fabAddRestaurant);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NewRestaurantActivity.class);
            intent.putExtra("currentUser", currentUser);
            startActivityForResult(intent, 1);
        });
    }

    private void loadRestaurants() {
        restaurantList = databaseHelper.getAllRestaurants();
        adapter = new RestaurantAdapter(this, restaurantList, currentUser);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadRestaurants(); // Refresh list after adding restaurant
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show menu items when logged in
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (prefs.getString("username", null) != null) {
            getMenuInflater().inflate(R.menu.main_menu, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_logout) {
            // Clear SharedPreferences and restart activity
            getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().clear().apply();
            recreate(); // Restart activity to show login screen
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the view in case login state changed
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", null);

        if (username == null && currentUser != null) {
            // User logged out - switch to login screen
            recreate();
        } else if (username != null && currentUser == null) {
            // User logged in - switch to home screen
            recreate();
        }
    }
}