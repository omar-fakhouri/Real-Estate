package com.app.activities;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.app.adapters.CustomSpinnerAdapter;
import com.app.realestateapp.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPropertyActivity extends AppCompatActivity {

    Spinner spAddCat, spAddPurpose, spAddLoc, spAddFurnished, spAddVerified;
    EditText etTitle, etDesc, etPhone, etAddress, etBed, etBath, etArea, etPrice, etAmenity, etLat, etLong;
    ImageView ivMainImagePreview;
    MaterialButton btnSelectMainImage, btnSelectGalleryImages, mbSubmit;
    ProgressBar progressHome;

    FirebaseFirestore db;
    FirebaseStorage storage;

    Uri mainImageUri = null;
    ArrayList<Uri> galleryImageUris = new ArrayList<>();

    ActivityResultLauncher<Intent> mainImageLauncher;
    ActivityResultLauncher<Intent> galleryImagesLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initViews();
        setupImagePickers();
        setupClicks();
        setupSpinners();

        findViewById(R.id.fabBack).setOnClickListener(v -> finish());

        findViewById(R.id.mbgetlatitude).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.latlong.net/"));
            startActivity(intent);
        });
    }

    private void initViews() {
        spAddCat = findViewById(R.id.spAddCat);
        spAddPurpose = findViewById(R.id.spAddPurpose);
        spAddLoc = findViewById(R.id.spAddLoc);
        spAddFurnished = findViewById(R.id.spAddFurnished);
        spAddVerified = findViewById(R.id.spAddVerified);

        etTitle = findViewById(R.id.etTitle);
        etDesc = findViewById(R.id.etDesc);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etBed = findViewById(R.id.etBed);
        etBath = findViewById(R.id.etBath);
        etArea = findViewById(R.id.etArea);
        etPrice = findViewById(R.id.etPrice);
        etAmenity = findViewById(R.id.etAmenity);
        etLat = findViewById(R.id.etLat);
        etLong = findViewById(R.id.etLong);

        ivMainImagePreview = findViewById(R.id.ivMainImagePreview);
        btnSelectMainImage = findViewById(R.id.btnSelectMainImage);
        btnSelectGalleryImages = findViewById(R.id.btnSelectGalleryImages);
        mbSubmit = findViewById(R.id.mbSubmit);
        progressHome = findViewById(R.id.progressHome);
    }

    private void setupImagePickers() {
        mainImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        mainImageUri = result.getData().getData();
                        if (mainImageUri != null) {
                            ivMainImagePreview.setImageURI(mainImageUri);
                        }
                    }
                }
        );

        galleryImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        galleryImageUris.clear();

                        if (data.getClipData() != null) {
                            ClipData clipData = data.getClipData();
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                Uri uri = clipData.getItemAt(i).getUri();
                                if (uri != null) {
                                    galleryImageUris.add(uri);
                                }
                            }
                        } else if (data.getData() != null) {
                            galleryImageUris.add(data.getData());
                        }

                        Toast.makeText(this, galleryImageUris.size() + " gallery images selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupClicks() {
        btnSelectMainImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            mainImageLauncher.launch(intent);
        });

        btnSelectGalleryImages.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            galleryImagesLauncher.launch(Intent.createChooser(intent, "Select Gallery Images"));
        });

        mbSubmit.setOnClickListener(v -> saveProperty());
    }

    private void saveProperty() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String bed = etBed.getText().toString().trim();
        String bath = etBath.getText().toString().trim();
        String area = etArea.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String amenity = etAmenity.getText().toString().trim();
        String latText = etLat.getText().toString().trim();
        String longText = etLong.getText().toString().trim();

        String category = spAddCat.getSelectedItem().toString().trim();
        String purpose = spAddPurpose.getSelectedItem().toString().trim();
        String location = spAddLoc.getSelectedItem().toString().trim();
        String furnished = spAddFurnished.getSelectedItem().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Enter title");
            return;
        }

        if (desc.isEmpty()) {
            etDesc.setError("Enter description");
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Enter phone");
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Enter address");
            return;
        }

        if (price.isEmpty()) {
            etPrice.setError("Enter price");
            return;
        }

        if (category.equals("Select Category")) {
            Toast.makeText(this, "Enter category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (purpose.equals("Select Purpose")) {
            Toast.makeText(this, "Enter purpose", Toast.LENGTH_SHORT).show();
            return;
        }

        if (location.equals("Select Location")) {
            Toast.makeText(this, "Enter location", Toast.LENGTH_SHORT).show();
            return;
        }

        if (furnished.equals("Select Furnished")) {
            Toast.makeText(this, "Enter if furnished", Toast.LENGTH_SHORT).show();
            return;
        }

        if (latText.isEmpty()) {
            etLat.setError("Enter latitude");
            return;
        }

        if (longText.isEmpty()) {
            etLong.setError("Enter longitude");
            return;
        }

        double latitude;
        double longitude;

        try {
            latitude = Double.parseDouble(latText);
            longitude = Double.parseDouble(longText);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid latitude or longitude", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mainImageUri == null) {
            Toast.makeText(this, "Select main image", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        progressHome.setVisibility(View.VISIBLE);
        mbSubmit.setEnabled(false);

        String propertyId = db.collection("properties").document().getId();

        uploadMainImage(propertyId, new OnMainImageUploaded() {
            @Override
            public void onSuccess(String mainImageUrl) {
                uploadGalleryImages(propertyId, new OnGalleryImagesUploaded() {
                    @Override
                    public void onSuccess(List<String> galleryUrls) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", propertyId);
                        map.put("userId", userId);
                        map.put("title", title);
                        map.put("description", desc);
                        map.put("phone", phone);
                        map.put("address", address);
                        map.put("bed", bed);
                        map.put("bath", bath);
                        map.put("area", area);
                        map.put("price", price);
                        map.put("amenity", amenity);
                        map.put("category", category);
                        map.put("purpose", purpose);
                        map.put("location", location);
                        map.put("furnished", furnished);
                        map.put("latitude", latitude);
                        map.put("longitude", longitude);
                        map.put("available", true);
                        map.put("mainImage", mainImageUrl);
                        map.put("galleryImages", galleryUrls);
                        map.put("views", 0);
                        map.put("timestamp", System.currentTimeMillis());
                        map.put("createdAt", FieldValue.serverTimestamp());

                        db.collection("properties")
                                .document(propertyId)
                                .set(map)
                                .addOnSuccessListener(unused -> {
                                    progressHome.setVisibility(View.GONE);
                                    mbSubmit.setEnabled(true);
                                    Toast.makeText(AddPropertyActivity.this, "Property added successfully", Toast.LENGTH_SHORT).show();
                                    clearFields();
                                    startActivity(new Intent(AddPropertyActivity.this, MyPropertyActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressHome.setVisibility(View.GONE);
                                    mbSubmit.setEnabled(true);
                                    Toast.makeText(AddPropertyActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }

                    @Override
                    public void onFailure(String error) {
                        progressHome.setVisibility(View.GONE);
                        mbSubmit.setEnabled(true);
                        Toast.makeText(AddPropertyActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                progressHome.setVisibility(View.GONE);
                mbSubmit.setEnabled(true);
                Toast.makeText(AddPropertyActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadMainImage(String propertyId, OnMainImageUploaded callback) {
        StorageReference ref = storage.getReference()
                .child("property_images")
                .child(propertyId)
                .child("main_image.jpg");

        ref.putFile(mainImageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void uploadGalleryImages(String propertyId, OnGalleryImagesUploaded callback) {
        if (galleryImageUris.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<Task<Uri>> taskList = new ArrayList<>();

        for (int i = 0; i < galleryImageUris.size(); i++) {
            Uri uri = galleryImageUris.get(i);

            StorageReference ref = storage.getReference()
                    .child("property_images")
                    .child(propertyId)
                    .child("gallery_" + i + ".jpg");

            Task<Uri> task = ref.putFile(uri)
                    .continueWithTask(task1 -> {
                        if (!task1.isSuccessful()) {
                            throw task1.getException();
                        }
                        return ref.getDownloadUrl();
                    });

            taskList.add(task);
        }

        Tasks.whenAllSuccess(taskList)
                .addOnSuccessListener(results -> {
                    List<String> urls = new ArrayList<>();

                    for (Object object : results) {
                        Uri uri = (Uri) object;
                        urls.add(uri.toString());
                    }

                    callback.onSuccess(urls);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void clearFields() {
        etTitle.setText("");
        etDesc.setText("");
        etPhone.setText("");
        etAddress.setText("");
        etBed.setText("");
        etBath.setText("");
        etArea.setText("");
        etPrice.setText("");
        etAmenity.setText("");
        etLat.setText("");
        etLong.setText("");

        spAddCat.setSelection(0);
        spAddPurpose.setSelection(0);
        spAddLoc.setSelection(0);
        spAddFurnished.setSelection(0);

        mainImageUri = null;
        galleryImageUris.clear();
        ivMainImagePreview.setImageResource(R.drawable.detail_placeholder);
    }

    interface OnMainImageUploaded {
        void onSuccess(String mainImageUrl);
        void onFailure(String error);
    }

    interface OnGalleryImagesUploaded {
        void onSuccess(List<String> galleryUrls);
        void onFailure(String error);
    }

    private void setupSpinners() {
        List<String> purposeList = new ArrayList<>();
        purposeList.add("Select Purpose");
        purposeList.add("Sell");
        purposeList.add("Rent");

        List<String> categoryList = new ArrayList<>();
        categoryList.add("Select Category");
        categoryList.add("Apartment");
        categoryList.add("Commercial");
        categoryList.add("House");
        categoryList.add("Industrial");
        categoryList.add("Land");

        List<String> locationList = new ArrayList<>();
        locationList.add("Select Location");
        locationList.add("Jerusalem");
        locationList.add("Tel Aviv");
        locationList.add("Haifa");
        locationList.add("Ashdod");
        locationList.add("Netanya");
        locationList.add("Beer Sheva");
        locationList.add("Petah Tikva");
        locationList.add("Holon");
        locationList.add("Bnei Brak");
        locationList.add("Ramat Gan");
        locationList.add("Ashkelon");
        locationList.add("Rehovot");
        locationList.add("Bat Yam");
        locationList.add("Rishon LeZion");
        locationList.add("Kfar Saba");
        locationList.add("Hadera");
        locationList.add("Nazareth");
        locationList.add("Umm al-Fahm");
        locationList.add("Safed");
        locationList.add("Tiberias");
        locationList.add("Eilat");
        locationList.add("Acre");
        locationList.add("Nazareth Illit");
        locationList.add("Modiin");
        locationList.add("Beit Shemesh");
        locationList.add("Lod");
        locationList.add("Ramla");

        List<String> furnishedList = new ArrayList<>();
        furnishedList.add("Select Furnished");
        furnishedList.add("Furnished");
        furnishedList.add("Semi Furnished");
        furnishedList.add("Unfurnished");

        CustomSpinnerAdapter purposeAdapter = new CustomSpinnerAdapter(this, purposeList, false);
        CustomSpinnerAdapter categoryAdapter = new CustomSpinnerAdapter(this, categoryList, false);
        CustomSpinnerAdapter locationAdapter = new CustomSpinnerAdapter(this, locationList, false);
        CustomSpinnerAdapter furnishedAdapter = new CustomSpinnerAdapter(this, furnishedList, false);

        spAddPurpose.setAdapter(purposeAdapter);
        spAddCat.setAdapter(categoryAdapter);
        spAddLoc.setAdapter(locationAdapter);
        spAddFurnished.setAdapter(furnishedAdapter);

        spAddPurpose.setSelection(0);
        spAddCat.setSelection(0);
        spAddLoc.setSelection(0);
        spAddFurnished.setSelection(0);
    }
}