package com.wordmas.payment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.onesignal.OSNotification;
import com.onesignal.OSMutableNotification;
import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;


@SuppressWarnings("unused")
public class NotificationServiceExtension implements OneSignal.OSRemoteNotificationReceivedHandler {
    int notificId;
    CommonLibrary Library = new CommonLibrary();

    @Override
    public void remoteNotificationReceived(Context context, OSNotificationReceivedEvent notificationReceivedEvent) {
        OSNotification notification = notificationReceivedEvent.getNotification();

        // Example of modifying the notification's accent color
        OSMutableNotification mutableNotification = notification.mutableCopy();
        mutableNotification.setExtender(builder -> builder.setColor(context.getResources().getColor(R.color.colorPrimary)));

        JSONObject data = notification.getAdditionalData();
        Log.d("OneSignalExample", "Received Notification Data: " + data);

        // If complete isn't call within a time period of 25 seconds, OneSignal internal logic will show
        // the original notification
        // To omit displaying a notification, pass `null` to complete()
        //notificationReceivedEvent.complete(mutableNotification);
        notificationReceivedEvent.complete(null);
    }


    private void displayNotification(Context context, String title, String task) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("simplifiedcoding", "simplifiedcoding", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "simplifiedcoding")
                .setContentTitle(title)
                .setContentText(task)
                .setSmallIcon(R.mipmap.ic_launcher);

        notificationManager.notify(notificId, notification.build());
    }

}
