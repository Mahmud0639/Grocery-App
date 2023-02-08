package com.manuni.groceryapp;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        orderTo = getIntent().getStringExtra("orderTo");//orderTo contains user id of the shop where we placed order
        orderId = getIntent().getStringExtra("orderId");

        loadShopInfo();



        //Log.d("MyTag", "onCreate: "+orderTo);
        binding.backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reviewIntent = new Intent(OrderDetailsUsersActivity.this,ReviewActivity.class);
                reviewIntent.putExtra("shopUid",orderTo);
                startActivity(reviewIntent);
            }
        });
    }

    private void loadOrderedItems() {
        modelOrderedItems = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbRef.child(orderTo).child("Orders").child(orderId).child("Items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    modelOrderedItems.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ModelOrderedItems items = dataSnapshot.getValue(ModelOrderedItems.class);
                        modelOrderedItems.add(items);
                    }
                    adapterOrderedItems = new AdapterOrderedItems(OrderDetailsUsersActivity.this, modelOrderedItems);
                    binding.orderedItemsRV.setAdapter(adapterOrderedItems);

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
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    orderBy = "" + snapshot.child("orderBy").getValue();
                    orderCost = "" + snapshot.child("orderCost").getValue();
                     orderId = "" + snapshot.child("orderId").getValue();
                     orderStatus = "" + snapshot.child("orderStatus").getValue();
                     orderTo = "" + snapshot.child("orderTo").getValue();
                     deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                     latitude = "" + snapshot.child("latitude").getValue();
                     longitude = "" + snapshot.child("longitude").getValue();

                     orderTime = "" + snapshot.child("orderTime").getValue();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(Long.parseLong(orderTime));
                    String dateTime = DateFormat.format("dd/MM/yy hh:mm aa", calendar).toString();

                    binding.dateTV.setText(dateTime);



                }




                //convert time


                if (orderStatus.equals("In Progress")) {
                    binding.statusTV.setTextColor(getResources().getColor(R.color.background_theme));
                } else if (orderStatus.equals("Completed")) {
                    binding.statusTV.setTextColor(getResources().getColor(R.color.colorGreen));
                } else if (orderStatus.equals("Cancelled")) {
                    binding.statusTV.setTextColor(getResources().getColor(R.color.colorRed));
                }

                binding.orderIdTV.setText(orderId);
                binding.statusTV.setText(orderStatus);
                binding.totalPriceTV.setText("৳" + orderCost + "[Including delivery fee ৳" + deliveryFee + "]");


                findAddress(latitude, longitude);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void findAddress(String myLatitude, String myLongitude) {
        double lat = Double.parseDouble(myLatitude);
        double lon = Double.parseDouble(myLongitude);

        Geocoder geocoder;
        List<Address> addresses;

        geocoder = new Geocoder(OrderDetailsUsersActivity.this, Locale.getDefault());

        try {


            addresses = geocoder.getFromLocation(lat, lon, 1);
            String address = addresses.get(0).getAddressLine(0);//complete address
            binding.deliveryAddressTV.setText(address);

        } catch (Exception e) {

        }
    }

    private void loadShopInfo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child(orderTo).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String shopName = "" + snapshot.child("shopName").getValue();
                binding.shopNameTV.setText(shopName);

                loadOrderDetails();
                loadOrderedItems();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}