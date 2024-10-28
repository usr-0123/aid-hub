package com.example.aidhub.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import com.example.aidhub.R;
import com.example.aidhub.groups.GroupActivity;

public class MessageNotificationHelper {

    public static final String CHANNEL_ID = "messages_channel_id";

    // Method to create notification channel with high importance
    public static void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Messages Channel",
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setDescription("Notification channels for group and chat messages.");

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    // Method to show heads-up notification with sender, message, and timestamp
    public static void showNotification(Context context, String groupId, String sender, String message) {
        // Create the channel
        createNotificationChannel(context);

        // Intent to open GroupActivity when the notification is clicked
        Intent intent = new Intent(context, GroupActivity.class);
        intent.putExtra("groupId", groupId);  // Pass group ID to open the correct group
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Custom sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Build the notification with high priority and a big text style
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(sender)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(soundUri)
                .setVibrate(new long[]{1000, 1000});

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());  // Use a unique ID for each notification
        }
    }
}