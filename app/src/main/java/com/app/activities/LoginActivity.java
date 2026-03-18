package com.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.realestateapp.databinding.ActivityLoginBinding;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding bindingLogin;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindingLogin = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(bindingLogin.getRoot());

        db = FirebaseFirestore.getInstance();

        bindingLogin.tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        bindingLogin.btnSkip.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        });

        bindingLogin.mbLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String email = bindingLogin.etEmail.getText().toString().trim();
        String password = bindingLogin.etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            bindingLogin.etEmail.setError("Enter email");
            return;
        }

        if (password.isEmpty()) {
            bindingLogin.etPassword.setError("Enter password");
            return;
        }

        db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String userId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
                        sp.edit().putString("userId", userId).apply();

                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Wrong email or password", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show());
    }
}