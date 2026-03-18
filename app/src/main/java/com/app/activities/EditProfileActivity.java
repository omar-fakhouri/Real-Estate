package com.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.app.realestateapp.databinding.ActivityEditProfileBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileActivity extends AppCompatActivity {

    ActivityEditProfileBinding bindingEditProfile;
    FirebaseFirestore db;

    FirebaseStorage storage;
    Uri imageUri;

    ActivityResultLauncher<String> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindingEditProfile = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(bindingEditProfile.getRoot());

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        bindingEditProfile.fabBack.setOnClickListener(v -> finish());

        bindingEditProfile.ForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(EditProfileActivity.this, ForgotPasswordActivity.class)));

        bindingEditProfile.mbUpdate.setOnClickListener(v -> Update());

        loadUserData();
        setupImagePicker();
    }

    private void loadUserData() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    String name = document.getString("name");
                    String email = document.getString("email");
                    String phone = document.getString("phone");
                    String profileImage = document.getString("profileImage");

                    bindingEditProfile.tvProfileUserName.setText(name);
                    bindingEditProfile.tvProfileEmail.setText(email);

                    bindingEditProfile.etName.setText(name);
                    bindingEditProfile.etEmail.setText(email);
                    bindingEditProfile.etPhone.setText(phone);

                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(this)
                                .load(profileImage)
                                .into(bindingEditProfile.ivUser);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private void setupImagePicker() {
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        bindingEditProfile.ivUser.setImageURI(uri);
                        uploadProfileImage(uri);
                    }
                }
        );

        bindingEditProfile.ivEditIcon.setOnClickListener(v ->
                imagePicker.launch("image/*"));
    }

    public void Update() {
        String name = bindingEditProfile.etName.getText().toString().trim();
        String email = bindingEditProfile.etEmail.getText().toString().trim();
        String phone = bindingEditProfile.etPhone.getText().toString().trim();

        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .update(
                        "name", name,
                        "email", email,
                        "phone", phone
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile successfully updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show());
    }

    public void uploadProfileImage(Uri uri) {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference ref = storage.getReference()
                .child("profileImages")
                .child(userId + ".jpg");

        ref.putFile(uri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    String imageUrl = downloadUri.toString();

                    db.collection("users")
                            .document(userId)
                            .update("profileImage", imageUrl)
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "Image updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }
}