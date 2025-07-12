package com.example.restaurant.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.R;
import com.example.restaurant.activities.RestaurantDetailActivity;
import com.example.restaurant.models.Restaurant;
import com.example.restaurant.models.User;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {
    private Context context;
    private List<Restaurant> restaurantList;
    private User currentUser;

    public RestaurantAdapter(Context context, List<Restaurant> restaurantList, User currentUser) {
        this.context = context;
        this.restaurantList = restaurantList;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);

        holder.tvName.setText(restaurant.getName());
        holder.tvPostcode.setText(restaurant.getPostcode());

        // Load image using the file path
        if (restaurant.getImagePath() != null && !restaurant.getImagePath().isEmpty()) {
            // Using Picasso for efficient image loading
            Picasso.get()
                    .load(new File(restaurant.getImagePath()))
                    .placeholder(R.drawable.placeholder_restaurant) // Add a placeholder drawable
                    .error(R.drawable.placeholder_restaurant) // Add an error drawable
                    .fit()
                    .centerCrop()
                    .into(holder.ivRestaurant);
        } else {
            // If no image is available, show a placeholder
            holder.ivRestaurant.setImageResource(R.drawable.placeholder_restaurant);
        }

        // Set average rating if available
        if (restaurant.getAverageRating() != null) {
            holder.tvAvgRating.setText(String.format("%.1f", restaurant.getAverageRating().getOverall()));
        } else {
            holder.tvAvgRating.setText("-");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RestaurantDetailActivity.class);
            intent.putExtra("restaurant", restaurant);
            intent.putExtra("currentUser", currentUser);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public void updateRestaurants(List<Restaurant> newRestaurants) {
        restaurantList.clear();
        restaurantList.addAll(newRestaurants);
        notifyDataSetChanged();
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPostcode, tvAvgRating;
        ImageView ivRestaurant;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPostcode = itemView.findViewById(R.id.tvPostcode);
            tvAvgRating = itemView.findViewById(R.id.tvAvgRating);
            ivRestaurant = itemView.findViewById(R.id.ivRestaurant);
        }
    }
}