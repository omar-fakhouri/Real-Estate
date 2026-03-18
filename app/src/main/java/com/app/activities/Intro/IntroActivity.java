package com.app.activities.Intro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.app.realestateapp.R;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro); // this connects intro.xml
    }

    public void goIntro2(View view) {
        Intent intent = new Intent(IntroActivity.this, Intro2Activity.class);
        startActivity(intent);
    }

}