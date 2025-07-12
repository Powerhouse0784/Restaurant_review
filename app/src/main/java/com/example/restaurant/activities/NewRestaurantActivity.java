package com.example.restaurant.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.restaurant.R;
import com.example.restaurant.models.Restaurant;
import com.example.restaurant.models.User;
import com.example.restaurant.utils.DatabaseHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewRestaurantActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 102;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 103;
    private static final int MAX_IMAGE_WIDTH = 1024;
    private static final int MAX_IMAGE_HEIGHT = 1024;

    // UI Components
    private EditText etName, etPostcode, etFoodType, etDescription;
    private ImageView ivRestaurantPhoto;
    private Button btnTakePhoto, btnSubmit;

    // Variables
    private DatabaseHelper databaseHelper;
    private Bitmap restaurantPhoto;
    private User currentUser;
    private FusedLocationProviderClient fusedLocationClient;
    private String currentPhotoPath;
    private String imageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_restaurant);

        initializeComponents();
        setupClickListeners();
    }

    private void initializeComponents() {
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        databaseHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etName = findViewById(R.id.etName);
        etPostcode = findViewById(R.id.etPostcode);
        etFoodType = findViewById(R.id.etFoodType);
        etDescription = findViewById(R.id.etDescription);
        ivRestaurantPhoto = findViewById(R.id.ivRestaurantPhoto);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupClickListeners() {
        btnTakePhoto.setOnClickListener(v -> showImageSourceDialog());
        btnSubmit.setOnClickListener(v -> submitRestaurant());
    }

    private void showImageSourceDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Image Source")
                .setItems(new String[]{"Camera", "Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermission();
                    } else {
                        checkStoragePermission();
                    }
                })
                .show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            openGallery();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.restaurant.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            currentPhotoPath = image.getAbsolutePath();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
        }
        return image;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_GALLERY);
    }

    private void submitRestaurant() {
        String name = etName.getText().toString().trim();
        String postcode = etPostcode.getText().toString().trim();
        String foodType = etFoodType.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty() || postcode.isEmpty() || foodType.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (restaurantPhoto == null) {
            Toast.makeText(this, "Please add a photo of the restaurant", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.restaurantExists(name, postcode)) {
            Toast.makeText(this, "Restaurant with this name and postcode already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocationAndSaveRestaurant(name, postcode, foodType, description);
        }
    }

    private void getLocationAndSaveRestaurant(String name, String postcode, String foodType, String description) {
        // Check if we have location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                saveRestaurant(name, postcode, foodType, description, location);
                            } else {
                                Toast.makeText(this, "Unable to get location. Please ensure location services are enabled.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(this, e -> {
                            Toast.makeText(this, "Failed to get location: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            } catch (SecurityException e) {
                Toast.makeText(this, "Location permission was revoked while getting location",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void saveRestaurant(String name, String postcode, String foodType,
                                String description, Location location) {
        // Save image to file and get path
        String imagePath = saveImageToStorage(restaurantPhoto);
        if (imagePath == null) {
            Toast.makeText(this, "Failed to save restaurant image", Toast.LENGTH_SHORT).show();
            return;
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setPostcode(postcode);
        restaurant.setImagePath(imagePath);
        restaurant.setFoodType(foodType);
        restaurant.setDescription(description);
        restaurant.setAddedBy(currentUser.getUsername());
        restaurant.setAddedDate(new Date());
        restaurant.setLatitude(location.getLatitude());
        restaurant.setLongitude(location.getLongitude());

        long id = databaseHelper.addRestaurant(restaurant);
        if (id != -1) {
            Toast.makeText(this, "Restaurant added successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to add restaurant", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveImageToStorage(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "RESTAURANT_" + timeStamp + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, imageFileName);

        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            // Resize image before saving
            Bitmap resizedBitmap = resizeBitmap(bitmap, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap resizeBitmap(Bitmap original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    handleCameraResult();
                    break;
                case REQUEST_IMAGE_GALLERY:
                    handleGalleryResult(data);
                    break;
            }
        }
    }

    private void handleCameraResult() {
        File imgFile = new File(currentPhotoPath);
        if (imgFile.exists()) {
            restaurantPhoto = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ivRestaurantPhoto.setImageBitmap(restaurantPhoto);
        } else {
            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleGalleryResult(Intent data) {
        try {
            Uri selectedImage = data.getData();
            InputStream imageStream = getContentResolver().openInputStream(selectedImage);
            restaurantPhoto = BitmapFactory.decodeStream(imageStream);
            ivRestaurantPhoto.setImageBitmap(restaurantPhoto);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case CAMERA_PERMISSION_REQUEST_CODE:
                    dispatchTakePictureIntent();
                    break;
                case STORAGE_PERMISSION_REQUEST_CODE:
                    openGallery();
                    break;
                case LOCATION_PERMISSION_REQUEST_CODE:
                    submitRestaurant();
                    break;
            }
        } else {
            Toast.makeText(this, "Permission required to continue", Toast.LENGTH_SHORT).show();
        }
    }
}