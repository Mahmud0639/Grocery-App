package com.manuni.groceryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import com.manuni.groceryapp.databinding.ActivityProductInfoBinding;
import com.squareup.picasso.Picasso;

public class ProductInfoActivity extends AppCompatActivity {
    ActivityProductInfoBinding binding;
    private String productIcon,originalPrice,discountPrice,discountNote,productTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productIcon = getIntent().getStringExtra("productIcon");
        originalPrice = getIntent().getStringExtra("originalPrice");
        discountPrice = getIntent().getStringExtra("discountPrice");
        discountNote = getIntent().getStringExtra("discountNote");
        productTitle = getIntent().getStringExtra("productTitle");


        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.impl1).into(binding.productIV);
        } catch (Exception e) {
           Picasso.get().load(R.drawable.impl1).into(binding.productIV);
        }
        if (discountPrice.equals("0.0")){
            binding.textView.setPaintFlags(0);
        }else {
           binding.textView.setPaintFlags(binding.textView.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
        }

        binding.pDiscount.setText("Discount price : "+discountPrice+" tk");
        binding.productName.setText("Product: "+productTitle);
        binding.textView.setText("Original price: "+originalPrice+" tk");
        binding.descriptionTV.setText("This is "+productTitle+".This product is very energetic and authentic.You can take it without any thinking.We are always trusted.");
        if (discountNote.equals("")){
            binding.discountNoteTV.setVisibility(View.GONE);
        }else {
            binding.discountNoteTV.setVisibility(View.VISIBLE);
            binding.discountNoteTV.setText(discountNote+"% OFF");
        }


    }
}