package com.manuni.groceryapp;

import android.app.AlertDialog;
import android.content.Context;
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

        holder.binding.titleTV.setText(productTitle);
        holder.binding.descriptionTV.setText(productDescription);
        holder.binding.discountNoteTV.setText(discountNote+"% OFF");
        holder.binding.originalPriceTV.setText("৳"+originalPrice);
        holder.binding.discountPriceTV.setText("৳"+discountPrice);

        if (discountAvailable.equals("true")){
            holder.binding.discountPriceTV.setVisibility(View.VISIBLE);
            holder.binding.discountNoteTV.setVisibility(View.VISIBLE);
            holder.binding.originalPriceTV.setPaintFlags(holder.binding.discountPriceTV.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
        }else {
            holder.binding.discountPriceTV.setVisibility(View.GONE);
            holder.binding.discountNoteTV.setVisibility(View.GONE);
            holder.binding.originalPriceTV.setPaintFlags(0);
        }
        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.ic_shopping_cart_theme_color).into(holder.binding.productIconIV);
        }catch (Exception e){
            holder.binding.productIconIV.setImageResource(R.drawable.ic_shopping_cart_theme_color);
        }

        holder.binding.addToCartTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQuantityDialog(data);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private double cost = 0.0,finalCost = 0.0;
    private int quantity = 0;

    private void showQuantityDialog(ModelProduct modelProduct) {
        DialogQuantityBinding binding;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity,null);
        binding = DialogQuantityBinding.bind(view);

        String productId = modelProduct.getProductId();
        String title = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDesc();
        String discountNote = modelProduct.getProductDiscountNote();
        String image = modelProduct.getProductIcon();

        String price;
        if (modelProduct.getProductDiscountAvailable().equals("true")){
            price = modelProduct.getProductDiscountPrice();
            binding.discountNoteTV.setVisibility(View.VISIBLE);
            binding.originalPriceTV.setPaintFlags(binding.originalPriceTV.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);
        }else {
            price = modelProduct.getProductOriginalPrice();
            binding.discountNoteTV.setVisibility(View.GONE);
            binding.discountPriceTV.setVisibility(View.GONE);
        }
        cost = Double.parseDouble(price.replaceAll("৳",""));
        finalCost = Double.parseDouble(price.replaceAll("৳",""));
        quantity = 1;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(binding.getRoot());
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_cart_gray).into(binding.productIV);
        }catch (Exception e){
            binding.productIV.setImageResource(R.drawable.ic_cart_gray);
        }
        binding.titleTV.setText(""+title);
        binding.quantityTV.setText(""+quantity);
        binding.pDescription.setText(""+description);
        binding.finalTV.setText("৳"+finalCost);
        binding.discountNoteTV.setText(""+discountNote+"% OFF");
        binding.originalPriceTV.setText("৳"+modelProduct.getProductOriginalPrice());
        binding.discountPriceTV.setText("৳"+modelProduct.getProductDiscountPrice());
        binding.pQuantityTV.setText("["+productQuantity+"]");

       AlertDialog dialog = builder.create();
       dialog.show();

       binding.incrementBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               finalCost = finalCost+cost;
               quantity++;

               binding.finalTV.setText("৳"+String.format("%.2f",finalCost));
               binding.quantityTV.setText(""+quantity);
           }
       });

       binding.decrementBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if (quantity>1){
                   finalCost = finalCost-cost;
                   quantity--;

                   binding.finalTV.setText("৳"+finalCost);
                   binding.quantityTV.setText(""+quantity);
               }
           }
       });
       binding.continueBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               String title = binding.titleTV.getText().toString().trim();
               String priceEach = price;
               String totalPrice = binding.finalTV.getText().toString().trim().replace("৳","");
               String quantity = binding.quantityTV.getText().toString().trim();

               //add to database(sqlite)
               addToCart(productId,title,priceEach,totalPrice,quantity);
               dialog.dismiss();
           }
       });
    }

    private int itemId = 1;
    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {
        itemId++;
        EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Each_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        Boolean b = easyDB.addData("Item_Id",itemId)
                .addData("Item_PID",productId)
                .addData("Item_Name",title)
                .addData("Item_Each_Price",priceEach)
                .addData("Item_Price",price)
                .addData("Item_Quantity",quantity)
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
