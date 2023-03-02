package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.RowOrderUserBinding;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.AdapterOrderUserViewHolder> implements Filterable {
    private Context context;
    public ArrayList<ModelOrderUser> list,filterList;
    private FilterOrderUser filter;

    public AdapterOrderUser(Context context, ArrayList<ModelOrderUser> list){
        this.context = context;
        this.list = list;
        this.filterList = list;
    }

    @NonNull
    @Override
    public AdapterOrderUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user,parent,false);
        return new AdapterOrderUserViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterOrderUserViewHolder holder, int position) {
        ModelOrderUser data = list.get(position);
        String orderId = data.getOrderId();
        String orderTime = data.getOrderTime();
        String orderBy = data.getOrderBy();
        String orderTo = data.getOrderTo();
        String orderStatus = data.getOrderStatus();
        String orderCost = data.getOrderCost();


        //shop name paoyar jonno
        loadShopInfo(data,holder);

        holder.binding.amountTV.setText("Amount ৳"+orderCost);
        holder.binding.orderIdTV.setText("Order Id: "+orderId);
        holder.binding.statusTV.setText(orderStatus);

        if (orderStatus.equals("In Progress")){
            holder.binding.statusTV.setTextColor(context.getResources().getColor(R.color.background_theme));
        }else if (orderStatus.equals("Completed")){
            holder.binding.statusTV.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }else if (orderStatus.equals("Cancelled")){
            holder.binding.statusTV.setTextColor(context.getResources().getColor(R.color.colorRed));
        }

        //convert in proper time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String dateTime = DateFormat.format("dd/MM/yyyy",calendar).toString();

        holder.binding.dateTV.setText(dateTime);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context,OrderDetailsUsersActivity.class);
            intent.putExtra("orderTo",orderTo);
            intent.putExtra("orderId",orderId);
            context.startActivity(intent);
        });
    }

    private void loadShopInfo(ModelOrderUser data, AdapterOrderUserViewHolder holder) {
        DatabaseReference dref = FirebaseDatabase.getInstance().getReference().child("Users");
        dref.child(data.getOrderTo()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String shopName = ""+snapshot.child("shopName").getValue();
                holder.binding.shopNameTV.setText(shopName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
            filter = new FilterOrderUser(this,filterList);
        }


        return filter;
    }

    public class AdapterOrderUserViewHolder extends RecyclerView.ViewHolder{

        RowOrderUserBinding binding;
        public AdapterOrderUserViewHolder(@NonNull View itemView){
            super(itemView);
            binding = RowOrderUserBinding.bind(itemView);
        }
    }
}