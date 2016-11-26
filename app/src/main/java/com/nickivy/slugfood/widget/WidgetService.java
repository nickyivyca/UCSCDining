package com.nickivy.slugfood.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.nickivy.slugfood.R;
import com.nickivy.slugfood.parser.MenuParser;
import com.nickivy.slugfood.util.Util;

import java.util.ArrayList;

/**
 * RemoteViewsService for the UCSCDining Widget. This only handles the ListView.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author Nicky Ivy parkedraccoon@gmail.com
 */

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return(new MealWidgetViewsFactory(this.getApplicationContext(),
                intent));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
}

class MealWidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context = null;
    private int appWidgetId;

    public MealWidgetViewsFactory(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        if (MenuWidget.getWidgetDataById(appWidgetId) == null) {
            Intent intent = new Intent(context, MenuWidget.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, -2,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {}
            return -1;
        }
        switch (MenuWidget.getWidgetDataById(appWidgetId).getMeal()) {
            case Util.BREAKFAST:
                return Util.fullMenuObj.get(MenuWidget.getWidgetDataById
                        (appWidgetId).getCollege()).getBreakfast().size();
            case Util.LUNCH:
                return Util.fullMenuObj.get(MenuWidget.getWidgetDataById
                        (appWidgetId).getCollege()).getLunch().size();
            case Util.DINNER:
                return Util.fullMenuObj.get(MenuWidget.getWidgetDataById
                        (appWidgetId).getCollege()).getDinner().size();
            case Util.LATENIGHT:
                return Util.fullMenuObj.get(MenuWidget.getWidgetDataById
                        (appWidgetId).getCollege()).getLateNight().size();
            default:
                return -1;
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_row);
        if (MenuWidget.getWidgetDataById(appWidgetId) == null) {
            return row;
        }
        // If user mashes buttons too fast things can crash. This should prevent it
        if (position >=
                getCurrentMenu(MenuWidget.getWidgetDataById(appWidgetId).getCollege()).size()) {
            return row;
        }
        row.setTextViewText(android.R.id.text1,
                getCurrentMenu(MenuWidget.getWidgetDataById
                        (appWidgetId).getCollege()).get(position));

        Intent intent = new Intent();
        Bundle extras = new Bundle();
        extras.putString(Util.EXTRA_WORD,
                getCurrentMenu(MenuWidget.getWidgetDataById
                        (appWidgetId).getCollege()).get(position));
        intent.putExtras(extras);// Set intent on other bits for launching main app
        row.setOnClickFillInIntent(android.R.id.text1, intent);
        return row;
    }

    /**
     * Given parameters of current college, decides what meal is current and returns its list
     * @param college
     * @return
     */
    private ArrayList<String> getCurrentMenu(final int college) {
        /*if (!Util.fullMenuObj.get(college).getIsOpen()) {
            ArrayList<String> ret = new ArrayList<String>();
            ret.add(Util.collegeList[college] +
                    "dining hall closed today");
            return ret;
        }*/
        switch(MenuWidget.getWidgetDataById(appWidgetId).getMeal()) {
            case Util.BREAKFAST:
                return Util.fullMenuObj.get(college).getBreakfastList();
            case Util.LUNCH:
                return Util.fullMenuObj.get(college).getLunchList();
            case Util.DINNER:
                return Util.fullMenuObj.get(college).getDinnerList();
            case Util.LATENIGHT:
                return Util.fullMenuObj.get(college).getLateNightList();
            default:
                return null;
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
