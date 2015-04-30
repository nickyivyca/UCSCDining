package com.nickivy.ucscdining.parser;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.nickivy.ucscdining.MealViewFragment;

import java.util.ArrayList;

/**
 * Class for containing the AsyncTasks and related methods for retrieving data.
 */
public class MealDataFetcher {

    /**
     * Loads data from database or web into the full menu object.
     */
    public static void fetchData(Context context, int month, int day, int year) {
        MealStorage mealStore = new MealStorage(context);
        SQLiteDatabase db;

        db = mealStore.getReadableDatabase();

        String selection = MealStorage.COLUMN_MONTH + "= ? AND " + MealStorage.COLUMN_DAY + "= ? AND "
                + MealStorage.COLUMN_YEAR + "= ?";
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
        // If data for today does not exist, or manual refresh is requested, load data from web and store it
        if(cexists || MenuParser.needsRefresh){
            MenuParser.getMealList(month, day, year);

            db = mealStore.getWritableDatabase();

				/*
				 * Delete data from today and before, requires a couple different sql commands
				 * Note: today, not date the retrieve menu task was called with
				 *
				 * if:
				 * month is less than, year equal
				 * year less than current year
				 * day < current, year + month equal
				 * if manual refresh (MenuParser.needsRefresh true), delete all (assume database got messed up somehow)
				 */

            int today[] = MealViewFragment.getToday();

            if (MenuParser.needsRefresh) {
                db.delete(MealStorage.TABLE_MEALS, null,null);
            } else {
                db.delete(MealStorage.TABLE_MEALS, MealStorage.COLUMN_MONTH + "<? AND " + MealStorage.COLUMN_YEAR + " =?",
                        new String[] {"" + today[0], "" + today[2]});

                db.delete(MealStorage.TABLE_MEALS, MealStorage.COLUMN_YEAR + " <?", new String[] {"" + today[2]});

                db.delete(MealStorage.TABLE_MEALS, MealStorage.COLUMN_MONTH + "=? AND " + MealStorage.COLUMN_DAY + "<? AND " +
                        MealStorage.COLUMN_YEAR + " =?", new String[] {"" + today[0], "" + today[1], "" + today[2]});
            }

            // We need to write at the end of the table - so find size and offset first column by that much
            String countQuery = "SELECT * FROM " + MealStorage.TABLE_MEALS;
            Cursor cursor = db.rawQuery(countQuery, null);
            int offset = cursor.getCount();
            cursor.close();

            // Begin writing data

            SQLiteStatement statement = db.compileStatement("INSERT INTO "+ MealStorage.TABLE_MEALS +" VALUES (?,?,?,?,?,?,?);");

            // Using sqlite statement keeps the database 'open' and apparently is a bit faster
            db.beginTransaction();

            // Accumulate amount of nodes written in
            int accumulatedBreakfast = 0,
                    accumulatedLunch = 0,
                    accumulatedDinner = 0;

            for(int j = 0; j < 5; j++){

                statement.clearBindings();
                for (int i = 0; i < MenuParser.fullMenuObj.get(j).getBreakfast().size(); i++) {
                    statement.bindLong(1, offset + i + accumulatedBreakfast + accumulatedLunch + accumulatedDinner);
                    statement.bindLong(2,j);
                    statement.bindLong(3, 0);
                    statement.bindString(4, MenuParser.fullMenuObj.get(j).getBreakfast().get(i));
                    statement.bindLong(5, month);
                    statement.bindLong(6, day);
                    statement.bindLong(7, year);
                    statement.execute();
                }
                accumulatedBreakfast += MenuParser.fullMenuObj.get(j).getBreakfast().size();
                for (int i = 0; i < MenuParser.fullMenuObj.get(j).getLunch().size(); i++) {
                    statement.bindLong(1, offset + i + accumulatedBreakfast + accumulatedLunch + accumulatedDinner);
                    statement.bindLong(2,j);
                    statement.bindLong(3, 1);
                    statement.bindString(4, MenuParser.fullMenuObj.get(j).getLunch().get(i));
                    statement.bindLong(5, month);
                    statement.bindLong(6, day);
                    statement.bindLong(7, year);
                    statement.execute();
                }
                accumulatedLunch += MenuParser.fullMenuObj.get(j).getLunch().size();
                for (int i = 0; i < MenuParser.fullMenuObj.get(j).getDinner().size(); i++) {
                    statement.bindLong(1, offset + i + accumulatedBreakfast + accumulatedLunch + accumulatedDinner);
                    statement.bindLong(2,j);
                    statement.bindLong(3, 2);
                    statement.bindString(4, MenuParser.fullMenuObj.get(j).getDinner().get(i));
                    statement.bindLong(5, month);
                    statement.bindLong(6, day);
                    statement.bindLong(7, year);
                    statement.execute();
                }
                accumulatedDinner += MenuParser.fullMenuObj.get(j).getDinner().size();
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
                    MealStorage.COLUMN_COLLEGE,
                    MealStorage.COLUMN_MONTH,
                    MealStorage.COLUMN_DAY,
                    MealStorage.COLUMN_YEAR
            };

            ArrayList<String> breakfastLoaded,
                    lunchLoaded, dinnerLoaded;

            selection = MealStorage.COLUMN_COLLEGE + "= ? AND " + MealStorage.COLUMN_MEAL + "= ? AND "
                    + MealStorage.COLUMN_MONTH + "= ? AND " + MealStorage.COLUMN_DAY + "= ? AND "
                    + MealStorage.COLUMN_YEAR + "= ?";
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
                breakfastLoaded = new ArrayList<String>();

                for(int i = 0; i < c.getCount(); i++){
                    breakfastLoaded.add(c.getString(c.getColumnIndexOrThrow(MealStorage.COLUMN_MENUITEM)));
                    c.moveToNext();
                }
                MenuParser.fullMenuObj.get(j).setBreakfast(breakfastLoaded);
                c.close();

                mainSelectionArgs[1] = "" + 1;

                c = db.query(MealStorage.TABLE_MEALS,
                        mainProjection, selection, mainSelectionArgs, null, null, null);

                c.moveToFirst();
                lunchLoaded = new ArrayList<String>();

                for(int i = 0; i < c.getCount(); i++){
                    lunchLoaded.add(c.getString(c.getColumnIndexOrThrow(MealStorage.COLUMN_MENUITEM)));
                    c.moveToNext();
                }
                MenuParser.fullMenuObj.get(j).setLunch(lunchLoaded);
                c.close();

                mainSelectionArgs[1] = "" + 2;

                c = db.query(MealStorage.TABLE_MEALS,
                        mainProjection, selection, mainSelectionArgs, null, null, null);

                c.moveToFirst();
                dinnerLoaded = new ArrayList<String>();

                for(int i = 0; i < c.getCount(); i++){
                    dinnerLoaded.add(c.getString(c.getColumnIndexOrThrow(MealStorage.COLUMN_MENUITEM)));
                    c.moveToNext();
                }
                MenuParser.fullMenuObj.get(j).setDinner(dinnerLoaded);
                c.close();

            }
            db.close();
            mealStore.close();

        }
    }
}
