package com.manuni.groceryapp;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.manuni.groceryapp.databinding.ActivityAddCategoryBinding;

import java.util.HashMap;
import java.util.Objects;

public class AddCategoryActivity extends AppCompatActivity {
    ActivityAddCategoryBinding binding;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbRef = FirebaseDatabase.getInstance().getReference().child("Categories");


        auth = FirebaseAuth.getInstance();

        binding.backBtn.setOnClickListener(view -> onBackPressed());


        binding.addCategoryBtn.setOnClickListener(view -> {

            String key = dbRef.push().getKey();

            String category = Objects.requireNonNull(binding.textInputLayout.getEditText()).getText().toString().trim();

            if (!validateCategory()){
                return;
            }

                binding.textInputLayout.getEditText().setText("");
               // binding.textInputLayout.setError(null);

                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("category",""+category);
                hashMap.put("categoryId",""+key);

            assert key != null;
            dbRef.child(Objects.requireNonNull(auth.getUid())).child(key).setValue(hashMap).addOnSuccessListener(unused -> Toast.makeText(AddCategoryActivity.this, "Category added successfully!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(AddCategoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());


        });


        binding.categoryET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               if (charSequence.length()>18){
                    binding.textInputLayout.setError("Put under 18 char.");
                }else {
                    binding.textInputLayout.setError(null);
                    binding.textInputLayout.setHelperText("Valid category name.");
                    binding.textInputLayout.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }
    private boolean validateCategory(){
        String category = Objects.requireNonNull(binding.textInputLayout.getEditText()).getText().toString().trim();
        if (category.isEmpty()){
            binding.textInputLayout.setError("Field can't be empty.");
            return false;
        }else if (category.length()>18){
            binding.textInputLayout.setError("Put under 18 char.");
            return false;
        }else {
            binding.textInputLayout.setError(null);
            return true;
        }

    }
}