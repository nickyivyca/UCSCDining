package com.nickivy.ucscdining.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.nickivy.ucscdining.MainActivity;
import com.nickivy.ucscdining.R;
import com.nickivy.ucscdining.parser.MealDataFetcher;
import com.nickivy.ucscdining.parser.MenuParser;
import com.nickivy.ucscdining.util.Util;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * Implementation of App Widget functionality.
 *
 * <p>The widget itself displays the name of the college, name of the meal and the meal list, along
 * with buttons to shuffle between the meals and the colleges.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author Nick Ivy parkedraccoon@gmail.com
 */
public class MenuWidget extends AppWidgetProvider {

    private PendingIntent breakfastIntent = null,
    lunchIntent= null,
    dinnerIntent = null;

    private static final String CLICKTAG_COLLEGELEFT = "COLLEGE_LEFT",
    CLICKTAG_COLLEGERIGHT = "COLLEGE_RIGHT",
    CLICKTAG_MEALLEFT = "MEAL_LEFT",
    CLICKTAG_MEALRIGHT = "MEAL_RIGHT",
    TAG_TIMEUPDATE="time_update",
    TAG_WIDGETID="widget_id",
    TAG_UPDATEALL="update_all",
    KEY_COLLEGES="key_colleges",
    KEY_MEALS="key_meals";

    //public static int currentCollege = -1,
    //currentMeal = -2;

    //private static RemoteViews views;

    public static ArrayList<WidgetData> widgetData = new ArrayList<WidgetData>();

    // Direction for telling which meal to skip over on brunch days
    public static boolean directionRight = true;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        setAlarm(context);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        setAlarm(context);
        super.onEnabled(context);
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
        final Intent timeIntent = new Intent(context, MenuWidget.class);
        timeIntent.setAction(TAG_TIMEUPDATE);

        // If getBroadcast with the NO_CREATE flag returns null for all of these all alarms are set
        // Don't reset them or else the alarm manager will run off with them
        boolean alarmEnabled = (PendingIntent.getBroadcast(context, Util.BREAKFAST_SWITCH_TIME,
                timeIntent, PendingIntent.FLAG_NO_CREATE) != null) &&
                (PendingIntent.getBroadcast(context, Util.LUNCH_SWITCH_TIME,
                        timeIntent, PendingIntent.FLAG_NO_CREATE) != null) &&
                (PendingIntent.getBroadcast(context, Util.DINNER_SWITCH_TIME,
                        timeIntent, PendingIntent.FLAG_NO_CREATE) != null);
        if (alarmEnabled) {
            return;
        }

        final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();

        breakfastIntent = PendingIntent.getBroadcast(context, Util.BREAKFAST_SWITCH_TIME,
                timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        calendar.set(Calendar.HOUR_OF_DAY, Util.BREAKFAST_SWITCH_TIME);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        m.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, breakfastIntent);

        lunchIntent = PendingIntent.getBroadcast(context, Util.LUNCH_SWITCH_TIME,
                timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        calendar.set(Calendar.HOUR_OF_DAY, Util.LUNCH_SWITCH_TIME);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        m.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, lunchIntent);

        dinnerIntent = PendingIntent.getBroadcast(context, Util.DINNER_SWITCH_TIME,
                timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        calendar.set(Calendar.HOUR_OF_DAY, Util.DINNER_SWITCH_TIME);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        m.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, dinnerIntent);
    }

    @Override
    public void onDisabled(Context context) {
        final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        m.cancel(breakfastIntent);
        m.cancel(lunchIntent);
        m.cancel(dinnerIntent);
        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int i : appWidgetIds) {
            widgetData.remove(getWidgetDataById(i));
        }
        saveData(context);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        WidgetData thisWidgetData = getWidgetDataById(appWidgetId);
        if (thisWidgetData == null) {
            thisWidgetData = new WidgetData(appWidgetId, getSavedCollegeData(context,
                    widgetData.size()), context);
            widgetData.add(thisWidgetData);
            //saveData(context);
        }
        new RetrieveMenuInWidgetTask(context, appWidgetManager, thisWidgetData).execute();
        // Actual setting of widget data is accomplished in the postexecute of the asynctask
    }

    private static class RetrieveMenuInWidgetTask extends AsyncTask<Void, Void, Long> {

        private Context mContext;
        private AppWidgetManager mAppWidgetManager;
        private WidgetData mData;

        public RetrieveMenuInWidgetTask(Context context, AppWidgetManager appWidgetManager,
                WidgetData data) {
            mContext = context;
            mData = data;
            mAppWidgetManager = appWidgetManager;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            int res = MealDataFetcher.fetchData(mContext, mData.getMonth(),
                    mData.getDay(), mData.getYear());
            return Double.valueOf(res).longValue();
        }

        @Override
        protected void onPreExecute() {
            /*if (views == null) {
                views = new RemoteViews(mContext.getPackageName(), R.layout.menu_widget);
            }*/
            mData.getViews().setViewVisibility(R.id.widget_progresscircle, View.VISIBLE);
            mAppWidgetManager.updateAppWidget(mData.getWidgetId(), mData.getViews());
        }

        protected void onPostExecute(Long result) {
            /*if (views == null) {
                views = new RemoteViews(mContext.getPackageName(), R.layout.menu_widget);
            }*/
            mData.getViews().setViewVisibility(R.id.widget_progresscircle, View.INVISIBLE);

            if (!result.equals(Double.valueOf(Util.GETLIST_SUCCESS).longValue())) {
                Log.v(Util.LOGTAG, "No internet connection or database error not updating widget");
                return;
            }
            // Check if all dining halls are closed
            boolean allClosed = true;
            for (int i = 0; i < 5; i++) {
                allClosed = !MenuParser.fullMenuObj.get(i).getIsOpen();
                if (!allClosed) {
                    break;
                }
            }
            Intent svcIntent = new Intent(mContext, WidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mData.getWidgetId());
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            // Set adapter for listview
            mData.getViews().setRemoteAdapter(R.id.widget_list, svcIntent);
            mAppWidgetManager.notifyAppWidgetViewDataChanged(mData.getWidgetId(),
                    R.id.widget_list);

            // If so display 'All Closed' and nothing else
            if (allClosed) {
                mData.getViews().setTextViewText(R.id.widget_collegename, "All Closed");
                mData.getViews().setTextColor(R.id.widget_collegename, Color.BLACK);
            } else {
                if (mData.getMeal() == Util.BREAKFAST &&
                        MenuParser.fullMenuObj.get(mData.getCollege()).getBreakfast().size() > 0) {
                    if (MenuParser.fullMenuObj.get(mData.getCollege()).getBreakfast().get(0)
                            .equals(Util.brunchMessage)) {
                        mData.setMeal(directionRight ? Util.LUNCH : Util.DINNER);
                    }
                }

                // Set view text of college and current meal
                mData.getViews().setTextViewText(R.id.widget_collegename,
                        Util.collegeList[mData.getCollege()]);
                //Log.v(Util.LOGTAG, "college name set");
                if (MenuParser.fullMenuObj.get(mData.getCollege()).getIsCollegeNight()) {
                    mData.getViews().setTextColor(R.id.widget_collegename, Color.BLUE);
                } else if (MenuParser.fullMenuObj.get(mData.getCollege()).getIsFarmFriday() ||
                        MenuParser.fullMenuObj.get(mData.getCollege()).getIsHealthyMonday()) {
                    // 'Green Apple'
                    mData.getViews().setTextColor(R.id.widget_collegename, Color.rgb(0x4C, 0xC5, 0x52));
                } else if (!MenuParser.fullMenuObj.get(mData.getCollege()).getIsOpen()) {
                    mData.getViews().setTextColor(R.id.widget_collegename, Color.LTGRAY);
                } else {
                    mData.getViews().setTextColor(R.id.widget_collegename, Color.BLACK);
                }
                mData.getViews().setTextViewText(R.id.widget_mealname, mData.getMonth() + "/" + mData.getDay()
                        + " " + Util.meals[mData.getMeal()]);
            }

            // Set intents on all four buttons
            Intent intent = new Intent(mContext, MenuWidget.class);
            // Store the widget tag in it for use in onReceive
            intent.putExtra(TAG_WIDGETID, mData.getWidgetId());
            /*
             * Arrow button presses do not need to update all the widgets, just their own
             * (alarms update all)
             */
            intent.putExtra(TAG_UPDATEALL, false);

            intent.setAction(CLICKTAG_COLLEGELEFT);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, mData.getWidgetId(),
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mData.getViews().setOnClickPendingIntent(R.id.widget_college_leftbutton, pendingIntent);

            intent.setAction(CLICKTAG_COLLEGERIGHT);
            pendingIntent = PendingIntent.getBroadcast(mContext, mData.getWidgetId(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mData.getViews().setOnClickPendingIntent(R.id.widget_college_rightbutton, pendingIntent);

            intent.setAction(CLICKTAG_MEALLEFT);
            pendingIntent = PendingIntent.getBroadcast(mContext, mData.getWidgetId(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mData.getViews().setOnClickPendingIntent(R.id.widget_mealname_leftbutton, pendingIntent);

            intent.setAction(CLICKTAG_MEALRIGHT);
            pendingIntent = PendingIntent.getBroadcast(mContext, mData.getWidgetId(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mData.getViews().setOnClickPendingIntent(R.id.widget_mealname_rightbutton, pendingIntent);

            // Set intent on other bits for launching main app
            Intent launchAppIntent = new Intent(mContext, MainActivity.class);
            launchAppIntent.putExtra(Util.TAG_MONTH, mData.getMonth());
            launchAppIntent.putExtra(Util.TAG_DAY, mData.getDay());
            launchAppIntent.putExtra(Util.TAG_YEAR, mData.getYear());
            launchAppIntent.putExtra(Util.TAG_COLLEGE, mData.getCollege());
            launchAppIntent.putExtra(Util.TAG_MEAL, mData.getMeal());

            PendingIntent pendingLaunch = PendingIntent.getActivity(mContext, mData.getWidgetId(),
                    launchAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mData.getViews().setOnClickPendingIntent(R.id.widget_college, pendingLaunch);
            mData.getViews().setOnClickPendingIntent(R.id.widget_collegename, pendingLaunch);
            mData.getViews().setOnClickPendingIntent(R.id.widget_mealname, pendingLaunch);
            mData.getViews().setPendingIntentTemplate(R.id.widget_list, pendingLaunch);

            mAppWidgetManager.updateAppWidget(mData.getWidgetId(), mData.getViews());
            saveData(mContext);
        }
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        WidgetData data = getWidgetDataById(intent.getIntExtra(TAG_WIDGETID, -1));
        if (data == null) {
            // Trigger update for all
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
                    MenuWidget.class.getName());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
            onUpdate(context, appWidgetManager, appWidgetIds);
            super.onReceive(context, intent);
            return;
        }
        int thisDataIndex = widgetData.indexOf(data);
        if(!intent.getBooleanExtra(TAG_UPDATEALL, true)) {
            if (CLICKTAG_COLLEGELEFT.equals(intent.getAction())) {
                widgetData.get(thisDataIndex).decrementCollege();
            }
            if (CLICKTAG_COLLEGERIGHT.equals(intent.getAction())) {
                widgetData.get(thisDataIndex).incrementCollege();
            }
            if (CLICKTAG_MEALLEFT.equals(intent.getAction())) {
                widgetData.get(thisDataIndex).decrementMeal();
            }
            if (CLICKTAG_MEALRIGHT.equals(intent.getAction())) {
                widgetData.get(thisDataIndex).incrementMeal();
            }
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
                    MenuWidget.class.getName());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            updateAppWidget(context, appWidgetManager, widgetData.get(thisDataIndex).getWidgetId());
        } else {
            if (TAG_TIMEUPDATE.equals(intent.getAction())) {
                widgetData.get(thisDataIndex).setMeal(Util.getCurrentMeal
                        (widgetData.get(thisDataIndex).getCollege()));
                widgetData.get(thisDataIndex).setToToday();
            }
            // Trigger update
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
                    MenuWidget.class.getName());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
        super.onReceive(context, intent);
    }

    public static WidgetData getWidgetDataById(int widgetId) {
        if (widgetData == null) {
            return null;
        }
        for (WidgetData data : widgetData) {
            if (data.getWidgetId() == widgetId) {
                return data;
            }
        }
        return null;
    }

    /**
     * Saves the current colleges and meals of the widgets to sharedprefs. That way if things
     * fall out of memory, our widget doesn't completely reset.
     *
     * <p>Data is stored as a string containing each int in order as one character (since they can be
     * a max of 4)
     */
    private static void saveData(Context context) {
        String savedCollege = "";//,
                //savedMeals = "";
        for (WidgetData data : widgetData) {
            savedCollege = savedCollege + data.getCollege();
            //savedMeals = savedMeals + data.getMeal();
        }
        SharedPreferences settings = context.getSharedPreferences(Util.WIDGETSTATE_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_COLLEGES, savedCollege);
        //editor.putString(KEY_MEALS, savedMeals);
        editor.commit();
    }

    private static int getSavedCollegeData(Context context, int index) {
        SharedPreferences settings = context.getSharedPreferences(Util.WIDGETSTATE_PREFS, 0);
        String collegeData = settings.getString(KEY_COLLEGES, "");
        try {
            return collegeData.charAt(index) - '0';
        } catch (StringIndexOutOfBoundsException e) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences
                    (context.getApplicationContext());
            return Integer.parseInt(prefs.getString("default_college", "0"));
        }
    }
}


