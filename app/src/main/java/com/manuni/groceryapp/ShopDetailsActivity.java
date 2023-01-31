package com.manuni.groceryapp;

import static com.manuni.groceryapp.Constants.TOPICS;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.groceryapp.databinding.ActivityShopDetailsBinding;
import com.manuni.groceryapp.databinding.DialogCartBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopDetailsActivity extends AppCompatActivity {
    ActivityShopDetailsBinding binding;
    private String shopUid;
    private FirebaseAuth auth;
    private String myLatitude, myLongitude, phoneNumber;
    private String shopName, shopEmail, shopPhone, shopAddress, shopLatitude, shopLongitude;
    private ArrayList<ModelProduct> modelProducts;
    private ProductUserAdapter productUserAdapter;
    public String deliveryFee;

    AlertDialog dialog;
    int count;

    private EasyDB easyDB;

    private ArrayList<ModelCartItem> modelCartItemsList;
    private AdapterCartItem adapterCartItem;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        shopUid = getIntent().getStringExtra("shopUid");

        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(ShopDetailsActivity.this);
        progressDialog.setTitle("Placing an order");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadRatings();


        easyDB = EasyDB.init(ShopDetailsActivity.this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Each_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //each shop have its own products and orders
        deleteCartData();
        cartCount();

        binding.backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCartDialog();
                binding.cartCountTV.setVisibility(View.GONE);
            }
        });
        //commit korar new ekta upay paoya gece...seta holo alt+(1 key er purber tilda key mane ~ key)

        binding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialPhone();
            }
        });
        binding.mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });

        binding.searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    productUserAdapter.getFilter().filter(charSequence);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category").setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String selected = Constants.productCategories1[i];
                        binding.filterProductTV.setText(selected);
                        if (selected.equals("All")) {
                            loadShopProducts();
                        } else {
                            productUserAdapter.getFilter().filter(selected);
                        }
                    }
                }).show();
            }
        });

        binding.reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });

    }

    private float ratingSum = 0;

    private void loadRatings() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(shopUid).child("Ratings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ratingSum = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    float rating = Float.parseFloat("" + dataSnapshot.child("ratings").getValue());//e.g 4.5
                    ratingSum = ratingSum + rating;


                }


                long numberOfReviews = snapshot.getChildrenCount();
                float avgOfReviews = ratingSum / numberOfReviews;

                binding.ratingBar.setRating(avgOfReviews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void deleteCartData() {


        easyDB.deleteAllDataFromTable();//it will delete all data from the cart

    }

    public void cartCount() {
        //make it public so we can access it in the adapter
        count = easyDB.getAllData().getCount();
        if (count <= 0) {
            //no items in the db so hide
            binding.cartCountTV.setVisibility(View.GONE);
        } else {
            binding.cartCountTV.setVisibility(View.VISIBLE);
            binding.cartCountTV.setText("" + count);
        }
    }

    public double allTotalPrice = 0.00;
    public TextView subTotalPriceTV, deliFeeTV, allTotalPriceTV;

    private void showCartDialog() {

        //init list
        modelCartItemsList = new ArrayList<>();


        DialogCartBinding binding;
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        binding = DialogCartBinding.bind(view);

        subTotalPriceTV = view.findViewById(R.id.subTotalTV);
        deliFeeTV = view.findViewById(R.id.deliveryFeeTV);
        allTotalPriceTV = view.findViewById(R.id.totalPriceTV);
        binding.shopNameTV.setText(shopName);

        AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
        builder.setView(view);


        EasyDB easyDB = EasyDB.init(ShopDetailsActivity.this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Each_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //get all data from db
        Cursor result = easyDB.getAllData();
        while (result.moveToNext()) {
            String id = result.getString(1);
            String pId = result.getString(2);
            String name = result.getString(3);
            String price = result.getString(4);
            String cost = result.getString(5);
            String quantity = result.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem("" + id, "" + pId, "" + name, "" + price, "" + cost, "" + quantity);
            modelCartItemsList.add(modelCartItem);
        }

        adapterCartItem = new AdapterCartItem(ShopDetailsActivity.this, modelCartItemsList);
        binding.cartItemRV.setAdapter(adapterCartItem);

        binding.deliveryFeeTV.setText("$" + deliveryFee);
        binding.subTotalTV.setText("" + String.format("%.2f", allTotalPrice));
        binding.totalPriceTV.setText("$" + (allTotalPrice + Double.parseDouble(deliveryFee.replaceAll("$", ""))));

        dialog = builder.create();
        dialog.show();

        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                allTotalPrice = 0.00;

            }
        });

        binding.checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //first validate location address
                if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")) {
                    Toast.makeText(ShopDetailsActivity.this, "Please set your location in your profile.", Toast.LENGTH_SHORT).show();
                    return;//don't proceed further
                }
                if (phoneNumber.equals("") || phoneNumber.equals("null")) {
                    Toast.makeText(ShopDetailsActivity.this, "Please set your phone in your profile.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (modelCartItemsList.size() == 0) {
                    Toast.makeText(ShopDetailsActivity.this, "No items available to place an order", Toast.LENGTH_SHORT).show();
                    return;
                }
                submitOrder();
            }
        });

    }

    private void submitOrder() {
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();
        String cost = allTotalPriceTV.getText().toString().replace("$", "");//if contains $ then replace with ""

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", "" + timestamp);
        hashMap.put("orderTime", "" + timestamp);
        hashMap.put("orderStatus", "In Progress");
        hashMap.put("orderCost", "" + cost);
        hashMap.put("orderBy", "" + auth.getUid());
        hashMap.put("orderTo", "" + shopUid);
        hashMap.put("latitude", "" + myLatitude);
        hashMap.put("longitude", "" + myLongitude);
        hashMap.put("deliveryFee", "" + deliveryFee);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(shopUid).child("Orders");
        dbRef.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //order info added now add order items
                for (int i = 0; i < modelCartItemsList.size(); i++) {
                    String pId = modelCartItemsList.get(i).getpId();
                    String id = modelCartItemsList.get(i).getId();
                    String cost = modelCartItemsList.get(i).getCost();
                    String price = modelCartItemsList.get(i).getPrice();
                    String quantity = modelCartItemsList.get(i).getQuantity();
                    String name = modelCartItemsList.get(i).getName();

                    HashMap<String, String> hashMap1 = new HashMap<>();
                    hashMap1.put("pId", pId);
                    hashMap1.put("name", name);
                    hashMap1.put("cost", cost);
                    hashMap1.put("price", price);
                    hashMap1.put("quantity", quantity);

                    dbRef.child(timestamp).child("Items").child(pId).setValue(hashMap1);

                    //ei pId ta holo ekta timestamp jeta product ^add korar somoy neya hoyeche (AddProductActivity)--->addProductTodb-->177 no. lines
                }

                DatabaseReference myAdRef = FirebaseDatabase.getInstance().getReference().child("Admin").child(shopUid).child("Orders");
                myAdRef.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        for (int i = 0; i < modelCartItemsList.size(); i++) {
                            String pId = modelCartItemsList.get(i).getpId();
                            String id = modelCartItemsList.get(i).getId();
                            String cost = modelCartItemsList.get(i).getCost();
                            String price = modelCartItemsList.get(i).getPrice();
                            String quantity = modelCartItemsList.get(i).getQuantity();
                            String name = modelCartItemsList.get(i).getName();

                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", pId);
                            hashMap1.put("name", name);
                            hashMap1.put("cost", cost);
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);

                            myAdRef.child(timestamp).child("Items").child(pId).setValue(hashMap1);

                            //ei pId ta holo ekta timestamp jeta product ^add korar somoy neya hoyeche (AddProductActivity)--->addProductTodb-->177 no. lines
                        }
                    }
                });
                progressDialog.dismiss();
                dialog.dismiss();
                //binding.cartCountTV.setVisibility(View.GONE);

                Toast.makeText(ShopDetailsActivity.this, "Product order placed to " + shopName + " successfully!", Toast.LENGTH_SHORT).show();
                deleteCartData();
                //sending notification after submitting order successfully.
                prepareNotification(timestamp);


                //nicher ei data gulo notification set er pore ekhan theke cut kore neya hoyeche...
//                Intent intent = new Intent(ShopDetailsActivity.this,OrderDetailsUsersActivity.class);
//                intent.putExtra("orderTo",shopUid);
//                intent.putExtra("orderId",timestamp);
//                startActivity(intent);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ShopDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, "" + shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void loadMyInfo() {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference().child("Users");
        dR.orderByChild("uid").equalTo(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String name = "" + dataSnapshot.child("fullName").getValue();
                    String accountType = "" + dataSnapshot.child("accountType").getValue();
                    String email = "" + dataSnapshot.child("email").getValue();
                    phoneNumber = "" + dataSnapshot.child("phoneNumber").getValue();
                    String profileImage = "" + dataSnapshot.child("profileImage").getValue();
                    String city = "" + dataSnapshot.child("city").getValue();
                    myLatitude = "" + dataSnapshot.child("latitude").getValue();
                    myLongitude = "" + dataSnapshot.child("longitude").getValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopDetails() {
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().child("Users");
        dbr.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fullName = "" + snapshot.child("fullName").getValue();
                shopName = "" + snapshot.child("shopName").getValue();
                shopEmail = "" + snapshot.child("email").getValue();
                shopAddress = "" + snapshot.child("address").getValue();
                shopPhone = "" + snapshot.child("phoneNumber").getValue();
                shopLatitude = "" + snapshot.child("latitude").getValue();
                shopLongitude = "" + snapshot.child("longitude").getValue();
                deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();
                String shopOpen = "" + snapshot.child("shopOpen").getValue();


                binding.shopNameTV.setText(shopName);
                binding.emailTV.setText(shopEmail);
                binding.addressTV.setText(shopAddress);
                binding.phoneTV.setText(shopPhone);
                binding.deliveryFeeTV.setText("Delivery Fee $" + deliveryFee);

                if (shopOpen.equals("true")) {
                    binding.openCloseTV.setText("Open");
                } else {
                    binding.openCloseTV.setText("Closed");
                }

                try {
                    Picasso.get().load(profileImage).into(binding.shopIV);
                } catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopProducts() {

        modelProducts = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(shopUid).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelProducts.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ModelProduct data = dataSnapshot.getValue(ModelProduct.class);
                    modelProducts.add(data);
                }
                productUserAdapter = new ProductUserAdapter(ShopDetailsActivity.this, modelProducts);
                binding.productRV.setAdapter(productUserAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void prepareNotification(String orderId) {
        //when user places order, send notification to seller

        //prepare data for notification
        // String NOTIFICATION_TOPIC = "/topics/"+Constants.FCM_TOPIC; //must be same as subscribed by user
        String NOTIFICATION_TITLE = "New Order " + orderId;
        String NOTIFICATION_MESSAGE = "Congratulations..! You have a new order.";
        String NOTIFICATION_TYPE = "NewOrder";

//        //prepare json(what to send and where to send)
//        JSONObject notificationJO = new JSONObject();
//        JSONObject notificationBodyJO = new JSONObject();
//
//        try {
//            //what to send
//            notificationBodyJO.put("notificationType",NOTIFICATION_TYPE);
//            notificationBodyJO.put("buyerUid",auth.getUid());
//            notificationBodyJO.put("sellerUid",shopUid);
//            notificationBodyJO.put("orderId",orderId);
//            notificationBodyJO.put("notificationTitle",NOTIFICATION_TITLE);
//            notificationBodyJO.put("notificationMessage",NOTIFICATION_MESSAGE);
//            //where to send
//           // notificationJO.put("to",NOTIFICATION_TOPIC);//to all who subscribe this topic
//            notificationJO.put("data",notificationBodyJO);
//
//        }catch (Exception e){
//            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//
//        }

        String buyerUid = auth.getUid();
        String sellerUid = shopUid;


        sendFcmNotification(NOTIFICATION_TYPE, buyerUid, sellerUid, orderId, NOTIFICATION_TITLE, NOTIFICATION_MESSAGE);
    }

    private void sendFcmNotification(String notificationType, String buyerUid, String sellerUid, String orderId, String notificationTitle, String notificationMessage) {
        PushNotification notification = new PushNotification(new NotificationData(notificationType, buyerUid, sellerUid, orderId, notificationTitle, notificationMessage), TOPICS);
        sendNotification(notification, orderId);


//        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST,"https://fcm.googleapis.com/fcm/send", notificationJO, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                //after sending fcm request start order details activity
//                Intent intent = new Intent(ShopDetailsActivity.this,OrderDetailsUsersActivity.class);
//                intent.putExtra("orderTo",shopUid);
//                intent.putExtra("orderId",orderId);
//                startActivity(intent);
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                //if failed perform the same operation
//                Intent intent = new Intent(ShopDetailsActivity.this,OrderDetailsUsersActivity.class);
//                intent.putExtra("orderTo",shopUid);
//                intent.putExtra("orderId",orderId);
//                startActivity(intent);
//
//            }
//        }){
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String,String> headers = new HashMap<>();
//                headers.put("Content-Type","application/json");
//               // headers.put("Authorization:","key="+Constants.FCM_KEY);
//                return headers;
//            }
//        };
//        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void sendNotification(PushNotification notification, String orderId) {
        ApiUtilities.getClient().sendNotification(notification).enqueue(new Callback<PushNotification>() {
            @Override
            public void onResponse(Call<PushNotification> call, Response<PushNotification> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                    intent.putExtra("orderTo", shopUid);
                    intent.putExtra("orderId", orderId);
                    startActivity(intent);
                    Toast.makeText(ShopDetailsActivity.this, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShopDetailsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PushNotification> call, Throwable t) {
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
                Toast.makeText(ShopDetailsActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}