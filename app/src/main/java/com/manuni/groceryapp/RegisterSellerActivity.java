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
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.manuni.groceryapp.databinding.ActivityRegisterSellerBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterSellerActivity extends AppCompatActivity implements LocationListener {
    ActivityRegisterSellerBinding binding;
    ProgressDialog dialog,dialogForAccount;

    private FirebaseAuth auth;
    private DatabaseReference reference;
    private StorageReference storageReference;


    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int STORAGE_REQUEST_CODE = 102;

    private static final int REQUEST_FOR_CAMERA_CODE = 103;
    private static final int REQUEST_FOR_STORAGE_CODE = 104;


    private String[] location_permissions;
    private String[] camera_permissions;
    private String[] storage_permissions;


    private Uri imageUri;


    private LocationManager locationManager;
    private double latitude=0.0, longitude=0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(RegisterSellerActivity.this);
        dialog.setMessage("Please wait...");
        dialog.setCanceledOnTouchOutside(false);

        dialogForAccount = new ProgressDialog(RegisterSellerActivity.this);
        dialogForAccount.setTitle("Account");


        location_permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        camera_permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storage_permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        binding.gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkLocationPermission()) {
                    dialog.show();
                    detectLocation();
                } else {
                    requestLocationPermission();
                }
            }
        });

        binding.personImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });
        binding.backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        binding.registerBtnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputDataToDatabaseAndStorage();
            }
        });
    }


    private void inputDataToDatabaseAndStorage() {
        if (TextUtils.isEmpty(binding.fullNameET.getText().toString().trim())){
            Toast.makeText(this, "Insert full name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(binding.shopET.getText().toString().trim())){
            Toast.makeText(this, "Insert Shop Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(binding.phoneET.getText().toString().trim())){
            Toast.makeText(this, "Insert phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(binding.deliveryET.getText().toString().trim())){
            Toast.makeText(this, "Insert delivery fee", Toast.LENGTH_SHORT).show();
            return;
        }
        if (latitude==0.0 || longitude==0.0){
            Toast.makeText(this, "Please click on the GPS tracker", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailEt.getText().toString().trim()).matches()){
            Toast.makeText(this, "Invalid email address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (binding.passwordET.getText().toString().trim().length()<6){
            Toast.makeText(this, "You must take character 6 digits at least!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!binding.confirmPasswordET.getText().toString().trim().equals(binding.passwordET.getText().toString().trim())){
            Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();
            return;
        }
        createAccount();
    }


    private void createAccount(){
        dialogForAccount.setMessage("Creating Account...");
        dialogForAccount.show();
        auth.createUserWithEmailAndPassword(binding.emailEt.getText().toString().trim(),binding.passwordET.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                     saveDataInfoToDatabase();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialogForAccount.dismiss();
                Toast.makeText(RegisterSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDataInfoToDatabase() {
        dialogForAccount.setMessage("Saving Data to Database...");

        if (imageUri == null){
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("fullName",""+binding.fullNameET.getText().toString().trim());
            hashMap.put("shopName",""+binding.shopET.getText().toString().trim());
            hashMap.put("phoneNumber",""+binding.phoneET.getText().toString().trim());
            hashMap.put("deliveryFee",""+binding.deliveryET.getText().toString().trim());
            hashMap.put("countryName",""+binding.countryET.getText().toString().trim());
            hashMap.put("state",""+binding.stateET.getText().toString().trim());
            hashMap.put("city",""+binding.cityET.getText().toString().trim());
            hashMap.put("address",""+binding.completeAddressET.getText().toString().trim());
            hashMap.put("email",""+binding.emailEt.getText().toString().trim());
//            hashMap.put("password",""+binding.passwordET.getText().toString().trim());
//            hashMap.put("confirmPassword",""+binding.confirmPasswordET.getText().toString().trim());
            hashMap.put("uid",""+auth.getUid());
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("accountType","Seller");
            hashMap.put("shopOpen","true");
            hashMap.put("timestamp",""+System.currentTimeMillis());
            hashMap.put("online","true");
            hashMap.put("profileImage","");
            hashMap.put("accountStatus","blocked");

            reference.child(auth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    dialogForAccount.dismiss();
                    startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialogForAccount.dismiss();
                }
            });

        }else {
            String filePathAndName = "profile_images/"+""+auth.getUid();
            storageReference.child(filePathAndName).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                 Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                 while (!uriTask.isSuccessful());
                 Uri downloadUrl = uriTask.getResult();

                 if (uriTask.isSuccessful()){
                     HashMap<String,Object> hashMap = new HashMap<>();
                     hashMap.put("fullName",""+binding.fullNameET.getText().toString().trim());
                     hashMap.put("shopName",""+binding.shopET.getText().toString().trim());
                     hashMap.put("phoneNumber",""+binding.phoneET.getText().toString().trim());
                     hashMap.put("deliveryFee",""+binding.deliveryET.getText().toString().trim());
                     hashMap.put("countryName",""+binding.countryET.getText().toString().trim());
                     hashMap.put("state",""+binding.stateET.getText().toString().trim());
                     hashMap.put("city",""+binding.cityET.getText().toString().trim());
                     hashMap.put("address",""+binding.completeAddressET.getText().toString().trim());
                     hashMap.put("email",""+binding.emailEt.getText().toString().trim());
//            hashMap.put("password",""+binding.passwordET.getText().toString().trim());
//            hashMap.put("confirmPassword",""+binding.confirmPasswordET.getText().toString().trim());
                     hashMap.put("uid",""+auth.getUid());
                     hashMap.put("latitude",""+latitude);
                     hashMap.put("longitude",""+longitude);
                     hashMap.put("accountType","Seller");
                     hashMap.put("shopOpen","true");
                     hashMap.put("timestamp",""+System.currentTimeMillis());
                     hashMap.put("online","true");
                     hashMap.put("profileImage",""+downloadUrl);
                     hashMap.put("accountStatus","blocked");

                     reference.child(auth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void unused) {
                             dialogForAccount.dismiss();
                             startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivity.class));
                             finish();
                         }
                     }).addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             dialogForAccount.dismiss();
                         }
                     });
                 }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialogForAccount.dismiss();
                    Toast.makeText(RegisterSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

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
                binding.personImage.setImageURI(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(this, ""+ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        }
    }

    //    private void showImagePickDialog() {
//        String[] options = {"Camera", "Gallery"};
//        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterSellerActivity.this);
//        builder.setTitle("Pick Image")
//                .setItems(options, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        if (i == 0) {
//                            if (checkCameraPermission()) {
//                                pickUsingCamera();
//                            } else {
//                                requestCameraPermission();
//                            }
//
//                        } else {
//                            if (checkStoragePermission()) {
//                                pickFromGallery();
//                            } else {
//                                requestStoragePermission();
//                            }
//
//                        }
//                    }
//                }).show();
//    }
//
//    private void pickUsingCamera() {
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_image Title");
//        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_image Description");
//
//        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//        //startActivityForResult(intent, REQUEST_FOR_CAMERA_CODE);
//        resultLauncherForCamera.launch(intent);
//    }
//    private ActivityResultLauncher<Intent> resultLauncherForCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//        @Override
//        public void onActivityResult(ActivityResult result) {
//            if (result.getResultCode() == RESULT_OK){
//                try {
//                    binding.personImage.setImageURI(imageUri);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }
//    });
//
//    private void pickFromGallery() {
//        Intent pickGallery = new Intent(Intent.ACTION_PICK);
//        pickGallery.setType("image/*");
//        //startActivityForResult(pickGallery, REQUEST_FOR_STORAGE_CODE);
//        resultLauncherForGallery.launch(pickGallery);
//    }
//    private ActivityResultLauncher<Intent> resultLauncherForGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//        @Override
//        public void onActivityResult(ActivityResult result) {
//            if (result.getResultCode() == RESULT_OK){
//                Intent data = result.getData();
//                imageUri = data.getData();
//
//                try {
//                    binding.personImage.setImageURI(imageUri);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    });
//
//    private boolean checkStoragePermission() {
//        boolean result = ContextCompat.checkSelfPermission(RegisterSellerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//        return result;
//    }
//
//    private void requestStoragePermission() {
//        ActivityCompat.requestPermissions(RegisterSellerActivity.this, storage_permissions, STORAGE_REQUEST_CODE);
//    }
//
//    private boolean checkCameraPermission() {
//        boolean resultForCamera = ContextCompat.checkSelfPermission(RegisterSellerActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
//        boolean resultForStorage = ContextCompat.checkSelfPermission(RegisterSellerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//        return resultForCamera && resultForStorage;
//    }
//
//    private void requestCameraPermission() {
//        ActivityCompat.requestPermissions(RegisterSellerActivity.this, camera_permissions, CAMERA_REQUEST_CODE);
//    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(RegisterSellerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return result;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, location_permissions, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (permissionAccepted) {
                        detectLocation();
                    } else {
                        Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
//            case CAMERA_REQUEST_CODE: {
//                if (grantResults.length > 0) {
//                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
//                    if (cameraAccepted && storageAccepted) {
//                        pickUsingCamera();
//                    } else {
//                        Toast.makeText(this, "Camera permissions are required!", Toast.LENGTH_SHORT).show();
//                    }
//
//                }
//            }
//            break;
//            case STORAGE_REQUEST_CODE: {
//                if (grantResults.length > 0) {
//                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                    if (storageAccepted) {
//                        pickFromGallery();
//                    } else {
//                        Toast.makeText(this, "Storage permission is required!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }

        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("MissingPermission")
    private void detectLocation() {
        Toast.makeText(this, "Please wait for a while!", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }

    private void findAddress() {
        Geocoder geocoder;
        List<Address> addressList;
        geocoder = new Geocoder(RegisterSellerActivity.this,Locale.getDefault());
        try {
            addressList = geocoder.getFromLocation(latitude,longitude,1);
            String fullAddress = addressList.get(0).getAddressLine(0);
            String city = addressList.get(0).getLocality();
            String state = addressList.get(0).getAdminArea();
            String country = addressList.get(0).getCountryName();

            binding.completeAddressET.setText(fullAddress);
            binding.cityET.setText(city);
            binding.stateET.setText(state);
            binding.countryET.setText(country);
            dialog.dismiss();
        }catch (Exception e){
            dialog.dismiss();
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
        dialog.dismiss();
        Toast.makeText(this, "Please enable location service.", Toast.LENGTH_SHORT).show();
    }
}