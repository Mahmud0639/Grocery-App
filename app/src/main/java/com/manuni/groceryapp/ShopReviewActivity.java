package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivityShopReviewBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewActivity extends AppCompatActivity {
    ActivityShopReviewBinding binding;
    private String shopUid;
    private ArrayList<ModelReview> modelReviewArrayList;
    private AdapterReview adapterReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            shopUid = getIntent().getStringExtra("shopUid");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            loadShopDetails();
            loadShopReviews();
        } catch (Exception e) {
            e.printStackTrace();
        }


        binding.backArrowBtn.setOnClickListener(view -> onBackPressed());

    }

    private float ratingSum = 0;
    private void loadShopReviews() {
        modelReviewArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(shopUid).child("Ratings").addValueEventListener(new ValueEventListener() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelReviewArrayList.clear();
                ratingSum = 0;
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    float rating = 0;//e.g 4.5
                    try {
                        rating = Float.parseFloat(""+dataSnapshot.child("ratings").getValue());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    ratingSum = ratingSum+rating;

                    ModelReview modelReview = null;
                    try {
                        modelReview = dataSnapshot.getValue(ModelReview.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        modelReviewArrayList.add(modelReview);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                adapterReview = new AdapterReview(ShopReviewActivity.this,modelReviewArrayList);

                try {
                    binding.reviewRV.setAdapter(adapterReview);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                long numberOfReviews = snapshot.getChildrenCount();
                float avgOfReviews = ratingSum/numberOfReviews;

                binding.ratingsTV.setText(String.format("%.1f",avgOfReviews)+"["+numberOfReviews+"]");
                binding.ratingBar.setRating(avgOfReviews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopDetails() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbRef.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String shopName = null;
                String profileImage = null;
                try {
                    shopName = ""+snapshot.child("shopName").getValue();
                    profileImage = ""+snapshot.child("profileImage").getValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                binding.shopNameTV.setText(shopName);
                try {
                    Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(binding.shopIV);
                } catch (Exception e) {
                    Picasso.get().load(R.drawable.ic_store_gray).into(binding.shopIV);
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}