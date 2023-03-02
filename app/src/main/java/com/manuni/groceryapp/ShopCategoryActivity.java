package com.manuni.groceryapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivityShopCategoryBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ShopCategoryActivity extends AppCompatActivity {
    ActivityShopCategoryBinding binding;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    String[] data,dataArea;
    ArrayList<String> dataList,areaList;

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();



        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");

        progressDialog = new ProgressDialog(ShopCategoryActivity.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCancelable(false);


        try {
            loadToSpinner();
            loadArea();
        } catch (Exception e) {
            e.printStackTrace();
        }


        binding.saveBtn.setOnClickListener(view -> {

            if (binding.categoryTV.getText().toString().equals("Shop category")|| binding.areaTV.getText().toString().equals("Shop Area")){
                Toast.makeText(ShopCategoryActivity.this, "Select a category for the both", Toast.LENGTH_SHORT).show();
            }else  if ( binding.categoryTV.getText().toString().equals("All")||binding.areaTV.getText().toString().equals("All")){
                Toast.makeText(ShopCategoryActivity.this, "You can't set as All.Select except All", Toast.LENGTH_SHORT).show();
            }else {

                String shopCat = binding.categoryTV.getText().toString().trim();
                String shopArea = binding.areaTV.getText().toString().trim();
               ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
               NetworkInfo wifi =  manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
               NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

               if (wifi.isConnected()){
                   try {
                       makeShopCategory(shopCat,shopArea);
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
               }else if (mobile.isConnected()){
                   try {
                       makeShopCategory(shopCat,shopArea);
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
               }else {
                   Toast.makeText(ShopCategoryActivity.this, "No connection", Toast.LENGTH_SHORT).show();
               }

            }



        });

        binding.backBtn.setOnClickListener(view -> onBackPressed());

        binding.doneBtn.setOnClickListener(view -> {
            try {
                checkConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        binding.categoryTV.setOnClickListener(view -> categoryDialog());
        binding.areaTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                areaDialog();
            }
        });
    }

    private void areaDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Shop Area").setItems(dataArea, (dialogInterface, i) -> {
            String shopArea = dataArea[i];//ekhane kono category select kora hole seta ei variable er moddhe chole ashbe

            binding.areaTV.setText(shopArea);
        }).show();
    }

    private void loadArea() {

        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        DatabaseReference myDbRef = FirebaseDatabase.getInstance().getReference().child("ShopArea");
        myDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                areaList = new ArrayList<>();
                if (snapshot.exists()){
                    areaList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String area = null;
                        try {
                            area = ""+dataSnapshot.child("area").getValue();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            areaList.add(area);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    dataArea = areaList.toArray(new String[areaList.size()]);



                }
                progressDialog.dismiss();


                //adapter.notifyDataSetChanged();

            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void makeShopCategory(String myShopCategory,String myShopArea) {
        progressDialog.setTitle("Shop category");
        progressDialog.setMessage("Changing and saving as "+myShopCategory);
        progressDialog.show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("shopCategory",myShopCategory);

        reference.child(Objects.requireNonNull(auth.getUid())).updateChildren(hashMap).addOnSuccessListener(unused -> {
            Toast.makeText(ShopCategoryActivity.this, "Shop category has been set as "+myShopCategory, Toast.LENGTH_SHORT).show();
            makeShopArea(myShopArea);
            //progressDialog.dismiss();
        }).addOnFailureListener(e -> {
            Toast.makeText(ShopCategoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
    }

    private void makeShopArea(String myShopAreaHere) {
        progressDialog.setTitle("Shop area");
        progressDialog.setMessage("Changing and saving as "+myShopAreaHere);
       // progressDialog.show();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("shopArea",myShopAreaHere);

        reference.child(Objects.requireNonNull(auth.getUid())).updateChildren(hashMap).addOnSuccessListener(unused -> {
            Toast.makeText(ShopCategoryActivity.this, "Shop area has been set as "+myShopAreaHere, Toast.LENGTH_SHORT).show();

            progressDialog.dismiss();
        }).addOnFailureListener(e -> {
            Toast.makeText(ShopCategoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
    }

    private void checkConnection() {
        ConnectivityManager manager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected()){
            try {
                checkUserType();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (mobile.isConnected()){
            try {
                checkUserType();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkUserType(){
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
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
                                        Toast.makeText(ShopCategoryActivity.this, "You are blocked.Please contact with your admin.", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }else {
                                        String shopCat = ""+snapshot.child("shopCategory").getValue();
                                        if (shopCat.equals("false")){
                                            Toast.makeText(ShopCategoryActivity.this, "Please select a category", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }else {
                                            progressDialog.dismiss();
                                            try {
                                                startActivity(new Intent(ShopCategoryActivity.this,MainSellerActivity.class));
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
                        progressDialog.dismiss();
                        try {
                            startActivity(new Intent(ShopCategoryActivity.this,MainUserActivity.class));
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
    private void categoryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Shop Category").setItems(data, (dialogInterface, i) -> {
            String category = data[i];//ekhane kono category select kora hole seta ei variable er moddhe chole ashbe

            binding.categoryTV.setText(category);
        }).show();
    }
    private void loadToSpinner() {

        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        DatabaseReference myDbRef = FirebaseDatabase.getInstance().getReference().child("ShopCategory");
        myDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList = new ArrayList<>();
                if (snapshot.exists()){
                    dataList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String categories = null;
                        try {
                            categories = ""+dataSnapshot.child("category").getValue();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            dataList.add(categories);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        dataList.add(0,"All");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    data = dataList.toArray(new String[dataList.size()]);



                }
                progressDialog.dismiss();


                //adapter.notifyDataSetChanged();

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