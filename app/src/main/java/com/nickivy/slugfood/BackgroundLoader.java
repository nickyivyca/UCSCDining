package com.nickivy.slugfood;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nickivy.slugfood.parser.MealDataFetcher;
import com.nickivy.slugfood.util.Util;
import com.nickivy.slugfood.widget.MenuWidget;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class controls the background loading of the app. The
 * alarms are set in this class, and this class sends out broadcasts
 * to relevant areas when loading is done.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author @author Nicky Ivy parkedraccoon@gmail.com
 */

public class BackgroundLoader extends BroadcastReceiver {

    private PendingIntent breakfastIntent = null,
            lunchIntent= null,
            dinnerIntent = null,
            latenightIntent = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Util.TAG_TIMEUPDATE.equals(intent.getAction())) {
        //if (Util.TAG_TIMEUPDATE.equals(intent.getAction()) || Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            setAlarm(context);
            // Run background data load
            int today[] = Util.getToday();
            new BackgroundLoadTask(today[0], today[1],
                    today[2], context).execute();
        } else if (Util.TAG_ENABLEBACKGROUND.equals(intent.getAction())) {
            setAlarm(context);
        } else if (Util.TAG_WIDGETENABLED.equals(intent.getAction())) {
            setAlarm(context);
            // Set preferences to force background load on for when widget active
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("widget_enabled", true);
            edit.putBoolean("background_load", true);
            edit.commit();
        } else if (Util.TAG_DISABLEBACKGROUND.equals(intent.getAction())) {
            disableAlarm(context);
        } else if (Util.TAG_WIDGETGONE.equals(intent.getAction())) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("widget_enabled", false);
            edit.commit();
        //} else if (Util.TAG_NOTIFICATIONSON.equals(intent.getAction())) {
        //} else if (Util.TAG_NOTIFICATIONSOFF.equals(intent.getAction())) {
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Reset alarm in onReceive if enabled from boot
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean("background_load", false)) {
                setAlarm(context);
            }
            /*// Test code for testing time-based events
            int today[] = Util.getToday();
            new BackgroundLoadTask(today[0], today[1],
                    today[2], context).execute();
            Log.v(Util.LOGTAG, "boot completed received");*/
        }
    }

    /**
     * Android can be idiotic sometimes and not call the onEnabled method, which means that if the
     * alarm manager is set up in onEnabled, which would be the proper place to do it since it only
     * needs to run once, it will not always be enabled. I've noticed that it will work the first
     * time I install the app, but if I update the app the Alarm manager will not run. With this
     * method we can call it from both onUpdate and onEnabled.
     *
     * However! AlarmManager is stupid. If you reset the alarm, it runs the alarm again in about
     * a minute. This means that the alarm set to run every hour or every day runs every day. If you
     * add two or three alarms they can start piling up on top of each other and triggering
     * constantly. So we have to run checks to make sure that the alarm is not already set before
     * resetting it. The PendingIntent.FLAG_UPDATE_CURRENT is supposed to take care of this, but it
     * doesn't.
     */
    public void setAlarm(Context context) {
        final Intent timeIntent = new Intent(context, BackgroundLoader.class);
        timeIntent.setAction(Util.TAG_TIMEUPDATE);

        final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Clearing the old breakfast intent.
        PendingIntent oldIntent = PendingIntent.getBroadcast(context, 21,
                timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        m.cancel(oldIntent);
        oldIntent.cancel();


        SharedPreferences settings = context.getSharedPreferences(Util.DST_PREF, 0);
        SharedPreferences.Editor editor = settings.edit();

        // If getBroadcast with the NO_CREATE flag returns null for all of these all alarms are set
        // Don't reset them or else the alarm manager will run off with them
        boolean alarmEnabled = (PendingIntent.getBroadcast(context, Util.BREAKFAST_SWITCH_TIME,
                        timeIntent, PendingIntent.FLAG_NO_CREATE) != null) &&
                (PendingIntent.getBroadcast(context, Util.LUNCH_SWITCH_TIME,
                        timeIntent, PendingIntent.FLAG_NO_CREATE) != null) &&
                (PendingIntent.getBroadcast(context, Util.DINNER_SWITCH_TIME,
                        timeIntent, PendingIntent.FLAG_NO_CREATE) != null) &&
                (PendingIntent.getBroadcast(context, Util.LATENIGHT_SWITCH_TIME,
                        timeIntent, PendingIntent.FLAG_NO_CREATE) != null);
        if (alarmEnabled) {
            if (settings.getBoolean(Util.KEY_DST_PREF, true) != TimeZone.getDefault().inDaylightTime(new Date())) {
                Util.log("Resetting alarm to change DST mode");
                disableAlarm(context);
                editor.putBoolean(Util.KEY_DST_PREF, TimeZone.getDefault().inDaylightTime(new Date()));
                editor.commit();
            } else {
                return;
            }
        }
        Calendar calendar = Calendar.getInstance();

        breakfastIntent = PendingIntent.getBroadcast(context, Util.BREAKFAST_SWITCH_TIME,
                timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        calendar.set(Calendar.HOUR_OF_DAY, Util.BREAKFAST_SWITCH_TIME);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        m.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, breakfastIntent);

        lunchIntent = PendingIntent.getBroadcast(context, Util.LUNCH_SWITCH_TIME,
                timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        calendar.set(Calendar.HOUR_OF_DAY, Util.LUNCH_SWITCH_TIME);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        m.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, lunchIntent);

        dinnerIntent = PendingIntent.getBroadcast(context, Util.DINNER_SWITCH_TIME,
                timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        calendar.set(Calendar.HOUR_OF_DAY, Util.DINNER_SWITCH_TIME);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        m.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, dinnerIntent);

        latenightIntent = PendingIntent.getBroadcast(context, Util.LATENIGHT_SWITCH_TIME,
                timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        calendar.set(Calendar.HOUR_OF_DAY, Util.LATENIGHT_SWITCH_TIME);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        m.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, latenightIntent);
    }

    public void disableAlarm(Context context) {
        final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final Intent timeIntent = new Intent(context, BackgroundLoader.class);
        timeIntent.setAction(Util.TAG_TIMEUPDATE);

        // Remake intents to cancel them in case they get lost in memory
        breakfastIntent = PendingIntent.getBroadcast(context, Util.BREAKFAST_SWITCH_TIME,
                timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        lunchIntent = PendingIntent.getBroadcast(context, Util.LUNCH_SWITCH_TIME,
                timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        dinnerIntent = PendingIntent.getBroadcast(context, Util.DINNER_SWITCH_TIME,
                timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        latenightIntent = PendingIntent.getBroadcast(context, Util.LATENIGHT_SWITCH_TIME,
                timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Clearing the old breakfast intent.
        PendingIntent oldIntent = PendingIntent.getBroadcast(context, 21,
                timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Cancel the alarms
        m.cancel(breakfastIntent);
        m.cancel(lunchIntent);
        m.cancel(dinnerIntent);
        m.cancel(latenightIntent);
        m.cancel(oldIntent);
        // Cancel the intents themselves (so the alarmenabled calculations will work properly)
        breakfastIntent.cancel();
        lunchIntent.cancel();
        dinnerIntent.cancel();
        latenightIntent.cancel();
        oldIntent.cancel();
    }


    private class BackgroundLoadTask extends AsyncTask<Void, Void, Long> {

        private int mAttemptedMonth,
                mAttemptedDay,
                mAttemptedYear;

        private Context mContext;

        /**
         * @param month Month of day to fetch
         * @param day Day of day to fetch
         * @param year Year of day to fetch
         * @param context context
         */
        public BackgroundLoadTask(int month, int day, int year, Context context) {
            mAttemptedMonth = month;
            mAttemptedDay = day;
            mAttemptedYear = year;
            mContext = context;
        }

        @Override
        protected void onPreExecute(){}

        @Override
        protected Long doInBackground(Void... voids) {
            int res = Util.GETLIST_SUCCESS;

            try {
                MealDataFetcher.fetchData(mContext, mAttemptedMonth, mAttemptedDay,
                        mAttemptedYear);
            } catch (UnknownHostException un) {
                un.printStackTrace();
                res = Util.GETLIST_INTERNET_FAILURE;
            } catch (IOException io) {
                io.printStackTrace();
                res = Util.GETLIST_OKHTTP_FAILURE;
            }
            return Double.valueOf(res).longValue();
        }

        protected void onPostExecute(Long result) {
            // call notification system here
            Intent notificationsIntent = new Intent(mContext, Notifications.class);
            notificationsIntent.setAction(Util.TAG_TIMEUPDATE);
            mContext.sendBroadcast(notificationsIntent);
            // Also send time update to widget
            Intent widgetIntent = new Intent(mContext, MenuWidget.class);
            widgetIntent.setAction(Util.TAG_TIMEUPDATE);
            mContext.sendBroadcast(widgetIntent);
        }

    }
}