package com.example.shlomi.publichand;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Shlomi on 22/03/2017.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, HomePageActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(HomePageActivity.class);
        taskStackBuilder.addNextIntent(intent1);
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(100, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new  NotificationCompat.Builder(context);
        Notification notification = builder.setContentTitle(context.getString(R.string.title_notification))
                .setContentText(context.getString(R.string.content))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(((BitmapDrawable) context.getResources().getDrawable(R.drawable.logo)).getBitmap())
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}
