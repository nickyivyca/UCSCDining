package com.nickivy.ucscdining.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.nickivy.ucscdining.R;
import com.nickivy.ucscdining.parser.MenuParser;

import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 */

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return(new MealWidgetViewsFactory(this.getApplicationContext(),
                intent));
    }
}

class MealWidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context = null;
    private int appWidgetId;

    private final int DINNER_SWITCH_TIME = 15; // 3 PM
    private final int LUNCH_SWITCH_TIME = 11; // 11 AM
    private final int BREAKFAST_SWITCH_TIME = 0; // 12 AM

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
        return MenuParser.fullMenuObj.get(0).getBreakfast().size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.v("ucscdining", "get view at");
        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_row);
        row.setTextViewText(android.R.id.text1, getCurrentMenu(0).get(position));

        Intent intent = new Intent();
        Bundle extras = new Bundle();
        extras.putString(MenuWidget.EXTRA_WORD, getCurrentMenu(0).get(position));
        intent.putExtras(extras);
        row.setOnClickFillInIntent(android.R.id.text1, intent);
        return row;
    }

    /**
     * Given parameters of current college, decides what meal is current and returns its list
     * @param college
     * @return
     */
    private ArrayList<String> getCurrentMenu(final int college) {
        if (!MenuParser.fullMenuObj.get(college).getIsOpen()) {
            ArrayList<String> ret = new ArrayList<String>();
            ret.add(MenuParser.collegeList[college] +
                    "dining hall closed today");
            return ret;
        }
//        Log.v("ucscdining", "yay");
        Calendar cal = Calendar.getInstance();
        // Switch to dinner at 5 PM
        if (cal.get(Calendar.HOUR_OF_DAY) >= DINNER_SWITCH_TIME) {
            return MenuParser.fullMenuObj.get(college).getDinner();
        }
        /*if (cal.get(Calendar.MINUTE) >= 37) {
            return MenuParser.fullMenuObj.get(college).getBreakfast();
        } */
        if (cal.get(Calendar.HOUR_OF_DAY) >= LUNCH_SWITCH_TIME) {
            return MenuParser.fullMenuObj.get(college).getLunch();
        }
        if(cal.get(Calendar.HOUR_OF_DAY) >= BREAKFAST_SWITCH_TIME) {
            return MenuParser.fullMenuObj.get(college).getBreakfast();
        }
        // unreachable here
        return null;
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
