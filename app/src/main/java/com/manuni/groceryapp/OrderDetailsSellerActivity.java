package com.manuni.groceryapp;

import static com.manuni.groceryapp.Constants.TOPICS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.manuni.groceryapp.databinding.ActivityOrderDetailsSellerBinding;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class OrderDetailsSellerActivity extends AppCompatActivity {
    ActivityOrderDetailsSellerBinding binding;
    private String orderId,orderBy;

    private FirebaseAuth auth;
    private String sourceLatitude,sourceLongitude;
    private String sourceBuyerLatitude, sourceBuyerLongitude;

    private ArrayList<ModelOrderedItems> modelOrderedItems;
    private AdapterOrderedItems adapterOrderedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");

        auth = FirebaseAuth.getInstance();

        FirebaseMessaging.getInstance().subscribeToTopic(TOPICS);

        loadMyInfo();
        loadBuyerInfo();
        loadOrderDetails();
        loadOrderedItems();

        binding.backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });

        binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editOrderedStatusDialog();
            }
        });

    }

    private void editOrderedStatusDialog() {
        final String[] options = {"In Progress","Completed","Cancelled"};
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetailsSellerActivity.this);
        builder.setTitle("Select to change status").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String selectedOption = options[i];
                changeOrderStatus(selectedOption);
            }
        }).show();

    }

    private void changeOrderStatus(String setSelectedOption) {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus",""+setSelectedOption);

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dRef.child(auth.getUid()).child("Orders").child(orderId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                String message = "Order is now in "+setSelectedOption;
                Toast.makeText(OrderDetailsSellerActivity.this, message, Toast.LENGTH_SHORT).show();

                prepareNotification(orderId,""+message);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OrderDetailsSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrderDetails() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbRef.child(auth.getUid()).child("Orders").child(orderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                String latitude = ""+snapshot.child("latitude").getValue();
                String longitude = ""+snapshot.child("longitude").getValue();
                String orderBy = ""+snapshot.child("orderBy").getValue();
                String orderCost = ""+snapshot.child("orderCost").getValue();
                String orderId = ""+snapshot.child("orderId").getValue();
                String orderStatus = ""+snapshot.child("orderStatus").getValue();
                String orderTime = ""+snapshot.child("orderTime").getValue();
                String orderTo = ""+snapshot.child("orderTo").getValue();

                //covert timestamp time to proper time
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.parseLong(orderTime));
                String dateTime = DateFormat.format("dd/MM/yyyy aa",calendar).toString();

                binding.orderDateTV.setText(dateTime);

                //set status color

                binding.orderStatusTV.setText(orderStatus);
                if (orderStatus.equals("In Progress")){
                    binding.orderStatusTV.setTextColor(getResources().getColor(R.color.background_theme));
                }else if (orderStatus.equals("Completed")){
                    binding.orderStatusTV.setTextColor(getResources().getColor(R.color.colorGreen));
                }else if (orderStatus.equals("Cancelled")){
                    binding.orderStatusTV.setTextColor(getResources().getColor(R.color.colorRed));
                }

                binding.orderIdTV.setText(orderId);
                binding.amountTV.setText("$"+orderCost+"[Including delivery fee "+deliveryFee+"]");

                findAddress(latitude,longitude);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        Geocoder geocoder;

        List<Address> addressList;

        geocoder = new Geocoder(OrderDetailsSellerActivity.this, Locale.getDefault());

        try {
            addressList = geocoder.getFromLocation(lat,lon,1);

            String address = addressList.get(0).getAddressLine(0);

            binding.deliveryAddressTV.setText(address);

        } catch (IOException e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void loadOrderedItems(){
        modelOrderedItems = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child(auth.getUid()).child("Orders").child(orderId).child("Items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelOrderedItems.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ModelOrderedItems items = dataSnapshot.getValue(ModelOrderedItems.class);
                    modelOrderedItems.add(items);
                }
                adapterOrderedItems = new AdapterOrderedItems(OrderDetailsSellerActivity.this,modelOrderedItems);
                binding.orderedItemsRV.setAdapter(adapterOrderedItems);

                binding.itemsTV.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void openMap(){
        //saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr=" +sourceLatitude+","+sourceLongitude+"&daddr=" + sourceBuyerLatitude+","+sourceBuyerLongitude;//ekhane bola hocce kotha hote kothay map dekhabe
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void loadMyInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sourceLatitude = ""+snapshot.child("latitude").getValue();
                sourceLongitude = ""+snapshot.child("longitude").getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    } private void loadBuyerInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(orderBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sourceBuyerLatitude = ""+snapshot.child("latitude").getValue();
                sourceBuyerLongitude = ""+snapshot.child("longitude").getValue();
                String email = ""+snapshot.child("email").getValue();
                String phone = ""+snapshot.child("phoneNumber").getValue();

                binding.buyerEmailTV.setText(email);
                binding.buyerPhoneTV.setText(phone);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void prepareNotification(String orderId,String message){
        //when user seller changes order status In Progress/Completed/Cancelled, send notification to buyer

        //prepare data for notification
        //String NOTIFICATION_TOPIC = "/topics/"+Constants.FCM_TOPIC; //must be same as subscribed by user
        String NOTIFICATION_TITLE = "Your Order "+orderId;
        String NOTIFICATION_MESSAGE = ""+message;
        String NOTIFICATION_TYPE = "OrderStatusChanged";

//        //prepare json(what to send and where to send)
//        JSONObject notificationJO = new JSONObject();
//        JSONObject notificationBodyJO = new JSONObject();
//
//        try {
//            //what to send
//            notificationBodyJO.put("notificationType",NOTIFICATION_TYPE);
//            notificationBodyJO.put("buyerUid",orderBy);
//            notificationBodyJO.put("sellerUid",auth.getUid());//we are logged in as seller so current id is seller user id that is auth.getUid();
//            notificationBodyJO.put("orderId",orderId);
//            notificationBodyJO.put("notificationTitle",NOTIFICATION_TITLE);
//            notificationBodyJO.put("notificationMessage",NOTIFICATION_MESSAGE);
//            //where to send
//            //notificationJO.put("to",NOTIFICATION_TOPIC);//to all who subscribe this topic
//            notificationJO.put("data",notificationBodyJO);
//
//        }catch (Exception e){
//            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//
//        }


        sendFcmNotification(NOTIFICATION_TYPE, orderBy, auth.getUid(), orderId, NOTIFICATION_TITLE, NOTIFICATION_MESSAGE);
    }

//    private void sendFcmNotification(JSONObject notificationJO) {
//
////        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJO, new Response.Listener<JSONObject>() {
////            @Override
////            public void onResponse(JSONObject response) {
////                //sent notification
////                Log.e("TAG", ""+response );
////                Toast.makeText(OrderDetailsSellerActivity.this, "Notification sent successfully!", Toast.LENGTH_SHORT).show();
////
////            }
////        }, new Response.ErrorListener() {
////            @Override
////            public void onErrorResponse(VolleyError error) {
////                //failed to send notification
////                Log.e("TAG", ""+error.getMessage() );
////                Toast.makeText(OrderDetailsSellerActivity.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
////
////            }
////        }){
////            @Override
////            public Map<String, String> getHeaders() throws AuthFailureError {
////                Map<String,String> headers = new HashMap<>();
////                headers.put("Content-Type","application/json");
////               // headers.put("Authorization","key="+Constants.FCM_KEY);
////                return headers;
////            }
////        };
////        Volley.newRequestQueue(this).add(jsonObjectRequest);
//    }
    private void sendFcmNotification(String notificationType, String buyerUid, String sellerUid, String orderId, String notificationTitle, String notificationMessage) {
        PushNotification notification = new PushNotification(new NotificationData(notificationType, buyerUid, sellerUid, orderId, notificationTitle, notificationMessage), TOPICS);
        sendNotification(notification, orderId);
    }

    private void sendNotification(PushNotification notification, String orderId) {
        ApiUtilities.getClient().sendNotification(notification).enqueue(new Callback<PushNotification>() {
            @Override
            public void onResponse(Call<PushNotification> call, retrofit2.Response<PushNotification> response) {
                if (response.isSuccessful()) {

                    Toast.makeText(OrderDetailsSellerActivity.this, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OrderDetailsSellerActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PushNotification> call, Throwable t) {

                Toast.makeText(OrderDetailsSellerActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}