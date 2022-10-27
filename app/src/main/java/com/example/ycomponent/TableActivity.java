package com.example.ycomponent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.ycomponent.databinding.ActivityTableBinding;

public class TableActivity extends AppCompatActivity {

    private ActivityTableBinding vb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ActivityTableBinding.inflate(getLayoutInflater());
    }
}