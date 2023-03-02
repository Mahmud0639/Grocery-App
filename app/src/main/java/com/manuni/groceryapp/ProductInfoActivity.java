package com.manuni.groceryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import com.manuni.groceryapp.databinding.ActivityProductInfoBinding;
import com.squareup.picasso.Picasso;

public class ProductInfoActivity extends AppCompatActivity {
    ActivityProductInfoBinding binding;
    private String productIcon,originalPrice,discountPrice,discountNote,productTitle,productDescription;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            productIcon = getIntent().getStringExtra("productIcon");
            originalPrice = getIntent().getStringExtra("originalPrice");
            discountPrice = getIntent().getStringExtra("discountPrice");
            discountNote = getIntent().getStringExtra("discountNote");
            productTitle = getIntent().getStringExtra("productTitle");
            productDescription = getIntent().getStringExtra("productDes");
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.impl1).into(binding.productIV);
        } catch (Exception e) {
           Picasso.get().load(R.drawable.impl1).into(binding.productIV);
        }
        if (discountPrice.equals("0.0")){
            try {
                binding.textView.setPaintFlags(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                binding.textView.setPaintFlags(binding.textView.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            binding.pDiscount.setText("Discount price : "+discountPrice+" tk");
            binding.productName.setText("Product: "+productTitle);
            binding.textView.setText("Original price: "+originalPrice+" tk");
            binding.descriptionTV.setText(productDescription+".You can take it without any thinking.We are always trusted.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (discountNote.equals("")){
            try {
                binding.discountNoteTV.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                binding.discountNoteTV.setVisibility(View.VISIBLE);
                binding.discountNoteTV.setText(discountNote+"% OFF");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        binding.backBtn.setOnClickListener(view -> onBackPressed());


    }
}