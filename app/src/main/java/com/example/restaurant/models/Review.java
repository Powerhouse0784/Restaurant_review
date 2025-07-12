package com.example.restaurant.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Review implements Parcelable {
    private long id;
    private long restaurantId;
    private long userId;
    private String comment;
    private float serviceScore;
    private float valueScore;
    private float foodScore;
    private Date reviewDate;
    private String username; // Added field to store username

    public Review() {
    }

    // Getters and setters for all fields
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public float getServiceScore() {
        return serviceScore;
    }

    public void setServiceScore(float serviceScore) {
        this.serviceScore = serviceScore;
    }

    public float getValueScore() {
        return valueScore;
    }

    public void setValueScore(float valueScore) {
        this.valueScore = valueScore;
    }

    public float getFoodScore() {
        return foodScore;
    }

    public void setFoodScore(float foodScore) {
        this.foodScore = foodScore;
    }

    public Date getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Parcelable implementation
    protected Review(Parcel in) {
        id = in.readLong();
        restaurantId = in.readLong();
        userId = in.readLong();
        comment = in.readString();
        serviceScore = in.readFloat();
        valueScore = in.readFloat();
        foodScore = in.readFloat();
        reviewDate = new Date(in.readLong());
        username = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(restaurantId);
        dest.writeLong(userId);
        dest.writeString(comment);
        dest.writeFloat(serviceScore);
        dest.writeFloat(valueScore);
        dest.writeFloat(foodScore);
        dest.writeLong(reviewDate.getTime());
        dest.writeString(username);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    // Helper method to calculate overall score
    public float getOverallScore() {
        return (serviceScore + valueScore + foodScore) / 3;
    }

    // Formatted date string
    public String getFormattedDate() {
        return new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                .format(reviewDate);
    }
}