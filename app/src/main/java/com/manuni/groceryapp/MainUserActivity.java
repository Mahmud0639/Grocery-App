package com.manuni.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
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
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class MainUserActivity extends AppCompatActivity {
    ActivityMainUserBinding binding;
    private FirebaseAuth auth;
   private String city;

    String[] data;
    ArrayList<String> dataList;

    private DatabaseReference dbRef;
    private ProgressDialog dialog,progressDialog;

    private ArrayList<ModelShop> modelShopArrayList;
    private AdapterShop adapterShop;
    private ModelShop modelShop;


    //private ArrayList<ModelOrderUser> modelOrderUsers;
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

        progressDialog = new ProgressDialog(MainUserActivity.this);


        auth = FirebaseAuth.getInstance();

        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");

        checkUser();

        loadToSpinner();

        showShopsUI();

        PopupMenu popupMenu = new PopupMenu(MainUserActivity.this,binding.moreBtn);
        popupMenu.getMenu().add("Edit Profile");
        popupMenu.getMenu().add("Settings");
        popupMenu.getMenu().add("Logout");


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle()=="Edit Profile"){
                    startActivity(new Intent(MainUserActivity.this,EditeProfileUserActivity.class));
                }else if (item.getTitle()=="Settings"){
                    Intent intent = new Intent(MainUserActivity.this,SettingsActivity.class);
                    startActivity(intent);
                }else if (item.getTitle()=="Logout"){
                    makeMeOffLine();
                }
                return true;
            }
        });

        binding.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenu.show();
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

        binding.filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
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

        binding.searchBar.setVisibility(View.VISIBLE);
        binding.searchForOrders.setVisibility(View.GONE);


        binding.shopsRl.setVisibility(View.VISIBLE);
        binding.ordersRl.setVisibility(View.GONE);

        binding.tabShopsTV.setTextColor(getResources().getColor(R.color.black));
        binding.tabShopsTV.setBackgroundResource(R.drawable.shape_rect04);

        binding.tabOrdersTV.setTextColor(getResources().getColor(R.color.white));
        binding.tabOrdersTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        binding.filterProductBtn.setVisibility(View.VISIBLE);
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

        binding.filterProductBtn.setVisibility(View.GONE);

        loadOrders();//ekhane rakhar karone duplicate ashe na


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
                    city = ""+dataSnapshot.child("city").getValue();

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

    private void loadOrders() {
        modelOrderUsers = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelOrderUsers.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(auth.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                               // modelOrderUsers.clear(); //ei line tir jonno onno store er order dekhato na.
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    ModelOrderUser orderUser = ds.getValue(ModelOrderUser.class);

                                    modelOrderUsers.add(0,orderUser);


                                }
                                adapterOrderUser = new AdapterOrderUser(MainUserActivity.this,modelOrderUsers);
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

                    String shopCity = ""+dataSnapshot1.child("city").getValue() ;
                    String status = ""+dataSnapshot1.child("accountStatus").getValue();
                    String onlineStatus = ""+dataSnapshot1.child("online").getValue();
                    String shopOpen = ""+dataSnapshot1.child("shopOpen").getValue();

                    if (Objects.requireNonNull(shopCity).equals(myCity) && status.equals("unblocked")&& shopOpen.equals("true")){
                        modelShopArrayList.add(modelShop);
                    }
//                    reference.orderByChild("accountType").equalTo("seller").addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if (snapshot.exists()){
//                                for (DataSnapshot dSnapShot: snapshot.getChildren()){
//
//
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
                    //Log.e("TAG", "onDataChange: checkAfter ModelShop Object" );

                    //Log.e("TAG", "onDataChange: checkAfter shopCity" );


                }
                adapterShop = new AdapterShop(MainUserActivity.this,modelShopArrayList);
                binding.shopRV.setAdapter(adapterShop);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else {

            Toast.makeText(getBaseContext(), "Press again to exit",
                    Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
    }

    private void categoryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Shop Category").setItems(data, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String category = data[i];//ekhane kono category select kora hole seta ei variable er moddhe chole ashbe
                if (category.equals("All")){

                    loadShops(city);
                }else{
                    loadAllShops(category);
                }
            }
        }).show();
    }

    private void loadAllShops(String selected) {
        modelShopArrayList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.orderByChild("accountType").equalTo("Seller").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                modelShopArrayList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String shopCat = ""+dataSnapshot.child("shopCategory").getValue();
                    String myCity = ""+dataSnapshot.child("city").getValue();

                    if (selected.equals(shopCat) && myCity.equals(city)){
                       ModelShop modelShop = dataSnapshot.getValue(ModelShop.class);
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
}