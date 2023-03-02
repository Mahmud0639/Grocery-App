package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivityOrderDetailsUsersBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class OrderDetailsUsersActivity extends AppCompatActivity {
    ActivityOrderDetailsUsersBinding binding;
    private String orderTo, orderId;

    private String orderBy,orderCost,orderStatus,deliveryFee,latitude,longitude,orderTime;

    private FirebaseAuth auth;
    private ArrayList<ModelOrderedItems> modelOrderedItems;
    private AdapterOrderedItems adapterOrderedItems;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        try {
            orderTo = getIntent().getStringExtra("orderTo");//orderTo contains user id of the shop where we placed order
            orderId = getIntent().getStringExtra("orderId");
        } catch (Exception e) {
            e.printStackTrace();
        }

        progressDialog = new ProgressDialog(OrderDetailsUsersActivity.this);
        progressDialog.setTitle("Order details");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);


        ConnectivityManager manager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected()){
            try {
                loadShopInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (mobile.isConnected()){
            try {
                loadShopInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
        }





        //Log.d("MyTag", "onCreate: "+orderTo);
        binding.backArrowBtn.setOnClickListener(view -> onBackPressed());

        binding.reviewBtn.setOnClickListener(view -> {
            Intent reviewIntent = new Intent(OrderDetailsUsersActivity.this,ReviewActivity.class);
            reviewIntent.putExtra("shopUid",orderTo);
            try {
                startActivity(reviewIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadOrderedItems() {

        modelOrderedItems = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbRef.child(orderTo).child("Orders").child(orderId).child("Items").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    modelOrderedItems.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ModelOrderedItems items = null;
                        try {
                            items = dataSnapshot.getValue(ModelOrderedItems.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            modelOrderedItems.add(items);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    adapterOrderedItems = new AdapterOrderedItems(OrderDetailsUsersActivity.this, modelOrderedItems);
                    try {
                        binding.orderedItemsRV.setAdapter(adapterOrderedItems);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    binding.totalItemsTV.setText("" + snapshot.getChildrenCount());

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadOrderDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(orderTo).child("Orders").child(orderId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    try {
                        orderBy = "" + snapshot.child("orderBy").getValue();
                        orderCost = "" + snapshot.child("orderCost").getValue();
                        orderId = "" + snapshot.child("orderId").getValue();
                        orderStatus = "" + snapshot.child("orderStatus").getValue();
                        orderTo = "" + snapshot.child("orderTo").getValue();
                        deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                        latitude = "" + snapshot.child("latitude").getValue();
                        longitude = "" + snapshot.child("longitude").getValue();

                        orderTime = "" + snapshot.child("orderTime").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(Long.parseLong(orderTime));
                    String dateTime = DateFormat.format("dd/MM/yy hh:mm aa", calendar).toString();

                    binding.dateTV.setText(dateTime);



                }




                //convert time


                switch (orderStatus) {
                    case "In Progress":
                        binding.statusTV.setTextColor(getResources().getColor(R.color.background_theme));
                        break;
                    case "Completed":
                        binding.statusTV.setTextColor(getResources().getColor(R.color.colorGreen));
                        break;
                    case "Cancelled":
                        binding.statusTV.setTextColor(getResources().getColor(R.color.colorRed));
                        break;
                }

                binding.orderIdTV.setText(orderId);
                binding.statusTV.setText(orderStatus);
                binding.totalPriceTV.setText("৳" + orderCost + "[Including delivery fee ৳" + deliveryFee + "]");


                try {
                    findAddress(latitude, longitude);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void findAddress(String myLatitude, String myLongitude) {
        double lat = 0;
        double lon = 0;
        try {
            lat = Double.parseDouble(myLatitude);
            lon = Double.parseDouble(myLongitude);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        Geocoder geocoder;
        List<Address> addresses;

        geocoder = new Geocoder(OrderDetailsUsersActivity.this, Locale.getDefault());

        try {


            addresses = geocoder.getFromLocation(lat, lon, 1);
            String address = addresses.get(0).getAddressLine(0);//complete address
            binding.deliveryAddressTV.setText(address);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadShopInfo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child(orderTo).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String shopName = null;
                try {
                    shopName = "" + snapshot.child("shopName").getValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                binding.shopNameTV.setText(shopName);

                try {
                    loadOrderDetails();
                    loadOrderedItems();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}