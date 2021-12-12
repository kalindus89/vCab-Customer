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
import com.vcab.vcabcustomer.Messages_Common_Class;
import com.vcab.vcabcustomer.R;
import com.vcab.vcabcustomer.model.events_notification.AcceptRequestFromDriver;
import com.vcab.vcabcustomer.model.events_notification.DeclineAndRemoveTripFromDriver;
import com.vcab.vcabcustomer.model.events_notification.DeclineRequestFromDriver;
import com.vcab.vcabcustomer.model.events_notification.DriverCompletedTripEvent;

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
            } else if (dataReceive.get("title").equals("DeclineAndRemoveTrip")) {
                EventBus.getDefault().postSticky(new DeclineAndRemoveTripFromDriver());
            } else if (dataReceive.get("title").equals("Accept")) { // if driver accept the request.
                EventBus.getDefault().postSticky(new AcceptRequestFromDriver(dataReceive.get("tripId")
                        , dataReceive.get("driverUid")));
            } else if (dataReceive.get("title").equals("DriverCompleteTrip")) { // if driver completed the rip
                EventBus.getDefault().postSticky(new DriverCompletedTripEvent(dataReceive.get("driverUid")));
            } else {
                Messages_Common_Class.showNotification(this, dataReceive.get("title"), dataReceive.get("body"));

            }
        }
    }
}
