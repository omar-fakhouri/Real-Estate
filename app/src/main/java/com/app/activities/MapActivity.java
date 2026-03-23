package com.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.models.Property;
import com.app.realestateapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    public static List<Property> propertyList = new ArrayList<>();

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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

        findViewById(R.id.btnBackMap).setOnClickListener(v -> finish());
    }

    private void loadAllPropertiesOnMap() {
        if (propertyList == null || propertyList.isEmpty()) return;

        for (int i = 0; i < propertyList.size(); i++) {
            Double lat = propertyList.get(i).getLatitude();
            Double lng = propertyList.get(i).getLongitude();

            if (lat == null || lng == null) continue;

            String title = propertyList.get(i).getTitle();
            String price = propertyList.get(i).getPrice();
            String location = propertyList.get(i).getLocation();

            String snippet = "";
            if (price != null) snippet += price + " ₪";
            if (location != null) {
                if (!snippet.isEmpty()) snippet += " - ";
                snippet += location;
            }

            LatLng propertyLatLng = new LatLng(lat, lng);

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(propertyLatLng)
                    .title(title != null ? title : "Property")
                    .snippet(snippet));

            if (marker != null) {
                marker.setTag(propertyList.get(i).getId());
            }
        }
    }
}