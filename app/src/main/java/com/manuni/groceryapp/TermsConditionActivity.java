package com.manuni.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import com.manuni.groceryapp.databinding.ActivityTermsConditionBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class TermsConditionActivity extends AppCompatActivity {
    ActivityTermsConditionBinding binding;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    String[] data;
    ArrayList<String> dataList;

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTermsConditionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        auth = FirebaseAuth.getInstance();



        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");

        progressDialog = new ProgressDialog(TermsConditionActivity.this);
        progressDialog.setCancelable(false);


        loadToSpinner();


        binding.goBackBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               startActivity(new Intent(TermsConditionActivity.this,RegisterSellerActivity.class));
               finish();

           }
       });

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (binding.categoryTV.getText().toString().equals("Shop category")){
                    Toast.makeText(TermsConditionActivity.this, "Select a category", Toast.LENGTH_SHORT).show();
                }else  if ( binding.categoryTV.getText().toString().equals("All")){
                    Toast.makeText(TermsConditionActivity.this, "You can't set as All.Select except All", Toast.LENGTH_SHORT).show();
                }else {

                    String shopCat = binding.categoryTV.getText().toString().trim();
                   ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                   NetworkInfo wifi =  manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                   NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                   if (wifi.isConnected()){
                       makeShopCategory(shopCat);
                   }else if (mobile.isConnected()){
                       makeShopCategory(shopCat);
                   }else {
                       Toast.makeText(TermsConditionActivity.this, "No connection", Toast.LENGTH_SHORT).show();
                   }

                }



            }
        });

       binding.agreeBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
                checkConnection();
           }
       });

       binding.categoryTV.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               categoryDialog();
           }
       });


    }

    private void makeShopCategory(String myShopCategory) {
        progressDialog.setTitle("Shop category");
        progressDialog.setMessage("Changing and saving as "+myShopCategory);
        progressDialog.show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("shopCategory",myShopCategory);

        reference.child(auth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(TermsConditionActivity.this, "Shop category has been set as "+myShopCategory, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(TermsConditionActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
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
                                        String shopCat = ""+snapshot.child("shopCategory").getValue();
                                        if (shopCat.equals("false")){
                                            Toast.makeText(TermsConditionActivity.this, "Please select a category", Toast.LENGTH_SHORT).show();
                                        }else {
                                            startActivity(new Intent(TermsConditionActivity.this,MainSellerActivity.class));
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
    private void categoryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Shop Category").setItems(data, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String category = data[i];//ekhane kono category select kora hole seta ei variable er moddhe chole ashbe

                binding.categoryTV.setText(category);
            }
        }).show();
    }
    private void loadToSpinner() {
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        DatabaseReference myDbRef = FirebaseDatabase.getInstance().getReference().child("ShopCategory");
        myDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dataList = new ArrayList<>();
                if (snapshot.exists()){
                    dataList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String categories = ""+dataSnapshot.child("category").getValue();
                        dataList.add(categories);
                    }
                    dataList.add(0,"All");
                    data = dataList.toArray(new String[dataList.size()]);



                }
                progressDialog.dismiss();


                //adapter.notifyDataSetChanged();

            }



            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}