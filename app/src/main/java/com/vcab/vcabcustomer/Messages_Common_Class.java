package com.vcab.vcabcustomer;

import android.animation.ValueAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.ui.IconGenerator;
import com.vcab.vcabcustomer.model.AnimationModel;
import com.vcab.vcabcustomer.model.DriverGeoModel;
import com.vcab.vcabcustomer.model.SelectPlaceEvent;
import com.vcab.vcabcustomer.notification_manager.FCMResponse;
import com.vcab.vcabcustomer.notification_manager.FCMSendData;
import com.vcab.vcabcustomer.notification_manager.IFCMService;
import com.vcab.vcabcustomer.notification_manager.RetrofitFCMClient;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class Messages_Common_Class {


    public static HashMap<String, DriverGeoModel> driverFound = new HashMap<String, DriverGeoModel>();

    // https://abhiandroid.com/java/hashmap  more about HashMaps .. key and value
    public static HashMap<String, Marker> markerList = new HashMap<String, Marker>();
    public static HashMap<String, AnimationModel> driverLocationSubscribe = new HashMap<String, AnimationModel>();

    public static void showToastMsg(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showSnackBar(String msg, View view) {

        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
    }

    //DECODE POLY--- gives the direction array
    public static List<LatLng> decodePoly(String encoded) {
        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;

            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    public static float getBearing(LatLng begin, LatLng end) {
        //You can copy this function by link at description
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    public static void setWelcomeMessage(TextView welcome_text) {

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 1 && hour <= 12) {
            welcome_text.setText("Good morning");
        } else if (hour >= 13 && hour <= 17) {
            welcome_text.setText("Good afternoon");
        } else {
            welcome_text.setText("Good evening");
        }
    }

    public static String formatDuration(String duration) {

        if (duration.contains("mins")) {
            return duration.substring(0, duration.length() - 1);//remove letter "s"
        } else {
            return duration;
        }
    }

    public static String formatAddress(String start_address) {
        int firstIndexOfComma = start_address.indexOf(",");
        return start_address.substring(0, firstIndexOfComma); // get only address
    }

    public static ValueAnimator valueAnimate(long duration, ValueAnimator.AnimatorUpdateListener listener) {

        ValueAnimator va = ValueAnimator.ofFloat(0, 100);
        va.setDuration(duration);
        va.addUpdateListener(listener);
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setRepeatMode(ValueAnimator.RESTART);
        va.start();
        return va;

    }

    public static void showNotification(Context context, String title, String body){
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = context.getString(R.string.d_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.taxi_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.add_user_two))
                        .setColor(Color.parseColor("#000000"))
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("vCab Customer Service");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    public static void sendRequestToDriver(Context requestDriverActivity, RelativeLayout main_request_layout, DriverGeoModel foundDriver, SelectPlaceEvent selectPlaceEvent) {

        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        //get driver Token to send notification

        DocumentReference nycRef = FirebaseFirestore.getInstance().document("users/drivers/userData/" + foundDriver.getKey());

        nycRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String driverToken = document.get("firebaseToken").toString();

                        Map<String, String> notificationData = new HashMap<>();
                        notificationData.put("title", "RequestDriver");
                        notificationData.put("body", "This message represent for request driver action");
                        notificationData.put("customerUid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        notificationData.put("PickupLocation", selectPlaceEvent.getUserOriginString());
                        notificationData.put("CustomerDestinationLocation", selectPlaceEvent.getUserDestinationString());
                        notificationData.put("CustomerDestinationAddress", selectPlaceEvent.getAddress());

                        /* notificationData.put("PickupLocation", new StringBuilder("")
                .append(selectPlaceEvent.getUserOrigin().latitude)
                .append(",")
                .append(selectPlaceEvent.getUserOrigin().longitude)
                .toString());*/

                        FCMSendData fcmSendData = new FCMSendData(driverToken, notificationData);

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<FCMResponse>() {
                                    @Override
                                    public void accept(FCMResponse fcmResponse) throws Exception {

                                        if (fcmResponse.getSuccess() == 0) {

                                            compositeDisposable.clear();
                                            showSnackBar("Failed to send request to driver", main_request_layout);

                                        }


                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        compositeDisposable.clear();
                                        showToastMsg(throwable.getMessage(), requestDriverActivity);
                                    }
                                }));


                    } else {
                    }
                } else {
                    Messages_Common_Class.showSnackBar("Try again. No driver token found", main_request_layout);
                }
            }
        });


    }

    public static Bitmap createIconWithDuration(Context context, String duration) {

        View view = LayoutInflater.from(context).inflate(R.layout.pickup_info_with_duration_windows,null);

        String tempDuration=duration.substring(0,duration.indexOf(" "));

        TextView txt_duration = (TextView) view.findViewById(R.id.txt_duration);
        txt_duration.setText(Messages_Common_Class.formatDuration(tempDuration));

        IconGenerator iconGenerator = new IconGenerator(context);
        iconGenerator.setContentView(view);
        iconGenerator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        return iconGenerator.makeIcon();
    }
}
