package com.manuni.groceryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivityTotalCostBinding;

public class TotalCostActivity extends AppCompatActivity {
    ActivityTotalCostBinding binding;
    private String orderToSeller;
    private String deliFee;
    private String shopName;
    private double totalOrderCost = 0.0, totalOrderCostForCancelled = 0.0, totalOrderCostForCompleted = 0.0, totalOrderCostForAll = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTotalCostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderToSeller = getIntent().getStringExtra("orderToSeller");
        loadAccountStatus();
        loadThisShopDeliveryFee();
        loadAllCompletedOrders();
        loadAllInProgressOrder();
        loadAllCancelledOrders();
        loadAllShopsInfo();



    }
    private void loadAllShopsInfo() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users");
        db.child(orderToSeller).child("Orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long totalOrders = snapshot.getChildrenCount();

                binding.totalOrders.setText("" + totalOrders);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String totalCostForAllType = "" + dataSnapshot.child("orderCost").getValue();

                    double orderCostInDouble = Double.parseDouble(totalCostForAllType);
                    totalOrderCostForAll = totalOrderCostForAll + orderCostInDouble;
                }

                binding.totalCost.setText(String.format("%.2f", totalOrderCostForAll)+" tk");

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
    private void loadAllCancelledOrders() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbRef.child(orderToSeller).child("Orders").orderByChild("orderStatus").equalTo("Cancelled").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long cancelledOrders = snapshot.getChildrenCount();

                binding.cancelledOrders.setText("" + cancelledOrders);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String orderCostCancelled = "" + dataSnapshot.child("orderCost").getValue();

                    double orderCostInDouble = Double.parseDouble(orderCostCancelled);
                    totalOrderCostForCancelled = totalOrderCostForCancelled + orderCostInDouble;
                }

                binding.totalCostCancelled.setText(String.format("%.2f", totalOrderCostForCancelled)+" tk");
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
    private void loadAllInProgressOrder() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(orderToSeller).child("Orders").orderByChild("orderStatus").equalTo("In Progress").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long inProgressOrders = snapshot.getChildrenCount();

                binding.inProgressOrders.setText("" + inProgressOrders);


                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String orderCost = "" + dataSnapshot.child("orderCost").getValue();

                    double orderCostInDouble = Double.parseDouble(orderCost);
                    totalOrderCost = totalOrderCost + orderCostInDouble;

                }

                binding.totalCostInProgress.setText(String.format("%.2f", totalOrderCost)+" tk");

                ;

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
    private void loadAccountStatus(){
        DatabaseReference dref = FirebaseDatabase.getInstance().getReference().child("Users");
        dref.child(orderToSeller).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String accountStatus = ""+snapshot.child("accountStatus").getValue();
                if (accountStatus.equals("blocked")){
                    binding.accountStatusTxt.setTextColor(getResources().getColor(R.color.colorRed));
                    binding.accountStatusTxt.setText("Account Status: "+accountStatus);
                }else {
                    binding.accountStatusTxt.setTextColor(getResources().getColor(R.color.colorGreen));
                    binding.accountStatusTxt.setText("Account Status: "+accountStatus);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
    private void loadAllCompletedOrders() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(orderToSeller).child("Orders").orderByChild("orderStatus").equalTo("Completed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long completedOrder = snapshot.getChildrenCount();

                String completedOrderASString = String.valueOf(completedOrder);
                double completedOrderAsDouble = Double.parseDouble(completedOrderASString);

                //long delFeeAsLong = Long.parseLong(deliFee);
                double delFeeAsDouble = Double.parseDouble(deliFee);

                double totalComFeeAsDouble = completedOrderAsDouble*delFeeAsDouble;



                // long totalCompletedDelFee = (long) (completedOrder*delFeeAsDouble);

                //binding.deliFeeForCompleted.setText(totalCompletedDelFee+" Taka");
                binding.deliFeeForCompleted.setText(String.format("%.2f",totalComFeeAsDouble)+" Taka");





                binding.completedOrders.setText("" + completedOrder);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String orderCostCompleted = "" + dataSnapshot.child("orderCost").getValue();

                    double orderCostInDouble = Double.parseDouble(orderCostCompleted);
                    totalOrderCostForCompleted = totalOrderCostForCompleted + orderCostInDouble;
                }

                binding.totalCompleted.setText(String.format("%.2f", totalOrderCostForCompleted)+" tk");
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
    private void loadThisShopDeliveryFee() {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dRef.child(orderToSeller).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                deliFee = "" + snapshot.child("deliveryFee").getValue();
                shopName = ""+snapshot.child("shopName").getValue();

                binding.shopNameTV.setText("Shop Name: "+shopName);
                binding.deliveryFee.setText("Delivery Fee: "+deliFee+" Taka");
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
}