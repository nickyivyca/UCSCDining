package com.nickivy.slugfood;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.nickivy.slugfood.parser.MenuParser;
import com.nickivy.slugfood.util.Util;

/**
 * Class for managing and delivering notifications. When it receives an intent
 * with TAG_TIMEUPDATE action, it will check and deliver notifications, based on
 * which are enabled in preferences.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author Nicky Ivy parkedraccoon@gmail.com
 */

public class Notifications extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(Util.LOGTAG, "Notifications: Received a thing");
        if (Util.TAG_TIMEUPDATE.equals(intent.getAction())) {
            Log.v(Util.LOGTAG, "Received timeupdate");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean("notifications_events", false)) {
                runEventNotifications(context);
            }
            if (prefs.getBoolean("notifications_favorites", false)) {
                runFavoritesNotifications(context);
            }
        }

    }

    private void runEventNotifications(Context context) {
        NotificationCompat.Builder mBuilder;

        for (int i = 0; i < 5; i++) {
            mBuilder = null;

            /*
             * If current day for showing has been adjusted because it is past dining closing,
             * say 'tomorrow' instead of 'today' in notification
             */
            int[] today = Util.getToday();
            boolean sayTomorrow = (today[1] != today[3]);

            if (MenuParser.fullMenuObj.get(i).getIsCollegeNight()) {
                mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_icon)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentTitle("College Night")
                        .setContentText(Util.collegeList[i] + " College Night " +
                                (sayTomorrow ? "tomorrow" : "today"))
                        .setAutoCancel(true);
            } else if (MenuParser.fullMenuObj.get(i).getIsHealthyMonday()) {
                mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_icon)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentTitle("Healthy Monday")
                        .setContentText(Util.collegeList[i] + " Healthy Monday " +
                                (sayTomorrow ? "tomorrow" : "today"))
                        .setAutoCancel(true);
            } else if (MenuParser.fullMenuObj.get(i).getIsFarmFriday()) {
                mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_icon)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentTitle("Farm Friday")
                        .setContentText(Util.collegeList[i] + " Farm Friday " +
                                (sayTomorrow ? "tomorrow" : "today"))
                        .setAutoCancel(true);
            }
            if (mBuilder != null) {
                Intent notificationIntent = new Intent(context, MainActivity.class);
                notificationIntent.putExtra(Util.TAG_COLLEGE, i);
                notificationIntent.putExtra(Util.TAG_FROMNOTIFICATION, true);

                PendingIntent pendingNotification = PendingIntent.getActivity(context, i,
                        notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder.setContentIntent(pendingNotification);

                NotificationManager mNotifyManager = (NotificationManager)
                        context.getSystemService(context.NOTIFICATION_SERVICE);

                mNotifyManager.notify(i, mBuilder.build());
            }
        }
    }

    private void runFavoritesNotifications(Context context) {

    }
}