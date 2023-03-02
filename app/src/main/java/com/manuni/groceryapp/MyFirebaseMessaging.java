package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;
import java.util.Random;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MyFirebaseMessaging extends FirebaseMessagingService {
    private static final String NOTIFICATION_CHANNEL_ID = "MY_NOTIFICATION_CHANNEL_ID";//required for O and above versions
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;


    private final String CHANNEL_ID = "channel_id";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        //all notification will be received here

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();


        String notificationType = message.getData().get("notificationType");

        assert notificationType != null;
        if (notificationType.equals("NewOrder")) {

            String buyerUid = message.getData().get("buyerUid");
            String sellerUid = message.getData().get("sellerUid");
            String orderId = message.getData().get("orderId");
            String notificationTitle = message.getData().get("notificationTitle");
            String notificationMessage = message.getData().get("notificationMessage");

            if (firebaseUser != null && Objects.equals(auth.getUid(), sellerUid)) {
                //user is signed in and he is same user to which notification to be sent
                showNotification(orderId, sellerUid, buyerUid, notificationTitle, notificationMessage, notificationType);

            }
        }
        if (notificationType.equals("OrderStatusChanged")) {
            String buyerUid = message.getData().get("buyerUid");
            String sellerUid = message.getData().get("sellerUid");
            String orderId = message.getData().get("orderId");
            String notificationTitle = message.getData().get("notificationTitle");
            String notificationMessage = message.getData().get("notificationMessage");


            if (firebaseUser != null && Objects.equals(auth.getUid(), buyerUid)) {
                //user is signed in and he is same user to which notification to be sent
                showNotification(orderId, sellerUid, buyerUid, notificationTitle, notificationMessage, notificationType);

            }

        }
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showNotification(String orderId, String sellerUid, String buyerUid, String notificationTitle, String notificationDescription, String notificationType) {
        //notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = new Random().nextInt(3000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannel(notificationManager);
        }
        //handle notification click, start order activity here
        Intent intent = null;
        if (notificationType.equals("NewOrder")) {
            intent = new Intent(this, OrderDetailsSellerActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderBy", buyerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        } else if (notificationType.equals("OrderStatusChanged")) {
            intent = new Intent(this, OrderDetailsUsersActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderTo", sellerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        }

       PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{intent}, PendingIntent.FLAG_IMMUTABLE);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.sop);
        //notification sound
        Uri notificationSoundUriRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.sop)
                .setLargeIcon(largeIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDescription)
                .setSound(notificationSoundUriRingtone)
                .setAutoCancel(true)//when click on the notification will be auto removed
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(notificationId, notification);
    }

//    @TargetApi(Build.VERSION_CODES.O)
//    private void createNotificationChannel(NotificationManager manager) {
//        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"channelName",NotificationManager.IMPORTANCE_HIGH);
//        channel.setDescription("My description");
//        channel.enableLights(true);
//        channel.setLockscreenVisibility(1);//0 means private message on lock screen,-1 means Do not reveal any part of this notification on a secure lockscreen.1 means show full message in the lock screen
//        channel.setLightColor(Color.RED);
//        channel.enableVibration(true);
//        channel.setVibrationPattern(new long[] { 1000, 1000, 1000, 1000, 1000 });
//
//        manager.createNotificationChannel(channel);
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "Some Sample Text";
        String channelDescription = "Channel Description Here";

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
