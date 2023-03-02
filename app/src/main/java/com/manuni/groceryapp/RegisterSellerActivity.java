package com.manuni.groceryapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterSellerActivity extends AppCompatActivity implements LocationListener {
    ActivityRegisterSellerBinding binding;
    ProgressDialog dialog, dialogForAccount, progressDialog;


    private FirebaseAuth auth;
    private DatabaseReference reference;
    private StorageReference storageReference;
    double delivery = 0.0;


    private static final int LOCATION_REQUEST_CODE = 100;



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
                    try {
                        detectLocation();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        requestLocationPermission();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        binding.fullNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               /* Pattern pattern = Pattern.compile("^[A-Za-z\\s]+$");//allow only space and alphabets
                Matcher matcher = pattern.matcher(charSequence);
                boolean isNameContains = matcher.find();*/

               if (charSequence.length()<=3){
                    binding.textInputFullName.setError("Name is too small.");

                }else if (charSequence.length()>=20){
                    binding.textInputFullName.setError("Name is too long. Put under 20 char.");

                } else{
                    binding.textInputFullName.setError(null);
                    binding.textInputFullName.setHelperText("Valid Name.");
                    binding.textInputFullName.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.shopET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length()<=3){
                    binding.textInputShopName.setError("Shop name is too small.");

                }else if (charSequence.length()>=20){
                    binding.textInputShopName.setError("Shop name is too long.");

                }else {
                    binding.textInputShopName.setError(null);
                    binding.textInputShopName.setHelperText("Valid shop name.");
                    binding.textInputShopName.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.emailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                 if (!Patterns.EMAIL_ADDRESS.matcher(charSequence).matches()){
                    binding.textInputEmail.setError("Invalid Email Pattern.");

                } else {
                    binding.textInputEmail.setError(null);
                    binding.textInputEmail.setErrorEnabled(false);
                    binding.textInputEmail.setHelperText("Valid email address.");
                    binding.textInputEmail.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.passwordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()>=6){
                    Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
                    Matcher matcher = pattern.matcher(charSequence);
                    boolean isPwdContains = matcher.find();
                    if (isPwdContains){
                        binding.textInputPass.setHelperText("Strong Password");
                        binding.textInputPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
                        binding.textInputPass.setError("");

                    }else {
                        binding.textInputPass.setHelperText("Weak Password.Include minimum 1 special char.");
                        binding.textInputPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.blue)));

                    }
                } else{
                    binding.textInputPass.setHelperText("Enter Minimum 6 char.");
                    binding.textInputPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.red_deep)));
                    binding.textInputPass.setError("");

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.confirmPasswordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()>=6){
                    Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
                    Matcher matcher = pattern.matcher(charSequence);
                    boolean isPwdContains = matcher.find();
                    if (isPwdContains){
                        binding.textInputConfirmPass.setHelperText("Strong Password");
                        binding.textInputConfirmPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
                        binding.textInputConfirmPass.setError("");

                    }else {
                        binding.textInputConfirmPass.setHelperText("Weak Password.Include minimum 1 special char.");
                        binding.textInputConfirmPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.blue)));

                    }
                } else{
                    binding.textInputConfirmPass.setHelperText("Enter Minimum 6 char.");
                    binding.textInputConfirmPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.red_deep)));
                    binding.textInputConfirmPass.setError("");

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.phoneET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                 if (charSequence.length() != 11){
                    binding.textInputPhone.setError("Put 11 digit phone number.");

                }else {
                    binding.textInputPhone.setError(null);
                    binding.textInputPhone.setHelperText("Valid phone number.");
                    binding.textInputPhone.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

       /* binding.deliveryET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


                try {
                    if (charSequence.equals("")){
                        Toast.makeText(RegisterSellerActivity.this, "Try nothing...", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!charSequence.equals("")){
                   delivery = Double.parseDouble(charSequence.toString());
                    if (delivery>=0 && delivery<5){
                        binding.textInputDelivery.setError("Delivery fee is too low");

                    }else {
                        binding.textInputDelivery.setError(null);
                        binding.textInputDelivery.setHelperText("Sufficient delivery fee.");
                        binding.textInputDelivery.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));

                    }


                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });*/



        binding.personImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    showImagePickDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

                try {
                    inputDataToDatabaseAndStorage();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }
    private String email,password,confirmPass,fullName,shopName,deliveryFee,phoneNumber;

    private void inputDataToDatabaseAndStorage() {

        if (!validateFullName() | !validateShopName() | !validatePhone() | !validateEmail() | !validatePassword() | !validateConfirmPassword() | !validateDeliveryFee() | !validateMatchPass()){
            return;
        }
//        if (TextUtils.isEmpty(binding.fullNameET.getText().toString().trim())) {
//            Toast.makeText(this, "Insert full name", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (TextUtils.isEmpty(binding.shopET.getText().toString().trim())) {
//            Toast.makeText(this, "Insert Shop Name", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (TextUtils.isEmpty(binding.phoneET.getText().toString().trim())) {
//            Toast.makeText(this, "Insert phone number", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (TextUtils.isEmpty(binding.deliveryET.getText().toString().trim())) {
//            Toast.makeText(this, "Insert delivery fee", Toast.LENGTH_SHORT).show();
//            return;
//        }
        if (latitude == 0.0 || longitude == 0.0) {
            Toast.makeText(this, "Please click on the GPS tracker", Toast.LENGTH_SHORT).show();
            return;
        }
//        if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailEt.getText().toString().trim()).matches()) {
//            Toast.makeText(this, "Invalid email address!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (binding.passwordET.getText().toString().trim().length() < 6) {
//            Toast.makeText(this, "You must take character 6 digits at least!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (!binding.confirmPasswordET.getText().toString().trim().equals(binding.passwordET.getText().toString().trim())) {
//            Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();
//            return;
//        }
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected()) {
            try {
                createAccount();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mobile.isConnected()) {
            try {
                createAccount();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                try {
                    saveDataInfoToDatabase();
                } catch (Exception e) {
                    e.printStackTrace();
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

            reference.child(Objects.requireNonNull(auth.getUid())).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    dialogForAccount.dismiss();
                    try {
                        checkSellerStatus();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

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
                                try {
                                    checkSellerStatus();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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
        reference.child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = null;
                    String shopCat = null;
                    try {
                        status = "" + snapshot.child("accountStatus").getValue();
                        shopCat = "" + snapshot.child("shopCategory").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    assert status != null;
                    if (status.equals("blocked")&& Objects.equals(shopCat, "false")) {

                        try {
                            startActivity(new Intent(RegisterSellerActivity.this, ShopCategoryActivity.class));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //binding.blockedTV.setVisibility(View.VISIBLE);
                        Toast.makeText(RegisterSellerActivity.this, "You are blocked.Please contact with your admin.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (status.equals("unblocked")) {
                            assert shopCat != null;
                            if (shopCat.equals("false")) {

                                //startActivity(new Intent(RegisterSellerActivity.this, ShopCategoryActivity.class));
                                Toast.makeText(RegisterSellerActivity.this, "Please save a category", Toast.LENGTH_SHORT).show();
                                finish();


                            }
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
            assert data != null;
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
        try {
            ActivityCompat.requestPermissions(this, location_permissions, LOCATION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (permissionAccepted) {
                        try {
                            detectLocation();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            findAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private boolean validateEmail(){


        email = binding.emailEt.getText().toString().trim();

        if (email.isEmpty()){
            binding.textInputEmail.setError("Field can't be empty");
            return false;
        }else  if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.textInputEmail.setError("Invalid Email Pattern");
            return false;
        } else {
            binding.textInputEmail.setError(null);
            binding.textInputEmail.setErrorEnabled(false);
            binding.textInputEmail.setHelperText("Valid email address.");
            binding.textInputEmail.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
            return true;
        }


    }
    private boolean validatePassword(){
        password = binding.passwordET.getText().toString().trim();
        if (password.length()>=6){
            Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
            Matcher matcher = pattern.matcher(password);
            boolean isPwdContains = matcher.find();
            if (isPwdContains){
                binding.textInputPass.setHelperText("Strong Password");
                binding.textInputPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
                binding.textInputPass.setError("");
                return true;
            }else {
                binding.textInputPass.setHelperText("Weak Password.Include minimum 1 special char.");
                binding.textInputPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                return true;
            }
        }else if (password.isEmpty()){
            binding.textInputPass.setHelperText("Field can't be empty.");
            binding.textInputPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.red_deep)));
            return false;
        } else{
            binding.textInputPass.setHelperText("Enter Minimum 6 char.");
            binding.textInputPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.red_deep)));
            binding.textInputPass.setError("");
            return false;
        }
    }
    private boolean validateConfirmPassword(){
        confirmPass = binding.confirmPasswordET.getText().toString().trim();
        if (confirmPass.length()>=6){
            Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
            Matcher matcher = pattern.matcher(confirmPass);
            boolean isPwdContains = matcher.find();
            if (isPwdContains){
                binding.textInputConfirmPass.setHelperText("Strong Password");
                binding.textInputConfirmPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
                binding.textInputConfirmPass.setError("");
                return true;
            }else {
                binding.textInputConfirmPass.setHelperText("Weak Password.Include minimum 1 special char.");
                binding.textInputConfirmPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                return true;
            }
        }else if (confirmPass.isEmpty()){
            binding.textInputConfirmPass.setHelperText("Field can't be empty.");
            binding.textInputConfirmPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.red_deep)));
            return false;
        } else{
            binding.textInputConfirmPass.setHelperText("Enter Minimum 6 char.");
            binding.textInputConfirmPass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.red_deep)));
            binding.textInputConfirmPass.setError("");
            return false;
        }
    }

    private boolean validateFullName(){
        fullName = binding.fullNameET.getText().toString().trim();

     /*   Pattern pattern = Pattern.compile("^[A-Za-z\\s]+$");//allow only space and alphabets
        Matcher matcher = pattern.matcher(fullName);
        boolean isNameContains = matcher.find();*/

        if (fullName.isEmpty()){
            binding.textInputFullName.setError("Field can't be empty.");
            return false;
        }else if (fullName.length()<=3){
            binding.textInputFullName.setError("Name is too small.");
            return false;
        }else if (fullName.length()>=20){
            binding.textInputFullName.setError("Name is too long. Put under 20 char.");
            return false;
        } else{
            binding.textInputFullName.setError(null);
            binding.textInputFullName.setHelperText("Valid name.");
            binding.textInputFullName.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
            return true;
        }
    }
    private boolean validateShopName(){
        shopName = binding.shopET.getText().toString().trim();


//        Pattern pattern = Pattern.compile("^[a-zA-Z\\s]*$");//allow space special character and number
//        Matcher matcher = pattern.matcher(shopName);
//        boolean isShopNameContains = matcher.find();


        if (shopName.isEmpty()){
            binding.textInputShopName.setError("Field can't be empty.");
            return false;
        } else if (shopName.length()<=3){
            binding.textInputShopName.setError("Shop name is too small.");
            return false;
        }else if (shopName.length()>=20){
            binding.textInputShopName.setError("Shop name is too long.");
            return false;
        }else {
            binding.textInputShopName.setError(null);
            binding.textInputShopName.setHelperText("Valid shop name.");
            binding.textInputShopName.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
            return true;
        }
    }
    private boolean validatePhone(){
        phoneNumber = binding.phoneET.getText().toString().trim();
        if (phoneNumber.isEmpty()){
            binding.textInputPhone.setError("Field can't be empty.");
            return false;
        }else if (phoneNumber.length() != 11){
            binding.textInputPhone.setError("Put 11 digit phone number.");
            return false;
        }else {
            binding.textInputPhone.setError(null);
            binding.textInputPhone.setHelperText("Valid phone number.");
            binding.textInputPhone.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
            return true;
        }
    }


    private boolean validateDeliveryFee(){
        deliveryFee = binding.deliveryET.getText().toString().trim();
        if (!deliveryFee.equals("")){
            try {
                delivery = Double.parseDouble(deliveryFee);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (deliveryFee.isEmpty()){
            binding.textInputDelivery.setError("Field can't be empty.");
            return false;
        }else if (delivery>=0 && delivery<5){
            binding.textInputDelivery.setError("Delivery fee is too low");
            return false;
        }else {
            binding.textInputDelivery.setError(null);
            binding.textInputDelivery.setHelperText("Sufficient delivery fee.");
            binding.textInputDelivery.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
            return true;
        }
    }
    private boolean validateMatchPass(){
        password = binding.passwordET.getText().toString().trim();
        confirmPass = binding.confirmPasswordET.getText().toString().trim();

        if (!password.isEmpty() && !confirmPass.isEmpty()){
            if (!password.equals(confirmPass)){
                Toast.makeText(this, "Password doesn't match.", Toast.LENGTH_SHORT).show();
                return false;
            }else {
                Toast.makeText(this, "Password matched.", Toast.LENGTH_SHORT).show();
                return true;
            }

        }else {
            Toast.makeText(this, "Make a password first.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


}