package com.manuni.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.manuni.groceryapp.databinding.ActivityReviewBinding;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ReviewActivity extends AppCompatActivity {
    ActivityReviewBinding binding;
    private String shopUid;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        shopUid = getIntent().getStringExtra("shopUid");

        auth = FirebaseAuth.getInstance();

        loadShopInfo();
        loadMyReview();

        binding.backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.fabReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });

    }

    private void loadShopInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String shopName = ""+snapshot.child("shopName").getValue();
                String shopImage = ""+snapshot.child("profileImage").getValue();

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
        dRef.child(shopUid).child("Ratings").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String uid = ""+snapshot.child("uid").getValue();
                    String ratings = ""+snapshot.child("ratings").getValue();
                    String reviews = ""+snapshot.child("reviews").getValue();
                    String timestamp = ""+snapshot.child("timestamp").getValue();

                    float myRatings = Float.parseFloat(ratings);
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
        String ratings = ""+binding.ratings.getRating();
        String reviewTxt = binding.reviewET.getText().toString().trim();
        String timestamp = ""+System.currentTimeMillis();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+auth.getUid());
        hashMap.put("ratings",""+ratings);
        hashMap.put("reviews",""+reviewTxt);
        hashMap.put("timestamp",""+timestamp);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbRef.child(shopUid).child("Ratings").child(auth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ReviewActivity.this, "We got your reviews!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ReviewActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}