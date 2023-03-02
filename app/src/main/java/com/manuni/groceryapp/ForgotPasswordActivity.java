package com.manuni.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.manuni.groceryapp.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {
    ActivityForgotPasswordBinding binding;
    private ProgressDialog dialog;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);

        binding.backArrowBtn.setOnClickListener(view -> onBackPressed());

        binding.recoverBtn.setOnClickListener(view -> recoverPassword());


    }
    private String email;
    private void recoverPassword(){

        email = binding.emailET.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email Address!", Toast.LENGTH_SHORT).show();
            return;
        }

        dialog.setMessage("Sending instruction to reset password...");
        dialog.show();
        auth.sendPasswordResetEmail(email).addOnSuccessListener(unused -> {
            dialog.dismiss();
            Toast.makeText(ForgotPasswordActivity.this, "Password reset instruction is sent...", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(ForgotPasswordActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}