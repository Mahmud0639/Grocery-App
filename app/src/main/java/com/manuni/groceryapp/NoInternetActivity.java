package com.manuni.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
                    if (user==null){
                        startActivity(new Intent(NoInternetActivity.this,LoginActivity.class));
                        finish();
                    }else {
                        //checkUserStatus();
                        checkUserType();
                    }
                }else if (mobile.isConnected()){
                    if (user==null){
                        startActivity(new Intent(NoInternetActivity.this,LoginActivity.class));
                        finish();
                    }else {
                        checkUserType();
                    }
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
}