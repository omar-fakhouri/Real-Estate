package com.app.activities.Intro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.app.realestateapp.R;

public class Intro2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro2);
    }
    public void goIntro3(View view) {
        Intent intent = new Intent(Intro2Activity.this, Intro3Activity.class);
        startActivity(intent);
    }
}