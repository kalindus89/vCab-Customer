package com.vcab.vcabcustomer.firebase_notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.vcab.vcabcustomer.MainActivity;
import com.vcab.vcabcustomer.R;
import com.vcab.vcabcustomer.model.AcceptRequestFromDriver;
import com.vcab.vcabcustomer.model.DeclineRequestFromDriver;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // String title,message;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        /*title = remoteMessage.getData().get("title");
        message = remoteMessage.getData().get("message");*/

        // any key send by server
        //  title = remoteMessage.getNotification().getTitle();
        //  message = remoteMessage.getNotification().getBody();

        showNotification(remoteMessage);
    }

    private void showNotification(RemoteMessage remoteMessage) {

        Map<String, String> dataReceive = remoteMessage.getData();

        if (dataReceive != null) {

            if (dataReceive.get("title").equals("Decline")) { // if driver decline the request. no need to get any notification values.
                EventBus.getDefault().postSticky(new DeclineRequestFromDriver());
            }

            else if (dataReceive.get("title").equals("Accept")) { // if driver accept the request.
                EventBus.getDefault().postSticky(new AcceptRequestFromDriver(dataReceive.get("tripId")
                        ,dataReceive.get("driverUid")));
            }

            else {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                String channelId = getString(R.string.d_channel_id);
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this, channelId)
                                .setSmallIcon(R.drawable.taxi_icon)
                                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.add_user_two))
                                .setColor(Color.parseColor("#000000"))
                                .setContentTitle("title")
                                .setContentText("message")
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                .setSound(defaultSoundUri)
                                .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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
        }
    }
}
