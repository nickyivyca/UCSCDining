package com.nickivy.ucscdining.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.nickivy.ucscdining.MealViewFragment;
import com.nickivy.ucscdining.R;
import com.nickivy.ucscdining.parser.MealDataFetcher;
import com.nickivy.ucscdining.parser.MenuParser;
import com.nickivy.ucscdining.util.Util;

import java.util.Calendar;


/**
 * Implementation of App Widget functionality.
 *
 * The widget itself displays the name of the college, name of the meal and the meal list, along
 * with buttons to shuffle between the meals and the colleges.
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
    TAG_TIMEUPDATE="time_update";

    public static int currentCollege = -1,
    currentMeal = -2;

    /**
     * This variable is set going into the AsyncTask by widget buttons, if the day being loaded is
     * different than the current date being passed in
     */
    public static int dayIncrement;
    // Direction for telling which meal to skip over on brunch days
    public static boolean directionRight = true;

    private static RetrieveMenuInWidgetTask mTask= null;


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
            Log.v("ucscdining", "alarm already set");
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

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        int today[] = Util.getToday();
        if (currentCollege < 0) {
            currentCollege = 0;
        }
        /*
         * This here controls the AsyncTask so only one runs at once. If the user presses the
         * buttons too fast they can get things overlapping on each other and it's not good.
         */
        if (mTask == null) {
            mTask = new RetrieveMenuInWidgetTask(context, appWidgetManager, appWidgetId, today[0],
                    today[1], today[2], currentCollege);
            mTask.execute();
        }
        // Actual setting of widget data is accomplished in the postexecute of the asynctask
    }

    private static class RetrieveMenuInWidgetTask extends AsyncTask<Void, Void, Long> {

        private Context mContext;
        private AppWidgetManager mAppWidgetManager;
        private int mAppWidgetId, mMonth, mDay, mYear, mCollege;

        public RetrieveMenuInWidgetTask(Context context, AppWidgetManager appWidgetManager,
                int appWidgetId, int month, int day, int year, int college) {
            mContext = context;
            mAppWidgetManager = appWidgetManager;
            mAppWidgetId = appWidgetId;
            mCollege = college;
            if (dayIncrement != 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MONTH, month - 1);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.YEAR, year);
                calendar.add(Calendar.DATE, dayIncrement);
                mMonth = calendar.get(Calendar.MONTH) + 1;
                mDay = calendar.get(Calendar.DAY_OF_MONTH);
                mYear = calendar.get(Calendar.YEAR);
            } else {
                mMonth = month;
                mDay = day;
                mYear = year;
            }
        }

        @Override
        protected Long doInBackground(Void... voids) {
            int res = MealDataFetcher.fetchData(mContext, mMonth, mDay, mYear);
            return Double.valueOf(res).longValue();
        }

        @Override
        protected void onPreExecute() {
            RemoteViews widget = new RemoteViews(mContext.getPackageName(), R.layout.menu_widget);
            widget.setViewVisibility(R.id.widget_progresscircle, View.VISIBLE);
            mAppWidgetManager.updateAppWidget(mAppWidgetId, widget);
        }

        protected void onPostExecute(Long result) {
            RemoteViews widget = new RemoteViews(mContext.getPackageName(), R.layout.menu_widget);
            widget.setViewVisibility(R.id.widget_progresscircle, View.INVISIBLE);

            if (!result.equals(Double.valueOf(Util.GETLIST_SUCCESS).longValue())) {
                Log.v("ucscdining", "No internet connection or database error not updating widget");
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
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            // If currentMeal uninitialized, initialize it here (this way we can check for brunch)
            if (currentMeal < 0) {
                currentMeal = Util.getCurrentMeal(currentCollege);
            }

            // Set adapter for listview
            widget.setRemoteAdapter(R.id.widget_list, svcIntent);
            mAppWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.widget_list);

            widget.setTextViewText(R.id.widget_mealname, mMonth + "/" + mDay + " " +
                    Util.meals[currentMeal]);
            // If so display 'All Closed' and nothing else
            if (allClosed) {
                widget.setTextViewText(R.id.widget_collegename, "All Closed");
                widget.setTextColor(R.id.widget_collegename, Color.BLACK);
            } else {
                if (currentMeal == Util.BREAKFAST &&
                        MenuParser.fullMenuObj.get(currentCollege).getBreakfast().size() > 0) {
                    if (MenuParser.fullMenuObj.get(currentCollege).getBreakfast().get(0)
                            .equals(Util.brunchMessage)) {
                        currentMeal = directionRight ? Util.LUNCH : Util.DINNER;
                    }
                }

                // Set view text of college and current meal
                widget.setTextViewText(R.id.widget_collegename, Util.collegeList[mCollege]);
                if (MenuParser.fullMenuObj.get(mCollege).getIsCollegeNight()) {
                    widget.setTextColor(R.id.widget_collegename, Color.BLUE);
                } else if (MenuParser.fullMenuObj.get(mCollege).getIsFarmFriday() ||
                        MenuParser.fullMenuObj.get(mCollege).getIsHealthyMonday()) {
                    // 'Green Apple'
                    widget.setTextColor(R.id.widget_collegename, Color.rgb(0x4C, 0xC5, 0x52));
                } else if (!MenuParser.fullMenuObj.get(mCollege).getIsOpen()) {
                    widget.setTextColor(R.id.widget_collegename, Color.LTGRAY);
                } else {
                    widget.setTextColor(R.id.widget_collegename, Color.BLACK);
                }
            }

            // Set intents on all four buttons
            Intent intent = new Intent(mContext, MenuWidget.class);
            intent.setAction(CLICKTAG_COLLEGELEFT);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            widget.setOnClickPendingIntent(R.id.widget_college_leftbutton,
                    pendingIntent);

            intent.setAction(CLICKTAG_COLLEGERIGHT);
            pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            widget.setOnClickPendingIntent(R.id.widget_college_rightbutton,
                    pendingIntent);

            intent.setAction(CLICKTAG_MEALLEFT);
            pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            widget.setOnClickPendingIntent(R.id.widget_mealname_leftbutton,
                    pendingIntent);

            intent.setAction(CLICKTAG_MEALRIGHT);
            pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            widget.setOnClickPendingIntent(R.id.widget_mealname_rightbutton,
                    pendingIntent);

            mAppWidgetManager.updateAppWidget(mAppWidgetId, widget);

            // Set mTask to null so that now we know it can be run again
            mTask = null;
        }
    }



    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        if (CLICKTAG_COLLEGELEFT.equals(intent.getAction())) {
            currentCollege--;
            if (currentCollege <= -1) {
                currentCollege = 4;
            }
            /*
             * If college is not open, cycle until find one that is. Only try five times
             *
             * All 5 closed is handled separately
             */
            if (!MenuParser.fullMenuObj.get(currentCollege).getIsOpen()) {
                for (int i = 0; i < 5; i++) {
                    currentCollege--;
                    if (currentCollege == -1) {
                        currentCollege = 4;
                    }
                    if (MenuParser.fullMenuObj.get(currentCollege).getIsOpen()) {
                        break;
                    }
                }
            }
        }
        if (CLICKTAG_COLLEGERIGHT.equals(intent.getAction())) {
            currentCollege++;
            if (currentCollege >= 5) {
                currentCollege = 0;
            }
            /*
             * If college is not open, cycle until find one that is. Only try five times
             *
             * All 5 closed is handled separately
             */
            if (!MenuParser.fullMenuObj.get(currentCollege).getIsOpen()) {
                for (int i = 0; i < 5; i++) {
                    currentCollege++;
                    if (currentCollege == 5) {
                        currentCollege = 0;
                    }
                    if (MenuParser.fullMenuObj.get(currentCollege).getIsOpen()) {
                        break;
                    }
                }
            }
        }
        if (CLICKTAG_MEALLEFT.equals(intent.getAction())) {
            currentMeal--;
            if (currentMeal == -1) {
                currentMeal = 2;
                // If at breakfast and left button pressed, go to previous day
                dayIncrement--;
                directionRight = false;
            }
            // In case when button press leads to a weekend breakfast, go to previous day
            if (currentMeal == Util.BREAKFAST &&
                    MenuParser.fullMenuObj.get(currentCollege).getBreakfast().size() > 0) {
                if (MenuParser.fullMenuObj.get(currentCollege).getBreakfast().get(0)
                        .equals(Util.brunchMessage)) {
                    dayIncrement--;
                    currentMeal = Util.DINNER;
                }
            }
        }
        if (CLICKTAG_MEALRIGHT.equals(intent.getAction())) {
            currentMeal++;
            if (currentMeal == 3) {
                currentMeal = 0;
                // If at dinner, and right button pressed, advance to next day
                dayIncrement++;
                directionRight = true;
            }
        }
        if (TAG_TIMEUPDATE.equals(intent.getAction())) {
            currentMeal = Util.getCurrentMeal(currentCollege);
            dayIncrement = 0;
        }
        // Trigger update
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
                MenuWidget.class.getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
        super.onReceive(context, intent);
    }
}


