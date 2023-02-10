package com.manuni.groceryapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.CategorySampleBinding;

import java.util.ArrayList;

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
    }

    @NonNull
    @Override
    public DeleteCategoryAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_sample,parent,false);
        return new DeleteCategoryAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeleteCategoryAdapterViewHolder holder, int position) {

        DeleteCategoryModel data = list.get(position);

        String categoryId = data.getCategoryId();
        String categoryName = data.getCategory();

        holder.binding.category.setText(categoryName);

        holder.binding.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Warning!");
                builder.setMessage("Are you sure you want to delete "+categoryName+"?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressDialog.show();
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Categories");
                        dbRef.child(auth.getUid()).child(categoryId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                Toast.makeText(context, ""+categoryName+" Removed successfully!", Toast.LENGTH_SHORT).show();
                                deleteCategoryProduct(categoryName);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

    }

    private void deleteCategoryProduct(String cateName) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(auth.getUid()).child("Products").orderByChild("productCategory").equalTo(cateName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String id = ""+dataSnapshot.child("productId").getValue();

                        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("Users");
                        dRef.child(auth.getUid()).child("Products").child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                Toast.makeText(context, "Deleted all category product!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
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
