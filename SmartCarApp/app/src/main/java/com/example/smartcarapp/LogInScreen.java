package com.example.smartcarapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LogInScreen extends AppCompatActivity implements View.OnClickListener {
    Button button;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        button = findViewById(R.id.login);

    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);}
    }



