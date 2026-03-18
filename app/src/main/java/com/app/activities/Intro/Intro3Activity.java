package com.app.activities.Intro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.app.activities.LoginActivity;
import com.app.realestateapp.R;

public class Intro3Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro3);
    }
    public void goLogin(View view) {
        Intent intent = new Intent(Intro3Activity.this, LoginActivity.class);
        startActivity(intent);
    }
}