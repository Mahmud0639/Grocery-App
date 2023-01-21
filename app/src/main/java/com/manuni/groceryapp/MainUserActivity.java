package com.manuni.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivityMainUserBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainUserActivity extends AppCompatActivity {
    ActivityMainUserBinding binding;
    private FirebaseAuth auth;

    private DatabaseReference dbRef;
    private ProgressDialog dialog;

    private ArrayList<ModelShop> modelShopArrayList;
    private AdapterShop adapterShop;
    private ModelShop modelShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setTitle("Please wait");
        dialog.setCanceledOnTouchOutside(false);

        auth = FirebaseAuth.getInstance();

        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");

        checkUser();

        showShopsUI();

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               makeMeOffLine();
            }
        });

        binding.editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainUserActivity.this,EditeProfileUserActivity.class));
            }
        });

        binding.tabShopsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShopsUI();
            }
        });
        binding.tabOrdersTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrdersUI();
            }
        });


    }

    private void showShopsUI() {
        binding.shopsRl.setVisibility(View.VISIBLE);
        binding.ordersRl.setVisibility(View.GONE);

        binding.tabShopsTV.setTextColor(getResources().getColor(R.color.black));
        binding.tabShopsTV.setBackgroundResource(R.drawable.shape_rect04);

        binding.tabOrdersTV.setTextColor(getResources().getColor(R.color.white));
        binding.tabOrdersTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    private void showOrdersUI(){

        binding.ordersRl.setVisibility(View.VISIBLE);
        binding.shopsRl.setVisibility(View.GONE);

        binding.tabOrdersTV.setTextColor(getResources().getColor(R.color.black));
        binding.tabOrdersTV.setBackgroundResource(R.drawable.shape_rect04);

        binding.tabShopsTV.setTextColor(getResources().getColor(R.color.white));
        binding.tabShopsTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void makeMeOffLine(){
        dialog.setMessage("Logging out...");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        dbRef.child(auth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //checkUserType();
                auth.signOut();
                checkUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(MainUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void checkUser(){
        if (auth.getCurrentUser()==null){
            startActivity(new Intent(MainUserActivity.this,LoginActivity.class));
        }else {
            loadMyInfo();
        }


    }
    private void loadMyInfo(){
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference().child("Users");
        dR.orderByChild("uid").equalTo(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String name = ""+dataSnapshot.child("fullName").getValue();
                    String accountType = ""+dataSnapshot.child("accountType").getValue();
                    String email = ""+dataSnapshot.child("email").getValue();
                    String phoneNumber = ""+dataSnapshot.child("phoneNumber").getValue();
                    String profileImage = ""+dataSnapshot.child("profileImage").getValue();
                    String city = ""+dataSnapshot.child("city").getValue();

                    binding.nameTxt.setText(name+"("+accountType+")");
                    binding.emailTV.setText(email);
                    binding.phoneTV.setText(phoneNumber);

                    try {
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(binding.profileIV);
                    }catch (Exception e){
                        binding.profileIV.setImageResource(R.drawable.ic_person_gray);
                    }

                    //load only those shops that are in the user area or city
                    loadShops(city);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadShops(String myCity) {
        modelShopArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.orderByChild("accountType").equalTo("Seller").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelShopArrayList.clear();
                for (DataSnapshot dataSnapshot1: snapshot.getChildren()){
                   // Log.e("TAG", "onDataChange: CheckAfter for loop" );
                    try {
                        modelShop = dataSnapshot1.getValue(ModelShop.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //Log.e("TAG", "onDataChange: checkAfter ModelShop Object" );
                    String shopCity = ""+dataSnapshot1.child("city").getValue() ;
                    //Log.e("TAG", "onDataChange: checkAfter shopCity" );
                    if (Objects.requireNonNull(shopCity).equals(myCity)){
                        modelShopArrayList.add(modelShop);
                    }

                }
                adapterShop = new AdapterShop(MainUserActivity.this,modelShopArrayList);
                binding.shopRV.setAdapter(adapterShop);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}