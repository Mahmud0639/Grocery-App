package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.manuni.groceryapp.databinding.RowCartItemBinding;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.CartItemViewHolder>{

    private Context context;
    public ArrayList<ModelCartItem> list;


    public AdapterCartItem(Context context, ArrayList<ModelCartItem> list){
        this.context = context;
        this.list = list;
    }


    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_cart_item,parent,false);
        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ModelCartItem data = list.get(position);
        String id = data.getId();
        String pId = data.getpId();
        String title = data.getName();
        String price = data.getPrice();
        String cost = data.getCost();
        String quantity = data.getQuantity();
        String prodQuantity = data.getProQuantity();




        double priceDouble = Double.parseDouble(price);
        double costDouble = Double.parseDouble(cost);



        holder.binding.productTotalDesc.setText(prodQuantity+" এর "+quantity+" টি");

        holder.binding.itemTitleTV.setText(title);
        holder.binding.itemPriceEachTV.setText(String.format("৳%.2f",priceDouble));
        holder.binding.itemPriceTV.setText(String.format("৳%.2f",costDouble));
        holder.binding.itemQuantityTV.setText("["+quantity+"]");


        holder.binding.removeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create table if not exists but in that case insha Allah must exists
//                EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
//                        .setTableName("ITEMS_TABLE")
//                        .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
//                        .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
//                        .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
//                        .addColumn(new Column("Item_Each_Price",new String[]{"text","not null"}))
//                        .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
//                        .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
//                        .doneTableColumn();

                EasyDB easyDB = EasyDB.init(context,"ITEM_DB")
                        .setTableName("ITEM_TABLE")
                        .addColumn(new Column("Items_Id",new String[]{"text","unique"}))
                        .addColumn(new Column("Items_PID",new String[]{"text","not null"}))
                        .addColumn(new Column("Items_Name",new String[]{"text","not null"}))
                        .addColumn(new Column("Items_Each_Price",new String[]{"text","not null"}))
                        .addColumn(new Column("Items_Price",new String[]{"text","not null"}))
                        .addColumn(new Column("Items_Quantity",new String[]{"text","not null"}))
                        .addColumn(new Column("Items_Pro_Quantity",new String[]{"text","not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1,id);
                Toast.makeText(context, "Removed from cart.", Toast.LENGTH_SHORT).show();

                //refresh list
                list.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                double tx = Double.parseDouble((((ShopDetailsActivity)context).allTotalPriceTV.getText().toString().trim().replace("৳","")));
                double totalPrice = tx-Double.parseDouble(cost.replace("৳",""));
                double deliveryFee = Double.parseDouble((((ShopDetailsActivity)context).deliveryFee.replace("৳","")));
                double subTotal = Double.parseDouble(String.format("%.2f",totalPrice))-Double.parseDouble(String.format("%.2f",deliveryFee));
                ((ShopDetailsActivity)context).allTotalPrice=0.00;//shopdetails activity access kore oi public allTotalPrice er value update kora hoyece ekhane ei process a
                ((ShopDetailsActivity)context).subTotalPriceTV.setText("৳"+String.format("%.2f",subTotal));
                ((ShopDetailsActivity)context).allTotalPriceTV.setText("৳"+String.format("%.2f",Double.parseDouble(String.format("%.2f",totalPrice))));

                //after removing update cart count
                ((ShopDetailsActivity)context).cartCount();

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class CartItemViewHolder extends RecyclerView.ViewHolder{
        RowCartItemBinding binding;
        public CartItemViewHolder(@NonNull View itemView){
            super(itemView);

            binding = RowCartItemBinding.bind(itemView);
        }
    }
}