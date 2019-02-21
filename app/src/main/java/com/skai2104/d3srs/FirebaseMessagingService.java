package com.skai2104.d3srs;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String messageTitle = remoteMessage.getNotification().getTitle();
        String messageBody = remoteMessage.getNotification().getBody();
        String clickAction = remoteMessage.getNotification().getClickAction();
        String dataMessage = remoteMessage.getData().get("message");
        String dataFrom = remoteMessage.getData().get("from_user");
        String fromUserId = remoteMessage.getData().get("from_user_id");
        String latitudeStr = remoteMessage.getData().get("latitude");
        String longitudeStr = remoteMessage.getData().get("longitude");
        String datetime = remoteMessage.getData().get("datetime");
        String status = remoteMessage.getData().get("status");

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel1";
            String description = "channel1";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(getString(R.string.default_notification_channel_id), name, importance);
            channel.setDescription(description);

            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.round_announcement_black_24)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true);

        Intent resultIntent = new Intent(clickAction);
        resultIntent.putExtra("message", dataMessage);
        resultIntent.putExtra("from_user", dataFrom);
        resultIntent.putExtra("from_user_id", fromUserId);
        resultIntent.putExtra("latitude", latitudeStr);
        resultIntent.putExtra("longitude", longitudeStr);
        resultIntent.putExtra("datetime", datetime);
        resultIntent.putExtra("status", status);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        builder.setContentIntent(resultPendingIntent);

        int notificationId = (int) System.currentTimeMillis();

        notificationManager.notify(notificationId, builder.build());
    }
}
