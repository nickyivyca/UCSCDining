package com.nickivy.slugfood.parser;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.nickivy.slugfood.util.CollegeMenu;
import com.nickivy.slugfood.util.MenuItem;
import com.nickivy.slugfood.util.Util;

import android.util.Log;

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

    private static final String legacyURLPart1 = "http://nutrition.sa.ucsc.edu/menuSamp.asp?" +
            "myaction=read&sName=&dtdate=";

    private static final String[] legacyURLListParts2 = {
            "&locationNum=05&locationName=%20Cowell&naFlag=1",
            "&locationNum=20&locationName=Crown+Merrill&sName=&naFlag=1",
            "&locationNum=25&locationName=Porter&sName=&naFlag=1",
            "&locationNum=30&locationName=College+Eight&sName=&naFlag=1",
            "&locationNum=40&locationName=College+Nine+%26+Ten&sName=&naFlag=1"
    };
    
    public static boolean manualRefresh = false;
    
    public static ArrayList<CollegeMenu> fullMenuObj = new ArrayList<CollegeMenu>(){{
    	add(new CollegeMenu());
    	add(new CollegeMenu());
    	add(new CollegeMenu());
    	add(new CollegeMenu());
    	add(new CollegeMenu());
    }};

    public static int getSingleMealList(int k, int month, int day, int year) {
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
            Log.v(Util.LOGTAG, "Internet connection missing");
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

        fullMenuObj.get(k).setBreakfast(breakfastList);
        fullMenuObj.get(k).setLunch(lunchList);
        fullMenuObj.get(k).setDinner(dinnerList);
        if(fullMenuObj.get(k).getBreakfast().isEmpty() &&
                (!(fullMenuObj.get(k).getLunch().isEmpty()) &&
                        !(fullMenuObj.get(k).getDinner().isEmpty()))){
            ArrayList<MenuItem> breakfastMessage = new ArrayList<MenuItem>();
            breakfastMessage.add(new MenuItem(Util.brunchMessage, "-1"));
            fullMenuObj.get(k).setBreakfast(breakfastMessage);
        }
        /*
         * If breakfast and lunch but no dinner, may be college night, check the old page
         * without nutrition info - the page we used to use before nutrition info
         */
        if(!fullMenuObj.get(k).getBreakfast().isEmpty() &&
                !(fullMenuObj.get(k).getLunch().isEmpty()) &&
                        (fullMenuObj.get(k).getDinner().isEmpty())){
            Document collegeNightDoc;
            try {
                collegeNightDoc = Jsoup.connect(legacyURLPart1 + month + "%2F" + day + "%2F" + year
                        + legacyURLListParts2[k]).get();
            } catch (UnknownHostException e) {
                // Internet connection completely missing is a separate error from okhttp
                Log.v(Util.LOGTAG, Util.LOGMSG_INTERNETERROR);
                e.printStackTrace();
                return Util.GETLIST_INTERNET_FAILURE;
            } catch (IOException e) {
                Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                try {
                    collegeNightDoc = Jsoup.connect(legacyURLPart1 + month + "%2F" + day + "%2F" +
                            year + legacyURLListParts2[k]).get();
                } catch (IOException e1) {
                    Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                    try {
                        collegeNightDoc = Jsoup.connect(legacyURLPart1 + month + "%2F" + day + "%2F"
                                + year + legacyURLListParts2[k]).get();
                    } catch (IOException e2) {
                        Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                        // Give up after three times
                        return Util.GETLIST_OKHTTP_FAILURE;
                    }
                }
            }

            dinnerFoodNames = collegeNightDoc.select("td[valign=\"top\"]");
            for(int j = 0; j < dinnerFoodNames.size(); j++) {
                //Dinner
                if(dinnerFoodNames.get(j).text().contains("Dinner")){
                    Elements dinner = dinnerFoodNames.get(j).select(" div[class=\"menusamprecipes\"]");
                    for(int i = 0; i < dinner.size(); i++) {
                        dinnerList = new ArrayList<MenuItem>();
                        //dinnerList.add(dinner.get(i).text());
                        Log.v(Util.LOGTAG, "names size: " + dinner.size());
                        Log.v(Util.LOGTAG, "i: " + i);
                        if (i == 0) {
                            dinnerList.add(new MenuItem(dinner.get(i).text(),
                                    "-1"));
                        } else {
                            dinnerList.add(new MenuItem(dinner.get(i).text(),
                                    dinnerNutIds.get(i - 1).attr("VALUE")));
                        }
                    }
                }
            }
            fullMenuObj.get(k).setDinner(dinnerList);
        }
        return Util.GETLIST_SUCCESS;
    }
    
    /**
     * Puts downloaded data from specified date (instead of today) into the full menu object.
     */
    public static int getMealList(int month, int day, int year) {
    	for (int i = 0; i < 5; i++) {
            int res = getSingleMealList(i, month, day, year);
            /*
             * For some stupid reason, it throws these stupid unexpected status line errors half the
             * time on mobile data. So we have to intercept those somehow. getsinglemeallist returns
             * okhttp failure if it gets one - and getsinglemeallist also tries multiple times
             * before returning the error. It will only try once for lost internet connection,
             * though.
             */
            if (res == Util.GETLIST_OKHTTP_FAILURE) {
                res = getSingleMealList(i, month, day, year);
                if (res == Util.GETLIST_OKHTTP_FAILURE) {
                    res = getSingleMealList(i, month, day, year);
                    if (res == Util.GETLIST_OKHTTP_FAILURE) {
                        res = getSingleMealList(i, month, day, year);
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
