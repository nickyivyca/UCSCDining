package com.nickivy.slugfood.parser;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.nickivy.slugfood.util.MenuItem;
import com.nickivy.slugfood.util.Util;

import java.util.ArrayList;

/**
 * Method for pulling data from either database or internet into the full menu object.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author Nicky Ivy parkedraccoon@gmail.com
 */
public class MealDataFetcher {

    // Keep track of if the fetch is running
    private static boolean running = false;

    /**
     * Loads data from database or web into the full menu object.
     */
    public static int fetchData(Context context, int month, int day, int year) {
        // If called multiple times, hold the second call until the first one exits.
        if (running) {
            Log.v(Util.LOGTAG, "waiting for another fetchdata to complete");
            while(running);
        }
        running = true;
        MealStorage mealStore = new MealStorage(context);
        SQLiteDatabase db;

        if (mealStore == null) {
            running = false;
            return Util.GETLIST_DATABASE_FAILURE;
        }

        db = mealStore.getReadableDatabase();

        String selection = MealStorage.COLUMN_MONTH + "= ? AND " + MealStorage.COLUMN_DAY +
                "= ? AND " + MealStorage.COLUMN_YEAR + "= ?";
        String[] selectionArgs = new String[3];

        selectionArgs[0] = "" + month;
        selectionArgs[1] = "" + day;
        selectionArgs[2] = "" + year;

        String[] projection = {
                MealStorage.COLUMN_MONTH,
                MealStorage.COLUMN_DAY,
                MealStorage.COLUMN_YEAR
        };

        Cursor c = db.query(MealStorage.TABLE_MEALS,
                projection, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        boolean cexists = (c.getCount() == 0);
        c.close();
        db.close();
        // If data for today does not exist, or manual refresh is requested, download/store data
        if(cexists || MenuParser.manualRefresh){
            int res = MenuParser.getMealList(month, day, year);
            if (res != Util.GETLIST_SUCCESS) {
                running = false;
                return res;
            }

            db = mealStore.getWritableDatabase();

            /*
             * Delete data from today and before, requires a couple different sql commands
             * Note: today, not date the retrieve menu task was called with
             *
             * if:
             * month is less than, year equal
             * year less than current year
             * day < current, year + month equal
             * if manual refresh (MenuParser.manualRefresh true), delete all
             *  (assume database got messed up somehow)
             */

            int today[] = Util.getToday();

            if (MenuParser.manualRefresh) {
                db.delete(MealStorage.TABLE_MEALS, null,null);
            } else {
                db.delete(MealStorage.TABLE_MEALS, MealStorage.COLUMN_MONTH + "<? AND " +
                        MealStorage.COLUMN_YEAR + " =?",
                        new String[] {"" + today[0], "" + today[2]});

                db.delete(MealStorage.TABLE_MEALS, MealStorage.COLUMN_YEAR + " <?",
                        new String[] {"" + today[2]});

                db.delete(MealStorage.TABLE_MEALS, MealStorage.COLUMN_MONTH + "=? AND " +
                        MealStorage.COLUMN_DAY + "<? AND " + MealStorage.COLUMN_YEAR + " =?",
                        new String[] {"" + today[0], "" + today[1], "" + today[2]});
            }

            // Begin writing data

            SQLiteStatement statement = db.compileStatement("INSERT INTO "+
                    MealStorage.TABLE_MEALS + "(" + MealStorage.COLUMN_COLLEGE + ", " +
                    MealStorage.COLUMN_MEAL + ", " + MealStorage.COLUMN_MENUITEM + ", " +
                    MealStorage.COLUMN_NUTID + ", " + MealStorage.COLUMN_MONTH + ", " +
                    MealStorage.COLUMN_DAY + ", " + MealStorage.COLUMN_YEAR +
                    ") VALUES (?,?,?,?,?,?,?);");

            // Using sqlite statement keeps the database 'open' and apparently is a bit faster
            db.beginTransaction();

            for(int j = 0; j < 5; j++) {
                statement.clearBindings();
                for (int i = 0; i < Util.fullMenuObj.get(j).getBreakfast().size(); i++) {
                    statement.bindLong(1, j);
                    statement.bindLong(2, 0);
                    statement.bindString(3, Util.fullMenuObj.get(j).getBreakfast().get(i)
                            .getItemName());
                    statement.bindString(4, Util.fullMenuObj.get(j).getBreakfast().get(i)
                            .getCode());
                    statement.bindLong(5, month);
                    statement.bindLong(6, day);
                    statement.bindLong(7, year);
                    // Database error will show up here if there is one. Catch it here.
                    try {
                        statement.execute();
                    } catch (SQLiteConstraintException e) {
                        running = false;
                        return Util.GETLIST_DATABASE_FAILURE;
                    }
                }
                for (int i = 0; i < Util.fullMenuObj.get(j).getLunch().size(); i++) {
                    statement.bindLong(1, j);
                    statement.bindLong(2, 1);
                    statement.bindString(3, Util.fullMenuObj.get(j).getLunch().get(i)
                            .getItemName());
                    statement.bindString(4, Util.fullMenuObj.get(j).getLunch().get(i)
                            .getCode());
                    statement.bindLong(5, month);
                    statement.bindLong(6, day);
                    statement.bindLong(7, year);
                    try {
                        statement.execute();
                    } catch (SQLiteConstraintException e) {
                        running = false;
                        return Util.GETLIST_DATABASE_FAILURE;
                    }
                }
                for (int i = 0; i < Util.fullMenuObj.get(j).getDinner().size(); i++) {
                    statement.bindLong(1,j);
                    statement.bindLong(2, 2);
                    statement.bindString(3, Util.fullMenuObj.get(j).getDinner().get(i)
                            .getItemName());
                    statement.bindString(4, Util.fullMenuObj.get(j).getDinner().get(i)
                            .getCode());
                    statement.bindLong(5, month);
                    statement.bindLong(6, day);
                    statement.bindLong(7, year);
                    try {
                        statement.execute();
                    } catch (SQLiteConstraintException e) {
                        running = false;
                        return Util.GETLIST_DATABASE_FAILURE;
                    }
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();

        } else {
            // I closed this up above, not sure why I have to do it again here
            db.close();
            db = mealStore.getReadableDatabase();

            String[] mainProjection = {
                    MealStorage.COLUMN_MENUITEM,
                    MealStorage.COLUMN_NUTID,
                    MealStorage.COLUMN_COLLEGE,
                    MealStorage.COLUMN_MONTH,
                    MealStorage.COLUMN_DAY,
                    MealStorage.COLUMN_YEAR
            };

            ArrayList<MenuItem> breakfastLoaded,
                    lunchLoaded, dinnerLoaded;

                    selection = MealStorage.COLUMN_COLLEGE + "= ? AND " + MealStorage.COLUMN_MEAL +
                            "= ? AND " + MealStorage.COLUMN_MONTH + "= ? AND " +
                            MealStorage.COLUMN_DAY + "= ? AND " + MealStorage.COLUMN_YEAR + "= ?";
            String[] mainSelectionArgs = new String[5];

            // For each of the 5 colleges, load data into the full menu object
            for(int j = 0; j < 5; j++){
                mainSelectionArgs[0] = "" + j;

                mainSelectionArgs[1] = "" + 0;

                mainSelectionArgs[2] = "" + month;
                mainSelectionArgs[3] = "" + day;
                mainSelectionArgs[4] = "" + year;

                c = db.query(MealStorage.TABLE_MEALS,
                        mainProjection, selection, mainSelectionArgs, null, null, null);

                c.moveToFirst();
                breakfastLoaded = new ArrayList<MenuItem>();

                for(int i = 0; i < c.getCount(); i++){
                    breakfastLoaded.add(new MenuItem(c.getString
                            (c.getColumnIndexOrThrow(MealStorage.COLUMN_MENUITEM)),
                            c.getString(c.getColumnIndexOrThrow(MealStorage.COLUMN_NUTID))));
                    c.moveToNext();
                }
                Util.fullMenuObj.get(j).setBreakfast(breakfastLoaded);
                c.close();

                mainSelectionArgs[1] = "" + 1;

                c = db.query(MealStorage.TABLE_MEALS,
                        mainProjection, selection, mainSelectionArgs, null, null, null);

                c.moveToFirst();
                lunchLoaded = new ArrayList<MenuItem>();

                for(int i = 0; i < c.getCount(); i++){
                    lunchLoaded.add(new MenuItem(c.getString
                            (c.getColumnIndexOrThrow(MealStorage.COLUMN_MENUITEM)),
                            c.getString(c.getColumnIndexOrThrow(MealStorage.COLUMN_NUTID))));
                    c.moveToNext();
                }
                Util.fullMenuObj.get(j).setLunch(lunchLoaded);
                c.close();

                mainSelectionArgs[1] = "" + 2;

                c = db.query(MealStorage.TABLE_MEALS,
                        mainProjection, selection, mainSelectionArgs, null, null, null);

                c.moveToFirst();
                dinnerLoaded = new ArrayList<MenuItem>();

                for(int i = 0; i < c.getCount(); i++){
                    dinnerLoaded.add(new MenuItem(c.getString
                            (c.getColumnIndexOrThrow(MealStorage.COLUMN_MENUITEM)),
                            c.getString(c.getColumnIndexOrThrow(MealStorage.COLUMN_NUTID))));
                    c.moveToNext();
                }
                Util.fullMenuObj.get(j).setDinner(dinnerLoaded);
                c.close();

            }
            db.close();
            mealStore.close();

        }
        running = false;
        return Util.GETLIST_SUCCESS;
    }
}
