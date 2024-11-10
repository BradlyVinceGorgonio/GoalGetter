package com.example.goalgetter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
        String taskType = intent.getStringExtra("taskType"); // Retrieve the task type

        // Create the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context, "taskReminderChannel")
                .setContentTitle(courseName)
                .setContentText("Task Type: " + taskType + " | Due Date: " + dueDate) // Include task type in the notification text
                .setSmallIcon(R.drawable.baseline_access_alarm_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        // Log and show toast for debugging
        Log.d("AlarmReceiver", "Alarm triggered for: " + courseName);
        Toast.makeText(context, "Alarm triggered for: " + courseName, Toast.LENGTH_SHORT).show();

        // Show the notification
        notificationManager.notify(0, notification);
    }
}
