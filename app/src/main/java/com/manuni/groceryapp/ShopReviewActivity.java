package com.manuni.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

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

        shopUid = getIntent().getStringExtra("shopUid");

        loadShopDetails();
        loadShopReviews();


        binding.backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private float ratingSum = 0;
    private void loadShopReviews() {
        modelReviewArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(shopUid).child("Ratings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelReviewArrayList.clear();
                ratingSum = 0;
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    float rating = Float.parseFloat(""+dataSnapshot.child("ratings").getValue());//e.g 4.5
                    ratingSum = ratingSum+rating;

                    ModelReview modelReview = dataSnapshot.getValue(ModelReview.class);

                    modelReviewArrayList.add(modelReview);
                }
                adapterReview = new AdapterReview(ShopReviewActivity.this,modelReviewArrayList);

                binding.reviewRV.setAdapter(adapterReview);

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
                String shopName = ""+snapshot.child("shopName").getValue();
                String profileImage = ""+snapshot.child("profileImage").getValue();

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