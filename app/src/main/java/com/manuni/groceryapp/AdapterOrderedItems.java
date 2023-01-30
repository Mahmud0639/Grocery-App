package com.manuni.groceryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manuni.groceryapp.databinding.RowOrderedItemBinding;

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

    @Override
    public void onBindViewHolder(@NonNull AdapterOrderedItemsViewHolder holder, int position) {
        ModelOrderedItems data = list.get(position);
        String pId = data.getpId();
        String name = data.getName();
        String cost = data.getCost();
        String price = data.getPrice();
        String quantity = data.getQuantity();

        holder.binding.itemTitleTV.setText(name);
        holder.binding.itemPriceEachTV.setText("$"+price);
        holder.binding.itemPriceTV.setText("$"+cost);
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
