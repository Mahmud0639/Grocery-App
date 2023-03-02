package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manuni.groceryapp.databinding.DialogQuantityBinding;
import com.manuni.groceryapp.databinding.SampleProductUserBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ProductUserAdapter extends RecyclerView.Adapter<ProductUserAdapter.ProductUserViewHolder> implements Filterable {
    private Context context;
    public ArrayList<ModelProduct> list,filterList;
    private FilterProductUser filterProductUser;

    public ProductUserAdapter(Context context,ArrayList<ModelProduct> list){
        this.context = context;
        this.list = list;
        this.filterList = list;
    }

    @NonNull
    @Override
    public ProductUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_product_user,parent,false);
        return new ProductUserViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ProductUserViewHolder holder, int position) {
        ModelProduct data = list.get(position);

        String discountAvailable = data.getProductDiscountAvailable();
        String discountNote = data.getProductDiscountNote();
        String discountPrice = data.getProductDiscountPrice();
        String productCategory = data.getProductCategory();
        String originalPrice = data.getProductOriginalPrice();
        String productDescription = data.getProductDesc();
        String productTitle = data.getProductTitle();
        String productQuantity = data.getProductQuantity();
        String productId = data.getProductId();
        String timestamp = data.getTimestamp();
        String productIcon = data.getProductIcon();
        String productAvailable = data.getProductAvailable();


        try {
            holder.binding.titleTV.setText(productTitle);
            holder.binding.descriptionTV.setText(productDescription);
            holder.binding.quantityTV.setText("পরিমাণঃ "+"("+productQuantity+")");

            holder.binding.originalPriceTV.setText("৳"+originalPrice);
            holder.binding.discountPriceTV.setText("৳"+discountPrice);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (productAvailable.equals("false")&&(discountAvailable.equals("true")||discountAvailable.equals("false"))){
            try {
                holder.binding.productAvailableTV.setText("Not Available");
                holder.binding.productAvailableTV.setTextColor(context.getResources().getColor(R.color.colorRed));
                holder.binding.discountNoteTV.setVisibility(View.GONE);
                holder.binding.productAvailableTV.setVisibility(View.VISIBLE);
                holder.binding.addToCartTV.setVisibility(View.INVISIBLE);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }

        }else {
            try {
                holder.binding.discountNoteTV.setText(discountNote+"% OFF");
                holder.binding.productAvailableTV.setVisibility(View.GONE);
                holder.binding.discountNoteTV.setVisibility(View.VISIBLE);
                holder.binding.addToCartTV.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (discountAvailable.equals("true") && productAvailable.equals("true")){
            try {
                holder.binding.discountPriceTV.setVisibility(View.VISIBLE);
                holder.binding.discountNoteTV.setVisibility(View.VISIBLE);
                holder.binding.originalPriceTV.setPaintFlags(holder.binding.discountPriceTV.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                holder.binding.discountPriceTV.setVisibility(View.GONE);
                holder.binding.discountNoteTV.setVisibility(View.GONE);
                holder.binding.originalPriceTV.setPaintFlags(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.impl1).into(holder.binding.productIconIV);
        }catch (Exception e){
            holder.binding.productIconIV.setImageResource(R.drawable.impl1);
        }

        try {
            holder.binding.addToCartTV.setOnClickListener(view -> showQuantityDialog(data));
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context,ProductInfoActivity.class);
            intent.putExtra("productIcon",productIcon);
            intent.putExtra("originalPrice",originalPrice);
            intent.putExtra("discountPrice",discountPrice);
            intent.putExtra("productTitle",productTitle);
            intent.putExtra("discountNote",discountNote);
            intent.putExtra("productDes",productDescription);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private double cost = 0.0,finalCost = 0.0;
    private int quantity = 0;
    private String productQuantity;
    private String image;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void showQuantityDialog(ModelProduct modelProduct) {
        DialogQuantityBinding binding;
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity,null);
        binding = DialogQuantityBinding.bind(view);

        String productId = modelProduct.getProductId();
        String title = modelProduct.getProductTitle();
        productQuantity = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDesc();
        String discountNote = modelProduct.getProductDiscountNote();
        image = modelProduct.getProductIcon();

        String price;
        if (modelProduct.getProductDiscountAvailable().equals("true")){
            price = modelProduct.getProductDiscountPrice();
            try {
                binding.discountNoteTV.setVisibility(View.VISIBLE);
                binding.originalPriceTV.setPaintFlags(binding.originalPriceTV.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            price = modelProduct.getProductOriginalPrice();
            try {
                binding.discountNoteTV.setVisibility(View.GONE);
                binding.discountPriceTV.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            cost = Double.parseDouble(price.replaceAll("৳",""));
            finalCost = Double.parseDouble(price.replaceAll("৳",""));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        quantity = 1;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(binding.getRoot());
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_cart_gray).into(binding.productIV);
        }catch (Exception e){
            binding.productIV.setImageResource(R.drawable.ic_cart_gray);
        }
        try {
            binding.titleTV.setText(""+title);
            binding.quantityTV.setText(""+quantity);
            binding.pDescription.setText(""+description);
            binding.finalTV.setText("৳"+finalCost);
            binding.discountNoteTV.setText(""+discountNote+"% OFF");
            binding.originalPriceTV.setText("৳"+modelProduct.getProductOriginalPrice());
            binding.discountPriceTV.setText("৳"+modelProduct.getProductDiscountPrice());
            binding.pQuantityTV.setText("["+productQuantity+"]");
        } catch (Exception e) {
            e.printStackTrace();
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        binding.incrementBtn.setOnClickListener(view13 -> {
            finalCost = finalCost+cost;
            quantity++;

            try {
                binding.finalTV.setText("৳"+String.format("%.1f",finalCost));
                binding.quantityTV.setText(""+quantity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.decrementBtn.setOnClickListener(view12 -> {
            if (quantity>1){
                finalCost = finalCost-cost;
                quantity--;

                try {
                    binding.finalTV.setText("৳"+String.format("%.1f",finalCost));
                    binding.quantityTV.setText(""+quantity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        binding.continueBtn.setOnClickListener(view1 -> {
            String title1 = binding.titleTV.getText().toString().trim();
            String priceEach = price;
            String totalPrice = binding.finalTV.getText().toString().trim().replace("৳","");
            String quantity = binding.quantityTV.getText().toString().trim();
            String pQuantity = binding.pQuantityTV.getText().toString().trim();


            //add to database(sqlite)
            addToCart(productId, title1,priceEach,totalPrice,quantity,pQuantity,image);
            dialog.dismiss();
        });
    }



    private void addToCart(String productId, String title, String priceEach, String price, String quantity,String proQuantity,String prImage) {

      long myItem =  System.currentTimeMillis();


            myItem++;

       // Toast.makeText(context, ""+myItem, Toast.LENGTH_SHORT).show();

        /*
        EasyDB easyDB = EasyDB.init(context,"ITEM_DB_NEW")
                .setTableName("ITEM_TABLE_NEW")
                .addColumn(new Column("Items_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Items_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Each_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Quantity",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Pro_Quantity",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Pro_Image",new String[]{"text","not null"}))
                .doneTableColumn();*/


        EasyDB easyDB = EasyDB.init(context,"ITEM_DB_NEW_TWO")
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
                .addData("Items_Name_Two",title)
                .addData("Items_Each_Price_Two",priceEach)
                .addData("Items_Price_Two",price)
                .addData("Items_Quantity_Two",quantity)
                .addData("Items_Pro_Quantity_Two",proQuantity)
                .addData("Items_Pro_Image_Two",prImage)
                .doneDataAdding();

        Toast.makeText(context, "Product Added.", Toast.LENGTH_SHORT).show();
        //update cart count
        ((ShopDetailsActivity)context).cartCount();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        if (filterProductUser==null){
            filterProductUser = new FilterProductUser(this,filterList);
        }
        return filterProductUser;
    }

    public class ProductUserViewHolder extends RecyclerView.ViewHolder{
        SampleProductUserBinding binding;

        public ProductUserViewHolder(@NonNull View itemView){
            super(itemView);

            binding = SampleProductUserBinding.bind(itemView);
        }
    }
}