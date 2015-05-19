package com.nickivy.ucscdining.widget;


import android.content.Context;
import android.widget.RemoteViews;

import com.nickivy.ucscdining.R;
import com.nickivy.ucscdining.parser.MenuParser;
import com.nickivy.ucscdining.util.Util;

import java.util.Calendar;

/**
 * Object for storing widget data, helps make each widget able to be independent.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author Nick Ivy parkedraccoon@gmail.com
 */
public class WidgetData {

    private int widgetId,
    currentCollege,
    currentMeal,
    currentMonth,
    currentDay,
    currentYear,
    backupCollege;

    private boolean directionRight;

    private RemoteViews views;

    public WidgetData(int widgetId, Context context) {
        this(widgetId, 0, context);
    }

    public WidgetData(int widgetId, int initialCollege, Context context) {
        this.widgetId = widgetId;
        this.currentCollege = initialCollege;
        currentMeal = Util.getCurrentMeal(this.currentCollege);
        // Set current day to today
        int today[] = Util.getToday();
        currentMonth = today[0];
        currentDay = today[1];
        currentYear = today[2];
        views = new RemoteViews(context.getPackageName(), R.layout.menu_widget);
        backupCollege = Util.NO_BACKUP_COLLEGE;
    }

    // Constructor used for recreating data
    public WidgetData(int widgetId, int initialCollege, int[] date, int meal, Context context,
                      int backupCollege) {
        this.widgetId = widgetId;
        this.currentCollege = initialCollege;
        currentMeal = meal;
        // Set current day to parameter
        currentMonth = date[0];
        currentDay = date[1];
        currentYear = date[2];
        views = new RemoteViews(context.getPackageName(), R.layout.menu_widget);
        this.backupCollege = backupCollege;
    }

    public int getWidgetId() {
        return widgetId;
    }

    public int getCollege() {
        return currentCollege;
    }

    public int getMeal() {
        return currentMeal;
    }

    public int getMonth() {
        return currentMonth;
    }

    public int getDay() {
        return currentDay;
    }

    public int getYear() {
        return currentYear;
    }

    public int getBackupCollege() {
        return backupCollege;
    }

    public RemoteViews getViews() {
        return views;
    }

    public boolean getDirectionRight() {
        return directionRight;
    }

    public void setCollege(int currentCollege) {
        this.currentCollege = currentCollege;
    }

    public void setMeal(int currentMeal) {
        this.currentMeal = currentMeal;
    }

    public void setMonth(int currentMonth) {
        this.currentMonth = currentMonth;
    }

    public void setDay(int currentDay) {
        this.currentDay = currentDay;
    }

    public void setYear(int currentYear) {
        this.currentYear = currentYear;
    }

    public void setDirectionRight(boolean direction) {
        this.directionRight = direction;
    }

    public void setBackupCollege() {
        backupCollege = currentCollege;
    }


    public void incrementCollege() {
        currentCollege++;
        if (currentCollege >= 5) {
            currentCollege = 0;
        }
    }

    public void decrementCollege() {
        currentCollege--;
        if (currentCollege <= -1) {
            currentCollege = 4;
        }
    }

    public void incrementMeal() {
        directionRight = true;
        currentMeal++;
        if (currentMeal == 3) {
            currentMeal = 0;
            // If at dinner, and right button pressed, advance to next day
            changeDay(1);
            /*
             * Skipping brunch checks must be done in Asynctask because we don't actually have
             * the new menu containing the brunch message yet
             */
        }
    }

    public void decrementMeal() {
        directionRight = false;
        currentMeal--;
        if (currentMeal == -1) {
            currentMeal = 2;
            // If at breakfast and left button pressed, go to previous day
            changeDay(-1);
        }
        if (currentMeal == Util.BREAKFAST &&
                MenuParser.fullMenuObj.get(currentCollege).getBreakfast().size() > 0) {
            if (MenuParser.fullMenuObj.get(currentCollege).getBreakfast().get(0).getItemName()
                    .equals(Util.brunchMessage)) {
                changeDay(-1);
                currentMeal = Util.DINNER;
            }
        }
    }

    private void changeDay(int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, currentMonth - 1);
        calendar.set(Calendar.DAY_OF_MONTH, currentDay);
        calendar.set(Calendar.YEAR, currentYear);
        calendar.add(Calendar.DATE, amount);
        currentMonth = calendar.get(Calendar.MONTH) + 1;
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        currentYear = calendar.get(Calendar.YEAR);
    }

    public void setToToday() {
        int today[] = Util.getToday();
        currentMonth = today[0];
        currentDay = today[1];
        currentYear = today[2];
    }
}

