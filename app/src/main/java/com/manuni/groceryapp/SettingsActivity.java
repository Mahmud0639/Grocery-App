package com.manuni.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.manuni.groceryapp.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;
    private static final String enabledMessaging = "Notifications are enabled";
    private static final String disabledMessaging = "Notifications are disabled";

    private boolean isChecked = false;

    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("SETTINGS_SP",MODE_PRIVATE);
        isChecked = sharedPreferences.getBoolean("FCM_ENABLED",false);//ekhaner false ta holo by default off thakbe switch..ar getBoolean er value tai isChecked er moddhe update hobe..like jodi on kora thake tahole true update hobe..off thakle false update hobe

        if (isChecked){
            binding.notificationStatusTV.setText(enabledMessaging);
        }else {
            binding.notificationStatusTV.setText(disabledMessaging);
        }

        binding.notificationSwitch.setChecked(isChecked);

        binding.backBtn.setOnClickListener(view -> onBackPressed());

        binding.notificationSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked){
                try {
                    subscribeToTopic();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    unSubscribeToTopic();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }
    private void subscribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.TOPICS).addOnSuccessListener(unused -> {
            spEditor = sharedPreferences.edit();
            spEditor.putBoolean("FCM_ENABLED",true);
            spEditor.apply();
            Toast.makeText(SettingsActivity.this, "You will be able to get all the notifications", Toast.LENGTH_SHORT).show();
            binding.notificationStatusTV.setText(enabledMessaging);
        }).addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void unSubscribeToTopic(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.TOPICS).addOnSuccessListener(unused -> {
            spEditor = sharedPreferences.edit();
            spEditor.putBoolean("FCM_ENABLED",false);
            spEditor.apply();
            //Toast.makeText(SettingsActivity.this, ""+disabledMessaging, Toast.LENGTH_SHORT).show();
            Toast.makeText(SettingsActivity.this, "You will not get any notifications", Toast.LENGTH_SHORT).show();
            binding.notificationStatusTV.setText(disabledMessaging);
        }).addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}