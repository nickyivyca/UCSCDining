package com.nickivy.slugfood;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;

import com.nickivy.slugfood.util.Util;

import java.util.HashSet;
import java.util.Set;

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
        if (Util.TAG_TIMEUPDATE.equals(intent.getAction())) {
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> selectedDhalls = prefs.getStringSet("notification_dhalls", null);

        for (String s : selectedDhalls.toArray(new String[0])) {
            int i = Integer.parseInt(s);

            // Only give notifications at breakfast time - don't need 3 notifications during the day
            if (Util.getCurrentMeal(i) == Util.BREAKFAST) {

                mBuilder = null;

                /*
                 * If current day for showing has been adjusted because it is past dining closing,
                 * say 'tomorrow' instead of 'today' in notification
                 */
                int[] today = Util.getToday();
                boolean sayTomorrow = (today[1] != today[3]);

                if (Util.fullMenuObj.get(i).getIsCollegeNight()) {
                    String title = Util.fullMenuObj.get(i).getDinnerList().get(0);
                    mBuilder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_icon)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setContentTitle(title)
                            .setContentText(title + (sayTomorrow ? " tomorrow" : " today"))
                            .setAutoCancel(true)
                            .setChannelId(Util.EVENT_CHANNEL_ID);
                } else if (Util.fullMenuObj.get(i).getIsHealthyMonday()) {
                    mBuilder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_icon)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setContentTitle("Healthy Monday")
                            .setContentText(Util.collegeList[i] + " Healthy Monday " +
                                    (sayTomorrow ? "tomorrow" : "today"))
                            .setAutoCancel(true)
                            .setChannelId(Util.EVENT_CHANNEL_ID);
                } else if (Util.fullMenuObj.get(i).getIsFarmFriday()) {
                    mBuilder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_icon)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setContentTitle("Farm Friday")
                            .setContentText(Util.collegeList[i] + " Farm Friday " +
                                    (sayTomorrow ? "tomorrow" : "today"))
                            .setAutoCancel(true)
                            .setChannelId(Util.EVENT_CHANNEL_ID);
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

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel mChannel = new NotificationChannel(Util.EVENT_CHANNEL_ID,
                                context.getResources().getString(R.string.notification_channel_events),
                                NotificationManager.IMPORTANCE_DEFAULT);
                        mNotifyManager.createNotificationChannel(mChannel);
                    }

                    // Don't notify if notification has already been posted?
                    mNotifyManager.notify(i, mBuilder.build());
                }
            }
        }
    }

    private void runFavoritesNotifications(Context context) {
        NotificationCompat.Builder mBuilder;
        int notNum = -1; // keep track of how many notifications are made - adds negatively
        NotificationManager mNotifyManager = (NotificationManager)
                context.getSystemService(context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(Util.MEAL_CHANNEL_ID,
                    context.getResources().getString(R.string.notification_channel_meal), NotificationManager.IMPORTANCE_DEFAULT);
            mNotifyManager.createNotificationChannel(mChannel);
        }

        /*
         * If current day for showing has been adjusted because it is past dining closing,
         * say 'tomorrow' instead of 'today' in notification
         */
        int[] today = Util.getToday();
        boolean sayTomorrow = (today[1] != today[3]);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String[] favsList = prefs.getStringSet("favorites_items_list", new HashSet<String>()).toArray(new String[0]);
        for (String item : favsList) {
            Set<String> selectedDhalls = prefs.getStringSet("notification_dhalls", null);

            for (String s : selectedDhalls.toArray(new String[0])) {
                int i = Integer.parseInt(s);
                switch(Util.getCurrentMeal(i)) {
                    case Util.BREAKFAST:
                        if (Util.fullMenuObj.get(i).getBreakfastList().contains(item)) {
                            mBuilder = new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.ic_icon)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setContentTitle(item)
                                    .setContentText("At " + Util.collegeList[i] + " Breakfast " +
                                            (sayTomorrow ? "tomorrow" : "today"))
                                    .setAutoCancel(true)
                                    .setChannelId(Util.MEAL_CHANNEL_ID);
                            Intent notificationIntent = new Intent(context, MainActivity.class);
                            notificationIntent.putExtra(Util.TAG_COLLEGE, i);
                            notificationIntent.putExtra(Util.TAG_MEAL, Util.BREAKFAST);
                            notificationIntent.putExtra(Util.TAG_FROMNOTIFICATION, true);

                            PendingIntent pendingNotification = PendingIntent.getActivity(context, notNum,
                                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                            mBuilder.setContentIntent(pendingNotification);

                            mNotifyManager.notify(notNum--, mBuilder.build());
                        }
                        break;
                    case Util.LUNCH:
                        if (Util.fullMenuObj.get(i).getLunchList().contains(item)) {
                            mBuilder = new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.ic_icon)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setContentTitle(item)
                                    .setContentText("At " + Util.collegeList[i] + " Lunch " +
                                            (sayTomorrow ? "tomorrow" : "today"))
                                    .setAutoCancel(true)
                                    .setChannelId(Util.MEAL_CHANNEL_ID);
                            Intent notificationIntent = new Intent(context, MainActivity.class);
                            notificationIntent.putExtra(Util.TAG_COLLEGE, i);
                            notificationIntent.putExtra(Util.TAG_MEAL, Util.LUNCH);
                            notificationIntent.putExtra(Util.TAG_FROMNOTIFICATION, true);

                            PendingIntent pendingNotification = PendingIntent.getActivity(context, notNum,
                                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                            mBuilder.setContentIntent(pendingNotification);

                            mNotifyManager.notify(notNum--, mBuilder.build());
                        }
                        break;
                    case Util.DINNER:
                        if (Util.fullMenuObj.get(i).getDinnerList().contains(item)) {
                            mBuilder = new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.ic_icon)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setContentTitle(item)
                                    .setContentText("At " + Util.collegeList[i] + " Dinner " +
                                            (sayTomorrow ? "tomorrow" : "today"))
                                    .setAutoCancel(true)
                                    .setChannelId(Util.MEAL_CHANNEL_ID);
                            Intent notificationIntent = new Intent(context, MainActivity.class);
                            notificationIntent.putExtra(Util.TAG_COLLEGE, i);
                            notificationIntent.putExtra(Util.TAG_MEAL, Util.DINNER);
                            notificationIntent.putExtra(Util.TAG_FROMNOTIFICATION, true);

                            PendingIntent pendingNotification = PendingIntent.getActivity(context, notNum,
                                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                            mBuilder.setContentIntent(pendingNotification);

                            mNotifyManager.notify(notNum--, mBuilder.build());
                        }
                        break;
                    case Util.LATENIGHT:
                        if (Util.fullMenuObj.get(i).getLateNightList().contains(item)) {
                            mBuilder = new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.ic_icon)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setContentTitle(item)
                                    .setContentText("At " + Util.collegeList[i] + " Late Night " +
                                            (sayTomorrow ? "tomorrow" : "tonight"))
                                    .setAutoCancel(true)
                                    .setChannelId(Util.MEAL_CHANNEL_ID);
                            Intent notificationIntent = new Intent(context, MainActivity.class);
                            notificationIntent.putExtra(Util.TAG_COLLEGE, i);
                            notificationIntent.putExtra(Util.TAG_MEAL, Util.LATENIGHT);
                            notificationIntent.putExtra(Util.TAG_FROMNOTIFICATION, true);

                            PendingIntent pendingNotification = PendingIntent.getActivity(context, notNum,
                                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                            mBuilder.setContentIntent(pendingNotification);

                            mNotifyManager.notify(notNum--, mBuilder.build());
                        }
                        break;

                }
            }
        }

    }
}