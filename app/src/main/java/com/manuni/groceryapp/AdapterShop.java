package com.manuni.groceryapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.RowShopBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.AdapterShopViewHolder>{
    private final Context context;
    public ArrayList<ModelShop> list;

    public AdapterShop(Context context, ArrayList<ModelShop> list){
        this.context = context;
        this.list = list;
    }


    @NonNull
    @Override
    public AdapterShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop,parent,false);
        return new AdapterShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterShopViewHolder holder, int position) {
        ModelShop data = list.get(position);
       String accountType = data.getAccountType();
        String address = data.getAddress();
        String city = data.getCity();
        String country = data.getCountryName();
        String deliveryFee = data.getDeliveryFee();
        String email = data.getEmail();
        String latitude = data.getLatitude();
        String longitude = data.getLongitude();
        String online = data.getOnline();
        String name = data.getFullName();
        String phone = data.getPhoneNumber();
        String uid = data.getUid();
        String timestamp = data.getTimestamp();
        String shopOpen = data.getShopOpen();
        String state = data.getState();
        String profileImage = data.getProfileImage();
        String shopName = data.getShopName();

        loadRatings(data,holder);

        holder.binding.shopNameTV.setText(shopName);
        holder.binding.addressTV.setText(address);
        holder.binding.phoneTV.setText(phone);

        if (online.equals("true")){
            holder.binding.onlineIV.setVisibility(View.VISIBLE);
        }else {
            holder.binding.onlineIV.setVisibility(View.GONE);
        }

        if (shopOpen.equals("true")){
            holder.binding.closedTV.setVisibility(View.GONE);
        }else {
            holder.binding.closedTV.setVisibility(View.VISIBLE);
        }

        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_store_gray).into(holder.binding.shopIV);
        }catch (Exception e){
            holder.binding.shopIV.setImageResource(R.drawable.ic_baseline_store_gray);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,ShopDetailsActivity.class);
                intent.putExtra("shopUid",uid);
                context.startActivity(intent);
            }
        });

    }
    private float ratingSum = 0;
    private void loadRatings(ModelShop data, AdapterShopViewHolder holder) {

        String shopUid = data.getUid();


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(shopUid).child("Ratings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ratingSum = 0;
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    float rating = Float.parseFloat(""+dataSnapshot.child("ratings").getValue());//e.g 4.5
                    ratingSum = ratingSum+rating;


                }


                long numberOfReviews = snapshot.getChildrenCount();
                float avgOfReviews = ratingSum/numberOfReviews;

                holder.binding.ratingBar.setRating(avgOfReviews);
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

    public class AdapterShopViewHolder extends RecyclerView.ViewHolder{
        RowShopBinding binding;

        public AdapterShopViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = RowShopBinding.bind(itemView);
        }
    }
}
