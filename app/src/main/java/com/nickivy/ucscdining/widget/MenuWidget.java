package com.nickivy.ucscdining.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.nickivy.ucscdining.MainActivity;
import com.nickivy.ucscdining.MealViewFragment;
import com.nickivy.ucscdining.R;
import com.nickivy.ucscdining.parser.MealDataFetcher;
import com.nickivy.ucscdining.parser.MenuParser;

import java.util.Calendar;


/**
 * Implementation of App Widget functionality.
 */
public class MenuWidget extends AppWidgetProvider {

    public static final String EXTRA_WORD = "com.nickivy.ucscdining.widget.WORD";
    private PendingIntent service = null;

    public static final int DINNER_SWITCH_TIME = 22, // 3 PM
            LUNCH_SWITCH_TIME = 11, // 11 AM
            BREAKFAST_SWITCH_TIME = 0,// 12 AM
            BREAKFAST = 0,
            LUNCH = 1,
            DINNER = 2;

    private static final String CLICKTAG_COLLEGELEFT = "COLLEGE_LEFT",
    CLICKTAG_COLLEGERIGHT = "COLLEGE_RIGHT",
    CLICKTAG_MEALLEFT = "MEAL_LEFT",
    CLICKTAG_MEALRIGHT = "MEAL_RIGHT";

    public static int currentCollege = -1,
    currentMeal = -1;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v("ucscdining", "updating widget");

        final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        final Calendar TIME = Calendar.getInstance();
        TIME.set(Calendar.MINUTE, 0);
        TIME.set(Calendar.SECOND, 0);
        TIME.set(Calendar.MILLISECOND, 0);

        final Intent timeIntent = new Intent(context, WidgetService.class);

        if (service == null) {
            service = PendingIntent.getService(context, 0, timeIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
        }
        // update every hour
        m.setRepeating(AlarmManager.RTC, TIME.getTime().getTime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                service);

        // There may be multiple widgets active, so update all of them
        // Above update timing will work for all widgets
        int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        m.cancel(service);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        int today[] = MealViewFragment.getToday();
        if (currentCollege < 0) {
            currentCollege = 0;
        }
        if (currentMeal < 0) {
            currentMeal = getCurrentMeal();
        }
        new RetrieveMenuInWidgetTask(context, appWidgetManager, appWidgetId, today[0], today[1],
                today[2], currentCollege).execute();
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
            mMonth = month;
            mDay = day;
            mYear = year;
            mCollege = college;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            MealDataFetcher.fetchData(mContext, mMonth, mDay, mYear);
            return null;
        }

        @Override
        protected void onPreExecute() {
            //refreshStarted = true;
        }

        protected void onPostExecute(Long result) {
            Log.v("ucscdining", "postexecute");
            Intent svcIntent = new Intent(mContext, WidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget = new RemoteViews(mContext.getPackageName(), R.layout.menu_widget);
            widget.setRemoteAdapter(R.id.widget_list, svcIntent);

            widget.setTextViewText(R.id.widget_collegename, MenuParser.collegeList[mCollege]);
            widget.setTextViewText(R.id.widget_mealname,
                    MenuParser.meals[currentMeal]);

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
        }
    }

    public static int getCurrentMeal() {
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.HOUR_OF_DAY) >= DINNER_SWITCH_TIME) {
            return DINNER;
        }
        if (cal.get(Calendar.HOUR_OF_DAY) >= LUNCH_SWITCH_TIME) {
            return LUNCH;
        }
        if(cal.get(Calendar.HOUR_OF_DAY) >= BREAKFAST_SWITCH_TIME) {
            return BREAKFAST;
        }
        return -1;
    }

    public void onReceive(Context context, Intent intent) {
        Log.v("ucscdining", "onreceive" + intent.getAction());
        if (CLICKTAG_COLLEGELEFT.equals(intent.getAction())){
            currentCollege--;
            if (currentCollege == -1) {
                currentCollege = 4;
            }
        }
        if (CLICKTAG_COLLEGERIGHT.equals(intent.getAction())){
            currentCollege++;
            if (currentCollege == 5) {
                currentCollege = 0;
            }
        }
        if (CLICKTAG_MEALLEFT.equals(intent.getAction())){
            currentMeal--;
            if (currentMeal == -1) {
                currentMeal = 2;
            }
        }
        if (CLICKTAG_MEALRIGHT.equals(intent.getAction())){
            currentMeal++;
            if (currentMeal == 3) {
                currentMeal = 0;
            }
        }
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
                MenuWidget.class.getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        super.onReceive(context, intent);
    };
}


