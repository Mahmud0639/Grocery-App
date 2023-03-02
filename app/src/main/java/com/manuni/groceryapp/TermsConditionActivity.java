package com.manuni.groceryapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.manuni.groceryapp.databinding.ActivityTermsConditionBinding;

public class TermsConditionActivity extends AppCompatActivity {
    ActivityTermsConditionBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTermsConditionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




       binding.gotoNextBtn.setOnClickListener(view -> {

           try {
               startActivity(new Intent(TermsConditionActivity.this,RegisterSellerActivity.class));
           } catch (Exception e) {
               e.printStackTrace();
           }
           // checkConnection();
       });


       binding.termsCheck.setOnCheckedChangeListener((compoundButton, b) -> binding.gotoNextBtn.setEnabled(b));




    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}