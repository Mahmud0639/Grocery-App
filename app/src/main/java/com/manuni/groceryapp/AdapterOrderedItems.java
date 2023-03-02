package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manuni.groceryapp.databinding.RowOrderedItemBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterOrderedItems extends RecyclerView.Adapter<AdapterOrderedItems.AdapterOrderedItemsViewHolder>{

    private Context context;
    private ArrayList<ModelOrderedItems> list;

    public AdapterOrderedItems(Context context, ArrayList<ModelOrderedItems> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public AdapterOrderedItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_ordered_item,parent,false);
        return new AdapterOrderedItemsViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterOrderedItemsViewHolder holder, int position) {
        ModelOrderedItems data = list.get(position);
        String pId = data.getpId();
        String name = data.getName();
        String cost = data.getCost();
        String price = data.getPrice();
        String quantity = data.getQuantity();
        String productQuantity = data.getProQuantity();
        String proImage = data.getPrImage();


        try {
            Picasso.get().load(proImage).placeholder(R.drawable.impl1).into(holder.binding.productImage);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.impl1).into(holder.binding.productImage);
        }
        holder.binding.productTotalDesc.setText(productQuantity+" এর "+quantity+" টি অর্ডার");
        holder.binding.itemTitleTV.setText(name);
        holder.binding.itemPriceEachTV.setText("৳"+price);
        holder.binding.itemPriceTV.setText("৳"+cost);
        holder.binding.itemQuantityTV.setText("["+quantity+"]");

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class AdapterOrderedItemsViewHolder extends RecyclerView.ViewHolder{

        RowOrderedItemBinding binding;
        public AdapterOrderedItemsViewHolder(@NonNull View itemView){
            super(itemView);
            binding = RowOrderedItemBinding.bind(itemView);
        }
    }
}
