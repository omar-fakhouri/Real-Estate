package com.app.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.app.realestateapp.R;
import com.app.realestateapp.databinding.ActivityAboutBinding;


public class AboutActivity extends AppCompatActivity {

    ActivityAboutBinding bindingAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindingAbout = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(bindingAbout.getRoot());

        bindingAbout.tvCompany.setText("Omar Fakhouri");
        bindingAbout.tvEmail.setText("omarfakhouri2802@gmail.com");
        bindingAbout.tvContact.setText("0528049193");

        findViewById(R.id.fabBack).setOnClickListener(v ->
                startActivity(new Intent(AboutActivity.this, SettingActivity.class)));

    }
}