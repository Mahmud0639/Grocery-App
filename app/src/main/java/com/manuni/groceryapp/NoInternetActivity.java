package com.manuni.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivityNoInternetBinding;

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

        binding.reloadit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        startActivity(new Intent(NoInternetActivity.this,NoInternetActivity.class));
                    }else {
                        if (user==null){
                            startActivity(new Intent(NoInternetActivity.this,LoginActivity.class));
                            finish();
                        }else {
                            checkUserType();
                        }
                    }

                }else if (mobile.isConnected()){

                    TelephonyManager telephonyManager = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                    @SuppressLint("MissingPermission") int networkType = telephonyManager.getNetworkType();
                    switch (networkType){
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:{
                            Toast.makeText(NoInternetActivity.this, "You need a strong connection to load", Toast.LENGTH_LONG).show();
                            break;
                        }
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:{
                            if (user==null){
                                startActivity(new Intent(NoInternetActivity.this,LoginActivity.class));
                                finish();
                            }else {
                                checkUserType();
                            }
                            break;
                        }
                        case TelephonyManager.NETWORK_TYPE_LTE:{
                            if (user==null){
                                startActivity(new Intent(NoInternetActivity.this,LoginActivity.class));
                                finish();
                            }else {
                                checkUserType();
                            }
                            break;
                        }


                    }
                }else if (wifi.isFailover()||mobile.isFailover()){
                    Toast.makeText(NoInternetActivity.this, "Check your connection", Toast.LENGTH_LONG).show();

                }else if (wifi.isAvailable()||mobile.isAvailable()){
                    Toast.makeText(NoInternetActivity.this, "Check your connection", Toast.LENGTH_LONG).show();
                }else if (wifi.isConnectedOrConnecting()){
                    Toast.makeText(NoInternetActivity.this, "Internet is slow to load", Toast.LENGTH_LONG).show();

                }else {
                    Snackbar.make(view,"Check your internet connection to further proceed",Snackbar.LENGTH_LONG).show();
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
                    String accountType = ""+dataSnapshot.child("accountType").getValue();
                    if (accountType.equals("Seller")){
                        DatabaseReference myReference = FirebaseDatabase.getInstance().getReference().child("Users");

                        myReference.child(auth.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    String status = ""+snapshot.child("accountStatus").getValue();
                                    if (status.equals("blocked")){
                                        Toast.makeText(NoInternetActivity.this, "You are blocked.Please contact with your admin.", Toast.LENGTH_SHORT).show();
                                    }else {
                                        startActivity(new Intent(NoInternetActivity.this,MainSellerActivity.class));
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
                        startActivity(new Intent(NoInternetActivity.this,MainUserActivity.class));
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