package com.manuni.groceryapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.manuni.groceryapp.databinding.ActivityAddProductBinding;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class AddProductActivity extends AppCompatActivity {
    ActivityAddProductBinding binding;
    public static final int STORAGE_REQUEST_CODE = 200;

    String[] data;
    ArrayList<String> dataList;


    private String[] cameraPermissions;
    private String[] storagePermission;

    private Uri imageUri;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // binding.discountPriceET.setVisibility(View.GONE);
        binding.discountNoteET.setVisibility(View.GONE);

        auth = FirebaseAuth.getInstance();

        try {
            loadToSpinner();
        } catch (Exception e) {
            e.printStackTrace();
        }


        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        binding.discountSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked){
                //binding.discountPriceET.setVisibility(View.GONE);
                try {
                    binding.discountNoteET.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                // binding.discountPriceET.setVisibility(View.GONE);
                try {
                    binding.discountNoteET.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        binding.productIconIV.setOnClickListener(view -> showImagePickDialog());
        binding.categoryTV.setOnClickListener(view -> categoryDialog());

        binding.addProductBtn.setOnClickListener(view -> inputData());
        binding.backArrowBtn.setOnClickListener(view -> onBackPressed());


    }
    private String productTitle,productDescription,productCategory,productQuantity,originalPrice,discountPrice,discountNote,productBrand;
    private  boolean discountAvailable = false;
    private double discountNoteSum=0.0;

    private void inputData(){
        productTitle = binding.titleET.getText().toString().trim();
        productDescription = binding.descriptionET.getText().toString().trim();
        productCategory = binding.categoryTV.getText().toString().trim();
        productQuantity = binding.quantityET.getText().toString().trim();
        originalPrice = binding.priceET.getText().toString().trim();
        productBrand = binding.brandET.getText().toString().trim();

        discountAvailable = binding.discountSwitch.isChecked();//true or false...jodi check thake tahole discountAvailable false theke true te update hoye jabe


        if (TextUtils.isEmpty(productTitle)){
            Toast.makeText(this, "Title required!", Toast.LENGTH_SHORT).show();
            return;//don't proceed further
        }
        if (TextUtils.isEmpty(productDescription)){
            Toast.makeText(this, "Description required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productCategory)){
            Toast.makeText(this, "Select a category!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productBrand)){
            Toast.makeText(this, "Brand is empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productQuantity)){
            Toast.makeText(this, "Put quantity!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(originalPrice)){
            Toast.makeText(this, "Original Price required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (discountAvailable){//ekhane discountAvailable hocche true

            //discountPrice = binding.discountPriceET.getText().toString().trim();
            discountNote = binding.discountNoteET.getText().toString().trim();

            /*double disNote = 0;
            double oriPrice = 0;*/
            try {
                double disNote   = Double.parseDouble(discountNote);
                double oriPrice = Double.parseDouble(originalPrice);

                double afterDiscount = disNote * oriPrice/100;

                discountNoteSum = oriPrice - afterDiscount;






            } catch (NumberFormatException e) {
                e.printStackTrace();
            }





//            if (TextUtils.isEmpty(discountPrice)){
//                Toast.makeText(this, "Discount price required!", Toast.LENGTH_SHORT).show();
//                return;
//            }
            if (TextUtils.isEmpty(discountNote)){
                Toast.makeText(this, "Discount Note required!", Toast.LENGTH_SHORT).show();
                return;
            }



        }else {
            //switchAvailable = true;
            //discountPrice="0";
            discountNote = "";
            discountNoteSum = 0.0;


        }

        addProductToDb();

    }
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private void addProductToDb(){
        progressDialog.setMessage("Adding product...");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();
        if (imageUri==null){
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("productId",""+timestamp);
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productDesc",""+productDescription);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("productQuantity",""+productQuantity);
            hashMap.put("productIcon","");
            hashMap.put("productOriginalPrice",""+originalPrice);
            hashMap.put("productDiscountPrice",""+discountNoteSum);
            hashMap.put("productDiscountNote",""+discountNote);
            hashMap.put("productDiscountAvailable",""+discountAvailable);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("uid",""+auth.getUid());
            hashMap.put("productAvailable","true");
            hashMap.put("productBrand",""+productBrand);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
            reference.child(Objects.requireNonNull(auth.getUid())).child("Products").child(timestamp).setValue(hashMap).addOnSuccessListener(unused -> {
                progressDialog.dismiss();
                Toast.makeText(AddProductActivity.this, "Product added successfully!", Toast.LENGTH_SHORT).show();

                clearData();
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        }else {
            String pathAndName = "Product_Images/"+""+timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(pathAndName);
            storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();
                if (uriTask.isSuccessful()){

                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("productId",""+timestamp);
                    hashMap.put("productTitle",""+productTitle);
                    hashMap.put("productDesc",""+productDescription);
                    hashMap.put("productCategory",""+productCategory);
                    hashMap.put("productQuantity",""+productQuantity);
                    hashMap.put("productIcon",""+downloadUri);
                    hashMap.put("productOriginalPrice",""+originalPrice);
                    hashMap.put("productDiscountPrice",""+discountNoteSum);
                    hashMap.put("productDiscountNote",""+discountNote);
                    hashMap.put("productDiscountAvailable",""+discountAvailable);
                    hashMap.put("timestamp",""+timestamp);
                    hashMap.put("uid",""+auth.getUid());
                    hashMap.put("productAvailable","true");
                    hashMap.put("productBrand",""+productBrand);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
                    reference.child(Objects.requireNonNull(auth.getUid())).child("Products").child(timestamp).setValue(hashMap).addOnSuccessListener(unused -> {
                        progressDialog.dismiss();
                        Toast.makeText(AddProductActivity.this, "Product added successfully!", Toast.LENGTH_SHORT).show();
                        clearData();
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void clearData() {
        binding.titleET.setText("");
        binding.descriptionET.setText("");
        binding.categoryTV.setText("");
        binding.quantityET.setText("");
        binding.priceET.setText("");
        //binding.discountPriceET.setText("");
        binding.discountNoteET.setText("");
        binding.productIconIV.setImageResource(R.drawable.ic_shopping_cart_theme_color);
        imageUri = null;
    }

    private void categoryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category").setItems(data, (dialogInterface, i) -> {
            String category = data[i];//ekhane kono category select kora hole seta ei variable er moddhe chole ashbe
            binding.categoryTV.setText(category);
        }).show();
    }

    private void showImagePickDialog(){
        ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)//Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            assert data != null;
            imageUri = data.getData();

            try {
                binding.productIconIVShow.setImageURI(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(this, ""+ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadToSpinner() {


        DatabaseReference myDbRef = FirebaseDatabase.getInstance().getReference().child("Categories");
        myDbRef.child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList = new ArrayList<>();
                if (snapshot.exists()){
                    dataList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String categories = ""+dataSnapshot.child("category").getValue();
                        dataList.add(categories);
                    }
                    dataList.add(0,"All");
                    data = dataList.toArray(new String[dataList.size()]);



                }
                progressDialog.dismiss();


                //adapter.notifyDataSetChanged();

            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}