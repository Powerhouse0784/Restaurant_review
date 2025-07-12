package com.example.restaurant.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

public class Restaurant implements Parcelable {
    private long id;
    private String name;
    private String postcode;
    private String imagePath; // Changed from byte[] imageBytes to String imagePath
    private String foodType;
    private String description;
    private String addedBy;
    private Date addedDate;
    private double latitude;
    private double longitude;
    private AverageRatings averageRating;
    private List<Review> reviews;

    public Restaurant() {
    }

    // Getters and setters for all fields
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public AverageRatings getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(AverageRatings averageRating) {
        this.averageRating = averageRating;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    // Parcelable implementation
    protected Restaurant(Parcel in) {
        id = in.readLong();
        name = in.readString();
        postcode = in.readString();
        imagePath = in.readString(); // Changed from createByteArray to readString
        foodType = in.readString();
        description = in.readString();
        addedBy = in.readString();
        addedDate = new Date(in.readLong());
        latitude = in.readDouble();
        longitude = in.readDouble();
        averageRating = in.readParcelable(AverageRatings.class.getClassLoader());
        reviews = in.createTypedArrayList(Review.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(postcode);
        dest.writeString(imagePath); // Changed from writeByteArray to writeString
        dest.writeString(foodType);
        dest.writeString(description);
        dest.writeString(addedBy);
        dest.writeLong(addedDate.getTime());
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeParcelable(averageRating, flags);
        dest.writeTypedList(reviews);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Restaurant> CREATOR = new Creator<Restaurant>() {
        @Override
        public Restaurant createFromParcel(Parcel in) {
            return new Restaurant(in);
        }

        @Override
        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };

    // Inner class for average ratings
    public static class AverageRatings implements Parcelable {
        private float service;
        private float value;
        private float food;
        private float overall;

        public AverageRatings() {
        }

        // Getters and setters
        public float getService() {
            return service;
        }

        public void setService(float service) {
            this.service = service;
        }

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }

        public float getFood() {
            return food;
        }

        public void setFood(float food) {
            this.food = food;
        }

        public float getOverall() {
            return overall;
        }

        public void setOverall(float overall) {
            this.overall = overall;
        }

        // Parcelable implementation
        protected AverageRatings(Parcel in) {
            service = in.readFloat();
            value = in.readFloat();
            food = in.readFloat();
            overall = in.readFloat();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeFloat(service);
            dest.writeFloat(value);
            dest.writeFloat(food);
            dest.writeFloat(overall);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<AverageRatings> CREATOR = new Creator<AverageRatings>() {
            @Override
            public AverageRatings createFromParcel(Parcel in) {
                return new AverageRatings(in);
            }

            @Override
            public AverageRatings[] newArray(int size) {
                return new AverageRatings[size];
            }
        };
    }
}