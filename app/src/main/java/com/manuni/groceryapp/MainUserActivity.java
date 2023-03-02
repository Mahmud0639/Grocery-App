package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivityMainUserBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainUserActivity extends AppCompatActivity {
    ActivityMainUserBinding binding;
    private FirebaseAuth auth;
   private String city,state;

    String[] data,dataArea;
    ArrayList<String> dataList,areaList;

    private DatabaseReference dbRef;
    private ProgressDialog dialog,progressDialog;

    private ArrayList<ModelShop> modelShopArrayList;
    private AdapterShop adapterShop;
    private ModelShop modelShop;


    //private ArrayList<ModelOrderUser> modelOrderUsers;
    private ArrayList<ModelOrderUser> modelOrderUsers;
    private AdapterOrderUser adapterOrderUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setTitle("Please wait");
        dialog.setCanceledOnTouchOutside(false);

        progressDialog = new ProgressDialog(MainUserActivity.this);


        auth = FirebaseAuth.getInstance();

        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");



        try {

            checkUser();

            loadToSpinner();

            showShopsUI();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PopupMenu popupMenu = new PopupMenu(MainUserActivity.this,binding.moreBtn);
        popupMenu.getMenu().add("Edit Profile");
        popupMenu.getMenu().add("Settings");
        popupMenu.getMenu().add("Send Feedback");
        popupMenu.getMenu().add("Logout");


        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle()=="Edit Profile"){
                try {
                    startActivity(new Intent(MainUserActivity.this,EditeProfileUserActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (item.getTitle()=="Settings"){
                Intent intent = new Intent(MainUserActivity.this,SettingsActivity.class);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (item.getTitle()=="Logout"){
                try {
                    makeMeOffLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (item.getTitle()=="Send Feedback"){
                try {
                    startActivity(new Intent(MainUserActivity.this,FeedbackActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });

        binding.moreBtn.setOnClickListener(view -> popupMenu.show());

        binding.searchForOrders.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterOrderUser.getFilter().filter(charSequence);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.filterProductBtn.setOnClickListener(view -> categoryDialog());

        binding.filterArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                areaDialog();
            }
        });

        binding.searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterShop.getFilter().filter(charSequence);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        binding.tabShopsTV.setOnClickListener(view -> showShopsUI());
        binding.tabOrdersTV.setOnClickListener(view -> showOrdersUI());



    }

    private void loadArea() {


        DatabaseReference myDbRef = FirebaseDatabase.getInstance().getReference().child("ShopArea");
        myDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                areaList = new ArrayList<>();
                if (snapshot.exists()){
                    areaList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String area = null;
                        try {
                            area = ""+dataSnapshot.child("area").getValue();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            areaList.add(area);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    dataArea = areaList.toArray(new String[areaList.size()]);

                    //Toast.makeText(MainUserActivity.this, "All area added.", Toast.LENGTH_SHORT).show();



                }
                progressDialog.dismiss();



                //adapter.notifyDataSetChanged();

            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showShopsUI() {

        binding.searchBar.setVisibility(View.VISIBLE);
        binding.searchForOrders.setVisibility(View.GONE);


        binding.shopsRl.setVisibility(View.VISIBLE);
        binding.ordersRl.setVisibility(View.GONE);

        binding.tabShopsTV.setTextColor(getResources().getColor(R.color.black));
        binding.tabShopsTV.setBackgroundResource(R.drawable.shape_rect04);

        binding.tabOrdersTV.setTextColor(getResources().getColor(R.color.white));
        binding.tabOrdersTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        binding.filterProductBtn.setVisibility(View.VISIBLE);
        binding.filterTxt.setVisibility(View.VISIBLE);
        binding.shopsFoundTV.setVisibility(View.VISIBLE);
        binding.filterArea.setVisibility(View.VISIBLE);

    }
    private void showOrdersUI(){

        binding.searchBar.setVisibility(View.GONE);
        binding.searchForOrders.setVisibility(View.VISIBLE);

        binding.ordersRl.setVisibility(View.VISIBLE);
        binding.shopsRl.setVisibility(View.GONE);

        binding.tabOrdersTV.setTextColor(getResources().getColor(R.color.black));
        binding.tabOrdersTV.setBackgroundResource(R.drawable.shape_rect04);

        binding.tabShopsTV.setTextColor(getResources().getColor(R.color.white));
        binding.tabShopsTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        binding.filterProductBtn.setVisibility(View.GONE);
        binding.filterTxt.setVisibility(View.GONE);
        binding.shopsFoundTV.setVisibility(View.GONE);
        binding.filterArea.setVisibility(View.GONE);
        binding.myArea.setVisibility(View.GONE);

        try {
            loadOrders();//ekhane rakhar karone duplicate ashe na
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void makeMeOffLine(){
        dialog.setMessage("Logging out...");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        dbRef.child(Objects.requireNonNull(auth.getUid())).updateChildren(hashMap).addOnSuccessListener(unused -> {
            //checkUserType();
            auth.signOut();
            try {
                checkUser();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(MainUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }
    private void checkUser(){
        if (auth.getCurrentUser()==null){
            try {
                startActivity(new Intent(MainUserActivity.this,LoginActivity.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                loadMyInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Toast.makeText(this, "You are logged in.", Toast.LENGTH_SHORT).show();
        }


    }
    private void loadMyInfo(){
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference().child("Users");
        dR.orderByChild("uid").equalTo(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String name = null;
                    String accountType = null;
                    String email = null;
                    String phoneNumber = null;
                    String profileImage = null;
                    try {
                        name = ""+dataSnapshot.child("fullName").getValue();
                        accountType = ""+dataSnapshot.child("accountType").getValue();
                        email = ""+dataSnapshot.child("email").getValue();
                        phoneNumber = ""+dataSnapshot.child("phoneNumber").getValue();
                        profileImage = ""+dataSnapshot.child("profileImage").getValue();
                        city = ""+dataSnapshot.child("city").getValue();
                        state = ""+dataSnapshot.child("state").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    binding.nameTxt.setText(name+"("+accountType+")");
                    binding.emailTV.setText(email);
                    binding.phoneTV.setText(phoneNumber);

                    try {
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(binding.profileIV);
                    }catch (Exception e){
                        binding.profileIV.setImageResource(R.drawable.ic_person_gray);
                    }

                    //load only those shops that are in the user area or state
                    try {

                            loadShops(state);



                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadOrders() {
        modelOrderUsers = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelOrderUsers.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String uid = null;
                    try {
                        uid = ""+ds.getRef().getKey();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    assert uid != null;
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(auth.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                               // modelOrderUsers.clear(); //ei line tir jonno onno store er order dekhato na.
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    ModelOrderUser orderUser = null;
                                    try {
                                        orderUser = ds.getValue(ModelOrderUser.class);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        modelOrderUsers.add(0,orderUser);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


                                }
                                adapterOrderUser = new AdapterOrderUser(MainUserActivity.this,modelOrderUsers);
                                try {
                                    binding.ordersRV.setAdapter(adapterOrderUser);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(String myState) {
        modelShopArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.orderByChild("accountType").equalTo("Seller").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelShopArrayList.clear();
                for (DataSnapshot dataSnapshot1: snapshot.getChildren()){
                   // Log.e("TAG", "onDataChange: CheckAfter for loop" );
                    try {
                        modelShop = dataSnapshot1.getValue(ModelShop.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String shopCity = null;
                    String status = null;
                    String shopOpen = null;
                    String shopState = null;
                    try {
                        shopCity = ""+dataSnapshot1.child("city").getValue();
                        shopState = ""+dataSnapshot1.child("state").getValue();
                        status = ""+dataSnapshot1.child("accountStatus").getValue();
                        String onlineStatus = ""+dataSnapshot1.child("online").getValue();
                        shopOpen = ""+dataSnapshot1.child("shopOpen").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (Objects.requireNonNull(shopState).equals(myState)) {
                        assert status != null;
                        if (status.equals("unblocked")) {
                            assert shopOpen != null;
                            if (shopOpen.equals("true")) {
                                try {
                                    modelShopArrayList.add(modelShop);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }


                }
                binding.shopsFoundTV.setText("("+modelShopArrayList.size()+" shops available)");
                adapterShop = new AdapterShop(MainUserActivity.this,modelShopArrayList);
                try {
                    binding.shopRV.setAdapter(adapterShop);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else {

            Toast.makeText(getBaseContext(), "Press again to exit",
                    Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
    }

    private void areaDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Shop Area").setItems(dataArea, (dialogInterface, i) -> {
            String shopArea = dataArea[i];//ekhane kono category select kora hole seta ei variable er moddhe chole ashbe
                binding.filterTxt.setVisibility(View.GONE);
                binding.myArea.setText(shopArea);

               // binding.myArea.setVisibility(View.VISIBLE);
                loadAllAreaShops(shopArea);


        }).show();
    }

    private void loadAllAreaShops(String shopAreaHere) {
        modelShopArrayList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.orderByChild("accountType").equalTo("Seller").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                modelShopArrayList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String shopCat = null;
                    String myCity = null;
                    String myDivision = null;
                    String myArea = null;
                    try {
                        shopCat = ""+dataSnapshot.child("shopCategory").getValue();
                        myCity = ""+dataSnapshot.child("city").getValue();
                        myDivision = ""+dataSnapshot.child("state").getValue();
                        myArea = ""+dataSnapshot.child("shopArea").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (shopAreaHere.equals(myArea)) {
                        if (myDivision.equals(state)) {
                            ModelShop modelShop = null;
                            try {
                                modelShop = dataSnapshot.getValue(ModelShop.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                modelShopArrayList.add(modelShop);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }


                binding.shopsFoundTV.setText("("+modelShopArrayList.size()+" shops found)");
                adapterShop = new AdapterShop(MainUserActivity.this,modelShopArrayList);
                try {
                    binding.shopRV.setAdapter(adapterShop);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void categoryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Shop Category").setItems(data, (dialogInterface, i) -> {
            String category = data[i];//ekhane kono category select kora hole seta ei variable er moddhe chole ashbe
           // Toast.makeText(this, ""+category, Toast.LENGTH_SHORT).show();
           String areaTxt;
           areaTxt = binding.myArea.getText().toString().trim();
           binding.myArea.setVisibility(View.VISIBLE);


            //Toast.makeText(this, ""+areaTxt, Toast.LENGTH_SHORT).show();
            if (category.equals("All")){
                binding.filterTxt.setVisibility(View.VISIBLE);
                binding.filterTxt.setText("Showing All");
                binding.myArea.setVisibility(View.GONE);
                try {
                    loadShops(state);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }/*else if (areaTxt.equals("All")){
                loadAllMyShops(category);
            }*/else{
                binding.filterTxt.setVisibility(View.VISIBLE);
                binding.filterTxt.setText(category);

                try {
                    loadAllShops(category,areaTxt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).show();
    }

    private void loadAllShops(String selected,String areaSelected) {

        modelShopArrayList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.orderByChild("accountType").equalTo("Seller").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                modelShopArrayList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String shopCat = null;
                    String myCity = null;
                    String myDivision = null;
                    String myShopArea = null;
                    try {
                        shopCat = ""+dataSnapshot.child("shopCategory").getValue();
                        myCity = ""+dataSnapshot.child("city").getValue();
                        myDivision = ""+dataSnapshot.child("state").getValue();
                        myShopArea = ""+dataSnapshot.child("shopArea").getValue();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (selected.equals(shopCat) && areaSelected.equals(myShopArea)) {
                        if (myDivision.equals(state)) {
                            ModelShop modelShop = null;
                            try {
                                modelShop = dataSnapshot.getValue(ModelShop.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                modelShopArrayList.add(modelShop);
                                binding.myArea.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }else if (selected.equals(shopCat)&&areaSelected.equals("")){

                       loadAllShops(selected);

                    }
                }

                binding.shopsFoundTV.setText("("+modelShopArrayList.size()+" shops found)");
                adapterShop = new AdapterShop(MainUserActivity.this,modelShopArrayList);
                try {
                    binding.shopRV.setAdapter(adapterShop);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void loadToSpinner() {
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        DatabaseReference myDbRef = FirebaseDatabase.getInstance().getReference().child("ShopCategory");
        myDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList = new ArrayList<>();
                if (snapshot.exists()){
                    dataList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String categories = null;
                        try {
                            categories = ""+dataSnapshot.child("category").getValue();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            dataList.add(categories);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        dataList.add(0,"All");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    data = dataList.toArray(new String[dataList.size()]);



                }
                //progressDialog.dismiss();
                loadArea();


                //adapter.notifyDataSetChanged();

            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadAllShops(String selected) {
        modelShopArrayList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.orderByChild("accountType").equalTo("Seller").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                modelShopArrayList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String shopCat = ""+dataSnapshot.child("shopCategory").getValue();
                    String myCity = ""+dataSnapshot.child("city").getValue();
                    String myDivision = ""+dataSnapshot.child("state").getValue();

                    if (selected.equals(shopCat) && myDivision.equals(state)){
                        ModelShop modelShop = dataSnapshot.getValue(ModelShop.class);
                        modelShopArrayList.add(modelShop);
                    }
                }

                binding.shopsFoundTV.setText("("+modelShopArrayList.size()+" shops found)");
                adapterShop = new AdapterShop(MainUserActivity.this,modelShopArrayList);
                binding.shopRV.setAdapter(adapterShop);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}