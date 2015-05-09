package com.nickivy.ucscdining.util;

import com.nickivy.ucscdining.parser.MenuParser;

import java.util.Calendar;
import java.util.Date;

/**
 * Class for holding static functions and enums such as getting today, getting current meal, etc
 * that were previously scattered around various classes.
 *
 * Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author @author Nicky Ivy parkedraccoon@gmail.com
 */
public class Util {

    // Times that the app decides to switch over to the specified meal.
    public static final int DINNER_SWITCH_TIME = 15, // 3 PM
            LUNCH_SWITCH_TIME = 11, // 11 AM
            BREAKFAST_SWITCH_TIME = 0;// 12 AM

    // Enums for meal constants. They match the indexes in the meals array.
    public static final int BREAKFAST = 0,
            LUNCH = 1,
            DINNER = 2;

    public static final String[] meals = {
            "Breakfast",
            "Lunch",
            "Dinner"
    };

    public static final String[] collegeList = {
            "Cowell/Stevenson",
            "Crown/Merrill",
            "Porter/Kresge",
            "Eight/Oakes",
            "Nine/Ten"
    };

    // Enums for returns of data fetching status
    public static final int GETLIST_SUCCESS = 1,
            GETLIST_INTERNET_FAILURE = 0,
            GETLIST_DATABASE_FAILURE = -1,
            GETLIST_OKHTTP_FAILURE = 2;

    // Extra word key for intent creation
    public static final String EXTRA_WORD = "com.nickivy.ucscdining.widget.WORD";

    public static final String LOGTAG = "ucscdining";

    public static final String WIDGETSTATE_PREFS = "widgetstate_prefs";

    public static final String brunchMessage = "See lunch for today\'s brunch menu";

    /**
     * Returns today's date as a 3-number int array. [month, day, year]
     */
    public static int[] getToday() {
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);

        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int year = cal.get(Calendar.YEAR);

        int ret[] = {month, day, year};
        return ret;
    }

    /**
     * Gets meal based on time of day, based on times at top of file
     *
     * Will not return breakfast if brunch message is present
     *
     * @return return values are also enumerated at top of file
     */
    public static int getCurrentMeal(int college) {
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.HOUR_OF_DAY) >= DINNER_SWITCH_TIME) {
            return DINNER;
        }
        if (cal.get(Calendar.HOUR_OF_DAY) >= LUNCH_SWITCH_TIME) {
            return LUNCH;
        }
        if(cal.get(Calendar.HOUR_OF_DAY) >= BREAKFAST_SWITCH_TIME) {
            if (MenuParser.fullMenuObj.get(college).getBreakfast().size() > 0) {
                if (MenuParser.fullMenuObj.get(college).getBreakfast().get(0)
                        .equals(brunchMessage)) {
                    return LUNCH;
                }
            }
            return BREAKFAST;
        }
        return -1;
    }
}
