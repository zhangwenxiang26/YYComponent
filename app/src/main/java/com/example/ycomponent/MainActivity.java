package com.example.ycomponent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.ycomponent.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding vb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ActivityMainBinding.inflate(getLayoutInflater());
    }
}