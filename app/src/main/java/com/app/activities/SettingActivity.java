package com.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.realestateapp.R;
import com.app.realestateapp.databinding.ActivitySettingBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingActivity extends AppCompatActivity {

    ActivitySettingBinding bindingSetting;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindingSetting = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(bindingSetting.getRoot());

        db = FirebaseFirestore.getInstance();

        setupClicks();
        setupBottomBar();
    }

    private void setupClicks() {
        bindingSetting.rlAbout.setOnClickListener(v -> showAbout());
        bindingSetting.rlShareApp.setOnClickListener(v -> shareApp());
        bindingSetting.rlLogOut.setOnClickListener(v -> logout());
        bindingSetting.rlDelete.setOnClickListener(v -> delete());
    }

    private void setupBottomBar() {
        bindingSetting.includeBottomBar.llHome.setOnClickListener(v ->
                startActivity(new Intent(SettingActivity.this, HomeActivity.class)));
        bindingSetting.includeBottomBar.ivHome.setColorFilter(Color.parseColor("#CC3F3D56"));
        bindingSetting.includeBottomBar.tvHome.setTextColor(Color.parseColor("#CC3F3D56"));

        bindingSetting.includeBottomBar.llLatest.setOnClickListener(v ->
                startActivity(new Intent(SettingActivity.this, SearchActivity.class)));
        bindingSetting.includeBottomBar.ivSearch.setColorFilter(Color.parseColor("#CC3F3D56"));
        bindingSetting.includeBottomBar.tvSearch.setTextColor(Color.parseColor("#CC3F3D56"));

        bindingSetting.includeBottomBar.llProperty.setOnClickListener(v ->
                startActivity(new Intent(SettingActivity.this, MyPropertyActivity.class)));
        bindingSetting.includeBottomBar.ivProperty.setColorFilter(Color.parseColor("#CC3F3D56"));
        bindingSetting.includeBottomBar.tvProperty.setTextColor(Color.parseColor("#CC3F3D56"));

        bindingSetting.includeBottomBar.llSetting.setOnClickListener(v ->
                startActivity(new Intent(SettingActivity.this, SettingActivity.class)));
        bindingSetting.includeBottomBar.ivSetting.setColorFilter(Color.parseColor("#7F56D9"));
        bindingSetting.includeBottomBar.tvSetting.setTextColor(Color.parseColor("#7F56D9"));
    }

    public void showAbout() {
        startActivity(new Intent(SettingActivity.this, AboutActivity.class));
    }

    public void shareApp() {
        String message = "Check out this real estate app, it is called real estate on google play!";

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

    public void delete() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "You don't even have an account", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.layout_delete);

        View btnYes = dialog.findViewById(R.id.mbYes);
        View btnCancel = dialog.findViewById(R.id.mbCancel);

        if (btnYes != null) {
            btnYes.setOnClickListener(v -> {
                db.collection("properties")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {

                            if (queryDocumentSnapshots.isEmpty()) {
                                deleteUserAccount(userId, sp, dialog);
                                return;
                            }

                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                db.collection("properties").document(doc.getId()).delete();
                            }

                            deleteUserAccount(userId, sp, dialog);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to delete properties", Toast.LENGTH_SHORT).show()
                        );
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }
    private void deleteUserAccount(String userId, SharedPreferences sp, BottomSheetDialog dialog) {
        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(unused -> {
                    sp.edit().remove("userId").apply();

                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, RegisterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    dialog.dismiss();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
                );
    }
}