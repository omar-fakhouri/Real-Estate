package com.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.adapters.FavoriteAdapter;
import com.app.models.Property;
import com.app.realestateapp.R;
import com.app.realestateapp.databinding.ActivityFavoriteBinding;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    ActivityFavoriteBinding bindingFavorite;
    FirebaseFirestore db;

    RecyclerView recyclerView;
    List<Property> propertyList;
    FavoriteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindingFavorite = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(bindingFavorite.getRoot());

        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        setupClicks();

        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId != null) {
            loadFavProperties();
        } else {
            bindingFavorite.progresshome.setVisibility(View.GONE);
            bindingFavorite.clUserImage.ivUserImage.setOnClickListener(v -> showLoginRequiredMessage());
            bindingFavorite.layState.getRoot().setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        recyclerView = bindingFavorite.rvFavProperty;
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        propertyList = new ArrayList<>();

        adapter = new FavoriteAdapter(
                this,
                propertyList,
                id -> showProperty(id),
                id -> removeFromFavorite(id)
        );

        recyclerView.setAdapter(adapter);
    }

    private void setupClicks() {
        bindingFavorite.clUserImage.ivUserImage.setOnClickListener(v -> profilePopUpWindow());

        bindingFavorite.rlTopSec.setOnClickListener(v ->
                startActivity(new Intent(FavoriteActivity.this, HomeActivity.class)));
    }

    private void loadFavProperties() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId == null) {
            bindingFavorite.progresshome.setVisibility(View.GONE);
            bindingFavorite.layState.getRoot().setVisibility(View.VISIBLE);
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    List<String> propertiesIds = (List<String>) document.get("favorite");

                    if (propertiesIds == null) {
                        propertiesIds = new ArrayList<>();
                    }

                    String profileImage = document.getString("profileImage");
                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(this)
                                .load(profileImage)
                                .into(bindingFavorite.clUserImage.ivUserImage);
                    }

                    if (propertiesIds.isEmpty()) {
                        propertyList.clear();
                        adapter.notifyDataSetChanged();
                        bindingFavorite.progresshome.setVisibility(View.GONE);
                        bindingFavorite.layState.getRoot().setVisibility(View.VISIBLE);
                        return;
                    }

                    List<String> finalPropertiesIds = propertiesIds;

                    db.collection("properties")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                propertyList.clear();

                                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                    if (finalPropertiesIds.contains(doc.getId())) {
                                        Property property = doc.toObject(Property.class);

                                        if (property != null) {
                                            property.setId(doc.getId());
                                            propertyList.add(property);
                                        }
                                    }
                                }

                                adapter.notifyDataSetChanged();
                                bindingFavorite.progresshome.setVisibility(View.GONE);

                                if (propertyList.isEmpty()) {
                                    bindingFavorite.layState.getRoot().setVisibility(View.VISIBLE);
                                } else {
                                    bindingFavorite.layState.getRoot().setVisibility(View.GONE);
                                }
                            })
                            .addOnFailureListener(e -> {
                                bindingFavorite.progresshome.setVisibility(View.GONE);
                                Toast.makeText(this, "Failed to load favorites", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    bindingFavorite.progresshome.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
    }

    public void showLoginRequiredMessage() {
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "You are not even logged in",
                Snackbar.LENGTH_LONG
        );

        snackbar.setDuration(4500);

        snackbar.setAction("Login", v ->
                startActivity(new Intent(FavoriteActivity.this, LoginActivity.class)));

        snackbar.setTextColor(Color.BLACK);
        snackbar.setActionTextColor(Color.BLACK);

        View view = snackbar.getView();

        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.setMargins(32, 0, 32, 32);
            view.setLayoutParams(params);
        }

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(36f);
        view.setBackground(bg);

        view.setElevation(12f);
        view.setPadding(24, 20, 24, 20);

        snackbar.show();
    }

    public void profilePopUpWindow() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId == null) {
            showLoginRequiredMessage();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.layout_profile_popup);

        RelativeLayout rlEditProfile = dialog.findViewById(R.id.layEdProfile);
        RelativeLayout rlFavProperty = dialog.findViewById(R.id.layFavProperty);
        RelativeLayout rlLogout = dialog.findViewById(R.id.layLogout);

        if (rlEditProfile != null) {
            rlEditProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, EditProfileActivity.class));
                dialog.dismiss();
            });
        }

        if (rlFavProperty != null) {
            rlFavProperty.setOnClickListener(v -> {
                startActivity(new Intent(this, FavoriteActivity.class));
                dialog.dismiss();
            });
        }

        if (rlLogout != null) {
            rlLogout.setOnClickListener(v -> {
                logout();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    public void logout() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.layout_logout);

        View btnYes = dialog.findViewById(R.id.mbYes);
        View btnCancel = dialog.findViewById(R.id.mbCancel);

        if (btnYes != null) {
            btnYes.setOnClickListener(v -> {
                SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
                sp.edit().remove("userId").apply();

                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                dialog.dismiss();
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    public void showProperty(String propertyId) {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        sp.edit().putString("propertyId", propertyId).apply();
        startActivity(new Intent(FavoriteActivity.this, DetailedPropertyActivity.class));
    }

    private void removeFromFavorite(String propertyId) {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId == null) {
            showLoginRequiredMessage();
            return;
        }

        db.collection("users")
                .document(userId)
                .update("favorite", FieldValue.arrayRemove(propertyId))
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();

                    for (int i = 0; i < propertyList.size(); i++) {
                        if (propertyId.equals(propertyList.get(i).getId())) {
                            propertyList.remove(i);
                            adapter.notifyItemRemoved(i);
                            break;
                        }
                    }

                    if (propertyList.isEmpty()) {
                        bindingFavorite.layState.getRoot().setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}