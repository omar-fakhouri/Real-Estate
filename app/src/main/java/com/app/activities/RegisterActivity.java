package com.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.app.realestateapp.databinding.ActivityRegisterBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding bindingRegister;
    FirebaseFirestore db;

    FirebaseStorage storage;
    Uri imageUri;
    ActivityResultLauncher<String> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindingRegister = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(bindingRegister.getRoot());

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        bindingRegister.mbRegister.setOnClickListener(v -> register());

        bindingRegister.tvLogin.setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        imageUri = null;

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        bindingRegister.ivUser.setImageURI(uri);
                    }
                }
        );

        bindingRegister.ivEditIcon.setOnClickListener(v ->
                imagePicker.launch("image/*"));
    }

    private void register() {
        String name = bindingRegister.etName.getText().toString().trim();
        String email = bindingRegister.etEmail.getText().toString().trim();
        String password = bindingRegister.etPassword.getText().toString().trim();
        String phone = bindingRegister.etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            bindingRegister.etName.setError("Enter name");
            return;
        }

        if (email.isEmpty()) {
            bindingRegister.etEmail.setError("Enter email");
            return;
        }

        if (password.isEmpty()) {
            bindingRegister.etPassword.setError("Enter password");
            return;
        }

        if (phone.isEmpty()) {
            bindingRegister.etPhone.setError("Enter phone");
            return;
        }

        bindingRegister.circle.setVisibility(View.VISIBLE);
        bindingRegister.mbRegister.setEnabled(false);

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("password", password);
        user.put("phone", phone);
        user.put("favorite", new ArrayList<String>());

        if (imageUri == null) {
            user.put("profileImage", null);
            saveUser(user);
        } else {
            uploadProfileImage(user);
        }
    }

    private void uploadProfileImage(Map<String, Object> user) {
        StorageReference ref = storage.getReference()
                .child("profile_images")
                .child(System.currentTimeMillis() + ".jpg");

        ref.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    user.put("profileImage", uri.toString());
                    saveUser(user);
                })
                .addOnFailureListener(e -> {
                    bindingRegister.circle.setVisibility(View.GONE);
                    bindingRegister.mbRegister.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveUser(Map<String, Object> user) {
        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    bindingRegister.circle.setVisibility(View.GONE);
                    bindingRegister.mbRegister.setEnabled(true);
                    Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    bindingRegister.circle.setVisibility(View.GONE);
                    bindingRegister.mbRegister.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}