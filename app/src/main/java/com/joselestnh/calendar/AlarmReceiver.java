package com.joselestnh.calendar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        PendingIntent pendingIntent = stackBuilder.getPendingIntent(MainActivity.NOTIFICATION_CODE, PendingIntent.FLAG_UPDATE_CURRENT);



        Notification notification = new NotificationCompat.Builder(context,MainActivity.CHANNEL_ID)
                .setContentTitle("Alarm from "+context.getString(R.string.app_name))
                .setContentText("Scheduled task at "+intent.getStringExtra(TaskFormActivity.TASK_START_TIME))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ball)
//                .setContentIntent(pendingIntent)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(MainActivity.CHANNEL_ID, "Calendar Channel", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        notificationManager.notify(0, notification);

        MainActivity.refreshAlarm(context);

    }
}
