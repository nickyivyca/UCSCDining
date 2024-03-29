package com.nickivy.slugfood.util;

import android.util.Log;

import com.nickivy.slugfood.parser.MenuParser;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Class for holding static functions and enums such as getting today, getting current meal, etc
 * that were previously scattered around various classes.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author @author Nicky Ivy parkedraccoon@gmail.com
 */
public class Util {

    // Times that the app decides to switch over to the specified meal.
    public static final int DINNER_SWITCH_TIME = 15, // 3 PM
            LUNCH_SWITCH_TIME = 11, // 11 AM
            BREAKFAST_SWITCH_TIME = 23,// 9 PM
            LATENIGHT_SWITCH_TIME = 20;

    // Enums for meal constants. They match the indexes in the meals array.
    public static final int BREAKFAST = 0,
            LUNCH = 1,
            DINNER = 2,
            LATENIGHT = 3;


    public static final String[] CookieLocations = {"05", "20", "25", "30", "40"};

    public static final String[] meals = {
            "Breakfast",
            "Lunch",
            "Dinner",
            "Late Night"
    };

    public static final String[] collegeList = {
            "Cowell/Stevenson",
            "Crown/Merrill",
            "Porter/Kresge",
            "Rachel Carson/Oakes",
            "Nine/Lewis"
    };

    public static ArrayList<CollegeMenu> fullMenuObj = new ArrayList<CollegeMenu>(){{
        add(new CollegeMenu());
        add(new CollegeMenu());
        add(new CollegeMenu());
        add(new CollegeMenu());
        add(new CollegeMenu());
        add(new CollegeMenu());
    }};

    // Enums for returns of data fetching status
    public static final int GETLIST_SUCCESS = 1,
            GETLIST_INTERNET_FAILURE = 0,
            GETLIST_DATABASE_FAILURE = -1,
            GETLIST_OKHTTP_FAILURE = 2,
            GETNUT_NO_INFO = 3;

    // Extra word key for intent creation
    public static final String EXTRA_WORD = "com.nickivy.ucscdining.widget.WORD";

    public static final String LOGTAG = "ucscdining",
    LOGMSG_INTERNETERROR = "Internet connection missing",
    LOGMSG_OKHTTP = "Connection error";


    public static final String WIDGETSTATE_PREFS = "widgetstate_prefs";
    public static final String DST_PREF = "dst_prefs";

    public static final String KEY_DST_PREF = "dst_key_pref";

    public static final String brunchMessage = "See lunch for today\'s brunch menu";

    public static final String nuturlpart1 = "https://nutrition.sa.ucsc.edu/label.aspx?locationNum=";

    // Tags for intent passed to main activity from widget
    public static final String TAG_COLLEGE = "tag_college",
    TAG_MEAL = "tag_meal",
    TAG_MONTH = "tag_month",
    TAG_DAY = "tag_day",
    TAG_YEAR = "tag_year",
    TAG_URL = "tag_url",
    TAG_COOKIE_COLLEGE = "tag_cookie_college",
    TAG_USESAVED = "tag_usesaved",
    TAG_TIMEUPDATE = "com.nickivy.slugfood.time_update",
    TAG_RELOAD = "tag_reload",
    TAG_WIDGETID = "widget_id",
    TAG_ENABLEBACKGROUND = "enable_bg",
    TAG_DISABLEBACKGROUND = "disable_bg",
    TAG_WIDGETENABLED = "enable_widget",
    TAG_WIDGETGONE = "widget_gone",
    TAG_NOTIFICATIONSON = "notifications_on",
    TAG_NOTIFICATIONSOFF = "notifications_off",
    TAG_FROMNOTIFICATION = "from_notification";

    public static final int NO_BACKUP_COLLEGE = 6;
    public static final String RCNONUT = "12345";

    public static final String MEAL_CHANNEL_ID = "channel_meals",
    EVENT_CHANNEL_ID = "channel_event";

    /**
     * Returns today's date as a 3-number int array. [month, day, year]
     *
     * <p>Does not necessarily return 'today' but instead returns the day the app should display.
     * This happens after the dining hall closes.
     */
    public static int[] getToday() {
        Calendar cal = Calendar.getInstance();
        // realDay is for notifications, when we need to phrase 'today' or 'tomorrow'
        int realDay = cal.get(Calendar.DAY_OF_MONTH);
        // If time is past dining hall closing (which is the breakfast switch time) return tomorrow
        if (cal.get(Calendar.HOUR_OF_DAY) >= BREAKFAST_SWITCH_TIME) {
            cal.add(Calendar.DATE, 1);
        }

        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int year = cal.get(Calendar.YEAR);

        int ret[] = {month, day, year, realDay};
        return ret;
    }

    /**
     * Gets meal based on time of day, based on times at top of file.
     * Will not return breakfast if brunch message is present
     *
     * @return return values are also enumerated at top of file
     */
    public static int getCurrentMeal(int college) {
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.HOUR_OF_DAY) >= LATENIGHT_SWITCH_TIME &&
                cal.get(Calendar.HOUR_OF_DAY) < BREAKFAST_SWITCH_TIME) {
            return LATENIGHT;
        }
        if (cal.get(Calendar.HOUR_OF_DAY) >= DINNER_SWITCH_TIME &&
                cal.get(Calendar.HOUR_OF_DAY) < LATENIGHT_SWITCH_TIME) {
            return DINNER;
        }
        if (cal.get(Calendar.HOUR_OF_DAY) >= LUNCH_SWITCH_TIME &&
                cal.get(Calendar.HOUR_OF_DAY) < DINNER_SWITCH_TIME) {
            return LUNCH;
        }
        if(cal.get(Calendar.HOUR_OF_DAY) >= BREAKFAST_SWITCH_TIME ||
                cal.get(Calendar.HOUR_OF_DAY) < LUNCH_SWITCH_TIME) {
            if (fullMenuObj.get(college).getBreakfast().size() > 0) {
                if (fullMenuObj.get(college).getBreakfast().get(0).getItemName()
                        .equals(brunchMessage)) {
                    return LUNCH;
                }
            }
            return BREAKFAST;
        }
        return -1;
    }

    public static void log(String str) {
        android.util.Log.v(Util.LOGTAG, str);
    }
}
