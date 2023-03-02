package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivitySplashBinding;

import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    ActivitySplashBinding binding;
    private FirebaseAuth auth;

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");


        ConnectivityManager manager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


        new Handler().postDelayed(() -> {
            FirebaseUser user = auth.getCurrentUser();
            if (wifi.isConnected()){
                WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                int numberOfLevels = 5;
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(),numberOfLevels);

                if (level < 2){
                    Toast.makeText(SplashActivity.this, "Your internet is unstable to load", Toast.LENGTH_LONG).show();
                    try {
                        startActivity(new Intent(SplashActivity.this,NoInternetActivity.class));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    if (user==null){
                        try {
                            startActivity(new Intent(SplashActivity.this,LoginActivity.class));
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
                        startActivity(new Intent(SplashActivity.this,LoginActivity.class));
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
                Toast.makeText(SplashActivity.this, "Check your connection", Toast.LENGTH_LONG).show();
                try {
                    startActivity(new Intent(SplashActivity.this,NoInternetActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (wifi.isAvailable()||mobile.isAvailable()){
                Toast.makeText(SplashActivity.this, "Check your connection", Toast.LENGTH_LONG).show();
                try {
                    startActivity(new Intent(SplashActivity.this,NoInternetActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (wifi.isConnectedOrConnecting()){
                Toast.makeText(SplashActivity.this, "Internet is slow to load", Toast.LENGTH_LONG).show();
                try {
                    startActivity(new Intent(SplashActivity.this,NoInternetActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else {
                try {
                    startActivity(new Intent(SplashActivity.this,NoInternetActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
            }




        },2000);
    }

    private void checkUserType(){

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
                                        Toast.makeText(SplashActivity.this, "You are blocked.Please contact with your admin.", Toast.LENGTH_SHORT).show();
                                        try {
                                            startActivity(new Intent(SplashActivity.this,TermsConditionActivity.class));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        finish();
                                    }else {
                                        String shopCat = null;
                                        try {
                                            shopCat = ""+snapshot.child("shopCategory").getValue();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        assert shopCat != null;
                                        if (shopCat.equals("false")){
                                            try {
                                                startActivity(new Intent(SplashActivity.this,TermsConditionActivity.class));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }else {
                                            try {
                                                startActivity(new Intent(SplashActivity.this,MainSellerActivity.class));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
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
                        try {
                            startActivity(new Intent(SplashActivity.this,MainUserActivity.class));
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
    }
}