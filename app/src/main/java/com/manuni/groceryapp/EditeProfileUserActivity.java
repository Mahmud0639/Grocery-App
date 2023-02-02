package com.manuni.groceryapp;

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

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
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
import com.manuni.groceryapp.databinding.ActivityEditeProfileUserBinding;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EditeProfileUserActivity extends AppCompatActivity implements LocationListener{
    ActivityEditeProfileUserBinding binding;
    private LocationManager locationManager;
    private double latitude=0.0,longitude=0.0;

    private ProgressDialog progressDialog;

    private FirebaseAuth auth;

    private static final int LOCATION_PERMISSION_CODE = 100;
    public static final int CAMERA_PERMISSION_CODE = 200;
    public static final int STORAGE_PERMISSION_CODE = 300;

    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditeProfileUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        checkUser();


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

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
                    showImagePickDialog();
                }
            });
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
                binding.profileIV.setImageURI(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(this, ""+ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
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

    private String name,phone,country,state,city,address;

    private void inputData() {
        name = binding.fullNameET.getText().toString().trim();
        phone = binding.phoneET.getText().toString().trim();
        country = binding.countryET.getText().toString().trim();
        state = binding.stateET.getText().toString().trim();
        city = binding.cityET.getText().toString().trim();
        address = binding.completeAddressET.getText().toString().trim();

        updateProfile();
    }
    private void updateProfile(){
        progressDialog.setMessage("Updating profile...");
        progressDialog.show();

        if (imageUri==null){
            //update without image

            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("fullName",""+name);
            hashMap.put("phoneNumber",""+phone);
            hashMap.put("countryName",""+country);
            hashMap.put("state",""+state);
            hashMap.put("city",""+city);
            hashMap.put("address",""+address);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",longitude);


            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
            databaseReference.child(auth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progressDialog.dismiss();
                    Toast.makeText(EditeProfileUserActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditeProfileUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        hashMap.put("phoneNumber",""+phone);
                        hashMap.put("countryName",""+country);
                        hashMap.put("state",""+state);
                        hashMap.put("city",""+city);
                        hashMap.put("address",""+address);
                        hashMap.put("latitude",""+latitude);
                        hashMap.put("longitude",longitude);
                        hashMap.put("profileImage",""+downloadUri);

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
                        databaseReference.child(auth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                Toast.makeText(EditeProfileUserActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(EditeProfileUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditeProfileUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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
                    String email = ""+dataSnapshot.child("email").getValue();
                    latitude = Double.parseDouble(""+dataSnapshot.child("latitude").getValue());
                    longitude = Double.parseDouble(""+dataSnapshot.child("longitude").getValue());
                    String name = ""+dataSnapshot.child("fullName").getValue();
                    String online = ""+dataSnapshot.child("online").getValue();
                    String phone = ""+dataSnapshot.child("phoneNumber").getValue();
                    String profileImage = ""+dataSnapshot.child("profileImage").getValue();
                    String timestamp = ""+dataSnapshot.child("timestamp").getValue();
                    String uid = ""+dataSnapshot.child("uid").getValue();

                    binding.fullNameET.setText(name);
                    binding.phoneET.setText(phone);
                    binding.countryET.setText(country);
                    binding.stateET.setText(state);
                    binding.cityET.setText(city);
                    binding.completeAddressET.setText(address);



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
//    private void showImagePickDialog(){
//        String[] options = {"Camera","Gallery"};
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Images Pick").setItems(options, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                if (i==0){
//                    //camera
//                    if (checkCameraPermission()){
//                        pickFromCamera();
//                    }else {
//                        requestPermissionForCamera();
//                    }
//                }else {
//                    //gallery
//                    if (checkStoragePermission()){
//                        pickFromStorage();
//                    }else {
//                        requestPermissionForStorage();
//                    }
//                }
//            }
//        }).show();
//    }
//    private boolean checkCameraPermission(){
//        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED;
//        boolean result2 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
//        return result1 && result2;
//    }
//    private void pickFromCamera(){
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
//                    binding.profileIV.setImageURI(imageUri);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }else {
//                Toast.makeText(EditeProfileUserActivity.this, "Try Again!", Toast.LENGTH_SHORT).show();
//            }
//        }
//    });
//    private void requestPermissionForCamera(){
//        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_PERMISSION_CODE);
//    }
//    private boolean checkStoragePermission(){
//        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
//        return result;
//    }
//    private void pickFromStorage(){
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
//
//        resultLauncherForStorage.launch(intent);
//    }
//    private ActivityResultLauncher<Intent> resultLauncherForStorage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//        @Override
//        public void onActivityResult(ActivityResult result) {
//            if (result.getResultCode()==RESULT_OK){
//                Intent data = result.getData();
//                imageUri = data.getData();
//
//                try {
//                    binding.profileIV.setImageURI(imageUri);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }else {
//                Toast.makeText(EditeProfileUserActivity.this, "Please try again!", Toast.LENGTH_SHORT).show();
//            }
//        }
//    });
//    private void requestPermissionForStorage(){
//        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_PERMISSION_CODE);
//
//    }

    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    @SuppressLint("MissingPermission")
    private void detectLocation(){
        Toast.makeText(this, "Please wait for a while...", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
    }
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,locationPermissions,LOCATION_PERMISSION_CODE);
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
            addresses = geocoder.getFromLocation(latitude,longitude,1);
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            binding.completeAddressET.setText(address);
            binding.cityET.setText(city);
            binding.stateET.setText(state);
            binding.countryET.setText(country);

        } catch (IOException e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

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
        Toast.makeText(this, "Location has disabled.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case LOCATION_PERMISSION_CODE:{
                if (grantResults.length>0){
                    boolean locationAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        detectLocation();
                    }else {
                        Toast.makeText(this, "Location permission required!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
//            case CAMERA_PERMISSION_CODE:{
//                if (grantResults.length>0){
//                    boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
//                    boolean storageAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
//
//                    if (cameraAccepted && storageAccepted){
//                        pickFromCamera();
//                    }else {
//                        Toast.makeText(this, "Camera permission required!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//            break;
//            case STORAGE_PERMISSION_CODE:{
//                if (grantResults.length>0){
//                    boolean storageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
//                    if (storageAccepted){
//                        pickFromStorage();
//                    }else {
//                        Toast.makeText(this, "Please try again!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
        }
    }


}