package com.manuni.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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


    private ArrayList<ModelOrderUser> modelOrderUsers;
    private AdapterOrderUser adapterOrderUser;

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

        binding.searchForOrders.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterOrderUser.getFilter().filter(charSequence);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterShop.getFilter().filter(charSequence);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

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

        binding.settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainUserActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
        });


    }

    private void showShopsUI() {

        binding.searchBar.setVisibility(View.VISIBLE);
        binding.searchForOrders.setVisibility(View.GONE);


        binding.shopsRl.setVisibility(View.VISIBLE);
        binding.ordersRl.setVisibility(View.GONE);

        binding.tabShopsTV.setTextColor(getResources().getColor(R.color.black));
        binding.tabShopsTV.setBackgroundResource(R.drawable.shape_rect04);

        binding.tabOrdersTV.setTextColor(getResources().getColor(R.color.white));
        binding.tabOrdersTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    private void showOrdersUI(){

        binding.searchBar.setVisibility(View.GONE);
        binding.searchForOrders.setVisibility(View.VISIBLE);

        binding.ordersRl.setVisibility(View.VISIBLE);
        binding.shopsRl.setVisibility(View.GONE);

        binding.tabOrdersTV.setTextColor(getResources().getColor(R.color.black));
        binding.tabOrdersTV.setBackgroundResource(R.drawable.shape_rect04);

        binding.tabShopsTV.setTextColor(getResources().getColor(R.color.white));
        binding.tabShopsTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        loadOrders();
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
            //Toast.makeText(this, "You are logged in.", Toast.LENGTH_SHORT).show();
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
                    //loadOrders();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadOrders() {
        modelOrderUsers = new ArrayList<>();

        DatabaseReference dbR = FirebaseDatabase.getInstance().getReference().child("Users");
        dbR.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelOrderUsers.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    String uid = ""+dataSnapshot.getRef().getKey();//eta holo specific vabe user er key er moddher orders ke pick kora...karon ei key er under a Products er child ti o ache.
                        //je key er under a products are orders ache si key select kora..oi key dhore amra query korbo
                    DatabaseReference mydb = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Orders");
                    mydb.orderByChild("orderBy").equalTo(auth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        modelOrderUsers.clear();
                                        for (DataSnapshot dataSnapshot1: snapshot.getChildren()){
                                            ModelOrderUser user = dataSnapshot1.getValue(ModelOrderUser.class);
                                            try {
                                                modelOrderUsers.add(0,user);
                                            } catch (Exception e) {
                                                Toast.makeText(MainUserActivity.this, "No records", Toast.LENGTH_SHORT).show();
                                                e.printStackTrace();
                                            }
                                        }
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this,modelOrderUsers);
                                        binding.ordersRV.setLayoutManager(new LinearLayoutManager(MainUserActivity.this));
                                        binding.ordersRV.setAdapter(adapterOrderUser);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
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