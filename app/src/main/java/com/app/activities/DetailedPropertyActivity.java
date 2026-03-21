package com.app.activities;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.adapters.GalleryAdapter;
import com.app.realestateapp.R;
import com.app.realestateapp.databinding.ActivityDetailedPropertyBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DetailedPropertyActivity extends AppCompatActivity implements OnMapReadyCallback {

    ActivityDetailedPropertyBinding bindingDetailedProperty;
    FirebaseFirestore db;

    List<String> galleryList;
    GalleryAdapter galleryAdapter;

    private boolean isFavorite = false;
    private String propertyId;
    private String userId;

    private GoogleMap mMap;
    private Double propertyLatitude = null;
    private Double propertyLongitude = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindingDetailedProperty = ActivityDetailedPropertyBinding.inflate(getLayoutInflater());
        setContentView(bindingDetailedProperty.getRoot());

        db = FirebaseFirestore.getInstance();

        setupGallery();
        setupMap();
        setupBasicClicks();
        getIds();

        if (propertyId == null) {
            bindingDetailedProperty.progresshome.setVisibility(View.GONE);
            Toast.makeText(this, "Property not found", Toast.LENGTH_SHORT).show();
            return;
        }

        loadFavoriteState();
        loadPropertyDetails();
        increaseViews();
    }

    private void setupGallery() {
        galleryList = new ArrayList<>();

        galleryAdapter = new GalleryAdapter(this, galleryList, imageUrl -> {
            bindingDetailedProperty.ivFullScreen.setVisibility(VISIBLE);

            Glide.with(DetailedPropertyActivity.this)
                    .load(imageUrl)
                    .into(bindingDetailedProperty.ivFullScreen);
        });

        bindingDetailedProperty.rvGallery.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        bindingDetailedProperty.rvGallery.setAdapter(galleryAdapter);

        bindingDetailedProperty.ivFullScreen.setOnClickListener(v ->
                bindingDetailedProperty.ivFullScreen.setVisibility(View.GONE));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupMap() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapDetail);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        View mapTouchView = findViewById(R.id.mapTouchView);

        mapTouchView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                case android.view.MotionEvent.ACTION_MOVE:
                    bindingDetailedProperty.cardMap.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    bindingDetailedProperty.cardMap.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });
    }

    private void setupBasicClicks() {
        bindingDetailedProperty.ibFav.setOnClickListener(v -> toggleFavorite());

        findViewById(R.id.fabBack).setOnClickListener(v -> finish());
    }

    private void getIds() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        propertyId = sp.getString("propertyId", null);
        userId = sp.getString("userId", null);
    }

    private void loadPropertyDetails() {
        db.collection("properties").document(propertyId).get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        bindingDetailedProperty.progresshome.setVisibility(View.GONE);
                        Toast.makeText(this, "Property not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String imageUrl = document.getString("mainImage");
                    Glide.with(DetailedPropertyActivity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.detail_placeholder)
                            .error(R.drawable.detail_placeholder)
                            .into(bindingDetailedProperty.ivProperty);


                    String price = document.getString("price");
                    bindingDetailedProperty.tvPrice.setText(price + " ₪");

                    Long views = document.getLong("views");
                    bindingDetailedProperty.tvView.setText(String.valueOf(views != null ? views : 0));

                    bindingDetailedProperty.tvTitle.setText(document.getString("title"));
                    bindingDetailedProperty.tvLocation.setText(document.getString("location"));
                    bindingDetailedProperty.tvBeds.setText(document.getString("bed") + " Beds");
                    bindingDetailedProperty.tvBath.setText(document.getString("bath") + " baths");
                    bindingDetailedProperty.tvSF.setText(document.getString("area") + " sq-m");
                    bindingDetailedProperty.tvAddress.setText(document.getString("address"));
                    bindingDetailedProperty.tvFurn.setText(document.getString("furnished"));
                    bindingDetailedProperty.tvdes.setText(document.getString("description"));
                    bindingDetailedProperty.tvame.setText(document.getString("amenity"));

                    if (Boolean.TRUE.equals(document.getBoolean("available"))) {
                        bindingDetailedProperty.tvAva.setText("available");
                    } else {
                        bindingDetailedProperty.tvAva.setText("not available");
                    }

                    propertyLatitude = document.getDouble("latitude");
                    propertyLongitude = document.getDouble("longitude");
                    loadPropertyLocationOnMap();

                    List<String> images = (List<String>) document.get("galleryImages");
                    galleryList.clear();

                    if (images != null && !images.isEmpty()) {
                        galleryList.addAll(images);
                        bindingDetailedProperty.rvGallery.setVisibility(VISIBLE);
                    } else {
                        bindingDetailedProperty.rvGallery.setVisibility(View.GONE);
                    }

                    galleryAdapter.notifyDataSetChanged();

                    String ownerId = document.getString("userId");
                    String ownerPhone = document.getString("phone");

                    loadOwnerData(ownerId, ownerPhone);
                })
                .addOnFailureListener(e -> {
                    bindingDetailedProperty.progresshome.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load property", Toast.LENGTH_SHORT).show();
                });
        bindingDetailedProperty.ibShare.setOnClickListener(v -> shareApp());
    }

    private void loadOwnerData(String ownerId, String ownerPhone) {
        if (ownerId == null || ownerId.isEmpty()) {
            bindingDetailedProperty.progresshome.setVisibility(View.GONE);
            return;
        }

        db.collection("users").document(ownerId).get()
                .addOnSuccessListener(document -> {
                    bindingDetailedProperty.tvOwnerEmail.setText(document.getString("email"));

                    if (ownerPhone != null) {
                        bindingDetailedProperty.tvOwnerPhone.setText(ownerPhone);
                    } else {
                        bindingDetailedProperty.tvOwnerPhone.setText("");
                    }

                    String profileImage = document.getString("profileImage");
                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(this)
                                .load(profileImage)
                                .into(bindingDetailedProperty.ivOwner);
                    }

                    bindingDetailedProperty.ibWhatApp.setOnClickListener(v ->
                            openWhatsAppChat(ownerPhone));

                    bindingDetailedProperty.ibCall.setOnClickListener(v ->
                            makePhoneCall(ownerPhone));

                    bindingDetailedProperty.progresshome.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    bindingDetailedProperty.progresshome.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load owner data", Toast.LENGTH_SHORT).show();
                });
    }

    private void increaseViews() {
        db.collection("properties")
                .document(propertyId)
                .update("views", FieldValue.increment(1));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        mMap.setOnInfoWindowClickListener(marker -> {
            Object tag = marker.getTag();

            if (tag != null) {
                String clickedPropertyId = tag.toString();

                SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
                sp.edit().putString("propertyId", clickedPropertyId).apply();

                startActivity(new Intent(DetailedPropertyActivity.this, DetailedPropertyActivity.class));
            }
        });

        loadPropertyLocationOnMap();
    }

    private void loadPropertyLocationOnMap() {
        if (mMap == null) {
            return;
        }

        mMap.clear();

        db.collection("properties")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");

                        if (lat == null || lng == null) {
                            continue;
                        }

                        String title = doc.getString("title");
                        String price = doc.getString("price");
                        String location = doc.getString("location");

                        String snippet = "";
                        if (price != null) {
                            snippet += price + " ₪";
                        }
                        if (location != null) {
                            if (!snippet.isEmpty()) {
                                snippet += " - ";
                            }
                            snippet += location;
                        }

                        LatLng propertyLatLng = new LatLng(lat, lng);

                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(propertyLatLng)
                                .title(title != null ? title : "Property")
                                .snippet(snippet));

                        if (marker != null) {
                            marker.setTag(doc.getId());
                        }
                    }

                    if (propertyLatitude != null && propertyLongitude != null) {
                        LatLng currentPropertyLatLng = new LatLng(propertyLatitude, propertyLongitude);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPropertyLatLng, 15f));
                    }
                });
    }

    public void openWhatsAppChat(String phone) {
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            return;
        }

        phone = phone.replace(" ", "");

        if (phone.startsWith("05")) {
            phone = "972" + phone.substring(1);
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("https://wa.me/" + phone));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    public void makePhoneCall(String phone) {
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            return;
        }

        phone = phone.replace(" ", "");

        if (phone.startsWith("05")) {
            phone = "+972" + phone.substring(1);
        }

        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:" + phone));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to make call", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFavoriteState() {
        if (userId == null || propertyId == null) {
            isFavorite = false;
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    List<String> favorites = (List<String>) document.get("favorite");
                    isFavorite = favorites != null && favorites.contains(propertyId);
                })
                .addOnFailureListener(e -> isFavorite = false);
    }

    private void toggleFavorite() {
        if (userId == null) {
            Toast.makeText(this, "You are not even logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DetailedPropertyActivity.this, LoginActivity.class));
            return;
        }

        if (propertyId == null) {
            Toast.makeText(this, "Property not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isFavorite) {
            db.collection("users").document(userId)
                    .update("favorite", FieldValue.arrayRemove(propertyId))
                    .addOnSuccessListener(unused -> {
                        isFavorite = false;
                        Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to remove favorite", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("users").document(userId)
                    .update("favorite", FieldValue.arrayUnion(propertyId))
                    .addOnSuccessListener(unused -> {
                        isFavorite = true;
                        Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to add favorite", Toast.LENGTH_SHORT).show());
        }
    }
    public void shareApp() {
        String title = bindingDetailedProperty.tvTitle.getText().toString().trim();
        String message = "Check out this real estate, its title is " + title;

        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setPackage("com.whatsapp");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }
}