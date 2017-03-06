package com.example.android.bjjscorekeeper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Ivars on 2017.03.04..
 * with this receiver the app detects when the timer has run out, while the app is closed
 */

public class TimerExpiredReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context, MainActivity.class);
        //Flags reguate how the intent is handled
        //FLAG_ACTIVITY_CLEAR_TOP does not allow duplicate activities made
        //TODO: this did not work for me, had to set android:launchMode="singleTop" in manifest WHY?
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //the pending intent opens the main activity. It will be used when clicked on notification
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, i, 0);

        //the information and actions of the notification are set VIA the builder
        NotificationCompat.Builder b = new NotificationCompat.Builder(context);
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        b.setSound(notification)
                .setContentTitle(context.getString(R.string.round_over_notification_message))//the notification title
                .setAutoCancel(true)//the notification gets cancelled when it is clicked on
                //.setContentText("Timer finished")//second row of notification
                .setSmallIcon(android.R.drawable.ic_notification_clear_all)//notification icon
                .setContentIntent(pIntent);//when user clicks the notification the app is opened

        //form the build() we get the notification with the settings previouslya added
        Notification n = b.build();
        NotificationManager myNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //post the notification
        myNotificationManager.notify(0, n);
    }
}
