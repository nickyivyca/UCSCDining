package com.nickivy.ucscdining.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
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


/**
 * Implementation of App Widget functionality.
 */
public class MenuWidget extends AppWidgetProvider {

    public static final String EXTRA_WORD = "com.nickivy.ucscdining.widget.WORD";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        int today[] = MealViewFragment.getToday();
        new RetrieveMenuInWidgetTask(context, appWidgetManager, appWidgetId, today[0], today[1],
                today[2]).execute();
//        Log.v("ucscdining", "retrieve menu in widget task run");
        Log.v("ucscdining", "updateappwidget completed");
    }

    private static class RetrieveMenuInWidgetTask extends AsyncTask<Void, Void, Long> {

        private Context mContext;
        private AppWidgetManager mAppWidgetManager;
        private int mAppWidgetId, mMonth, mDay, mYear;


        public RetrieveMenuInWidgetTask(Context context, AppWidgetManager appWidgetManager,
                int appWidgetId, int month, int day, int year) {
            mContext = context;
            mAppWidgetManager = appWidgetManager;
            mAppWidgetId = appWidgetId;
            mMonth = month;
            mDay = day;
            mYear = year;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            MealDataFetcher.fetchData(mContext, mMonth, mDay, mYear);
            Log.v("ucscdining", "do in background");
//            Log.v("ucscdining", MenuParser.fullMenuObj.get(0).getBreakfast().get(0));
            return null;
        }

        @Override
        protected void onPreExecute() {
            //refreshStarted = true;
        }

        protected void onPostExecute(Long result) {
            Intent svcIntent = new Intent(mContext, WidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget = new RemoteViews(mContext.getPackageName(), R.layout.menu_widget);
            widget.setRemoteAdapter(R.id.widget_list, svcIntent);
            mAppWidgetManager.updateAppWidget(mAppWidgetId, widget);
            Log.v("ucscdining", "postexecute");
        }
    }
}


