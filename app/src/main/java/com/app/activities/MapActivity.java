package com.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.realestateapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MapActivity extends AppCompatActivity {

    private GoogleMap mMap;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        db = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFull);

        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;

                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setScrollGesturesEnabled(true);

                LatLng israel = new LatLng(31.0461, 34.8516);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(israel, 7f));

                mMap.setOnInfoWindowClickListener(marker -> {
                    Object tag = marker.getTag();

                    if (tag != null) {
                        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
                        sp.edit().putString("propertyId", tag.toString()).apply();

                        startActivity(new Intent(MapActivity.this, DetailedPropertyActivity.class));
                    }
                });

                loadAllPropertiesOnMap();
            });
        }

        findViewById(R.id.btnBackMap).setOnClickListener(v ->
                startActivity(new Intent(MapActivity.this, SearchActivity.class)));

    }

    private void loadAllPropertiesOnMap() {
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
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MapActivity.this, "Failed to load map", Toast.LENGTH_SHORT).show());
    }
}