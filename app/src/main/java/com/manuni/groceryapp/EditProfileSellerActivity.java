package com.manuni.groceryapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.manuni.groceryapp.databinding.ActivityEditProfileSellerBinding;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EditProfileSellerActivity extends AppCompatActivity implements LocationListener {
    ActivityEditProfileSellerBinding binding;
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    private ProgressDialog progressDialog;

    private FirebaseAuth auth;
    private LocationManager locationManager;
    private double latitude = 0.0;
    private double longitude = 0.0;

    private Uri imageUri;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        auth = FirebaseAuth.getInstance();


        checkUser();





        dialog = new ProgressDialog(this);
        dialog.setTitle("Please wait");
        dialog.setCanceledOnTouchOutside(false);

        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        binding.gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkLocationPermission()){
                    detectLocation();
                }else {
                    requestLocationPermission();
                }
            }
        });
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });
        binding.profileIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePicDialog();
            }
        });
    }

    private String name,shopName,deliveryFee,phone,country,state,city,address;
    private boolean shopOpen;
    private void inputData() {
        name = binding.fullNameET.getText().toString().trim();
        shopName = binding.shopET.getText().toString().trim();
        deliveryFee = binding.deliveryET.getText().toString().trim();
        phone = binding.phoneET.getText().toString().trim();
        country = binding.countryET.getText().toString().trim();
        state = binding.stateET.getText().toString().trim();
        city = binding.cityET.getText().toString().trim();
        address = binding.completeAddressET.getText().toString().trim();

        shopOpen = binding.shopOpenSwitch.isChecked();//switch open ache kina? eta check kora jabe ei method er maddhome
        updateProfile();
    }
    private void updateProfile(){
        progressDialog.setMessage("Updating profile...");
        progressDialog.show();

        if (imageUri==null){
            //update without image

            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("fullName",""+name);
            hashMap.put("shopName",""+shopName);
            hashMap.put("phoneNumber",""+phone);
            hashMap.put("deliveryFee",""+deliveryFee);
            hashMap.put("countryName",""+country);
            hashMap.put("state",""+state);
            hashMap.put("city",""+city);
            hashMap.put("address",""+address);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("shopOpen",""+shopOpen);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
            databaseReference.child(auth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfileSellerActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfileSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else {
            //update with image
            String filePathAndName = "profile_images/"+""+auth.getUid();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                  Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                  while (!uriTask.isSuccessful());
                  Uri downloadUri = uriTask.getResult();

                  if (uriTask.isSuccessful()){
                      HashMap<String,Object> hashMap = new HashMap<>();
                      hashMap.put("fullName",""+name);
                      hashMap.put("shopName",""+shopName);
                      hashMap.put("phoneNumber",""+phone);
                      hashMap.put("deliveryFee",""+deliveryFee);
                      hashMap.put("countryName",""+country);
                      hashMap.put("state",""+state);
                      hashMap.put("city",""+city);
                      hashMap.put("address",""+address);
                      hashMap.put("latitude",""+latitude);
                      hashMap.put("longitude",""+longitude);
                      hashMap.put("shopOpen",""+shopOpen);
                      hashMap.put("profileImage",""+downloadUri);

                      DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
                      databaseReference.child(auth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                          @Override
                          public void onSuccess(Void unused) {
                              progressDialog.dismiss();
                              Toast.makeText(EditProfileSellerActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                          }
                      }).addOnFailureListener(new OnFailureListener() {
                          @Override
                          public void onFailure(@NonNull Exception e) {
                              progressDialog.dismiss();
                              Toast.makeText(EditProfileSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                          }
                      });
                  }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfileSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkUser() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null){
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
            finish();
        }else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.orderByChild("uid").equalTo(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String accountType = ""+dataSnapshot.child("accountType").getValue();
                    String address = ""+dataSnapshot.child("address").getValue();
                    String city = ""+dataSnapshot.child("city").getValue();
                    String state = ""+dataSnapshot.child("state").getValue();
                    String country = ""+dataSnapshot.child("countryName").getValue();
                    String deliveryFee = ""+dataSnapshot.child("deliveryFee").getValue();
                    String email = ""+dataSnapshot.child("email").getValue();
                     latitude = Double.parseDouble(""+dataSnapshot.child("latitude").getValue());
                     longitude = Double.parseDouble(""+dataSnapshot.child("longitude").getValue());
                    String name = ""+dataSnapshot.child("fullName").getValue();
                    String online = ""+dataSnapshot.child("online").getValue();
                    String phone = ""+dataSnapshot.child("phoneNumber").getValue();
                    String profileImage = ""+dataSnapshot.child("profileImage").getValue();
                    String timestamp = ""+dataSnapshot.child("timestamp").getValue();
                    String shopName = ""+dataSnapshot.child("shopName").getValue();
                    String shopOpen = ""+dataSnapshot.child("shopOpen").getValue();
                    String uid = ""+dataSnapshot.child("uid").getValue();

                    binding.fullNameET.setText(name);
                    binding.phoneET.setText(phone);
                    binding.countryET.setText(country);
                    binding.stateET.setText(state);
                    binding.cityET.setText(city);
                    binding.shopET.setText(shopName);
                    binding.completeAddressET.setText(address);
                    binding.deliveryET.setText(deliveryFee);

                    if (shopOpen.equals("true")){
                        binding.shopOpenSwitch.setChecked(true);
                    }else {
                        binding.shopOpenSwitch.setChecked(false);
                    }

                    try {
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(binding.profileIV);
                    } catch (Exception e) {
                        binding.profileIV.setImageResource(R.drawable.ic_person_gray);
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showImagePicDialog(){
        String[] options = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Images").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i==0){
                    //camera
                    if (checkCameraPermission()){
                        pickFromCamera();
                    }else {
                        requestCameraPermission();
                    }
                }else {
                    //gallery
                    if (checkStoragePermission()){
                        pickFromGallery();
                    }else {
                        requestStoragePermission();
                    }
                }
            }
        }).show();
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        resultLauncherForGallery.launch(intent);

    }
    private ActivityResultLauncher<Intent> resultLauncherForGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode()==RESULT_OK){
               Intent data = result.getData();
               imageUri = data.getData();

                try {
                    binding.profileIV.setImageURI(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                Toast.makeText(EditProfileSellerActivity.this, "Please try again!", Toast.LENGTH_SHORT).show();
            }
        }
    });
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;

        return result1 && result2;
    }
    private void pickFromCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_image Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);

        resultLauncherForCamera.launch(intent);

    }
    private ActivityResultLauncher<Intent> resultLauncherForCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode()==RESULT_OK){
                try {
                    binding.profileIV.setImageURI(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                Toast.makeText(EditProfileSellerActivity.this, "Try Again!", Toast.LENGTH_SHORT).show();
            }
        }
    });
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }
    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    @SuppressLint("MissingPermission")
    private void detectLocation(){
        dialog.show();
        Toast.makeText(this, "Please wait for a while...", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
    }
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,locationPermissions,LOCATION_REQUEST_CODE);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }
    private void findAddress(){
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
          addresses =  geocoder.getFromLocation(latitude,longitude,1);
          String address = addresses.get(0).getAddressLine(0);
          String city = addresses.get(0).getLocality();
          String state = addresses.get(0).getAdminArea();
          String country = addresses.get(0).getCountryName();

          binding.completeAddressET.setText(address);
          binding.cityET.setText(city);
          binding.stateET.setText(state);
          binding.countryET.setText(country);

          dialog.dismiss();

        } catch (IOException e) {
            dialog.dismiss();
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        dialog.dismiss();
        Toast.makeText(this, "Location is disabled.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean locationAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        detectLocation();
                    }else {
                        Toast.makeText(this, "Location Permission is required!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }else {
                        Toast.makeText(this, "Camera permission required!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickFromGallery();
                    }else {
                        Toast.makeText(this, "Gallery Permission required!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }


}