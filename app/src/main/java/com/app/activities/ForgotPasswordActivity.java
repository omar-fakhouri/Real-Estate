package com.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.realestateapp.R;
import com.app.realestateapp.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotPasswordActivity extends AppCompatActivity {

    ActivityForgotPasswordBinding bindingForgotPassword;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindingForgotPassword = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(bindingForgotPassword.getRoot());

        db = FirebaseFirestore.getInstance();

        bindingForgotPassword.Update.setOnClickListener(v -> ForgotPassword());

        bindingForgotPassword.getRoot()
                .findViewById(R.id.fabBack)
                .setOnClickListener(v -> finish());
    }

    public void ForgotPassword() {
        String password = bindingForgotPassword.etPassword.getText().toString().trim();
        String password2 = bindingForgotPassword.etPassword2.getText().toString().trim();

        if (password.isEmpty() || password2.isEmpty()) {
            Toast.makeText(this, "Enter password in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(password2)) {
            Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .update("password", password)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Password successfully updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ForgotPasswordActivity.this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                });
    }
}