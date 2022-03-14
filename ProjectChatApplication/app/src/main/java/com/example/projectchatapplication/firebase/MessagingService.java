package com.example.projectchatapplication.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.projectchatapplication.R;
import com.example.projectchatapplication.activities.ChatActivity;
import com.example.projectchatapplication.models.User;
import com.example.projectchatapplication.utilities.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM","Token " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d("FCM","Message " + remoteMessage.getData().get("id"));
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                User user = new User();
                user.id = remoteMessage.getData().get("id");
                user.name = remoteMessage.getData().get("name");
                user.image = remoteMessage.getData().get("image");
                sendNotification(title, body, user);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        super.onMessageReceived(remoteMessage);

        Log.d("FCM", "Message: "+ remoteMessage.getNotification().getBody() + " " + remoteMessage.getData());
    }

    private void sendNotification(String title, String message, User user){
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL,
                    "New Message",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }
        manager.notify(Constants.NOTIFICATION_ID, builder.build());
    }
}
