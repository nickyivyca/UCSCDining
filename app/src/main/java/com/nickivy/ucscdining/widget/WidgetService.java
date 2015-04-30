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

/**
 *
 */

/*public class MealWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.v("ucscdining", "getting view factory");
        return(new MealWidgetViewsFactory(this.getApplicationContext(), intent));
    }
} */

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return(new MealWidgetViewsFactory(this.getApplicationContext(),
                intent));
    }
}

class MealWidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

/*    private static final String[] items={"lorem", "ipsum", "dolor",
            "sit", "amet", "consectetuer",
            "adipiscing", "elit", "morbi",
            "vel", "ligula", "vitae",
            "arcu", "aliquet", "mollis",
            "etiam", "vel", "erat",
            "placerat", "ante",
            "porttitor", "sodales",
            "pellentesque", "augue",
            "purus"}; */

    private Context context = null;
    private int appWidgetId;

    public MealWidgetViewsFactory(Context context, Intent intent) {
        Log.v("ucscdining", "MealWidgetViewsFactory constructor");
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
        Log.v("ucscdining", "getviewat");
        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_row);
        row.setTextViewText(android.R.id.text1,
                MenuParser.fullMenuObj.get(0).getBreakfast().get(position));
//        Log.v("ucscdining", MenuParser.fullMenuObj.get(0).getBreakfast().get(0));

        Intent intent = new Intent();
        Bundle extras = new Bundle();
//        extras.putString(MenuWidget.EXTRA_WORD, MenuParser.fullMenuObj.get(0).getBreakfast().get(i));
//        extras.putString(MenuWidget.EXTRA_WORD, "pancakes");
        extras.putString(MenuWidget.EXTRA_WORD,
                MenuParser.fullMenuObj.get(0).getBreakfast().get(position));
        intent.putExtras(extras);
        row.setOnClickFillInIntent(android.R.id.text1, intent);
        return row;
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
