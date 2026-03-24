package com.app.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.adapters.SearchAdapter;
import com.app.models.Property;
import com.app.realestateapp.R;
import com.app.realestateapp.databinding.ActivitySearchBinding;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    ActivitySearchBinding bindingSearch;
    FirebaseFirestore db;

    View llBed;
    View llBath;
    View llFurn;
    View llPriceRange;
    View llPropertyType;
    View llPropertyPurpose;
    View llPropertyLoc;

    RecyclerView recyclerView;
    List<Property> searchList;
    SearchAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindingSearch = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(bindingSearch.getRoot());

        db = FirebaseFirestore.getInstance();

        llBed = findViewById(R.id.llBed);
        llBath = findViewById(R.id.llBath);
        llFurn = findViewById(R.id.llFurn);
        llPriceRange = findViewById(R.id.llPriceRange);
        llPropertyType = findViewById(R.id.llPropertyType);
        llPropertyPurpose = findViewById(R.id.llPropertyPurpose);
        llPropertyLoc = findViewById(R.id.llPropertyLoc);

        recyclerView = findViewById(R.id.rvSearchResult);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchList = new ArrayList<>();

        searchAdapter = new SearchAdapter(
                this,
                searchList,
                id -> showProperty(id),
                id -> addToFavorite(id)
        );

        recyclerView.setAdapter(searchAdapter);

        setupMenuClicks();
        setupPriceSlider();
        setupButtons();
        setupBottomBar();

        showSection(llBed);
        selectMenu(bindingSearch.tvBedRoom);
    }

    private void setupMenuClicks() {
        bindingSearch.tvBedRoom.setOnClickListener(v -> {
            showSection(llBed);
            selectMenu(bindingSearch.tvBedRoom);
        });

        bindingSearch.tvBathRoom.setOnClickListener(v -> {
            showSection(llBath);
            selectMenu(bindingSearch.tvBathRoom);
        });

        bindingSearch.tvFurnishing.setOnClickListener(v -> {
            showSection(llFurn);
            selectMenu(bindingSearch.tvFurnishing);
        });

        bindingSearch.tvRange.setOnClickListener(v -> {
            showSection(llPriceRange);
            selectMenu(bindingSearch.tvRange);
        });

        bindingSearch.tvTyeCat.setOnClickListener(v -> {
            showSection(llPropertyType);
            selectMenu(bindingSearch.tvTyeCat);
        });

        bindingSearch.tvPur.setOnClickListener(v -> {
            showSection(llPropertyPurpose);
            selectMenu(bindingSearch.tvPur);
        });

        bindingSearch.tvLoc.setOnClickListener(v -> {
            showSection(llPropertyLoc);
            selectMenu(bindingSearch.tvLoc);
        });
    }

    private void setupPriceSlider() {
        RangeSlider rangeSlider = findViewById(R.id.rangeSlider);
        TextView tvRangeValue = findViewById(R.id.tvRangeValue);

        if (rangeSlider != null && tvRangeValue != null) {
            rangeSlider.addOnChangeListener((slider, value, fromUser) -> {
                List<Float> values = slider.getValues();
                int min = values.get(0).intValue();
                int max = values.get(1).intValue();
                tvRangeValue.setText(min + " ₪ - " + max + " ₪");
            });
        }
    }

    private void setupButtons() {
        bindingSearch.btnClearFilter.setOnClickListener(v -> clearAllFilters());

        bindingSearch.btnApplyFilter.setOnClickListener(v -> applyFilters());

    }

    private void applyFilters() {
        String selectedBed = getSelectedBeds();
        String selectedBath = getSelectedBaths();
        String selectedFurnishing = getSelectedFurnishing();
        String selectedType = getSelectedType();
        String selectedPurpose = getSelectedPurpose();
        String selectedLocation = getSelectedLocation();

        RangeSlider rangeSlider = findViewById(R.id.rangeSlider);
        int minPrice = 0;
        int maxPrice = 5000000;

        if (rangeSlider != null) {
            minPrice = rangeSlider.getValues().get(0).intValue();
            maxPrice = rangeSlider.getValues().get(1).intValue();
        }

        int finalMinPrice = minPrice;
        int finalMaxPrice = maxPrice;

        db.collection("properties")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    searchList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Property property = doc.toObject(Property.class);

                        if (property == null) {
                            continue;
                        }

                        property.setId(doc.getId());

                        if (!selectedBed.isEmpty()) {
                            if (property.getBed() == null || !property.getBed().equals(selectedBed)) {
                                continue;
                            }
                        }

                        if (!selectedBath.isEmpty() && !selectedBath.equals("any")) {
                            if (property.getBath() == null || !property.getBath().equals(selectedBath)) {
                                continue;
                            }
                        }

                        if (!selectedFurnishing.isEmpty()) {
                            if (property.getFurnished() == null || !property.getFurnished().equalsIgnoreCase(selectedFurnishing)) {
                                continue;
                            }
                        }

                        if (!selectedType.isEmpty()) {
                            if (property.getCategory() == null || !property.getCategory().equalsIgnoreCase(selectedType)) {
                                continue;
                            }
                        }

                        if (!selectedPurpose.isEmpty()) {
                            if (property.getPurpose() == null || !property.getPurpose().equalsIgnoreCase(selectedPurpose)) {
                                continue;
                            }
                        }

                        if (!selectedLocation.isEmpty()) {
                            if (property.getLocation() == null || !property.getLocation().equalsIgnoreCase(selectedLocation)) {
                                continue;
                            }
                        }

                        int propertyPrice = 0;
                        try {
                            propertyPrice = Integer.parseInt(property.getPrice());
                        } catch (Exception e) {
                            propertyPrice = 0;
                        }

                        if (propertyPrice < finalMinPrice || propertyPrice > finalMaxPrice) {
                            continue;
                        }

                        searchList.add(property);
                    }

                    bindingSearch.llFilterContent.setVisibility(View.GONE);
                    bindingSearch.rvSearchResult.setVisibility(View.VISIBLE);

                    searchAdapter.notifyDataSetChanged();

                    if (searchList.isEmpty()) {
                        Toast.makeText(this, "No properties found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    bindingSearch.ibLocation.setVisibility(View.VISIBLE);
                    bindingSearch.ibLocation.setOnClickListener(v -> {
                        MapActivity.propertyList = new ArrayList<>(searchList);
                        startActivity(new Intent(SearchActivity.this, MapActivity.class));
                    });

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load properties", Toast.LENGTH_SHORT).show()
                );
    }

    private void showSection(View section) {
        llBed.setVisibility(View.GONE);
        llBath.setVisibility(View.GONE);
        llFurn.setVisibility(View.GONE);
        llPriceRange.setVisibility(View.GONE);
        llPropertyType.setVisibility(View.GONE);
        llPropertyPurpose.setVisibility(View.GONE);
        llPropertyLoc.setVisibility(View.GONE);

        section.setVisibility(View.VISIBLE);
    }

    private void selectMenu(TextView selected) {
        resetMenu(bindingSearch.tvBedRoom);
        resetMenu(bindingSearch.tvBathRoom);
        resetMenu(bindingSearch.tvFurnishing);
        resetMenu(bindingSearch.tvRange);
        resetMenu(bindingSearch.tvTyeCat);
        resetMenu(bindingSearch.tvPur);
        resetMenu(bindingSearch.tvLoc);

        selected.setBackgroundColor(getColor(R.color.app_purple_bg));
        selected.setTextColor(getColor(R.color.white2));
    }

    private void resetMenu(TextView tv) {
        tv.setBackgroundColor(getColor(R.color.white_bg));
        tv.setTextColor(getColor(R.color.gray));
    }

    private void clearAllFilters() {
        int[] checkIds = {
                R.id.cbBed1, R.id.cbBed2, R.id.cbBed3, R.id.cbBed4,
                R.id.cbBathAny, R.id.cbBath1, R.id.cbBath2, R.id.cbBath3,
                R.id.cbFur, R.id.cbSemi, R.id.cbUnFur,
                R.id.cbTypeApartment, R.id.cbTypeCommercial, R.id.cbTypeHouse, R.id.cbTypeIndustrial, R.id.cbTypeLand,
                R.id.cbPurposeSell, R.id.cbPurposeRent,
                R.id.cbLocJerusalem, R.id.cbLocTelAviv, R.id.cbLocHaifa, R.id.cbLocAshdod,
                R.id.cbLocNetanya, R.id.cbLocBeerSheva, R.id.cbLocPetahTikva, R.id.cbLocHolon,
                R.id.cbLocBneiBrak, R.id.cbLocRamatGan, R.id.cbLocAshkelon, R.id.cbLocRehovot,
                R.id.cbLocBatYam, R.id.cbLocRishon, R.id.cbLocKfarSaba, R.id.cbLocHadera,
                R.id.cbLocNazareth, R.id.cbLocUmmAlFahm, R.id.cbLocSafed, R.id.cbLocTiberias,
                R.id.cbLocEilat, R.id.cbLocAcre, R.id.cbLocNazarethIllit, R.id.cbLocModiin,
                R.id.cbLocBeitShemesh, R.id.cbLocLod, R.id.cbLocRamla
        };

        for (int id : checkIds) {
            CheckBox cb = findViewById(id);
            if (cb != null) {
                cb.setChecked(false);
            }
        }

        RangeSlider rangeSlider = findViewById(R.id.rangeSlider);
        TextView tvRangeValue = findViewById(R.id.tvRangeValue);

        if (rangeSlider != null) {
            rangeSlider.setValues(0f, 5000000f);
        }

        if (tvRangeValue != null) {
            tvRangeValue.setText("0 ₪ - 5000000 ₪");
        }

        bindingSearch.rvSearchResult.setVisibility(View.GONE);
        bindingSearch.llFilterContent.setVisibility(View.VISIBLE);

        showSection(llBed);
        selectMenu(bindingSearch.tvBedRoom);

        bindingSearch.ibLocation.setVisibility(View.GONE);
    }

    private String getSelectedBeds() {
        CheckBox cbBed1 = findViewById(R.id.cbBed1);
        CheckBox cbBed2 = findViewById(R.id.cbBed2);
        CheckBox cbBed3 = findViewById(R.id.cbBed3);
        CheckBox cbBed4 = findViewById(R.id.cbBed4);

        if (cbBed1 != null && cbBed1.isChecked()) return "1";
        if (cbBed2 != null && cbBed2.isChecked()) return "2";
        if (cbBed3 != null && cbBed3.isChecked()) return "3";
        if (cbBed4 != null && cbBed4.isChecked()) return "4";
        return "";
    }

    private String getSelectedBaths() {
        CheckBox cbBathAny = findViewById(R.id.cbBathAny);
        CheckBox cbBath1 = findViewById(R.id.cbBath1);
        CheckBox cbBath2 = findViewById(R.id.cbBath2);
        CheckBox cbBath3 = findViewById(R.id.cbBath3);

        if (cbBathAny != null && cbBathAny.isChecked()) return "any";
        if (cbBath1 != null && cbBath1.isChecked()) return "1";
        if (cbBath2 != null && cbBath2.isChecked()) return "2";
        if (cbBath3 != null && cbBath3.isChecked()) return "3";
        return "";
    }

    private String getSelectedFurnishing() {
        CheckBox cbFur = findViewById(R.id.cbFur);
        CheckBox cbSemi = findViewById(R.id.cbSemi);
        CheckBox cbUnFur = findViewById(R.id.cbUnFur);

        if (cbFur != null && cbFur.isChecked()) return "Furnished";
        if (cbSemi != null && cbSemi.isChecked()) return "Semi Furnished";
        if (cbUnFur != null && cbUnFur.isChecked()) return "Unfurnished";
        return "";
    }

    private String getSelectedType() {
        CheckBox cbTypeApartment = findViewById(R.id.cbTypeApartment);
        CheckBox cbTypeCommercial = findViewById(R.id.cbTypeCommercial);
        CheckBox cbTypeHouse = findViewById(R.id.cbTypeHouse);
        CheckBox cbTypeIndustrial = findViewById(R.id.cbTypeIndustrial);
        CheckBox cbTypeLand = findViewById(R.id.cbTypeLand);

        if (cbTypeApartment != null && cbTypeApartment.isChecked()) return "Apartment";
        if (cbTypeCommercial != null && cbTypeCommercial.isChecked()) return "Commercial";
        if (cbTypeHouse != null && cbTypeHouse.isChecked()) return "House";
        if (cbTypeIndustrial != null && cbTypeIndustrial.isChecked()) return "Industrial";
        if (cbTypeLand != null && cbTypeLand.isChecked()) return "Land";
        return "";
    }

    private String getSelectedPurpose() {
        CheckBox cbPurposeSell = findViewById(R.id.cbPurposeSell);
        CheckBox cbPurposeRent = findViewById(R.id.cbPurposeRent);

        if (cbPurposeSell != null && cbPurposeSell.isChecked()) return "Sell";
        if (cbPurposeRent != null && cbPurposeRent.isChecked()) return "Rent";
        return "";
    }

    private String getSelectedLocation() {
        CheckBox cbLocJerusalem = findViewById(R.id.cbLocJerusalem);
        CheckBox cbLocTelAviv = findViewById(R.id.cbLocTelAviv);
        CheckBox cbLocHaifa = findViewById(R.id.cbLocHaifa);
        CheckBox cbLocAshdod = findViewById(R.id.cbLocAshdod);
        CheckBox cbLocNetanya = findViewById(R.id.cbLocNetanya);
        CheckBox cbLocBeerSheva = findViewById(R.id.cbLocBeerSheva);
        CheckBox cbLocPetahTikva = findViewById(R.id.cbLocPetahTikva);
        CheckBox cbLocHolon = findViewById(R.id.cbLocHolon);
        CheckBox cbLocBneiBrak = findViewById(R.id.cbLocBneiBrak);
        CheckBox cbLocRamatGan = findViewById(R.id.cbLocRamatGan);
        CheckBox cbLocAshkelon = findViewById(R.id.cbLocAshkelon);
        CheckBox cbLocRehovot = findViewById(R.id.cbLocRehovot);
        CheckBox cbLocBatYam = findViewById(R.id.cbLocBatYam);
        CheckBox cbLocRishon = findViewById(R.id.cbLocRishon);
        CheckBox cbLocKfarSaba = findViewById(R.id.cbLocKfarSaba);
        CheckBox cbLocHadera = findViewById(R.id.cbLocHadera);
        CheckBox cbLocNazareth = findViewById(R.id.cbLocNazareth);
        CheckBox cbLocUmmAlFahm = findViewById(R.id.cbLocUmmAlFahm);
        CheckBox cbLocSafed = findViewById(R.id.cbLocSafed);
        CheckBox cbLocTiberias = findViewById(R.id.cbLocTiberias);
        CheckBox cbLocEilat = findViewById(R.id.cbLocEilat);
        CheckBox cbLocAcre = findViewById(R.id.cbLocAcre);
        CheckBox cbLocNazarethIllit = findViewById(R.id.cbLocNazarethIllit);
        CheckBox cbLocModiin = findViewById(R.id.cbLocModiin);
        CheckBox cbLocBeitShemesh = findViewById(R.id.cbLocBeitShemesh);
        CheckBox cbLocLod = findViewById(R.id.cbLocLod);
        CheckBox cbLocRamla = findViewById(R.id.cbLocRamla);

        if (cbLocJerusalem != null && cbLocJerusalem.isChecked()) return "Jerusalem";
        if (cbLocTelAviv != null && cbLocTelAviv.isChecked()) return "Tel Aviv";
        if (cbLocHaifa != null && cbLocHaifa.isChecked()) return "Haifa";
        if (cbLocAshdod != null && cbLocAshdod.isChecked()) return "Ashdod";
        if (cbLocNetanya != null && cbLocNetanya.isChecked()) return "Netanya";
        if (cbLocBeerSheva != null && cbLocBeerSheva.isChecked()) return "Beer Sheva";
        if (cbLocPetahTikva != null && cbLocPetahTikva.isChecked()) return "Petah Tikva";
        if (cbLocHolon != null && cbLocHolon.isChecked()) return "Holon";
        if (cbLocBneiBrak != null && cbLocBneiBrak.isChecked()) return "Bnei Brak";
        if (cbLocRamatGan != null && cbLocRamatGan.isChecked()) return "Ramat Gan";
        if (cbLocAshkelon != null && cbLocAshkelon.isChecked()) return "Ashkelon";
        if (cbLocRehovot != null && cbLocRehovot.isChecked()) return "Rehovot";
        if (cbLocBatYam != null && cbLocBatYam.isChecked()) return "Bat Yam";
        if (cbLocRishon != null && cbLocRishon.isChecked()) return "Rishon LeZion";
        if (cbLocKfarSaba != null && cbLocKfarSaba.isChecked()) return "Kfar Saba";
        if (cbLocHadera != null && cbLocHadera.isChecked()) return "Hadera";
        if (cbLocNazareth != null && cbLocNazareth.isChecked()) return "Nazareth";
        if (cbLocUmmAlFahm != null && cbLocUmmAlFahm.isChecked()) return "Umm al-Fahm";
        if (cbLocSafed != null && cbLocSafed.isChecked()) return "Safed";
        if (cbLocTiberias != null && cbLocTiberias.isChecked()) return "Tiberias";
        if (cbLocEilat != null && cbLocEilat.isChecked()) return "Eilat";
        if (cbLocAcre != null && cbLocAcre.isChecked()) return "Acre";
        if (cbLocNazarethIllit != null && cbLocNazarethIllit.isChecked()) return "Nazareth Illit";
        if (cbLocModiin != null && cbLocModiin.isChecked()) return "Modiin";
        if (cbLocBeitShemesh != null && cbLocBeitShemesh.isChecked()) return "Beit Shemesh";
        if (cbLocLod != null && cbLocLod.isChecked()) return "Lod";
        if (cbLocRamla != null && cbLocRamla.isChecked()) return "Ramla";

        return "";
    }
    public void showProperty(String propertyId) {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        sp.edit().putString("propertyId", propertyId).apply();
        startActivity(new Intent(SearchActivity.this, DetailedPropertyActivity.class));
    }

    private void addToFavorite(String propertyId) {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "You are not even logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SearchActivity.this, LoginActivity.class));
            return;
        }

        db.collection("users")
                .document(userId)
                .update("favorite", FieldValue.arrayUnion(propertyId))
                .addOnSuccessListener(unused ->
                        Toast.makeText(SearchActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void setupBottomBar() {
        bindingSearch.includeBottomBar.llHome.setOnClickListener(v ->
                startActivity(new Intent(SearchActivity.this, HomeActivity.class)));

        bindingSearch.includeBottomBar.llLatest.setOnClickListener(v ->
                startActivity(new Intent(SearchActivity.this, SearchActivity.class)));

        bindingSearch.includeBottomBar.llProperty.setOnClickListener(v ->
                startActivity(new Intent(SearchActivity.this, MyPropertyActivity.class)));

        bindingSearch.includeBottomBar.llSetting.setOnClickListener(v ->
                startActivity(new Intent(SearchActivity.this, SettingActivity.class)));

        bindingSearch.includeBottomBar.ivHome.setColorFilter(Color.parseColor("#CC3F3D56"));
        bindingSearch.includeBottomBar.tvHome.setTextColor(Color.parseColor("#CC3F3D56"));
        bindingSearch.includeBottomBar.fmHome.setBackgroundResource(R.drawable.bottom_bar_normal_bg);

        bindingSearch.includeBottomBar.ivSearch.setColorFilter(Color.parseColor("#7F56D9"));
        bindingSearch.includeBottomBar.tvSearch.setTextColor(Color.parseColor("#7F56D9"));
        bindingSearch.includeBottomBar.fmSea.setBackgroundResource(R.drawable.bottom_bar_select_bg);

        bindingSearch.includeBottomBar.ivProperty.setColorFilter(Color.parseColor("#CC3F3D56"));
        bindingSearch.includeBottomBar.tvProperty.setTextColor(Color.parseColor("#CC3F3D56"));
        bindingSearch.includeBottomBar.fmProperty.setBackgroundResource(R.drawable.bottom_bar_normal_bg);

        bindingSearch.includeBottomBar.ivSetting.setColorFilter(Color.parseColor("#CC3F3D56"));
        bindingSearch.includeBottomBar.tvSetting.setTextColor(Color.parseColor("#CC3F3D56"));
        bindingSearch.includeBottomBar.fmSetting.setBackgroundResource(R.drawable.bottom_bar_normal_bg);

    }



}