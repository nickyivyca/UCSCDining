package com.nickivy.slugfood.parser;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.nickivy.slugfood.util.MenuItem;
import com.nickivy.slugfood.util.Util;

import android.util.Log;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;

/**
 * Parses the incoming file.
 * 
 * <p>Data is stored in the static fullMenu arraylist of 
 * CollegeMenu objects.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 * 
 * @author Nicky Ivy parkedraccoon@gmail.com
 */

public class MenuParser {

    public static final String URLPart1 = "http://nutrition.sa.ucsc.edu/pickMenu.asp";

    public static final String[] URLPart2s = {
            "?locationNum=05&locationName=Cowell&dtdate=",
            "?locationNum=20&locationName=+Crown+Merrill&dtdate=",
            "?locationNum=25&locationName=Porter&dtdate=",
            "?locationNum=30&locationName=College+Eight&dtdate=",
            "?locationNum=40&locationName=College+Nine+%26+Ten&dtdate="
    };

    private static final String URLPart3 = "&mealName=";

    private static final String icalurl = "https://calendar.google.com/calendar/ical/ucsc.edu_t59u0f85lnvamgj30m22e3fmgo%40group.calendar.google.com/public/basic.ics";
    
    public static boolean manualRefresh = false;

    public static int getSingleMealList(int k, int month, int day, int year, boolean collegeNight,
                                        boolean healthyMonday, boolean farmFriday) {
        Document breakfastDoc, lunchDoc, dinnerDoc;
        Elements breakfastNutIds = null,
                breakfastFoodNames = null,
                lunchNutIds = null,
                lunchFoodNames = null,
                dinnerNutIds = null,
                dinnerFoodNames = null;

        try {
            breakfastDoc = Jsoup.connect(URLPart1 + URLPart2s[k] + month + "%2F" +
                    day + "%2F" + year + URLPart3 + Util.meals[0]).get();
        } catch (UnknownHostException e) {
            // Internet connection completely missing is a separate error from okhttp
            Log.v(Util.LOGTAG, Util.LOGMSG_INTERNETERROR);
            e.printStackTrace();
            return Util.GETLIST_INTERNET_FAILURE;
        } catch (IOException e) {
            Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
            try {
                breakfastDoc = Jsoup.connect(URLPart1 + URLPart2s[k] + month + "%2F" +
                        day + "%2F" + year + URLPart3 + Util.meals[0]).get();
            } catch (IOException e1) {
                Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                try {
                    breakfastDoc = Jsoup.connect(URLPart1 + URLPart2s[k] + month + "%2F" +
                            day + "%2F" + year + URLPart3 + Util.meals[0]).get();
                } catch (IOException e2) {
                    Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                    // Give up after three times
                    return Util.GETLIST_OKHTTP_FAILURE;
                }
            }
        }

        try {
            lunchDoc = Jsoup.connect(URLPart1 + URLPart2s[k] + month + "%2F" +
                    day + "%2F" + year + URLPart3 + Util.meals[1]).get();
        } catch (UnknownHostException e) {
            // Internet connection completely missing is a separate error from okhttp
            Log.v(Util.LOGTAG, Util.LOGMSG_INTERNETERROR);
            e.printStackTrace();
            return Util.GETLIST_INTERNET_FAILURE;
        } catch (IOException e) {
            Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
            try {
                lunchDoc = Jsoup.connect(URLPart1 + URLPart2s[k] + month + "%2F" +
                        day + "%2F" + year + URLPart3 + Util.meals[1]).get();
            } catch (IOException e1) {
                Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                try {
                    lunchDoc = Jsoup.connect(URLPart1 + URLPart2s[k] + month + "%2F" +
                            day + "%2F" + year + URLPart3 + Util.meals[1]).get();
                } catch (IOException e2) {
                    Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                    // Give up after three times
                    return Util.GETLIST_OKHTTP_FAILURE;
                }
            }
        }

        try {
            dinnerDoc = Jsoup.connect(URLPart1 + URLPart2s[k] + month + "%2F" +
                    day + "%2F" + year + URLPart3 + Util.meals[2]).get();
        } catch (UnknownHostException e) {
            // Internet connection completely missing is a separate error from okhttp
            Log.v(Util.LOGTAG, Util.LOGMSG_INTERNETERROR);
            e.printStackTrace();
            return Util.GETLIST_INTERNET_FAILURE;
        } catch (IOException e) {
            Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
            try {
                dinnerDoc = Jsoup.connect(URLPart1 + URLPart2s[k] + month + "%2F" +
                        day + "%2F" + year + URLPart3 + Util.meals[2]).get();
            } catch (IOException e1) {
                Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                try {
                    dinnerDoc = Jsoup.connect(URLPart1 + URLPart2s[k] + month + "%2F" +
                            day + "%2F" + year + URLPart3 + Util.meals[2]).get();
                } catch (IOException e2) {
                    Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                    // Give up after three times
                    return Util.GETLIST_OKHTTP_FAILURE;
                }
            }
        }
        breakfastFoodNames = breakfastDoc.select("div[class=\"pickmenucoldispname\"]");
        breakfastNutIds = breakfastDoc.select("INPUT[TYPE=\"CHECKBOX\"]");

        lunchFoodNames = lunchDoc.select("div[class=\"pickmenucoldispname\"]");
        lunchNutIds = lunchDoc.select("INPUT[TYPE=\"CHECKBOX\"]");

        dinnerFoodNames = dinnerDoc.select("div[class=\"pickmenucoldispname\"]");
        dinnerNutIds = dinnerDoc.select("INPUT[TYPE=\"CHECKBOX\"]");

        ArrayList<MenuItem> breakfastList = new ArrayList<MenuItem>(),
                lunchList = new ArrayList<MenuItem>(),
                dinnerList = new ArrayList<MenuItem>();

        //Catch if the dining hall is closed for that day
        if(breakfastFoodNames != null && breakfastFoodNames.size() > 0){
            for(int i = 0; i < breakfastFoodNames.size(); i++){
                breakfastList.add(new MenuItem(breakfastFoodNames.get(i).text(),
                        breakfastNutIds.get(i).attr("VALUE")));
            }
        }
        //Catch if the dining hall is closed for that day
        if(lunchFoodNames != null && lunchFoodNames.size() > 0){
            for(int i = 0; i < lunchFoodNames.size(); i++){
                lunchList.add(new MenuItem(lunchFoodNames.get(i).text(),
                        lunchNutIds.get(i).attr("VALUE")));
            }
        }
        //Catch if the dining hall is closed for that day
        if(dinnerFoodNames != null && dinnerFoodNames.size() > 0){
            for(int i = 0; i < dinnerFoodNames.size(); i++){
                dinnerList.add(new MenuItem(dinnerFoodNames.get(i).text(),
                        dinnerNutIds.get(i).attr("VALUE")));
            }
        }

        Util.fullMenuObj.get(k).setBreakfast(breakfastList);
        Util.fullMenuObj.get(k).setLunch(lunchList);
        Util.fullMenuObj.get(k).setDinner(dinnerList);
        if(Util.fullMenuObj.get(k).getBreakfast().isEmpty() &&
                (!(Util.fullMenuObj.get(k).getLunch().isEmpty()) &&
                        !(Util.fullMenuObj.get(k).getDinner().isEmpty()))){
            ArrayList<MenuItem> breakfastMessage = new ArrayList<MenuItem>();
            breakfastMessage.add(new MenuItem(Util.brunchMessage, "-1"));
            Util.fullMenuObj.get(k).setBreakfast(breakfastMessage);
        }

        // If events are on Calendar but not on menu, insert them.

        if (collegeNight && !Util.fullMenuObj.get(k).getIsCollegeNight()) {
            // Whenever it's college night the rest of the list should be empty
            ArrayList<MenuItem> dinner = new ArrayList<MenuItem>();
            //ArrayList<MenuItem> dinner = Util.fullMenuObj.get(k).getDinner();
            dinner.add(new MenuItem("College Night", "-1"));
            Util.fullMenuObj.get(k).setDinner(dinner);
        }

        if (healthyMonday && !Util.fullMenuObj.get(k).getIsHealthyMonday()) {
            ArrayList<MenuItem> breakfast = Util.fullMenuObj.get(k).getBreakfast();
            ArrayList<MenuItem> lunch = Util.fullMenuObj.get(k).getLunch();
            ArrayList<MenuItem> dinner = Util.fullMenuObj.get(k).getDinner();
            breakfast.add(0, new MenuItem("Healthy Mondays", "-1"));
            lunch.add(0, new MenuItem("Healthy Mondays", "-1"));
            dinner.add(0, new MenuItem("Healthy Mondays", "-1"));
            Util.fullMenuObj.get(k).setBreakfast(breakfast);
            Util.fullMenuObj.get(k).setDinner(lunch);
            Util.fullMenuObj.get(k).setDinner(dinner);
        }

        if (farmFriday && !Util.fullMenuObj.get(k).getIsFarmFriday()) {
            ArrayList<MenuItem> breakfast = Util.fullMenuObj.get(k).getBreakfast();
            ArrayList<MenuItem> lunch = Util.fullMenuObj.get(k).getLunch();
            ArrayList<MenuItem> dinner = Util.fullMenuObj.get(k).getDinner();
            breakfast.add(0, new MenuItem("Farm Fridays", "-1"));
            lunch.add(0, new MenuItem("Farm Fridays", "-1"));
            dinner.add(0, new MenuItem("Farm Fridays", "-1"));
            Util.fullMenuObj.get(k).setBreakfast(breakfast);
            Util.fullMenuObj.get(k).setLunch(lunch);
            Util.fullMenuObj.get(k).setDinner(dinner);
        }
        return Util.GETLIST_SUCCESS;
    }
    
    /**
     * Puts downloaded data from specified date (instead of today) into the full menu object.
     */
    public static int getMealList(int month, int day, int year) {
        /**
         * outer array is colleges, inner is [0]
         * college night, [1] healthy monday, [2] farm friday
         */
        boolean[][] eventBools = new boolean[5][3];
        Document icaldoc = null;

        try {
            icaldoc = Jsoup.connect(icalurl).get();
        } catch (UnknownHostException e) {
            // Internet connection completely missing is a separate error from okhttp
            Log.v(Util.LOGTAG, Util.LOGMSG_INTERNETERROR);
            e.printStackTrace();
            return Util.GETLIST_INTERNET_FAILURE;
        } catch (IOException e) {
            Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
            try {
                icaldoc = Jsoup.connect(icalurl).get();
            } catch (IOException e1) {
                Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                try {
                    icaldoc = Jsoup.connect(icalurl).get();
                } catch (IOException e2) {
                    Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                    // Give up after three times
                    return Util.GETLIST_OKHTTP_FAILURE;
                }
            }
        }

        icaldoc.outputSettings(new Document.OutputSettings().prettyPrint(false));
        String icalstring = icaldoc.body().html();

        // Why does this take so long here?
        ICalendar ical = Biweekly.parse(icalstring).first();

        List<VEvent> events = ical.getEvents();
        for (VEvent e : events) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(e.getDateStart().getValue());
            if (month == (cal.get(Calendar.MONTH) + 1) && day == cal.get(Calendar.DAY_OF_MONTH)
                    && year == cal.get(Calendar.YEAR)) {
                String desc = e.getSummary().getValue();
                if (desc.contains("College Night")) {
                    if (desc.contains("Cowell)") || desc.contains("Stevenson")) {
                        eventBools[0][0] = true;
                    } else if (desc.contains("Crown") || desc.contains("Merrill")) {
                        eventBools[1][0] = true;
                    } else if (desc.contains("Porter") || desc.contains("Kresge")) {
                        eventBools[2][0] = true;
                    } else if (desc.contains("Eight") || desc.contains("Oakes")) {
                        eventBools[3][0] = true;
                    } else if (desc.contains("Nine") || desc.contains("Ten")) {
                        eventBools[4][0] = true;
                    }
                }
                if (desc.contains("Healthy Monday")) {
                    if (desc.contains("Cowell)")) {
                        eventBools[0][1] = true;
                    } else if (desc.contains("Crown")) {
                        eventBools[1][1] = true;
                    } else if (desc.contains("Porter")) {
                        eventBools[2][1] = true;
                    } else if (desc.contains("Eight")) {
                        eventBools[3][1] = true;
                    } else if (desc.contains("Nine")) {
                        eventBools[4][1] = true;
                    }
                }
                if (desc.contains("Farm Friday")) {
                    if (desc.contains("Cowell)")) {
                        eventBools[0][2] = true;
                    } else if (desc.contains("Crown")) {
                        eventBools[1][2] = true;
                    } else if (desc.contains("Porter")) {
                        eventBools[2][2] = true;
                    } else if (desc.contains("Eight")) {
                        eventBools[3][2] = true;
                    } else if (desc.contains("Nine")) {
                        eventBools[4][2] = true;
                    }
                }
            }
        }


        for (int i = 0; i < 5; i++) {
            int res = getSingleMealList(i, month, day, year, eventBools[i][0], eventBools[i][1],
                    eventBools[i][2]);
            /*
             * For some stupid reason, it throws these stupid unexpected status line errors half the
             * time on mobile data. So we have to intercept those somehow. getsinglemeallist returns
             * okhttp failure if it gets one - and getsinglemeallist also tries multiple times
             * before returning the error. It will only try once for lost internet connection,
             * though.
             */
            if (res == Util.GETLIST_OKHTTP_FAILURE) {
                res = getSingleMealList(i, month, day, year, eventBools[i][0], eventBools[i][1],
                        eventBools[i][2]);
                if (res == Util.GETLIST_OKHTTP_FAILURE) {
                    res = getSingleMealList(i, month, day, year, eventBools[i][0], eventBools[i][1],
                            eventBools[i][2]);
                    if (res == Util.GETLIST_OKHTTP_FAILURE) {
                        res = getSingleMealList(i, month, day, year, eventBools[i][0], eventBools[i][1],
                                eventBools[i][2]);
                        if (res == Util.GETLIST_OKHTTP_FAILURE) {
                            return Util.GETLIST_INTERNET_FAILURE;
                        }
                    }
                }
            } else if (res != Util.GETLIST_SUCCESS) {
                return res;
            }
    	}
        return Util.GETLIST_SUCCESS;
    }
}
