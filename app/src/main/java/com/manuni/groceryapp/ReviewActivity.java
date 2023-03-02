package com.manuni.groceryapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivityReviewBinding;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

public class ReviewActivity extends AppCompatActivity {
    ActivityReviewBinding binding;
    private String shopUid;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            shopUid = getIntent().getStringExtra("shopUid");
        } catch (Exception e) {
            e.printStackTrace();
        }

        auth = FirebaseAuth.getInstance();

        try {
            loadShopInfo();
            loadMyReview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.backArrowBtn.setOnClickListener(view -> onBackPressed());

        binding.fabReview.setOnClickListener(view -> {
            try {
                inputData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private void loadShopInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String shopName = null;
                String shopImage = null;
                try {
                    shopName = ""+snapshot.child("shopName").getValue();
                    shopImage = ""+snapshot.child("profileImage").getValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                binding.shopNameTV.setText(shopName);

                try {
                    Picasso.get().load(shopImage).placeholder(R.drawable.ic_store_gray).into(binding.storeIV);
                } catch (Exception e) {
                    Picasso.get().load(R.drawable.ic_store_gray).into(binding.storeIV);
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyReview() {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dRef.child(shopUid).child("Ratings").child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String ratings = null;
                    String reviews = null;
                    try {
                        String uid = ""+snapshot.child("uid").getValue();
                        ratings = ""+snapshot.child("ratings").getValue();
                        reviews = ""+snapshot.child("reviews").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    float myRatings = 0;
                    try {
                        assert ratings != null;
                        myRatings = Float.parseFloat(ratings);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    binding.ratings.setRating(myRatings);
                    binding.reviewET.setText(reviews);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inputData(){
        String ratings = null;
        String reviewTxt = null;
        String timestamp = null;
        try {
            ratings = ""+binding.ratings.getRating();
            reviewTxt = binding.reviewET.getText().toString().trim();
            timestamp = ""+System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+auth.getUid());
        hashMap.put("ratings",""+ratings);
        hashMap.put("reviews",""+reviewTxt);
        hashMap.put("timestamp",""+timestamp);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbRef.child(shopUid).child("Ratings").child(Objects.requireNonNull(auth.getUid())).updateChildren(hashMap).addOnSuccessListener(unused -> Toast.makeText(ReviewActivity.this, "We got your reviews!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(ReviewActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}