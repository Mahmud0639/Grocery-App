package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

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
import java.util.Objects;

public class MainSellerActivity extends AppCompatActivity {
    ActivityMainSellerBinding binding;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private ProgressDialog dialog;
    private String selected;

    private ArrayList<ModelProduct> list;
    private ProductSellerAdapter productSellerAdapter;

    private ArrayList<ModelOrderShop> modelOrderShops;
    private AdapterOrderShop adapterOrderShop;

    String[] data;
    ArrayList<String> dataList;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




        auth = FirebaseAuth.getInstance();

        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");




        try {

            checkUser();

            loadToSpinner();

            showProductsUI();

            loadAllProducts();
        } catch (Exception e) {
            e.printStackTrace();
        }


        dialog = new ProgressDialog(this);
        dialog.setTitle("Please wait");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //searching
        binding.searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (charSequence.equals("")){
                        loadFilteredProducts(selected);
                    }else {
                        productSellerAdapter.getFilter().filter(charSequence);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        PopupMenu popupMenu = new PopupMenu(MainSellerActivity.this,binding.moreBtn);
        popupMenu.getMenu().add("Add Category");
        popupMenu.getMenu().add("Edit Profile");
        popupMenu.getMenu().add("Delete Category");
        popupMenu.getMenu().add("Add Product");
        popupMenu.getMenu().add("Account Info");
        popupMenu.getMenu().add("Today Balance");
        popupMenu.getMenu().add("Reviews");
        popupMenu.getMenu().add("Settings");
        popupMenu.getMenu().add("Send Feedback");
        popupMenu.getMenu().add("Logout");

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle()=="Edit Profile"){
                try {
                    startActivity(new Intent(MainSellerActivity.this,EditProfileSellerActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (item.getTitle()=="Account Info"){
                try {
                    startActivity(new Intent(MainSellerActivity.this,AccountInfoActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }  else if (item.getTitle()=="Add Product"){
                try {
                    startActivity(new Intent(MainSellerActivity.this,AddProductActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (item.getTitle()=="Send Feedback"){
                try {
                    startActivity(new Intent(MainSellerActivity.this,FeedbackActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (item.getTitle()=="Today Balance"){
                try {
                    startActivity(new Intent(MainSellerActivity.this,TotalCostActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (item.getTitle()=="Reviews"){
                Intent intent = new Intent(MainSellerActivity.this,ShopReviewActivity.class);
                intent.putExtra("shopUid",auth.getUid());
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (item.getTitle()=="Settings"){
                Intent intent = new Intent(MainSellerActivity.this,SettingsActivity.class);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (item.getTitle()=="Logout"){
                ConnectivityManager manager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                if (wifi.isConnected()){
                    WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    int numberOfLevels = 5;
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(),numberOfLevels);

                    if (level < 2){
                        Toast.makeText(MainSellerActivity.this, "Your internet is unstable to logout", Toast.LENGTH_SHORT).show();
                    }else {
                        try {
                            makeMeOffLine();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }else if (mobile.isConnected()){
                    try {
                        makeMeOffLine();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else if (wifi.isFailover()||mobile.isFailover()){
                    Toast.makeText(MainSellerActivity.this, "Check your connection", Toast.LENGTH_SHORT).show();
                }else if (wifi.isAvailable()||mobile.isAvailable()){
                    Toast.makeText(MainSellerActivity.this, "Check your connection", Toast.LENGTH_SHORT).show();
                }else if (wifi.isConnectedOrConnecting()){
                    Toast.makeText(MainSellerActivity.this, "Internet is slow to logout", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(MainSellerActivity.this, "No internet", Toast.LENGTH_SHORT).show();
                }

            }else if (item.getTitle()=="Add Category"){
                try {
                    startActivity(new Intent(MainSellerActivity.this,AddCategoryActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (item.getTitle()=="Delete Category"){
                try {
                    startActivity(new Intent(MainSellerActivity.this,DeleteCategoryActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });

        binding.moreBtn.setOnClickListener(view -> popupMenu.show());

        binding.tabProductsTV.setOnClickListener(view -> showProductsUI());

        binding.tabOrdersTV.setOnClickListener(view -> showOrdersUI());

        binding.filterProductBtn.setOnClickListener(view -> {
            if (dataList.size()==0){
                Toast.makeText(MainSellerActivity.this, "Please add category first", Toast.LENGTH_SHORT).show();
            }else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose Category").setItems(data, (dialogInterface, i) -> {
                    selected =data[i];
                    binding.filterProductTV.setText(selected);
                    if (selected.equals("All")){
                        try {
                            loadAllProducts();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        try {
                            loadFilteredProducts(selected);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                }).show();
            }

        });

        binding.filterOrderBtn.setOnClickListener(view -> {
            if (modelOrderShops.size()==0){
                Toast.makeText(MainSellerActivity.this, "No orders available.", Toast.LENGTH_SHORT).show();
            }else {
                final String[] options = {"All","In Progress","Completed","Cancelled"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders")
                        .setItems(options, (dialogInterface, i) -> {
                            if (i==0){
                                binding.filterOrderTV.setText("Showing All Orders");
                                adapterOrderShop.getFilter().filter("");
                            }else {
                                String optionClicked = options[i];
                                binding.filterOrderTV.setText("Showing "+optionClicked+" Orders");
                                adapterOrderShop.getFilter().filter(optionClicked);
                            }
                        }).show();
            }


        });


    }

    private void loadAllOrders() {
        modelOrderShops = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(Objects.requireNonNull(auth.getUid())).child("Orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    modelOrderShops.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        ModelOrderShop modelOrderShop = null;
                        try {
                            modelOrderShop = dataSnapshot.getValue(ModelOrderShop.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            modelOrderShops.add(modelOrderShop);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this,modelOrderShops);
                    try {
                        binding.ordersRV.setAdapter(adapterOrderShop);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadFilteredProducts(String selected) {
        binding.itemsFoundTV.setVisibility(View.VISIBLE);
        list = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child(Objects.requireNonNull(auth.getUid())).child("Products").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //before getting data clear the list data
                list.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                    String productCategory = null;
                    try {
                        productCategory = ""+dataSnapshot.child("productCategory").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (selected.equals(productCategory)){
                        ModelProduct data = null;
                        try {
                            data = dataSnapshot.getValue(ModelProduct.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            list.add(data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // binding.filterProductTV.setText(list.size()+" items found");
                    }

                }
                binding.itemsFoundTV.setText(" ("+list.size()+" items found)");
                productSellerAdapter = new ProductSellerAdapter(MainSellerActivity.this,list);
                try {
                    binding.productRV.setAdapter(productSellerAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAllProducts() {
        binding.itemsFoundTV.setVisibility(View.GONE);
        list = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child(Objects.requireNonNull(auth.getUid())).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //before getting data clear the list data
                list.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ModelProduct data = null;
                    try {
                        data = dataSnapshot.getValue(ModelProduct.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        list.add(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                productSellerAdapter = new ProductSellerAdapter(MainSellerActivity.this,list);
                try {
                    binding.productRV.setAdapter(productSellerAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }

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

        DatabaseReference myD = FirebaseDatabase.getInstance().getReference().child("Users");
        myD.child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
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
                        try {
                            startActivity(new Intent(MainSellerActivity.this,LoginActivity.class));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        finish();
                    }else {
                        try {
                            loadAllOrders();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainSellerActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });




    }
    private void makeMeOffLine(){
        dialog.setMessage("Logging out...");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        dbRef.child(Objects.requireNonNull(auth.getUid())).updateChildren(hashMap).addOnSuccessListener(unused -> {
            //checkUserType();
            auth.signOut();
            try {
                checkUser();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(MainSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }
    private void checkUser(){
        if (auth.getCurrentUser()==null){
            try {
                startActivity(new Intent(MainSellerActivity.this,LoginActivity.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
            finishAffinity();
        }else {
            try {
                loadMyInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
    private void loadMyInfo(){
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference().child("Users");
        dR.orderByChild("uid").equalTo(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String name = null;
                    String accountType = null;
                    String email = null;
                    String shopName = null;
                    String profileImage = null;
                    try {
                        name = ""+dataSnapshot.child("fullName").getValue();
                        accountType = ""+dataSnapshot.child("accountType").getValue();
                        email = ""+dataSnapshot.child("email").getValue();
                        shopName = ""+dataSnapshot.child("shopName").getValue();
                        profileImage = ""+dataSnapshot.child("profileImage").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


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

    private void loadToSpinner() {


        DatabaseReference myDbRef = FirebaseDatabase.getInstance().getReference().child("Categories");
        myDbRef.child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList = new ArrayList<>();
                if (snapshot.exists()){
                    dataList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String categories = ""+dataSnapshot.child("category").getValue();
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
                dialog.dismiss();


                //adapter.notifyDataSetChanged();

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
}