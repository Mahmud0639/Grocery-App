package com.manuni.groceryapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.manuni.groceryapp.databinding.ActivityAddProductBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class AddProductActivity extends AppCompatActivity {
    ActivityAddProductBinding binding;
    private static final int CAMERA_REQUEST_CODE = 100;
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

        loadToSpinner();


        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    //binding.discountPriceET.setVisibility(View.GONE);
                    binding.discountNoteET.setVisibility(View.VISIBLE);
                }else {
                   // binding.discountPriceET.setVisibility(View.GONE);
                    binding.discountNoteET.setVisibility(View.GONE);
                }
            }
        });

        binding.productIconIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });
        binding.categoryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });

        binding.addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });
        binding.backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }
    private String productTitle,productDescription,productCategory,productQuantity,originalPrice,discountPrice,discountNote;
    private  boolean discountAvailable = false;
    private double discountNoteSum=0.0;

    private void inputData(){
        productTitle = binding.titleET.getText().toString().trim();
        productDescription = binding.descriptionET.getText().toString().trim();
        productCategory = binding.categoryTV.getText().toString().trim();
        productQuantity = binding.quantityET.getText().toString().trim();
        originalPrice = binding.priceET.getText().toString().trim();

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

            double disNote = Double.parseDouble(discountNote);
            double oriPrice = Double.parseDouble(originalPrice);

            double afterDiscount = disNote * oriPrice/100;

            discountNoteSum = oriPrice - afterDiscount;



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

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
            reference.child(auth.getUid()).child("Products").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, "Product added successfully!", Toast.LENGTH_SHORT).show();

                    clearData();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else {
            String pathAndName = "Product_Images/"+""+timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(pathAndName);
            storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
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

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
                        reference.child(auth.getUid()).child("Products").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                Toast.makeText(AddProductActivity.this, "Product added successfully!", Toast.LENGTH_SHORT).show();
                                clearData();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
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
        builder.setTitle("Product Category").setItems(data, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
             String category = data[i];//ekhane kono category select kora hole seta ei variable er moddhe chole ashbe
                binding.categoryTV.setText(category);
            }
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
            imageUri = data.getData();

            try {
                binding.productIconIV.setImageURI(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(this, ""+ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadToSpinner() {


        DatabaseReference myDbRef = FirebaseDatabase.getInstance().getReference().child("Categories");
        myDbRef.child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
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
            public void onCancelled(DatabaseError error) {

            }
        });
    }


//    private void showImagePickDialog(){
//        String[] options = {"Camera","Gallery"};
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                if (i==0){
//                    //camera
//                    if (checkCameraPermission()){
//                        imagePickFromCamera();
//                    }else {
//                        requestCameraPermissions();
//                    }
//                }else {
//                    //gallery
//                    if (checkStoragePermission()){
//                        imagePickFromGallery();
//                    }else {
//                        requestStoragePermission();
//                    }
//                }
//            }
//        }).show();
//    }
//    private void imagePickFromGallery(){
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
//        resultLauncherForGallery.launch(intent);
//    }
//    private ActivityResultLauncher<Intent> resultLauncherForGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//        @Override
//        public void onActivityResult(ActivityResult result) {
//            if (result.getResultCode()==RESULT_OK){
//                Intent data = result.getData();
//                imageUri = data.getData();
//
//                try {
//                    binding.productIconIV.setImageURI(imageUri);
//                } catch (Exception e) {
//                    Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                }
//            }else {
//                Toast.makeText(AddProductActivity.this, "Please try again!", Toast.LENGTH_SHORT).show();
//            }
//        }
//    });
//
//    private void imagePickFromCamera(){
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_image Title");
//        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_image Description");
//
//        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
//
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
//
//        resultLauncherForCamera.launch(intent);
//
//    }
//    private ActivityResultLauncher<Intent> resultLauncherForCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//        @Override
//        public void onActivityResult(ActivityResult result) {
//            if (result.getResultCode()==RESULT_OK){
//                try {
//                    binding.productIconIV.setImageURI(imageUri);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }else {
//                Toast.makeText(AddProductActivity.this, "Try again!", Toast.LENGTH_SHORT).show();
//            }
//        }
//    });
//    private boolean checkStoragePermission(){
//        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
//        return result;
//    }
//    private void requestStoragePermission(){
//        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
//    }
//    private boolean checkCameraPermission(){
//        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED;
//        boolean result2 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
//
//        return result1 && result2;
//    }
//    private void requestCameraPermissions(){
//        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode){
//            case CAMERA_REQUEST_CODE:{
//                if (grantResults.length>0){
//                    boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
//                    boolean storageAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
//                    if (cameraAccepted && storageAccepted){
//                        imagePickFromCamera();
//                    }else {
//                        Toast.makeText(this, "Camera permission required!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//            break;
//            case STORAGE_REQUEST_CODE:{
//                if (grantResults.length>0){
//                    boolean storageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
//                    if (storageAccepted){
//                        imagePickFromGallery();
//                    }else {
//                        Toast.makeText(this,"Storage permission required!",Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        }
//    }
}