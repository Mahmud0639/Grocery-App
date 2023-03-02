package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.CategorySampleBinding;

import java.util.ArrayList;
import java.util.Objects;

public class DeleteCategoryAdapter extends RecyclerView.Adapter<DeleteCategoryAdapter.DeleteCategoryAdapterViewHolder>{
    private Context context;
    private ArrayList<DeleteCategoryModel> list;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;


    public DeleteCategoryAdapter(Context context, ArrayList<DeleteCategoryModel> list) {
        this.context = context;
        this.list = list;
        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting records...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
    }

    @NonNull
    @Override
    public DeleteCategoryAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_sample,parent,false);
        return new DeleteCategoryAdapterViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull DeleteCategoryAdapterViewHolder holder, @SuppressLint("RecyclerView") int position) {

        DeleteCategoryModel data = list.get(position);

        String categoryId = data.getCategoryId();
        String categoryName = data.getCategory();

        holder.binding.category.setText(categoryName);

        holder.binding.deleteBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Warning!");
            builder.setMessage("Are you sure you want to delete "+categoryName+"?");
            builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                progressDialog.show();
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Categories");
                dbRef.child(Objects.requireNonNull(auth.getUid())).child(categoryId).removeValue().addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(context, ""+categoryName+" Removed successfully!", Toast.LENGTH_SHORT).show();

                    notifyItemRemoved(position);
                    notifyItemChanged(position);
                    notifyDataSetChanged();
                    list.clear();

                    deleteCategoryProduct(categoryName,position);
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }).setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

        });

    }

    private void deleteCategoryProduct(String cateName,int pos) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(Objects.requireNonNull(auth.getUid())).child("Products").orderByChild("productCategory").equalTo(cateName).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String id = ""+dataSnapshot.child("productId").getValue();

                        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("Users");
                        dRef.child(auth.getUid()).child("Products").child(id).removeValue().addOnSuccessListener(unused -> {

                            Toast.makeText(context, "Deleted all category product!", Toast.LENGTH_SHORT).show();
                           notifyItemRemoved(pos);
                            notifyItemChanged(pos);
                            notifyDataSetChanged();
                            list.clear();
                        }).addOnFailureListener(e -> Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
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

    public class DeleteCategoryAdapterViewHolder extends RecyclerView.ViewHolder{

        CategorySampleBinding binding;
        public DeleteCategoryAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = CategorySampleBinding.bind(itemView);
        }
    }
}
