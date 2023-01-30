package com.manuni.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.manuni.groceryapp.databinding.ActivityMainSellerBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity {
    ActivityMainSellerBinding binding;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private ProgressDialog dialog;

    private ArrayList<ModelProduct> list;
    private ProductSellerAdapter productSellerAdapter;

    private ArrayList<ModelOrderShop> modelOrderShops;
    private AdapterOrderShop adapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        dialog = new ProgressDialog(this);
        dialog.setTitle("Please wait");
        dialog.setCanceledOnTouchOutside(false);

        auth = FirebaseAuth.getInstance();

        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");


        checkUser();
        showProductsUI();
        loadAllOrders();
        loadAllProducts();

        //searching
        binding.searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    productSellerAdapter.getFilter().filter(charSequence);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeMeOffLine();

            }
        });

        binding.editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainSellerActivity.this,EditProfileSellerActivity.class));
            }
        });

        binding.addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainSellerActivity.this,AddProductActivity.class));
            }
        });

        binding.tabProductsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProductsUI();
            }
        });

        binding.tabOrdersTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrdersUI();
            }
        });

        binding.filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose Category").setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String selected = Constants.productCategories1[i];
                        binding.filterProductTV.setText(selected);
                        if (selected.equals("All")){
                            loadAllProducts();
                        }else {
                            loadFilteredProducts(selected);
                        }
                    }
                }).show();
            }
        });

        binding.filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] options = {"All","In Progress","Completed","Cancelled"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i==0){
                                    binding.filterOrderTV.setText("Showing All Orders");
                                    adapterOrderShop.getFilter().filter("");
                                }else {
                                    String optionClicked = options[i];
                                    binding.filterOrderTV.setText("Showing "+optionClicked+" Orders");
                                    adapterOrderShop.getFilter().filter(optionClicked);
                                }
                            }
                        }).show();
            }
        });

        binding.reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainSellerActivity.this,ShopReviewActivity.class);
                intent.putExtra("shopUid",auth.getUid());
                startActivity(intent);
            }
        });

        binding.settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainSellerActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
        });


    }

    private void loadAllOrders() {
        modelOrderShops = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(auth.getUid()).child("Orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelOrderShops.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ModelOrderShop modelOrderShop = dataSnapshot.getValue(ModelOrderShop.class);
                    modelOrderShops.add(modelOrderShop);
                }
                adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this,modelOrderShops);
                binding.ordersRV.setAdapter(adapterOrderShop);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadFilteredProducts(String selected) {
        list = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child(auth.getUid()).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //before getting data clear the list data
                list.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                    String productCategory = ""+dataSnapshot.child("productCategory").getValue();
                    if (selected.equals(productCategory)){
                        ModelProduct data = dataSnapshot.getValue(ModelProduct.class);
                        list.add(data);
                    }


                }
                productSellerAdapter = new ProductSellerAdapter(MainSellerActivity.this,list);
                binding.productRV.setAdapter(productSellerAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAllProducts() {
        list = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child(auth.getUid()).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //before getting data clear the list data
                list.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ModelProduct data = dataSnapshot.getValue(ModelProduct.class);
                    list.add(data);
                }
                productSellerAdapter = new ProductSellerAdapter(MainSellerActivity.this,list);
                binding.productRV.setAdapter(productSellerAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showProductsUI(){
        binding.productsRL.setVisibility(View.VISIBLE);
        binding.ordersRL.setVisibility(View.GONE);

        binding.tabProductsTV.setTextColor(getResources().getColor(R.color.black));
        binding.tabProductsTV.setBackgroundResource(R.drawable.shape_rect04);

        binding.tabOrdersTV.setTextColor(getResources().getColor(R.color.white));
        binding.tabOrdersTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    private void showOrdersUI(){
        binding.ordersRL.setVisibility(View.VISIBLE);
        binding.productsRL.setVisibility(View.GONE);

        binding.tabProductsTV.setTextColor(getResources().getColor(R.color.white));
        binding.tabProductsTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        binding.tabOrdersTV.setTextColor(getResources().getColor(R.color.black));
        binding.tabOrdersTV.setBackgroundResource(R.drawable.shape_rect04);
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
                Toast.makeText(MainSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void checkUser(){
        if (auth.getCurrentUser()==null){
            startActivity(new Intent(MainSellerActivity.this,LoginActivity.class));
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
                    String shopName = ""+dataSnapshot.child("shopName").getValue();
                    String profileImage = ""+dataSnapshot.child("profileImage").getValue();

                    binding.nameTxt.setText(name+"("+accountType+")");
                    binding.shopName.setText(shopName);
                    binding.email.setText(email);
                    try {
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(binding.profileIV);
                    } catch (Exception e) {
                        binding.profileIV.setImageResource(R.drawable.ic_baseline_store_gray);
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}