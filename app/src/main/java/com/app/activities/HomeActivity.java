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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.adapters.CustomSpinnerAdapter;
import com.app.adapters.PopularAdapter;
import com.app.adapters.LatestAdapter;
import com.app.adapters.SearchAdapter;
import com.app.models.Property;
import com.app.realestateapp.R;
import com.app.realestateapp.databinding.ActivityHomeBinding;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding bindingHome;
    FirebaseFirestore db;

    RecyclerView recyclerViewPopular, recyclerViewLatest;

    List<Property> popularList;
    List<Property> latestList;
    List<Property> searchList;

    PopularAdapter popularAdapter;
    LatestAdapter latestAdapter;
    SearchAdapter searchAdapter;

    int loadedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindingHome = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(bindingHome.getRoot());

        db = FirebaseFirestore.getInstance();

        setupSpinners();
        setupUserInfo();
        setupBottomBar();
        setupRecyclerViews();
        setupSearchButton();

        loadPopularProperties();
        loadLatestProperties();
    }

    private void setupUserInfo() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener(document -> {
                String name = document.getString("name");
                String profileImage = document.getString("profileImage");

                bindingHome.topHome.tvUserName.setText(name);

                if (profileImage != null && !profileImage.isEmpty()) {
                    Glide.with(this).load(profileImage).into(bindingHome.topHome.clUserImage.ivUserImage);
                }
            });
        } else {
            bindingHome.topHome.tvUserName.setVisibility(View.GONE);
            bindingHome.topHome.tvWelcomeBack.setTextSize(26);
            bindingHome.topHome.clUserImage.ivUserImage.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class)));
        }

        bindingHome.topHome.clUserImage.ivUserImage.setOnClickListener(v -> profilePopUpWindow());
    }

    private void setupBottomBar() {
        bindingHome.includeBottomBar.llHome.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, HomeActivity.class)));

        bindingHome.includeBottomBar.llLatest.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SearchActivity.class)));

        bindingHome.includeBottomBar.llProperty.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, MyPropertyActivity.class)));

        bindingHome.includeBottomBar.llSetting.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SettingActivity.class)));

        bindingHome.includeBottomBar.ivHome.setColorFilter(Color.parseColor("#7F56D9"));
        bindingHome.includeBottomBar.tvHome.setTextColor(Color.parseColor("#7F56D9"));
        bindingHome.includeBottomBar.fmHome.setBackgroundResource(R.drawable.bottom_bar_select_bg);

        bindingHome.includeBottomBar.ivSearch.setColorFilter(Color.parseColor("#CC3F3D56"));
        bindingHome.includeBottomBar.tvSearch.setTextColor(Color.parseColor("#CC3F3D56"));
        bindingHome.includeBottomBar.fmSea.setBackgroundResource(R.drawable.bottom_bar_normal_bg);

        bindingHome.includeBottomBar.ivProperty.setColorFilter(Color.parseColor("#CC3F3D56"));
        bindingHome.includeBottomBar.tvProperty.setTextColor(Color.parseColor("#CC3F3D56"));
        bindingHome.includeBottomBar.fmProperty.setBackgroundResource(R.drawable.bottom_bar_normal_bg);

        bindingHome.includeBottomBar.ivSetting.setColorFilter(Color.parseColor("#CC3F3D56"));
        bindingHome.includeBottomBar.tvSetting.setTextColor(Color.parseColor("#CC3F3D56"));
        bindingHome.includeBottomBar.fmSetting.setBackgroundResource(R.drawable.bottom_bar_normal_bg);

        bindingHome.tvHomeSeeAllPopular.setOnClickListener(v -> searchProperties());
        bindingHome.tvHomeSeeAllLatest.setOnClickListener(v -> searchProperties());

    }

    private void setupRecyclerViews() {
        popularList = new ArrayList<>();
        latestList = new ArrayList<>();
        searchList = new ArrayList<>();

        popularAdapter = new PopularAdapter(this, popularList,
                id -> showProperty(id),
                id -> addToFavorite(id));

        latestAdapter = new LatestAdapter(this, latestList,
                id -> showProperty(id),
                id -> addToFavorite(id));

        searchAdapter = new SearchAdapter(this, searchList,
                id -> showProperty(id),
                id -> addToFavorite(id));

        recyclerViewPopular = bindingHome.rvPopular;
        recyclerViewPopular.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewPopular.setAdapter(popularAdapter);

        recyclerViewLatest = bindingHome.rvLatest;
        recyclerViewLatest.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewLatest.setAdapter(latestAdapter);
    }

    private void setupSearchButton() {
        bindingHome.topHome.mbHomeSearch.setOnClickListener(v -> searchProperties());
    }

    private void loadPopularProperties() {
        db.collection("properties")
                .orderBy("views", Query.Direction.DESCENDING)
                .limit(7)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    popularList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Property property = doc.toObject(Property.class);
                        if (property != null) {
                            property.setId(doc.getId());
                            popularList.add(property);
                        }
                    }

                    popularAdapter.notifyDataSetChanged();
                    checkLoadingFinished();
                })
                .addOnFailureListener(e -> {
                    checkLoadingFinished();
                });
    }

    private void loadLatestProperties() {
        db.collection("properties")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(7)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    latestList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Property property = doc.toObject(Property.class);
                        if (property != null) {
                            property.setId(doc.getId());
                            latestList.add(property);
                        }
                    }

                    latestAdapter.notifyDataSetChanged();
                    checkLoadingFinished();
                })
                .addOnFailureListener(e -> {
                    checkLoadingFinished();
                });
    }

    private void checkLoadingFinished() {
        loadedCount++;
        if (loadedCount >= 2) {
            bindingHome.progresshome.setVisibility(View.GONE);
        }
    }

    private void searchProperties() {
        String purpose = bindingHome.topHome.spPropertyPurpose.getSelectedItem().toString().trim();
        String category = bindingHome.topHome.spPropertyType.getSelectedItem().toString().trim();
        String location = bindingHome.topHome.spPropertyLoc.getSelectedItem().toString().trim();
        String keyword = bindingHome.topHome.etSearch.getText().toString().trim().toLowerCase();

        if (purpose.equalsIgnoreCase("Buy")) {
            purpose = "Sell";
        }

        Query query = db.collection("properties");

        if (!purpose.equals("Purpose")) {
            query = query.whereEqualTo("purpose", purpose);
        }

        if (!category.equals("Category")) {
            query = query.whereEqualTo("category", category);
        }

        if (!location.equals("Select Location")) {
            query = query.whereEqualTo("location", location);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    searchList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Property property = doc.toObject(Property.class);

                        if (property != null) {
                            property.setId(doc.getId());

                            String title = property.getTitle() == null ? "" : property.getTitle().toLowerCase();

                            if (keyword.isEmpty() || title.contains(keyword)) {
                                searchList.add(property);
                            }
                        }
                    }

                    showSearchResults();
                    searchAdapter.notifyDataSetChanged();

                    if (searchList.isEmpty()) {
                        bindingHome.layState.getRoot().setVisibility(View.VISIBLE);
                    } else {
                        bindingHome.layState.getRoot().setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to search properties", Toast.LENGTH_SHORT).show());
    }

    private void showSearchResults() {
        bindingHome.mcHomeItem.setVisibility(View.GONE);
        bindingHome.rlHomeLatest.setVisibility(View.GONE);
        bindingHome.rvLatest.setVisibility(View.VISIBLE);

        bindingHome.rvLatest.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        bindingHome.rvLatest.setAdapter(searchAdapter);
        bindingHome.rvLatest.setNestedScrollingEnabled(false);
    }

    private void showNormalHomeLists() {
        bindingHome.mcHomeItem.setVisibility(View.VISIBLE);
        bindingHome.rlHomeLatest.setVisibility(View.VISIBLE);
        bindingHome.rvLatest.setVisibility(View.VISIBLE);

        bindingHome.rvLatest.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bindingHome.rvLatest.setAdapter(latestAdapter);
        bindingHome.rvLatest.setNestedScrollingEnabled(false);

        bindingHome.layState.getRoot().setVisibility(View.GONE);
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

    private void setupSpinners() {
        List<String> purposeList = new ArrayList<>();
        purposeList.add("Purpose");
        purposeList.add("Buy");
        purposeList.add("Rent");

        List<String> categoryList = new ArrayList<>();
        categoryList.add("Category");
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

        CustomSpinnerAdapter purposeAdapter = new CustomSpinnerAdapter(this, purposeList, true);
        CustomSpinnerAdapter categoryAdapter = new CustomSpinnerAdapter(this, categoryList, true);
        CustomSpinnerAdapter locationAdapter = new CustomSpinnerAdapter(this, locationList, true);

        bindingHome.topHome.spPropertyPurpose.setAdapter(purposeAdapter);
        bindingHome.topHome.spPropertyType.setAdapter(categoryAdapter);
        bindingHome.topHome.spPropertyLoc.setAdapter(locationAdapter);
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

    public void showLoginRequiredMessage() {
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "You are not even logged in",
                Snackbar.LENGTH_LONG
        );

        snackbar.setDuration(4500);
        snackbar.setAction("Login", v ->
                startActivity(new Intent(HomeActivity.this, LoginActivity.class)));

        snackbar.setTextColor(Color.BLACK);
        snackbar.setActionTextColor(Color.BLACK);

        View view = snackbar.getView();

        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
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

    public void showProperty(String propertyId) {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        sp.edit().putString("propertyId", propertyId).apply();

        startActivity(new Intent(HomeActivity.this, DetailedPropertyActivity.class));
    }

    private void addToFavorite(String propertyId) {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId == null) {
            showLoginRequiredMessage();
            return;
        }

        db.collection("users")
                .document(userId)
                .update("favorite", FieldValue.arrayUnion(propertyId))
                .addOnSuccessListener(unused ->
                        Toast.makeText(HomeActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }
}