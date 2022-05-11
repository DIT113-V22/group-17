package com.example.smartcarapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

public class Settings extends AppCompatActivity {
    SwitchCompat switchCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_Dark);
        } else {
            setTheme(R.style.Theme_Light);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
    }

    public void mainActivity(View view) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}