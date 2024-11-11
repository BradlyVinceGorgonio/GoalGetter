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
        String taskID = intent.getStringExtra("taskID");  // Add taskID or any other necessary data

        // Create an Intent for DetailedTask activity
        Intent detailedTaskIntent = new Intent(context, DetailedTask.class);
        detailedTaskIntent.putExtra("taskID", taskID);  // Pass task data to the DetailedTask activity

        // Use TaskStackBuilder to ensure the correct activity stack behavior
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(DetailedTask.class); // Add the parent activity to the stack (if any)
        stackBuilder.addNextIntent(detailedTaskIntent);  // Add the DetailedTask activity to the stack

        // Create a PendingIntent that will open the DetailedTask activity when the notification is clicked
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);


        // Create the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context, "taskReminderChannel")
                .setContentTitle(courseName)
                .setContentText("Task Type: " + taskType + " | Due Date: " + dueDate)
                .setSmallIcon(R.drawable.baseline_access_alarm_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)  // Set the PendingIntent on the notification
                .build();

        // Log and show toast for debugging
        Log.d("AlarmReceiver", "Alarm triggered for: " + courseName);
        Toast.makeText(context, "Alarm triggered for: " + courseName, Toast.LENGTH_SHORT).show();

        // Show the notification
        notificationManager.notify(0, notification);
    }
}