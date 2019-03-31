package com.nickivy.slugfood.widget;

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

import com.nickivy.slugfood.BackgroundLoader;
import com.nickivy.slugfood.MainActivity;
import com.nickivy.slugfood.R;
import com.nickivy.slugfood.parser.MealDataFetcher;
import com.nickivy.slugfood.parser.MenuParser;
import com.nickivy.slugfood.util.Util;

import java.io.IOException;
import java.net.UnknownHostException;
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
 * @author Nicky Ivy parkedraccoon@gmail.com
 */
public class MenuWidget extends AppWidgetProvider {

    public static final String CLICKTAG_COLLEGELEFT = "COLLEGE_LEFT",
    CLICKTAG_COLLEGERIGHT = "COLLEGE_RIGHT",
    CLICKTAG_MEALLEFT = "MEAL_LEFT",
    CLICKTAG_MEALRIGHT = "MEAL_RIGHT",
    TAG_WIDGETID = "widget_id",
    KEY_COLLEGES = "key_colleges",
    KEY_MEALS = "key_meals",
    KEY_MONTHS = "key_months",
    KEY_DAYS = "key_days",
    KEY_YEARS = "key_years",
    KEY_ID = "key_id",
    KEY_BACKUP_COLLEGE = "key_backup_college";

    public static ArrayList<WidgetData> widgetData = new ArrayList<WidgetData>();

    public static boolean timeUpdate;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        timeUpdate = false;
    }

    @Override
    public void onDisabled(Context context) {
        Intent intent = new Intent(context, BackgroundLoader.class);
        intent.setAction(Util.TAG_WIDGETGONE);
        context.sendBroadcast(intent);

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
        Intent intent = new Intent(context, BackgroundLoader.class);
        intent.setAction(Util.TAG_WIDGETENABLED);
        context.sendBroadcast(intent);
        WidgetData thisWidgetData = getWidgetDataById(appWidgetId);

        if (thisWidgetData == null) {
            // Not doing full reinitialization here, this is how we make the initial data when added
            SharedPreferences prefs = context.getSharedPreferences(Util.WIDGETSTATE_PREFS, 0);
            String iddata = prefs.getString(KEY_ID, "");
            // If appwidgetID exists in savedwidgetID, use backed up data
            if (iddata.contains("" + appWidgetId)) {
                // We are either at a device restart or a app reinstall, data should be in saved
                reinitializeWidgetData(context);
                thisWidgetData = getWidgetDataById(appWidgetId);
            } else {
                // Else, initialize new widget
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences
                        (context.getApplicationContext());
                thisWidgetData = new WidgetData(appWidgetId, Integer.parseInt(
                        settings.getString("default_college", "0")), context);
                widgetData.add(thisWidgetData);
            }
            if (!thisWidgetData.getTaskRunning()) {
                thisWidgetData.setTaskRunning(true);
                new RetrieveMenuInWidgetTask(context, appWidgetManager, thisWidgetData).execute();
            }
        } else {
            if (!thisWidgetData.getTaskRunning()) {
                thisWidgetData.setTaskRunning(true);
                new RetrieveMenuInWidgetTask(context, appWidgetManager, thisWidgetData).execute();
            }
        }
        // Actual setting of widget data is accomplished in the postexecute of the asynctask
    }

    private static class RetrieveMenuInWidgetTask extends AsyncTask<Void, Void, Long> {
        private Context mContext;
        private AppWidgetManager mAppWidgetManager;
        private WidgetData mData;
        private boolean mTimeUpdate;

        public RetrieveMenuInWidgetTask(Context context, AppWidgetManager appWidgetManager,
                WidgetData data) {
            mContext = context;
            mData = data;
            mAppWidgetManager = appWidgetManager;
            mTimeUpdate = timeUpdate;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            int res = Util.GETLIST_SUCCESS;

            try {
                MealDataFetcher.fetchData(mContext, mData.getMonth(), mData.getDay(),
                        mData.getYear());
            } catch (UnknownHostException un) {
                un.printStackTrace();
                res = Util.GETLIST_INTERNET_FAILURE;
            } catch (IOException io) {
                io.printStackTrace();
                res = Util.GETLIST_OKHTTP_FAILURE;
            }
            return Double.valueOf(res).longValue();
        }

        @Override
        protected void onPreExecute() {
            mData.getViews().setViewVisibility(R.id.widget_progresscircle, View.VISIBLE);
            mAppWidgetManager.updateAppWidget(mData.getWidgetId(), mData.getViews());
        }

        protected void onPostExecute(Long result) {
            mData.getViews().setViewVisibility(R.id.widget_progresscircle, View.INVISIBLE);
            mData.setTaskRunning(false);

            if (!result.equals(Double.valueOf(Util.GETLIST_SUCCESS).longValue())) {
                Log.w(Util.LOGTAG, "No internet connection or database error, not updating widget");
                // We still need to update the widget to remove the spinny
                mAppWidgetManager.updateAppWidget(mData.getWidgetId(), mData.getViews());
                timeUpdate = false;
                return;
            }
            // Check if all dining halls are closed
            boolean allClosed = true;
            for (int i = 0; i < 5; i++) {
                allClosed = !Util.fullMenuObj.get(i).getIsOpen();
                if (!allClosed) {
                    break;
                }
            }
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences
                    (mContext.getApplicationContext());



            // Apply theme to widget
            if (settings.getBoolean("dark_theme",false)) {
                mData.getViews().setInt(R.id.widget_collegename, "setBackgroundColor", mContext.getResources().getColor(R.color.dark_primary));
                mData.getViews().setTextColor(R.id.widget_collegename, mContext.getResources().getColor(R.color.dark_primary_text));
                mData.getViews().setInt(R.id.widget_mealname, "setBackgroundColor", mContext.getResources().getColor(R.color.dark_primary));
                mData.getViews().setTextColor(R.id.widget_mealname, mContext.getResources().getColor(R.color.dark_primary_text));
                mData.getViews().setInt(R.id.widget_list, "setBackgroundColor", mContext.getResources().getColor(R.color.dark_primary));

                mData.getViews().setViewVisibility(R.id.widget_dark_college_leftbutton, View.VISIBLE);
                mData.getViews().setViewVisibility(R.id.widget_college_leftbutton, View.INVISIBLE);

                mData.getViews().setViewVisibility(R.id.widget_dark_college_rightbutton, View.VISIBLE);
                mData.getViews().setViewVisibility(R.id.widget_college_rightbutton, View.INVISIBLE);

                mData.getViews().setViewVisibility(R.id.widget_dark_mealname_leftbutton, View.VISIBLE);
                mData.getViews().setViewVisibility(R.id.widget_mealname_leftbutton, View.INVISIBLE);

                mData.getViews().setViewVisibility(R.id.widget_dark_mealname_rightbutton, View.VISIBLE);
                mData.getViews().setViewVisibility(R.id.widget_mealname_rightbutton, View.INVISIBLE);
            } else {
                mData.getViews().setInt(R.id.widget_collegename, "setBackgroundColor", mContext.getResources().getColor(R.color.primary));
                mData.getViews().setTextColor(R.id.widget_collegename, mContext.getResources().getColor(R.color.primary_text));
                mData.getViews().setInt(R.id.widget_mealname, "setBackgroundColor", mContext.getResources().getColor(R.color.primary));
                mData.getViews().setTextColor(R.id.widget_mealname, mContext.getResources().getColor(R.color.primary_text));
                mData.getViews().setInt(R.id.widget_list, "setBackgroundColor", Color.WHITE);

                mData.getViews().setViewVisibility(R.id.widget_dark_college_leftbutton, View.INVISIBLE);
                mData.getViews().setViewVisibility(R.id.widget_college_leftbutton, View.VISIBLE);

                mData.getViews().setViewVisibility(R.id.widget_dark_college_rightbutton, View.INVISIBLE);
                mData.getViews().setViewVisibility(R.id.widget_college_rightbutton, View.VISIBLE);

                mData.getViews().setViewVisibility(R.id.widget_dark_mealname_leftbutton, View.INVISIBLE);
                mData.getViews().setViewVisibility(R.id.widget_mealname_leftbutton, View.VISIBLE);

                mData.getViews().setViewVisibility(R.id.widget_dark_mealname_rightbutton, View.INVISIBLE);
                mData.getViews().setViewVisibility(R.id.widget_mealname_rightbutton, View.VISIBLE);
            }

            // If so display 'All Closed' and nothing else, but still set date
            mData.getViews().setTextViewText(R.id.widget_mealname, mData.getMonth() + "/" +
                    mData.getDay() + " " + Util.meals[mData.getMeal()]);
            if (allClosed) {
                mData.getViews().setTextViewText(R.id.widget_collegename, "All Closed");
                mData.getViews().setTextColor(R.id.widget_collegename, mContext.getResources()
                        .getColor(R.color.primary_text));
            } else {
                // Check if brunch message present - if so skip past it
                if (mData.getMeal() == Util.BREAKFAST &&
                        Util.fullMenuObj.get(mData.getCollege()).getBreakfast().size() > 0) {
                    if (Util.fullMenuObj.get(mData.getCollege()).getBreakfast().get(0)
                            .getItemName().equals(Util.brunchMessage)) {
                        mData.setMeal(mData.getDirectionRight() ? Util.LUNCH : Util.DINNER);
                        mData.getViews().setTextViewText(R.id.widget_mealname, mData.getMonth()
                                + "/" + mData.getDay() + " " + Util.meals[mData.getMeal()]);
                    }
                }

                /*
                 * Backup college: only use backup college on timeupdate switches, switch back to
                 * main college afterwards
                 */
                if (mTimeUpdate && mData.getBackupCollege() != Util.NO_BACKUP_COLLEGE &&
                        Util.fullMenuObj.get(mData.getBackupCollege()).getIsOpen()) {
                    mData.setCollege(mData.getBackupCollege());
                }

                int secondcollege = Integer.parseInt(settings.getString("default_college_2nd", "0"));

                // Setting for if dining hall is closed, or also use alternate colleges for late night
                if (mTimeUpdate && (!Util.fullMenuObj.get(mData.getCollege()).getIsOpen() ||
                        (Util.fullMenuObj.get(mData.getCollege()).getLateNight().size() == 0 &&
                                Util.fullMenuObj.get(secondcollege).getLateNight().size() > 0 &&
                                Util.getCurrentMeal(mData.getCollege()) == Util.LATENIGHT))) {
                    mData.setBackupCollege();
                    mData.setCollege(secondcollege);
                }

                // Set view text of college and current meal

                mData.getViews().setTextViewText(R.id.widget_collegename,
                        Util.collegeList[mData.getCollege()]);
                if (Util.fullMenuObj.get(mData.getCollege()).getIsCollegeNight()) {
                    mData.getViews().setTextColor(R.id.widget_collegename, Color.BLUE);
                } else if (Util.fullMenuObj.get(mData.getCollege()).getIsFarmFriday() ||
                        Util.fullMenuObj.get(mData.getCollege()).getIsHealthyMonday()) {
                    mData.getViews().setTextColor(R.id.widget_collegename,
                            mContext.getResources().getColor(R.color.healthy));
                } else if (!Util.fullMenuObj.get(mData.getCollege()).getIsOpen()) {
                    mData.getViews().setTextColor(R.id.widget_collegename, Color.LTGRAY);
                }
            }

            // Set intents on all four buttons
            Intent intent = new Intent(mContext, MenuWidget.class);
            // Store the widget tag in it for use in onReceive
            intent.putExtra(TAG_WIDGETID, mData.getWidgetId());

            intent.setAction(CLICKTAG_COLLEGELEFT);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, mData.getWidgetId(),
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mData.getViews().setOnClickPendingIntent(R.id.widget_college_leftbutton,
                    pendingIntent);
            mData.getViews().setOnClickPendingIntent(R.id.widget_dark_college_leftbutton,
                    pendingIntent);

            intent.setAction(CLICKTAG_COLLEGERIGHT);
            pendingIntent = PendingIntent.getBroadcast(mContext, mData.getWidgetId(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mData.getViews().setOnClickPendingIntent(R.id.widget_college_rightbutton,
                    pendingIntent);
            mData.getViews().setOnClickPendingIntent(R.id.widget_dark_college_rightbutton,
                    pendingIntent);

            intent.setAction(CLICKTAG_MEALLEFT);
            pendingIntent = PendingIntent.getBroadcast(mContext, mData.getWidgetId(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mData.getViews().setOnClickPendingIntent(R.id.widget_mealname_leftbutton,
                    pendingIntent);
            mData.getViews().setOnClickPendingIntent(R.id.widget_dark_mealname_leftbutton,
                    pendingIntent);

            intent.setAction(CLICKTAG_MEALRIGHT);
            pendingIntent = PendingIntent.getBroadcast(mContext, mData.getWidgetId(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mData.getViews().setOnClickPendingIntent(R.id.widget_mealname_rightbutton,
                    pendingIntent);
            mData.getViews().setOnClickPendingIntent(R.id.widget_dark_mealname_rightbutton,
                    pendingIntent);

            // Set intent on other bits for launching main app
            Intent launchAppIntent = new Intent(mContext, MainActivity.class);
            launchAppIntent.putExtra(Util.TAG_MONTH, mData.getMonth());
            launchAppIntent.putExtra(Util.TAG_DAY, mData.getDay());
            launchAppIntent.putExtra(Util.TAG_YEAR, mData.getYear());
            launchAppIntent.putExtra(Util.TAG_COLLEGE, mData.getCollege());
            launchAppIntent.putExtra(Util.TAG_MEAL, mData.getMeal());
            launchAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingLaunch = PendingIntent.getActivity(mContext, mData.getWidgetId(),
                    launchAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mData.getViews().setOnClickPendingIntent(R.id.widget_college, pendingLaunch);
            mData.getViews().setOnClickPendingIntent(R.id.widget_collegename, pendingLaunch);
            mData.getViews().setOnClickPendingIntent(R.id.widget_mealname, pendingLaunch);
            mData.getViews().setPendingIntentTemplate(R.id.widget_list, pendingLaunch);

            Intent svcIntent = new Intent(mContext, WidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mData.getWidgetId());
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            // Set adapter for listview
            mData.getViews().setRemoteAdapter(R.id.widget_list, svcIntent);
            mAppWidgetManager.notifyAppWidgetViewDataChanged(mData.getWidgetId(),
                    R.id.widget_list);

            mAppWidgetManager.updateAppWidget(mData.getWidgetId(), mData.getViews());
            saveData(mContext);
        }
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        /*
         * If intent contains widget ID data, make sure WidgetData exists in memory, otherwise
         * load it from saved data
         *
         * Otherwise, check if intent is an alarm time update intent and run that.
         */
        if (!(intent.getIntExtra(TAG_WIDGETID, -1) == -1)) {
            WidgetData data = getWidgetDataById(intent.getIntExtra(TAG_WIDGETID, -1));
            if (data == null) {
                // Not doing full reinitialization here, this is how we make the initial data when added
                SharedPreferences prefs = context.getSharedPreferences(Util.WIDGETSTATE_PREFS, 0);
                String iddata = prefs.getString(KEY_ID, "");
                // If appwidgetID exists in savedwidgetID, use backed up data
                if (iddata.contains("" + (intent.getIntExtra(TAG_WIDGETID, -1)))) {
                    // We are either at a device restart or a app reinstall, data should be in saved
                    reinitializeWidgetData(context);
                } else {
                    // Otherwise, user has cleared data of app
                    ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
                            MenuWidget.class.getName());
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            }
            int thisDataIndex = widgetData.indexOf(getWidgetDataById(
                    intent.getIntExtra(TAG_WIDGETID, -1)));
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
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            updateAppWidget(context, appWidgetManager, widgetData.get(thisDataIndex).getWidgetId());
        } else {
            if (Util.TAG_TIMEUPDATE.equals(intent.getAction())) {
                if (widgetData.size() == 0) {
                    reinitializeWidgetData(context);
                }
                for (int i = 0; i < widgetData.size(); i++) {
                    widgetData.get(i).setToToday();
                    widgetData.get(i).setMeal(Util.getCurrentMeal
                            (widgetData.get(i).getCollege()));
                }
                timeUpdate = true;
            }
            // Trigger update of all
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
        String savedColleges = "",
                savedIds = "",
                savedMeals = "",
                savedMonths = "",
                savedDays = "",
                savedYears = "",
                savedBackupColleges = "";

        for (WidgetData data : widgetData) {
            savedColleges = savedColleges + data.getCollege();
            savedMeals = savedMeals + data.getMeal();
            savedBackupColleges = savedBackupColleges + data.getBackupCollege();
            // savedIds, date data comma separated
            if (savedMonths.length() > 0) {
                savedMonths = savedMonths + "," + data.getMonth();
                savedDays = savedDays + "," + data.getDay();
                savedYears = savedYears + "," + data.getYear();
                savedIds = savedIds + "," + data.getWidgetId();
            } else {
                savedMonths = "" + data.getMonth();
                savedDays = "" + data.getDay();
                savedYears = "" + data.getYear();
                savedIds = "" + data.getWidgetId();
            }
        }
        SharedPreferences settings = context.getSharedPreferences(Util.WIDGETSTATE_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_COLLEGES, savedColleges);
        editor.putString(KEY_MEALS, savedMeals);
        editor.putString(KEY_MONTHS, savedMonths);
        editor.putString(KEY_DAYS, savedDays);
        editor.putString(KEY_YEARS, savedYears);
        editor.putString(KEY_ID, savedIds);
        editor.putString(KEY_BACKUP_COLLEGE, savedBackupColleges);
        editor.commit();
    }

    private static int getSavedCollegeNumber(Context context, int index) {
        SharedPreferences settings = context.getSharedPreferences(Util.WIDGETSTATE_PREFS, 0);
        String collegeData = settings.getString(KEY_COLLEGES, "");
        return collegeData.charAt(index) - '0';
    }

    private static int getSavedMeal(Context context, int index) {
        SharedPreferences settings = context.getSharedPreferences(Util.WIDGETSTATE_PREFS, 0);
        String mealData = settings.getString(KEY_MEALS, "");
        return mealData.charAt(index) - '0';
    }

    private static int getSavedBackupCollege(Context context, int index) {
        SharedPreferences settings = context.getSharedPreferences(Util.WIDGETSTATE_PREFS, 0);
        String mealData = settings.getString(KEY_BACKUP_COLLEGE, "");
        return mealData.charAt(index) - '0';
    }

    private static int[] getSavedDate(Context context, int index) {
        SharedPreferences settings = context.getSharedPreferences(Util.WIDGETSTATE_PREFS, 0);
        String monthData = settings.getString(KEY_MONTHS, ""),
                dayData = settings.getString(KEY_DAYS, ""),
                yearData = settings.getString(KEY_YEARS, "");
        // If only one data piece in string
        if (!monthData.contains(",")) {
                return new int[]{Integer.parseInt(monthData),
                Integer.parseInt(dayData), Integer.parseInt(yearData)};
        } else {
            try {
                String resMonthData[] = monthData.split(","),
                        resDayData[] = dayData.split(","),
                        resYearData[] = yearData.split(",");
                return new int[]{Integer.parseInt(resMonthData[index]),
                        Integer.parseInt(resDayData[index]), Integer.parseInt(resYearData[index])};
            } catch (NullPointerException e) {
                return Util.getToday();
            } catch (NumberFormatException e) {
                return Util.getToday();
            }
        }
    }

    private static int getSavedWidgetID(Context context, int index) {
        SharedPreferences settings = context.getSharedPreferences(Util.WIDGETSTATE_PREFS, 0);
        String idData = settings.getString(KEY_ID, "");
        // If only one data piece in string
        if (!idData.contains(",")) {
            if (!idData.isEmpty()) {
                return Integer.parseInt(idData);
            } else {
                return -1;
            }
        } else {
            try {
                String[] resIDData = idData.split(",");
                return Integer.parseInt(resIDData[index]);
            } catch (NullPointerException e) {
                // We're screwed if we reach here...
                return -1;
            } catch (java.lang.NumberFormatException e) {
                return -1;
            }
        }
    }

    private static void reinitializeWidgetData(Context context) {
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
                MenuWidget.class.getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        // Recreate all widgetData into the array, from saved data
        for (int i = 0; i < appWidgetIds.length; i++) {
            WidgetData data = new WidgetData(getSavedWidgetID(context, i),
                    getSavedCollegeNumber(context, i), getSavedDate(context, i),
                    getSavedMeal(context, i), context, getSavedBackupCollege(context, i));
            widgetData.add(data);
        }
        saveData(context);
    }
}


