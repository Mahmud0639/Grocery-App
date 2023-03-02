package com.manuni.groceryapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.manuni.groceryapp.databinding.ActivityRegisterUserBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterUserActivity extends AppCompatActivity implements LocationListener{
    public static final String FILE_NAME = "file_name";
    public static final String PASSWORD_KEY = "password_key";
    public static final String EMAIL_ADDRESS = "email_address";
    ActivityRegisterUserBinding binding;
    ProgressDialog dialog,dialogForAccount;

    private FirebaseAuth auth;
    private DatabaseReference reference;
    private StorageReference storageReference;

    private static final int LOCATION_REQUEST_CODE = 100;



    private String[] location_permissions;
    private String[] camera_permissions;
    private String[] storage_permissions;


    private Uri imageUri;


    private LocationManager locationManager;
    private double latitude, longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(RegisterUserActivity.this);
        dialog.setMessage("Please wait...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        dialogForAccount = new ProgressDialog(RegisterUserActivity.this);
        dialogForAccount.setTitle("Account");
        dialogForAccount.setCancelable(false);
        dialogForAccount.setCanceledOnTouchOutside(false);
        binding.backArrowBtn.setOnClickListener(view -> onBackPressed());
        location_permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        camera_permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storage_permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        binding.savePassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String myPass = binding.passwordET.getText().toString().trim();
                String myEmail = binding.emailEt.getText().toString().trim();
                if (myEmail.isEmpty()){
                    binding.savePassword.setChecked(false);
                    Toast.makeText(RegisterUserActivity.this, "Email box is empty.", Toast.LENGTH_SHORT).show();
                }else if (myPass.isEmpty()){
                    binding.savePassword.setChecked(false);
                    Toast.makeText(RegisterUserActivity.this, "Password box is empty.", Toast.LENGTH_SHORT).show();
                }else if (!Patterns.EMAIL_ADDRESS.matcher(myEmail).matches()){
                    binding.savePassword.setChecked(false);
                    Toast.makeText(RegisterUserActivity.this, "This email can't be saved.Your email is not correct.", Toast.LENGTH_SHORT).show();
                }else if (myPass.length()<6){
                    binding.savePassword.setChecked(false);
                    Toast.makeText(RegisterUserActivity.this, "This password can't be saved.Password length is too small.", Toast.LENGTH_SHORT).show();
                } else{
                    binding.savePassword.setChecked(true);
                    if (b){

                        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = getSharedPreferences(FILE_NAME,MODE_PRIVATE).edit();
                        editor.putString(EMAIL_ADDRESS,myEmail);
                        editor.putString(PASSWORD_KEY,myPass);
                        editor.apply();
                        Toast.makeText(RegisterUserActivity.this, "Password saved.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        binding.gpsBtn.setOnClickListener(view -> {

            if (checkLocationPermission()) {
                try {
                    detectLocation();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.show();
            } else {
                try {
                    requestLocationPermission();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        binding.registerAsSellerTV.setOnClickListener(view -> {
            try {
                startActivity(new Intent(RegisterUserActivity.this,TermsConditionActivity.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.personImage.setOnClickListener(view -> {
            try {
                showImagePickDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.registerBtnUser.setOnClickListener(view -> {
            try {
                inputDataToDatabaseAndStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        binding.fullNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

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
    }

    private String email,password,confirmPass,fullName,phoneNumber;

    private void inputDataToDatabaseAndStorage() {

        if (!validateFullName() | !validatePhone() | !validateEmail() | !validatePassword() | !validateConfirmPassword() | !validateMatchPass()){
            return;
        }

        if (latitude==0.0 || longitude==0.0){
            Toast.makeText(this, "Please click on the GPS tracker", Toast.LENGTH_SHORT).show();
            return;
        }
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
    private void createAccount(){
        dialogForAccount.setMessage("Creating Account...");
        dialogForAccount.show();
        auth.createUserWithEmailAndPassword(binding.emailEt.getText().toString().trim(),binding.passwordET.getText().toString().trim()).addOnSuccessListener(authResult -> {
            try {
                saveDataInfoToDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).addOnFailureListener(e -> {
            dialogForAccount.dismiss();
            Toast.makeText(RegisterUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveDataInfoToDatabase() {
        dialogForAccount.setMessage("Saving Data to Database...");

        if (imageUri == null){
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("fullName",""+binding.fullNameET.getText().toString().trim());
            hashMap.put("phoneNumber",""+binding.phoneET.getText().toString().trim());
            hashMap.put("countryName",""+binding.countryET.getText().toString().trim());
            hashMap.put("state",""+binding.stateET.getText().toString().trim());
            hashMap.put("city",""+binding.cityET.getText().toString().trim());
            hashMap.put("address",""+binding.completeAddressET.getText().toString().trim());
            hashMap.put("email",""+binding.emailEt.getText().toString().trim());
            hashMap.put("uid",""+auth.getUid());
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("accountType","User");
            hashMap.put("online","true");
            hashMap.put("timestamp",""+System.currentTimeMillis());
            hashMap.put("profileImage","");

            reference.child(Objects.requireNonNull(auth.getUid())).setValue(hashMap).addOnSuccessListener(unused -> {
                dialogForAccount.dismiss();
                try {
                    startActivity(new Intent(RegisterUserActivity.this,MainUserActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
            }).addOnFailureListener(e -> dialogForAccount.dismiss());

        }else {
            String filePathAndName = "profile_images/"+""+auth.getUid();
            storageReference.child(filePathAndName).putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                while (!uriTask.isSuccessful());
                Uri downloadUrl = uriTask.getResult();

                if (uriTask.isSuccessful()){
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("fullName",""+binding.fullNameET.getText().toString().trim());
                    hashMap.put("phoneNumber",""+binding.phoneET.getText().toString().trim());
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
                    hashMap.put("accountType","User");
                    hashMap.put("online","true");
                    hashMap.put("timestamp",""+System.currentTimeMillis());
                    hashMap.put("profileImage",""+downloadUrl);

                    reference.child(Objects.requireNonNull(auth.getUid())).setValue(hashMap).addOnSuccessListener(unused -> {
                        dialogForAccount.dismiss();
                        try {
                            startActivity(new Intent(RegisterUserActivity.this,MainUserActivity.class));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        finish();
                    }).addOnFailureListener(e -> dialogForAccount.dismiss());
                }

            }).addOnFailureListener(e -> {
                dialogForAccount.dismiss();
                Toast.makeText(RegisterUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        if (resultCode==RESULT_OK){
            assert data != null;
            imageUri = data.getData();

            try {
                binding.personImageShow.setImageURI(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(this, ""+ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        }
    }

    //    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
//        Intent data = result.getData();
//        if (data != null && result.getResultCode() ==RESULT_OK) {
//            imageUri = data.getData();
//
//            binding.personImage.setImageURI(imageUri);
//        } else {
//            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
//        }
//
//    });


//    private void showImagePickDialog() {
//        String[] options = {"Camera", "Gallery"};
//        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterUserActivity.this);
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
//            if (result.getResultCode()==RESULT_OK){
//                try {
//                    binding.personImage.setImageURI(imageUri);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }else {
//                Toast.makeText(RegisterUserActivity.this, "Cancelled.", Toast.LENGTH_SHORT).show();
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
//            if (result.getResultCode()==RESULT_OK){
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
//        boolean result = ContextCompat.checkSelfPermission(RegisterUserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//        return result;
//    }
//
//    private void requestStoragePermission() {
//        ActivityCompat.requestPermissions(RegisterUserActivity.this, storage_permissions, STORAGE_REQUEST_CODE);
//    }
//
//    private boolean checkCameraPermission() {
//        boolean resultForCamera = ContextCompat.checkSelfPermission(RegisterUserActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
//        boolean resultForStorage = ContextCompat.checkSelfPermission(RegisterUserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//        return resultForCamera && resultForStorage;
//    }

//    private void requestCameraPermission() {
//        ActivityCompat.requestPermissions(RegisterUserActivity.this, camera_permissions, CAMERA_REQUEST_CODE);
//    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(RegisterUserActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
        if (requestCode == LOCATION_REQUEST_CODE) {
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
        geocoder = new Geocoder(RegisterUserActivity.this, Locale.getDefault());
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

    private boolean validateFullName(){
        fullName = binding.fullNameET.getText().toString().trim();

      /*  Pattern pattern = Pattern.compile("^[A-Za-z\\s]+$");//allow only space and alphabets
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