package com.manuni.groceryapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.manuni.groceryapp.databinding.BottomSheetProductDetailsSellerBinding;
import com.manuni.groceryapp.databinding.SampleProductSellerBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductSellerAdapter extends RecyclerView.Adapter<ProductSellerAdapter.ProductSellerViewHolder> implements Filterable {
    private Context context;
    public ArrayList<ModelProduct> list,filterList;
    private FilterProduct filter;

    public ProductSellerAdapter(Context context,ArrayList<ModelProduct> list){
        this.context = context;
        this.list = list;
        this.filterList = list;
    }

    @NonNull
    @Override
    public ProductSellerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_product_seller,parent,false);
        return new ProductSellerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductSellerViewHolder holder, int position) {
            ModelProduct data = list.get(position);
            String id = data.getProductId();
            String uid = data.getUid();
            String discountAvailable = data.getProductDiscountAvailable();
            String discountNote = data.getProductDiscountNote();
            String discountPrice = data.getProductDiscountPrice();
            String productCategory = data.getProductCategory();
            String productDescription = data.getProductDesc();
            String icon = data.getProductIcon();
            String quantity = data.getProductQuantity();
            String title = data.getProductTitle();
            String timestamp = data.getTimestamp();
            String productOriginalPrice = data.getProductOriginalPrice();

            holder.binding.titleTV.setText(title);
            holder.binding.quantityTV.setText(quantity);
            holder.binding.discountNoteTV.setText(discountNote);
            holder.binding.discountPriceTV.setText("$"+discountPrice);
            holder.binding.originalPriceTV.setText("$"+productOriginalPrice);

            if (discountAvailable.equals("true")){
                //product is on discount

                holder.binding.discountPriceTV.setVisibility(View.VISIBLE);
                holder.binding.discountNoteTV.setVisibility(View.VISIBLE);
                //to make strike original price when it is on discount state
                holder.binding.originalPriceTV.setPaintFlags(holder.binding.originalPriceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            }else {
                //product is not on discount
                holder.binding.discountPriceTV.setVisibility(View.GONE);
                holder.binding.discountNoteTV.setVisibility(View.GONE);
                holder.binding.originalPriceTV.setPaintFlags(0);
            }

            try {
                Picasso.get().load(icon).placeholder(R.drawable.ic_shopping_cart_white).into(holder.binding.productIconIV);

            }catch (Exception e){
                holder.binding.productIconIV.setImageResource(R.drawable.ic_shopping_cart_theme_color);

            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    detailsBottomSheet(data);
                }
            });


    }

    private void detailsBottomSheet(ModelProduct data) {
        BottomSheetProductDetailsSellerBinding binding;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_product_details_seller,null);
        bottomSheetDialog.setContentView(view);


        binding = BottomSheetProductDetailsSellerBinding.bind(view);

        String id = data.getProductId();
        String uid = data.getUid();
        String discountAvailable = data.getProductDiscountAvailable();
        String discountNote = data.getProductDiscountNote();
        String discountPrice = data.getProductDiscountPrice();
        String productCategory = data.getProductCategory();
        String productDescription = data.getProductDesc();
        String icon = data.getProductIcon();
        String quantity = data.getProductQuantity();
        String title = data.getProductTitle();
        String timestamp = data.getTimestamp();
        String productOriginalPrice = data.getProductOriginalPrice();

        binding.titleTV.setText(title);
        binding.descriptionTV.setText(productDescription);
        binding.categoryTV.setText(productCategory);
        binding.quantityTV.setText(quantity);
        binding.discountNote.setText(discountNote);
        binding.discountPriceTV.setText("$"+discountPrice);
        binding.originalPriceTV.setText("$"+productOriginalPrice);

        if (discountAvailable.equals("true")){
            //product is on discount

            binding.discountPriceTV.setVisibility(View.VISIBLE);
            binding.discountNote.setVisibility(View.VISIBLE);
            //to make strike original price when it is on discount state
            binding.originalPriceTV.setPaintFlags(binding.originalPriceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }else {
            //product is not on discount
            binding.discountPriceTV.setVisibility(View.GONE);
            binding.discountNote.setVisibility(View.GONE);
        }

        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_shopping_cart_white).into(binding.productIconIV);

        }catch (Exception e){
           binding.productIconIV.setImageResource(R.drawable.ic_shopping_cart_theme_color);

        }

        bottomSheetDialog.show();

        binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(context,EditProductActivity.class);
                intent.putExtra("productId",id);
                context.startActivity(intent);
            }
        });
        binding.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete").setMessage("Are you sure to delete "+title+"?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteProduct(id);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });


    }

    private void deleteProduct(String id) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child(auth.getUid()).child("Products").child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Product deleted successfully!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProduct(this,filterList);
        }
        return filter;
    }

    public class ProductSellerViewHolder extends RecyclerView.ViewHolder{

        SampleProductSellerBinding binding;

        public ProductSellerViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = SampleProductSellerBinding.bind(itemView);
        }
    }
}
