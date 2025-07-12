package com.example.restaurant.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import com.example.restaurant.models.Restaurant;
import com.example.restaurant.models.Review;
import com.example.restaurant.models.User;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "restaurant_reviews.db";
    private static final int DATABASE_VERSION = 2; // Incremented version for schema changes

    // User table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_IS_ADMIN = "is_admin";

    // Restaurant table (updated to use image path instead of BLOB)
    private static final String TABLE_RESTAURANTS = "restaurants";
    private static final String COLUMN_RESTAURANT_ID = "restaurant_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_POSTCODE = "postcode";
    private static final String COLUMN_IMAGE_PATH = "image_path"; // Changed from COLUMN_IMAGE
    private static final String COLUMN_FOOD_TYPE = "food_type";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_ADDED_BY = "added_by";
    private static final String COLUMN_ADDED_DATE = "added_date";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";

    // Review table
    private static final String TABLE_REVIEWS = "reviews";
    private static final String COLUMN_REVIEW_ID = "review_id";
    private static final String COLUMN_REVIEW_RESTAURANT_ID = "restaurant_id";
    private static final String COLUMN_REVIEW_USER_ID = "user_id";
    private static final String COLUMN_COMMENT = "comment";
    private static final String COLUMN_SERVICE_SCORE = "service_score";
    private static final String COLUMN_VALUE_SCORE = "value_score";
    private static final String COLUMN_FOOD_SCORE = "food_score";
    private static final String COLUMN_REVIEW_DATE = "review_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_IS_ADMIN + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_USERS_TABLE);

        // Create restaurants table with image_path instead of BLOB
        String CREATE_RESTAURANTS_TABLE = "CREATE TABLE " + TABLE_RESTAURANTS + "("
                + COLUMN_RESTAURANT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_POSTCODE + " TEXT,"
                + COLUMN_IMAGE_PATH + " TEXT," // Changed to TEXT for file path
                + COLUMN_FOOD_TYPE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_ADDED_BY + " TEXT,"
                + COLUMN_ADDED_DATE + " INTEGER,"
                + COLUMN_LATITUDE + " REAL,"
                + COLUMN_LONGITUDE + " REAL,"
                + "UNIQUE(" + COLUMN_NAME + ", " + COLUMN_POSTCODE + "))";
        db.execSQL(CREATE_RESTAURANTS_TABLE);

        // Create reviews table
        String CREATE_REVIEWS_TABLE = "CREATE TABLE " + TABLE_REVIEWS + "("
                + COLUMN_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_REVIEW_RESTAURANT_ID + " INTEGER,"
                + COLUMN_REVIEW_USER_ID + " INTEGER,"
                + COLUMN_COMMENT + " TEXT,"
                + COLUMN_SERVICE_SCORE + " REAL,"
                + COLUMN_VALUE_SCORE + " REAL,"
                + COLUMN_FOOD_SCORE + " REAL,"
                + COLUMN_REVIEW_DATE + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_REVIEW_RESTAURANT_ID + ") REFERENCES " + TABLE_RESTAURANTS + "(" + COLUMN_RESTAURANT_ID + "),"
                + "FOREIGN KEY(" + COLUMN_REVIEW_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                + "UNIQUE(" + COLUMN_REVIEW_RESTAURANT_ID + ", " + COLUMN_REVIEW_USER_ID + "))";
        db.execSQL(CREATE_REVIEWS_TABLE);

        // Insert test data
        insertTestData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Migration from version 1 to 2
            // Create new table with image_path
            String CREATE_NEW_RESTAURANTS_TABLE = "CREATE TABLE restaurants_new ("
                    + COLUMN_RESTAURANT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME + " TEXT,"
                    + COLUMN_POSTCODE + " TEXT,"
                    + COLUMN_IMAGE_PATH + " TEXT,"
                    + COLUMN_FOOD_TYPE + " TEXT,"
                    + COLUMN_DESCRIPTION + " TEXT,"
                    + COLUMN_ADDED_BY + " TEXT,"
                    + COLUMN_ADDED_DATE + " INTEGER,"
                    + COLUMN_LATITUDE + " REAL,"
                    + COLUMN_LONGITUDE + " REAL,"
                    + "UNIQUE(" + COLUMN_NAME + ", " + COLUMN_POSTCODE + "))";
            db.execSQL(CREATE_NEW_RESTAURANTS_TABLE);

            // Copy data from old table to new table (without image data)
            db.execSQL("INSERT INTO restaurants_new ("
                    + COLUMN_RESTAURANT_ID + ", "
                    + COLUMN_NAME + ", "
                    + COLUMN_POSTCODE + ", "
                    + COLUMN_FOOD_TYPE + ", "
                    + COLUMN_DESCRIPTION + ", "
                    + COLUMN_ADDED_BY + ", "
                    + COLUMN_ADDED_DATE + ", "
                    + COLUMN_LATITUDE + ", "
                    + COLUMN_LONGITUDE + ") "
                    + "SELECT "
                    + COLUMN_RESTAURANT_ID + ", "
                    + COLUMN_NAME + ", "
                    + COLUMN_POSTCODE + ", "
                    + COLUMN_FOOD_TYPE + ", "
                    + COLUMN_DESCRIPTION + ", "
                    + COLUMN_ADDED_BY + ", "
                    + COLUMN_ADDED_DATE + ", "
                    + COLUMN_LATITUDE + ", "
                    + COLUMN_LONGITUDE + " "
                    + "FROM " + TABLE_RESTAURANTS);

            // Drop old table and rename new one
            db.execSQL("DROP TABLE " + TABLE_RESTAURANTS);
            db.execSQL("ALTER TABLE restaurants_new RENAME TO " + TABLE_RESTAURANTS);
        }
    }

    private void insertTestData(SQLiteDatabase db) {
        // Insert test users
        insertUser(db, "user1", "p455w0rd", false);
        insertUser(db, "user2", "p455w0rd", false);
        insertUser(db, "user3", "p455w0rd", false);
        insertUser(db, "admin", "p455w0rd", true);

        // Insert test restaurants with null image paths
        insertRestaurant(db, "The Gourmet Kitchen", "SW1A 1AA",
                null, "International", "A fine dining experience with global cuisine.", "admin",
                51.5014, -0.1419);
        insertRestaurant(db, "Pasta Palace", "W1D 1LL",
                null, "Italian", "Authentic Italian pasta dishes in a cozy setting.", "admin",
                51.5138, -0.1314);
        insertRestaurant(db, "Burger Barn", "EC1A 1BB",
                null, "American", "Juicy burgers and crispy fries.", "admin",
                51.5155, -0.0922);
        insertRestaurant(db, "Sushi Spot", "WC2E 9DD",
                null, "Japanese", "Fresh sushi and sashimi prepared daily.", "admin",
                51.5119, -0.1236);
        insertRestaurant(db, "Curry House", "E1 6AN",
                null, "Indian", "Traditional Indian curries with authentic spices.", "admin",
                51.5152, -0.0722);
        insertRestaurant(db, "Taco Town", "SE1 9SG",
                null, "Mexican", "Vibrant Mexican street food and cocktails.", "admin",
                51.5045, -0.0865);
    }

    // User methods (unchanged)
    public User authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID, COLUMN_USERNAME, COLUMN_IS_ADMIN},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getInt(2) == 1
            );
            cursor.close();
            return user;
        }
        return null;
    }

    // Updated Restaurant methods
    public long addRestaurant(Restaurant restaurant) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, restaurant.getName());
        values.put(COLUMN_POSTCODE, restaurant.getPostcode());
        values.put(COLUMN_IMAGE_PATH, restaurant.getImagePath());
        values.put(COLUMN_FOOD_TYPE, restaurant.getFoodType());
        values.put(COLUMN_DESCRIPTION, restaurant.getDescription());
        values.put(COLUMN_ADDED_BY, restaurant.getAddedBy());
        values.put(COLUMN_ADDED_DATE, restaurant.getAddedDate().getTime());
        values.put(COLUMN_LATITUDE, restaurant.getLatitude());
        values.put(COLUMN_LONGITUDE, restaurant.getLongitude());

        return db.insert(TABLE_RESTAURANTS, null, values);
    }

    public List<Restaurant> getAllRestaurants() {
        List<Restaurant> restaurants = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Only select non-image columns initially (image path is small)
        Cursor cursor = db.query(TABLE_RESTAURANTS,
                new String[]{
                        COLUMN_RESTAURANT_ID,
                        COLUMN_NAME,
                        COLUMN_POSTCODE,
                        COLUMN_IMAGE_PATH,
                        COLUMN_FOOD_TYPE,
                        COLUMN_DESCRIPTION,
                        COLUMN_ADDED_BY,
                        COLUMN_ADDED_DATE,
                        COLUMN_LATITUDE,
                        COLUMN_LONGITUDE
                },
                null, null, null, null, COLUMN_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                Restaurant restaurant = new Restaurant();
                restaurant.setId(cursor.getLong(0));
                restaurant.setName(cursor.getString(1));
                restaurant.setPostcode(cursor.getString(2));
                restaurant.setImagePath(cursor.getString(3));
                restaurant.setFoodType(cursor.getString(4));
                restaurant.setDescription(cursor.getString(5));
                restaurant.setAddedBy(cursor.getString(6));
                restaurant.setAddedDate(new Date(cursor.getLong(7)));
                restaurant.setLatitude(cursor.getDouble(8));
                restaurant.setLongitude(cursor.getDouble(9));

                // Calculate average ratings
                restaurant.setAverageRating(getAverageRatings(restaurant.getId()));

                restaurants.add(restaurant);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return restaurants;
    }

    public Restaurant getRestaurantById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RESTAURANTS,
                null,
                COLUMN_RESTAURANT_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(cursor.getLong(0));
            restaurant.setName(cursor.getString(1));
            restaurant.setPostcode(cursor.getString(2));
            restaurant.setImagePath(cursor.getString(3));
            restaurant.setFoodType(cursor.getString(4));
            restaurant.setDescription(cursor.getString(5));
            restaurant.setAddedBy(cursor.getString(6));
            restaurant.setAddedDate(new Date(cursor.getLong(7)));
            restaurant.setLatitude(cursor.getDouble(8));
            restaurant.setLongitude(cursor.getDouble(9));

            // Get all reviews for this restaurant
            restaurant.setReviews(getReviewsForRestaurant(id));

            cursor.close();
            return restaurant;
        }
        return null;
    }

    public boolean restaurantExists(String name, String postcode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RESTAURANTS,
                new String[]{COLUMN_RESTAURANT_ID},
                COLUMN_NAME + "=? AND " + COLUMN_POSTCODE + "=?",
                new String[]{name, postcode},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Review methods (unchanged)
    public long addReview(Review review) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REVIEW_RESTAURANT_ID, review.getRestaurantId());
        values.put(COLUMN_REVIEW_USER_ID, review.getUserId());
        values.put(COLUMN_COMMENT, review.getComment());
        values.put(COLUMN_SERVICE_SCORE, review.getServiceScore());
        values.put(COLUMN_VALUE_SCORE, review.getValueScore());
        values.put(COLUMN_FOOD_SCORE, review.getFoodScore());
        values.put(COLUMN_REVIEW_DATE, review.getReviewDate().getTime());

        return db.insert(TABLE_REVIEWS, null, values);
    }

    public List<Review> getReviewsForRestaurant(long restaurantId) {
        List<Review> reviews = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REVIEWS,
                null,
                COLUMN_REVIEW_RESTAURANT_ID + "=?",
                new String[]{String.valueOf(restaurantId)},
                null, null, COLUMN_REVIEW_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Review review = new Review();
                review.setId(cursor.getLong(0));
                review.setRestaurantId(cursor.getLong(1));
                review.setUserId(cursor.getLong(2));
                review.setComment(cursor.getString(3));
                review.setServiceScore(cursor.getFloat(4));
                review.setValueScore(cursor.getFloat(5));
                review.setFoodScore(cursor.getFloat(6));
                review.setReviewDate(new Date(cursor.getLong(7)));

                reviews.add(review);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return reviews;
    }

    // Other methods (unchanged)
    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID, COLUMN_USERNAME, COLUMN_IS_ADMIN},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getInt(2) == 1
            );
            cursor.close();
            return user;
        }
        return null;
    }

    public String getUsernameById(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USERNAME},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String username = cursor.getString(0);
            cursor.close();
            return username;
        }
        return null;
    }

    public List<Restaurant> searchRestaurants(String query) {
        List<Restaurant> restaurants = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RESTAURANTS,
                null,
                COLUMN_NAME + " LIKE ? OR " + COLUMN_FOOD_TYPE + " LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"},
                null, null, COLUMN_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                Restaurant restaurant = new Restaurant();
                restaurant.setId(cursor.getLong(0));
                restaurant.setName(cursor.getString(1));
                restaurant.setPostcode(cursor.getString(2));
                restaurant.setImagePath(cursor.getString(3));
                restaurant.setFoodType(cursor.getString(4));
                restaurant.setDescription(cursor.getString(5));
                restaurant.setAddedBy(cursor.getString(6));
                restaurant.setAddedDate(new Date(cursor.getLong(7)));
                restaurant.setLatitude(cursor.getDouble(8));
                restaurant.setLongitude(cursor.getDouble(9));
                restaurant.setAverageRating(getAverageRatings(restaurant.getId()));
                restaurants.add(restaurant);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return restaurants;
    }

    public boolean hasUserReviewedRestaurant(long userId, long restaurantId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REVIEWS,
                new String[]{COLUMN_REVIEW_ID},
                COLUMN_REVIEW_RESTAURANT_ID + "=? AND " + COLUMN_REVIEW_USER_ID + "=?",
                new String[]{String.valueOf(restaurantId), String.valueOf(userId)},
                null, null, null);

        boolean hasReviewed = cursor.getCount() > 0;
        cursor.close();
        return hasReviewed;
    }

    public Restaurant.AverageRatings getAverageRatings(long restaurantId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Restaurant.AverageRatings averageRatings = new Restaurant.AverageRatings();

        Cursor serviceCursor = db.rawQuery("SELECT AVG(" + COLUMN_SERVICE_SCORE + ") FROM " + TABLE_REVIEWS +
                        " WHERE " + COLUMN_REVIEW_RESTAURANT_ID + "=?",
                new String[]{String.valueOf(restaurantId)});
        if (serviceCursor.moveToFirst()) {
            averageRatings.setService(serviceCursor.getFloat(0));
        }
        serviceCursor.close();

        Cursor valueCursor = db.rawQuery("SELECT AVG(" + COLUMN_VALUE_SCORE + ") FROM " + TABLE_REVIEWS +
                        " WHERE " + COLUMN_REVIEW_RESTAURANT_ID + "=?",
                new String[]{String.valueOf(restaurantId)});
        if (valueCursor.moveToFirst()) {
            averageRatings.setValue(valueCursor.getFloat(0));
        }
        valueCursor.close();

        Cursor foodCursor = db.rawQuery("SELECT AVG(" + COLUMN_FOOD_SCORE + ") FROM " + TABLE_REVIEWS +
                        " WHERE " + COLUMN_REVIEW_RESTAURANT_ID + "=?",
                new String[]{String.valueOf(restaurantId)});
        if (foodCursor.moveToFirst()) {
            averageRatings.setFood(foodCursor.getFloat(0));
        }
        foodCursor.close();

        float overall = (averageRatings.getService() + averageRatings.getValue() + averageRatings.getFood()) / 3;
        averageRatings.setOverall(overall);

        return averageRatings;
    }

    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public long addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_IS_ADMIN, 0);

        return db.insert(TABLE_USERS, null, values);
    }

    // Utility methods
    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap getBitmapFromBytes(byte[] imageBytes) {
        if (imageBytes == null) return null;
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private long insertUser(SQLiteDatabase db, String username, String password, boolean isAdmin) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_IS_ADMIN, isAdmin ? 1 : 0);
        return db.insert(TABLE_USERS, null, values);
    }

    private long insertRestaurant(SQLiteDatabase db, String name, String postcode, String imagePath,
                                  String foodType, String description, String addedBy,
                                  double latitude, double longitude) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_POSTCODE, postcode);
        values.put(COLUMN_IMAGE_PATH, imagePath);
        values.put(COLUMN_FOOD_TYPE, foodType);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_ADDED_BY, addedBy);
        values.put(COLUMN_ADDED_DATE, new Date().getTime());
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        return db.insert(TABLE_RESTAURANTS, null, values);
    }

    private long insertReview(SQLiteDatabase db, long restaurantId, long userId,
                              String comment, float serviceScore, float valueScore, float foodScore) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_REVIEW_RESTAURANT_ID, restaurantId);
        values.put(COLUMN_REVIEW_USER_ID, userId);
        values.put(COLUMN_COMMENT, comment);
        values.put(COLUMN_SERVICE_SCORE, serviceScore);
        values.put(COLUMN_VALUE_SCORE, valueScore);
        values.put(COLUMN_FOOD_SCORE, foodScore);
        values.put(COLUMN_REVIEW_DATE, new Date().getTime());
        return db.insert(TABLE_REVIEWS, null, values);
    }
}