package com.manuni.groceryapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivityNoInternetBinding;

import java.util.Objects;

public class NoInternetActivity extends AppCompatActivity {
    ActivityNoInternetBinding binding;
    private FirebaseAuth auth;

    private DatabaseReference dbRef;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoInternetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);




        auth = FirebaseAuth.getInstance();

        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");

        progressDialog = new ProgressDialog(NoInternetActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Please wait...");

        binding.reloadit.setOnClickListener(view -> {
            ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            FirebaseUser user = auth.getCurrentUser();

            if (wifi.isConnected()){
                WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                int numberOfLevels = 5;
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(),numberOfLevels);

                if (level < 2){
                    Toast.makeText(NoInternetActivity.this, "Your internet is unstable to load", Toast.LENGTH_LONG).show();
                    try {
                        startActivity(new Intent(NoInternetActivity.this,NoInternetActivity.class));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    if (user==null){
                        try {
                            startActivity(new Intent(NoInternetActivity.this,LoginActivity.class));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        finish();
                    }else {
                        try {
                            checkUserType();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }else if (mobile.isConnected()){
                if (user==null){
                    try {
                        startActivity(new Intent(NoInternetActivity.this,LoginActivity.class));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                }else {
                    try {
                        checkUserType();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }else if (wifi.isFailover()||mobile.isFailover()){
                Toast.makeText(NoInternetActivity.this, "Check your connection", Toast.LENGTH_LONG).show();

            }else if (wifi.isAvailable()||mobile.isAvailable()){
                Toast.makeText(NoInternetActivity.this, "Check your connection", Toast.LENGTH_LONG).show();
            }else if (wifi.isConnectedOrConnecting()){
                Toast.makeText(NoInternetActivity.this, "Internet is slow to load", Toast.LENGTH_LONG).show();

            }else {
                try {
                    Snackbar.make(view,"Check your internet connection to further proceed",Snackbar.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });



    }

    private void checkUserType(){
        progressDialog.show();
        dbRef.orderByChild("uid").equalTo(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String accountType = null;
                    try {
                        accountType = ""+dataSnapshot.child("accountType").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    assert accountType != null;
                    if (accountType.equals("Seller")){
                        DatabaseReference myReference = FirebaseDatabase.getInstance().getReference().child("Users");

                        myReference.child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    String status = null;
                                    try {
                                        status = ""+snapshot.child("accountStatus").getValue();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    assert status != null;
                                    if (status.equals("blocked")){
                                        Toast.makeText(NoInternetActivity.this, "You are blocked.Please contact with your admin.", Toast.LENGTH_SHORT).show();
                                    }else {
                                        try {
                                            startActivity(new Intent(NoInternetActivity.this,MainSellerActivity.class));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        finish();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        progressDialog.dismiss();
                    }else {
                        try {
                            startActivity(new Intent(NoInternetActivity.this,MainUserActivity.class));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        finish();
                        progressDialog.dismiss();
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
}