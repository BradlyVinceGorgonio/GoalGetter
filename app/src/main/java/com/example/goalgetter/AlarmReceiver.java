package com.example.goalgetter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Create notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Reminder";
            String description = "Channel for task reminder notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("taskReminderChannel", name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Retrieve data from the intent
        String courseName = intent.getStringExtra("courseName");
        String dueDate = intent.getStringExtra("dueDate");
        String taskType = intent.getStringExtra("taskType");
        String taskID = intent.getStringExtra("taskID");
        String priorityMode = intent.getStringExtra("priorityMode");
        boolean isGroupTask = intent.getBooleanExtra("isGroup", false);

        // Choose the appropriate activity based on isGroupTask
        Class<?> targetActivity = isGroupTask ? CreatedGroupTask.class : DetailedTask.class;

        // Create an Intent for the selected activity
        Intent targetIntent = new Intent(context, targetActivity);
        targetIntent.putExtra("taskID", taskID);

        // Use TaskStackBuilder to ensure correct activity stack behavior
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(targetActivity);
        stackBuilder.addNextIntent(targetIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create notification text
        String notificationText = "Task Type: " + taskType + " | Due Date: " + dueDate;
        if ("Yes".equalsIgnoreCase(priorityMode)) {
            notificationText = "PRIORITY TASK!\n" + notificationText;
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "taskReminderChannel")
                .setContentTitle(courseName)
                .setContentText(notificationText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText)) // Use BigTextStyle for longer text
                .setSmallIcon(R.drawable.baseline_access_alarm_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Highlight priority tasks with red text
        if ("Yes".equalsIgnoreCase(priorityMode)) {
            builder.setColor(context.getResources().getColor(android.R.color.holo_red_light)) // Set red color
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("PRIORITY TASK!\n" + "Task Type: " + taskType + " | Due Date: " + dueDate)); // Large priority text
        }

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(taskID.hashCode(), builder.build());
    }

}