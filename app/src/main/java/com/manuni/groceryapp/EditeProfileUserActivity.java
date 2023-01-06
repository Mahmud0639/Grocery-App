package com.manuni.groceryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.manuni.groceryapp.databinding.ActivityEditeProfileUserBinding;

public class EditeProfileUserActivity extends AppCompatActivity {
    ActivityEditeProfileUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditeProfileUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

            binding.backArrowBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

            binding.gpsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            binding.updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
    }
}