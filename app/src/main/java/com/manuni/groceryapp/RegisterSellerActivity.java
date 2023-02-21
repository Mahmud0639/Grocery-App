package com.manuni.groceryapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.manuni.groceryapp.databinding.ActivityRegisterSellerBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterSellerActivity extends AppCompatActivity implements LocationListener {
    ActivityRegisterSellerBinding binding;
    ProgressDialog dialog, dialogForAccount, progressDialog;


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
    private double latitude = 0.0, longitude = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        // loadToSpinner();


        dialog = new ProgressDialog(RegisterSellerActivity.this);
        dialog.setMessage("Please wait...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        progressDialog = new ProgressDialog(this);


        dialogForAccount = new ProgressDialog(RegisterSellerActivity.this);
        dialogForAccount.setCancelable(false);
        dialogForAccount.setCanceledOnTouchOutside(false);
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
        if (TextUtils.isEmpty(binding.fullNameET.getText().toString().trim())) {
            Toast.makeText(this, "Insert full name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(binding.shopET.getText().toString().trim())) {
            Toast.makeText(this, "Insert Shop Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(binding.phoneET.getText().toString().trim())) {
            Toast.makeText(this, "Insert phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(binding.deliveryET.getText().toString().trim())) {
            Toast.makeText(this, "Insert delivery fee", Toast.LENGTH_SHORT).show();
            return;
        }
        if (latitude == 0.0 || longitude == 0.0) {
            Toast.makeText(this, "Please click on the GPS tracker", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailEt.getText().toString().trim()).matches()) {
            Toast.makeText(this, "Invalid email address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (binding.passwordET.getText().toString().trim().length() < 6) {
            Toast.makeText(this, "You must take character 6 digits at least!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!binding.confirmPasswordET.getText().toString().trim().equals(binding.passwordET.getText().toString().trim())) {
            Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();
            return;
        }
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected()) {
            createAccount();
        } else if (mobile.isConnected()) {
            createAccount();
        } else {
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
        }


    }


    private void createAccount() {
        dialogForAccount.setMessage("Creating Account...");
        dialogForAccount.show();
        auth.createUserWithEmailAndPassword(binding.emailEt.getText().toString().trim(), binding.passwordET.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                saveDataInfoToDatabase();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialogForAccount.dismiss();
                Toast.makeText(RegisterSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDataInfoToDatabase() {
        dialogForAccount.setMessage("Saving Data to Database...");

        if (imageUri == null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("fullName", "" + binding.fullNameET.getText().toString().trim());
            hashMap.put("shopName", "" + binding.shopET.getText().toString().trim());
            hashMap.put("phoneNumber", "" + binding.phoneET.getText().toString().trim());
            hashMap.put("deliveryFee", "" + binding.deliveryET.getText().toString().trim());
            hashMap.put("countryName", "" + binding.countryET.getText().toString().trim());
            hashMap.put("state", "" + binding.stateET.getText().toString().trim());
            hashMap.put("city", "" + binding.cityET.getText().toString().trim());
            hashMap.put("address", "" + binding.completeAddressET.getText().toString().trim());
            hashMap.put("email", "" + binding.emailEt.getText().toString().trim());
            hashMap.put("password", "" + binding.passwordET.getText().toString().trim());
            hashMap.put("confirmPassword", "" + binding.confirmPasswordET.getText().toString().trim());
            hashMap.put("uid", "" + auth.getUid());
            hashMap.put("latitude", "" + latitude);
            hashMap.put("longitude", "" + longitude);
            hashMap.put("accountType", "Seller");
            hashMap.put("shopOpen", "true");
            hashMap.put("timestamp", "" + System.currentTimeMillis());
            hashMap.put("online", "true");
            hashMap.put("profileImage", "");
            hashMap.put("accountStatus", "blocked");
            hashMap.put("shopCategory", "false");

            reference.child(auth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    dialogForAccount.dismiss();
                    checkSellerStatus();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialogForAccount.dismiss();
                }
            });

        } else {
            String filePathAndName = "profile_images/" + "" + auth.getUid();
            storageReference.child(filePathAndName).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                    while (!uriTask.isSuccessful()) ;
                    Uri downloadUrl = uriTask.getResult();

                    if (uriTask.isSuccessful()) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("fullName", "" + binding.fullNameET.getText().toString().trim());
                        hashMap.put("shopName", "" + binding.shopET.getText().toString().trim());
                        hashMap.put("phoneNumber", "" + binding.phoneET.getText().toString().trim());
                        hashMap.put("deliveryFee", "" + binding.deliveryET.getText().toString().trim());
                        hashMap.put("countryName", "" + binding.countryET.getText().toString().trim());
                        hashMap.put("state", "" + binding.stateET.getText().toString().trim());
                        hashMap.put("city", "" + binding.cityET.getText().toString().trim());
                        hashMap.put("address", "" + binding.completeAddressET.getText().toString().trim());
                        hashMap.put("email", "" + binding.emailEt.getText().toString().trim());
                        hashMap.put("password", "" + binding.passwordET.getText().toString().trim());
                        hashMap.put("confirmPassword", "" + binding.confirmPasswordET.getText().toString().trim());
                        hashMap.put("uid", "" + auth.getUid());
                        hashMap.put("latitude", "" + latitude);
                        hashMap.put("longitude", "" + longitude);
                        hashMap.put("accountType", "Seller");
                        hashMap.put("shopOpen", "true");
                        hashMap.put("timestamp", "" + System.currentTimeMillis());
                        hashMap.put("online", "true");
                        hashMap.put("profileImage", "" + downloadUrl);
                        hashMap.put("accountStatus", "blocked");
                        hashMap.put("shopCategory", "false");

                        reference.child(auth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                dialogForAccount.dismiss();
                                checkSellerStatus();
                                //startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivity.class));
                                //finish();
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
                    Toast.makeText(RegisterSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    private void checkSellerStatus() {
        reference.child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = "" + snapshot.child("accountStatus").getValue();
                    if (status.equals("blocked")) {

                        startActivity(new Intent(RegisterSellerActivity.this, TermsConditionActivity.class));

                        //binding.blockedTV.setVisibility(View.VISIBLE);
                        Toast.makeText(RegisterSellerActivity.this, "You are blocked.Please contact with your admin.", Toast.LENGTH_SHORT).show();
                    } else {
                        String shopCat = "" + snapshot.child("shopCategory").getValue();
                        if (shopCat.equals("false")) {
                            startActivity(new Intent(RegisterSellerActivity.this, TermsConditionActivity.class));
                        } else {
                            // binding.blockedTV.setVisibility(View.GONE);
                            startActivity(new Intent(RegisterSellerActivity.this, MainSellerActivity.class));
                            finish();
                        }


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void showImagePickDialog() {
        ImagePicker.with(this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)//Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            imageUri = data.getData();

            try {
                binding.personImageShow.setImageURI(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "" + ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        }
    }


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
        geocoder = new Geocoder(RegisterSellerActivity.this, Locale.getDefault());
        try {
            addressList = geocoder.getFromLocation(latitude, longitude, 1);
            String fullAddress = addressList.get(0).getAddressLine(0);
            String city = addressList.get(0).getLocality();
            String state = addressList.get(0).getAdminArea();
            String country = addressList.get(0).getCountryName();

            binding.completeAddressET.setText(fullAddress);
            binding.cityET.setText(city);
            binding.stateET.setText(state);
            binding.countryET.setText(country);
            dialog.dismiss();
        } catch (Exception e) {
            dialog.dismiss();
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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