package com.manuni.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivityTermsConditionBinding;

public class TermsConditionActivity extends AppCompatActivity {
    ActivityTermsConditionBinding binding;
    private FirebaseAuth auth;

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTermsConditionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        auth = FirebaseAuth.getInstance();

        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");


        binding.goBackBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               startActivity(new Intent(TermsConditionActivity.this,RegisterSellerActivity.class));
               finish();

           }
       });

       binding.agreeBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
                checkConnection();
           }
       });


    }

    private void checkConnection() {
        ConnectivityManager manager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected()){
            checkUserType();
        }else if (mobile.isConnected()){
            checkUserType();
        }else {
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
        }
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
                                        Toast.makeText(TermsConditionActivity.this, "You are blocked.Please contact with your admin.", Toast.LENGTH_SHORT).show();
                                    }else {
                                        startActivity(new Intent(TermsConditionActivity.this,MainSellerActivity.class));
                                        finish();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }else {
                        startActivity(new Intent(TermsConditionActivity.this,MainUserActivity.class));
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