package com.manuni.groceryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.manuni.groceryapp.databinding.ActivityProductInfoBinding;
import com.squareup.picasso.Picasso;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ProductInfoActivity extends AppCompatActivity {
    ActivityProductInfoBinding binding;
    private String productIcon,originalPrice,discountPrice,discountNote,productTitle,productDescription,productBrand,productPriceEach,productId,productQuantity;

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
            productBrand = getIntent().getStringExtra("productBrand");
            productPriceEach = getIntent().getStringExtra("priceEach");
            productId = getIntent().getStringExtra("productId");
            productQuantity = getIntent().getStringExtra("productQuantity");
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
            binding.productBrandTxt.setText("Brand: "+productBrand);
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


        binding.placeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                long myItem =  System.currentTimeMillis();


                myItem++;

                EasyDB easyDB = EasyDB.init(ProductInfoActivity.this,"ITEM_DB_NEW_TWO")
                        .setTableName("ITEM_TABLE_NEW_TWO")
                        .addColumn(new Column("Items_Id_Two",new String[]{"text","unique"}))
                        .addColumn(new Column("Items_PID_Two",new String[]{"text","not null"}))
                        .addColumn(new Column("Items_Name_Two",new String[]{"text","not null"}))
                        .addColumn(new Column("Items_Each_Price_Two",new String[]{"text","not null"}))
                        .addColumn(new Column("Items_Price_Two",new String[]{"text","not null"}))
                        .addColumn(new Column("Items_Quantity_Two",new String[]{"text","not null"}))
                        .addColumn(new Column("Items_Pro_Quantity_Two",new String[]{"text","not null"}))
                        .addColumn(new Column("Items_Pro_Image_Two",new String[]{"text","not null"}))
                        .doneTableColumn();

                Boolean b = easyDB.addData("Items_Id_Two", (int) myItem)
                        .addData("Items_PID_Two",productId)
                        .addData("Items_Name_Two",productTitle)
                        .addData("Items_Each_Price_Two",productPriceEach)
                        .addData("Items_Price_Two",productPriceEach)
                        .addData("Items_Quantity_Two","1")
                        .addData("Items_Pro_Quantity_Two",productQuantity)
                        .addData("Items_Pro_Image_Two",productIcon)
                        .doneDataAdding();

                Toast.makeText(ProductInfoActivity.this, "Product Added.", Toast.LENGTH_SHORT).show();
                //update cart count
                ((ShopDetailsActivity)getApplicationContext()).cartCount();
            }
        });
        binding.backBtn.setOnClickListener(view -> onBackPressed());


    }
}