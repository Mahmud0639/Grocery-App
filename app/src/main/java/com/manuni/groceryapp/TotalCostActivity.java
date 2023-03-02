package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivityTotalCostBinding;

import java.util.Objects;

public class TotalCostActivity extends AppCompatActivity {
    ActivityTotalCostBinding binding;
    private String orderToSeller;
    private String deliFee;
    private String shopName;
    private FirebaseAuth auth;
    private double totalOrderCost = 0.0, totalOrderCostForCancelled = 0.0, totalOrderCostForCompleted = 0.0, totalOrderCostForAll = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTotalCostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

      //  orderToSeller = getIntent().getStringExtra("orderToSeller");
        try {
            loadAccountStatus();
            loadThisShopDeliveryFee();
            loadAllCompletedOrders();
            loadAllInProgressOrder();
            loadAllCancelledOrders();
            loadAllShopsInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    private void loadAllShopsInfo() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users");
        db.child(Objects.requireNonNull(auth.getUid())).child("Orders").addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalOrders = 0;
                try {
                    totalOrders = snapshot.getChildrenCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                binding.totalOrders.setText("" + totalOrders);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String totalCostForAllType = null;
                    try {
                        totalCostForAllType = "" + dataSnapshot.child("orderCost").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    assert totalCostForAllType != null;
                    double orderCostInDouble = 0;
                    try {
                        orderCostInDouble = Double.parseDouble(totalCostForAllType);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    totalOrderCostForAll = totalOrderCostForAll + orderCostInDouble;
                }

                binding.totalCost.setText(String.format("%.2f", totalOrderCostForAll)+" tk");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.backBtn.setOnClickListener(view -> onBackPressed());
    }
    private void loadAllCancelledOrders() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbRef.child(Objects.requireNonNull(auth.getUid())).child("Orders").orderByChild("orderStatus").equalTo("Cancelled").addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long cancelledOrders = 0;
                try {
                    cancelledOrders = snapshot.getChildrenCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                binding.cancelledOrders.setText("" + cancelledOrders);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String orderCostCancelled = null;
                    try {
                        orderCostCancelled = "" + dataSnapshot.child("orderCost").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    assert orderCostCancelled != null;
                    double orderCostInDouble = 0;
                    try {
                        orderCostInDouble = Double.parseDouble(orderCostCancelled);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    totalOrderCostForCancelled = totalOrderCostForCancelled + orderCostInDouble;
                }

                binding.totalCostCancelled.setText(String.format("%.2f", totalOrderCostForCancelled)+" tk");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadAllInProgressOrder() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(Objects.requireNonNull(auth.getUid())).child("Orders").orderByChild("orderStatus").equalTo("In Progress").addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long inProgressOrders = 0;
                try {
                    inProgressOrders = snapshot.getChildrenCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                binding.inProgressOrders.setText("" + inProgressOrders);


                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String orderCost = null;
                    try {
                        orderCost = "" + dataSnapshot.child("orderCost").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    assert orderCost != null;
                    double orderCostInDouble = 0;
                    try {
                        orderCostInDouble = Double.parseDouble(orderCost);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    totalOrderCost = totalOrderCost + orderCostInDouble;

                }

                binding.totalCostInProgress.setText(String.format("%.2f", totalOrderCost)+" tk");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadAccountStatus(){
        DatabaseReference dref = FirebaseDatabase.getInstance().getReference().child("Users");
        dref.child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String accountStatus = null;
                try {
                    accountStatus = ""+snapshot.child("accountStatus").getValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                assert accountStatus != null;
                if (accountStatus.equals("blocked")){
                    binding.accountStatusTxt.setTextColor(getResources().getColor(R.color.colorRed));
                    binding.accountStatusTxt.setText("Account Status: "+accountStatus);
                }else {
                    binding.accountStatusTxt.setTextColor(getResources().getColor(R.color.colorGreen));
                    binding.accountStatusTxt.setText("Account Status: "+accountStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadAllCompletedOrders() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(Objects.requireNonNull(auth.getUid())).child("Orders").orderByChild("orderStatus").equalTo("Completed").addValueEventListener(new ValueEventListener() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long completedOrder = 0;
                try {
                    completedOrder = snapshot.getChildrenCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String completedOrderASString = String.valueOf(completedOrder);
                double completedOrderAsDouble = 0;
                try {
                    completedOrderAsDouble = Double.parseDouble(completedOrderASString);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                //long delFeeAsLong = Long.parseLong(deliFee);
                double delFeeAsDouble = 0;
                try {
                    delFeeAsDouble = Double.parseDouble(deliFee);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                double totalComFeeAsDouble = completedOrderAsDouble*delFeeAsDouble;


                binding.deliFeeForCompleted.setText(String.format("%.2f",totalComFeeAsDouble)+" Taka");





                binding.completedOrders.setText("" + completedOrder);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String orderCostCompleted = null;
                    try {
                        orderCostCompleted = "" + dataSnapshot.child("orderCost").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    assert orderCostCompleted != null;
                    double orderCostInDouble = 0;
                    try {
                        orderCostInDouble = Double.parseDouble(orderCostCompleted);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    totalOrderCostForCompleted = totalOrderCostForCompleted + orderCostInDouble;
                }

                binding.totalCompleted.setText(String.format("%.2f", totalOrderCostForCompleted)+" tk");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadThisShopDeliveryFee() {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dRef.child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    deliFee = "" + snapshot.child("deliveryFee").getValue();
                    shopName = ""+snapshot.child("shopName").getValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                binding.shopNameTV.setText("Shop Name: "+shopName);
                binding.deliveryFee.setText("Delivery Fee: "+deliFee+" Taka");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}