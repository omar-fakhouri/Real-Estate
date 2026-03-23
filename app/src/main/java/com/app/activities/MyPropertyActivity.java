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

import com.app.adapters.MyPropertyAdapter;
import com.app.models.Property;
import com.app.realestateapp.R;
import com.app.realestateapp.databinding.ActivityMyPropertyBinding;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyPropertyActivity extends AppCompatActivity {

    ActivityMyPropertyBinding bindingMyProperty;
    FirebaseFirestore db;

    RecyclerView recyclerView;
    List<Property> propertyList;
    MyPropertyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindingMyProperty = ActivityMyPropertyBinding.inflate(getLayoutInflater());
        setContentView(bindingMyProperty.getRoot());

        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        setupClicks();
        setupBottomBar();
        loadUserInfoAndProperties();
    }

    private void setupRecyclerView() {
        recyclerView = bindingMyProperty.rvMyProperty;
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        propertyList = new ArrayList<>();

        adapter = new MyPropertyAdapter(
                this,
                propertyList,
                id -> showProperty(id),
                id -> delete(id)
        );

        recyclerView.setAdapter(adapter);
    }

    private void setupClicks() {
        bindingMyProperty.ivAddProperty.setOnClickListener(v ->
                startActivity(new Intent(MyPropertyActivity.this, AddPropertyActivity.class)));

        bindingMyProperty.clUserImage.ivUserImage.setOnClickListener(v -> profilePopUpWindow());
    }

    private void setupBottomBar() {
        bindingMyProperty.includeBottomBar.llHome.setOnClickListener(v ->
                startActivity(new Intent(MyPropertyActivity.this, HomeActivity.class)));
        bindingMyProperty.includeBottomBar.ivHome.setColorFilter(Color.parseColor("#CC3F3D56"));
        bindingMyProperty.includeBottomBar.tvHome.setTextColor(Color.parseColor("#CC3F3D56"));
        bindingMyProperty.includeBottomBar.fmHome.setBackgroundResource(R.drawable.bottom_bar_normal_bg);

        bindingMyProperty.includeBottomBar.llLatest.setOnClickListener(v ->
                startActivity(new Intent(MyPropertyActivity.this, SearchActivity.class)));
        bindingMyProperty.includeBottomBar.ivSearch.setColorFilter(Color.parseColor("#CC3F3D56"));
        bindingMyProperty.includeBottomBar.tvSearch.setTextColor(Color.parseColor("#CC3F3D56"));
        bindingMyProperty.includeBottomBar.fmSea.setBackgroundResource(R.drawable.bottom_bar_normal_bg);

        bindingMyProperty.includeBottomBar.llProperty.setOnClickListener(v ->
                startActivity(new Intent(MyPropertyActivity.this, MyPropertyActivity.class)));
        bindingMyProperty.includeBottomBar.ivProperty.setColorFilter(Color.parseColor("#7F56D9"));
        bindingMyProperty.includeBottomBar.tvProperty.setTextColor(Color.parseColor("#7F56D9"));
        bindingMyProperty.includeBottomBar.fmProperty.setBackgroundResource(R.drawable.bottom_bar_select_bg);

        bindingMyProperty.includeBottomBar.llSetting.setOnClickListener(v ->
                startActivity(new Intent(MyPropertyActivity.this, SettingActivity.class)));
        bindingMyProperty.includeBottomBar.ivSetting.setColorFilter(Color.parseColor("#CC3F3D56"));
        bindingMyProperty.includeBottomBar.tvSetting.setTextColor(Color.parseColor("#CC3F3D56"));
        bindingMyProperty.includeBottomBar.fmSetting.setBackgroundResource(R.drawable.bottom_bar_normal_bg);

    }

    private void loadUserInfoAndProperties() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId != null) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(document -> {
                        String name = document.getString("name");
                        bindingMyProperty.tvUserName.setText(name);

                        String profileImage = document.getString("profileImage");
                        if (profileImage != null && !profileImage.isEmpty()) {
                            Glide.with(this)
                                    .load(profileImage)
                                    .into(bindingMyProperty.clUserImage.ivUserImage);
                        }

                        loadMyProperties();
                    })
                    .addOnFailureListener(e -> {
                        bindingMyProperty.progresshome.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    });
        } else {
            bindingMyProperty.progresshome.setVisibility(View.GONE);
            bindingMyProperty.tvUserName.setVisibility(View.GONE);
            bindingMyProperty.tvWelcomeBack.setTextSize(26);
            bindingMyProperty.clUserImage.ivUserImage.setOnClickListener(v ->
                    startActivity(new Intent(MyPropertyActivity.this, LoginActivity.class)));
            bindingMyProperty.layState.getRoot().setVisibility(View.VISIBLE);
            showLoginRequiredMessage();
        }
    }

    private void loadMyProperties() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId == null) {
            bindingMyProperty.progresshome.setVisibility(View.GONE);
            bindingMyProperty.layState.getRoot().setVisibility(View.VISIBLE);
            return;
        }

        db.collection("properties")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    propertyList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Property property = doc.toObject(Property.class);

                        if (property != null) {
                            property.setId(doc.getId());
                            propertyList.add(property);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    bindingMyProperty.progresshome.setVisibility(View.GONE);

                    if (propertyList.isEmpty()) {
                        bindingMyProperty.layState.getRoot().setVisibility(View.VISIBLE);
                    } else {
                        bindingMyProperty.layState.getRoot().setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    bindingMyProperty.progresshome.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load properties", Toast.LENGTH_SHORT).show();
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
                startActivity(new Intent(MyPropertyActivity.this, LoginActivity.class)));

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
        startActivity(new Intent(MyPropertyActivity.this, DetailedPropertyActivity.class));
    }

    private void delete(String propertyId) {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId == null) {
            showLoginRequiredMessage();
            return;
        }

        db.collection("properties")
                .document(propertyId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(
                            MyPropertyActivity.this,
                            "Your property has been deleted",
                            Toast.LENGTH_LONG
                    ).show();

                    for (int i = 0; i < propertyList.size(); i++) {
                        if (propertyId.equals(propertyList.get(i).getId())) {
                            propertyList.remove(i);
                            adapter.notifyItemRemoved(i);
                            break;
                        }
                    }

                    if (propertyList.isEmpty()) {
                        bindingMyProperty.layState.getRoot().setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                MyPropertyActivity.this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}