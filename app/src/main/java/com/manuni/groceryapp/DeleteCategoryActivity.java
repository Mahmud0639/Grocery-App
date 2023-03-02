package com.manuni.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivityDeleteCategoryBinding;

import java.util.ArrayList;
import java.util.Objects;

public class DeleteCategoryActivity extends AppCompatActivity {
    ActivityDeleteCategoryBinding binding;
    private DeleteCategoryAdapter adapter;
    private ArrayList<DeleteCategoryModel> list;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeleteCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Categories");
        databaseReference.child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    list = new ArrayList<>();
                    list.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        DeleteCategoryModel data = dataSnapshot.getValue(DeleteCategoryModel.class);
                        try {
                            list.add(data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    adapter = new DeleteCategoryAdapter(DeleteCategoryActivity.this,list);

                    binding.deleteCategoryRV.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }
}