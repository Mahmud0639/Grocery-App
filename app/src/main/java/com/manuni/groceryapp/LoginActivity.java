package com.manuni.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivityLoginBinding;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private ProgressDialog dialog;

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");


        dialog = new ProgressDialog(LoginActivity.this);
        auth = FirebaseAuth.getInstance();
        binding.notHaveAccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterUserActivity.class));
            }
        });

        binding.forgotTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
            }
        });

        binding.emailET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               if (!Patterns.EMAIL_ADDRESS.matcher(charSequence).matches()){
                   binding.textInputEmail.setHelperText("Email Patterns doesn't matched yet.");
                   binding.textInputEmail.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.red_deep)));
               }else {
                   binding.textInputEmail.setHelperText("Email Patterns matched!");
                   binding.textInputEmail.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
               }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.passET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()>=6){
                    Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
                    Matcher matcher = pattern.matcher(charSequence);
                    boolean isPwdContains = matcher.find();
                    if (isPwdContains){
                        binding.textInputPass.setHelperText("Strong Password");
                        binding.textInputPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
                        binding.textInputPass.setError("");

                    }else {
                        binding.textInputPass.setHelperText("");
                        binding.textInputPass.setError("Weak Password.Include minimum 1 special char.");

                    }
                } else{
                    binding.textInputPass.setHelperText("Enter Minimum 6 char.");
                    binding.textInputPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.red_deep)));
                    binding.textInputPass.setError("");

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager manager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                if (wifi.isConnected()){
                    login();
                }else if (mobile.isConnected()){
                    login();
                }else {
                    Toast.makeText(LoginActivity.this, "No internet", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }


    private String email,password;
    private void login(){
        dialog.setTitle("Please wait");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setMessage("Logging in...");



        if (!validateEmail() | !validatePassword()){
            return;
        }
//
//        email = binding.emailET.getText().toString().trim();
//        password = binding.passET.getText().toString().trim();
//
//        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
//            if (email.isEmpty()){
//                binding.textInputEmail.setError("Empty");
//            }else {
//                binding.textInputEmail.setError(null);
//            }
//            //Toast.makeText(this, "Invalid email address!", Toast.LENGTH_SHORT).show();
//            binding.textInputEmail.setError("Field can't be empty");
//            return;
//        }else if (password.length()<6){
//           // Toast.makeText(this, "Password should be at least 6 characters!", Toast.LENGTH_SHORT).show();
//            binding.textInputPass.setError("Password should be at least 6 characters");
//            return;
//        }else {
            dialog.show();
            binding.textInputEmail.setError(null);
            binding.textInputPass.setError(null);
            //binding.emailET.setText("");
            //binding.passET.setText("");
           // binding.textInputPass.setHelperText("");
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    dialog.dismiss();
                    makeMeOnline();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        //}





    }

    private void makeMeOnline() {
        dialog.setMessage("Checking user...");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","true");

        dbRef.child(auth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                checkUserType();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }
    private void checkUserType(){
        dbRef.orderByChild("uid").equalTo(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                  String accountType = ""+dataSnapshot.child("accountType").getValue();
                  if (accountType.equals("Seller")){
                      DatabaseReference myReference = FirebaseDatabase.getInstance().getReference().child("Users");

                      myReference.child(auth.getUid()).addValueEventListener(new ValueEventListener() {
                          @Override
                          public void onDataChange(@NonNull DataSnapshot snapshot) {
                              if (snapshot.exists()){
                                  String status = ""+snapshot.child("accountStatus").getValue();
                                  if (status.equals("blocked")){
                                      HashMap<String,Object> hashMap = new HashMap<>();
                                      hashMap.put("online","false");

                                      dbRef.child(auth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                          @Override
                                          public void onSuccess(Void unused) {
                                              //checkUserType();
                                              Toast.makeText(LoginActivity.this, "You are now in offline", Toast.LENGTH_SHORT).show();

                                          }
                                      }).addOnFailureListener(new OnFailureListener() {
                                          @Override
                                          public void onFailure(@NonNull Exception e) {

                                          }
                                      });
                                      Toast.makeText(LoginActivity.this, "You are blocked.Please contact with your admin.", Toast.LENGTH_SHORT).show();
                                      startActivity(new Intent(LoginActivity.this,TermsConditionActivity.class));
                                      finish();

                                  }else {
                                      String shopCat = ""+snapshot.child("shopCategory").getValue();

                                      if (shopCat.equals("false")){
                                          HashMap<String,Object> hashMap = new HashMap<>();
                                          hashMap.put("online","true");
                                          DatabaseReference myRR = FirebaseDatabase.getInstance().getReference().child("Users");
                                          myRR.child(auth.getUid()).updateChildren(hashMap);
                                          startActivity(new Intent(LoginActivity.this,TermsConditionActivity.class));


                                      }else {

                                          startActivity(new Intent(LoginActivity.this,MainSellerActivity.class));
                                          finish();


                                      }
                                  }
                              }
                          }

                          @Override
                          public void onCancelled(@NonNull DatabaseError error) {

                          }
                      });

                  }else {
                      dialog.dismiss();
                      startActivity(new Intent(LoginActivity.this,MainUserActivity.class));
                      finish();
                  }
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }


    private boolean validateEmail(){


        email = binding.emailET.getText().toString().trim();

        if (email.isEmpty()){
            binding.textInputEmail.setError("Field can't be empty");
            return false;
        }else  if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.textInputEmail.setError("Invalid Password Pattern");
            return false;
        } else {
            binding.textInputEmail.setError(null);
            binding.textInputEmail.setErrorEnabled(false);
            return true;
        }


    }
    private boolean validatePassword(){
        password = binding.passET.getText().toString().trim();
        if (password.length()>=6){
            Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
            Matcher matcher = pattern.matcher(password);
            boolean isPwdContains = matcher.find();
            if (isPwdContains){
                binding.textInputPass.setHelperText("Strong Password");
                binding.textInputPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
                binding.textInputPass.setError("");
                return true;
            }else {
                binding.textInputPass.setHelperText("");
                binding.textInputPass.setError("Weak Password.Include minimum 1 special char.");
                return false;
            }
        }else if (password.isEmpty()){
            binding.textInputPass.setHelperText("Field can't be empty.");
            binding.textInputPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.red_deep)));
            return false;
        } else{
            binding.textInputPass.setHelperText("Enter Minimum 6 char.");
            binding.textInputPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.red_deep)));
            binding.textInputPass.setError("");
            return false;
        }
    }
}