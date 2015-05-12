package com.nickivy.ucscdining.parser;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.nickivy.ucscdining.util.CollegeMenu;
import com.nickivy.ucscdining.util.MenuItem;
import com.nickivy.ucscdining.util.Util;

import android.util.Log;

/**
 * Parses the incoming file.
 * 
 * <p>Data is stored in the static fullMenu arraylist of 
 * CollegeMenu objects.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 * 
 * @author Nick Ivy parkedraccoon@gmail.com
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
            breakfastFoodNames = breakfastDoc.select("div[class=\"pickmenucoldispname\"]");
            breakfastNutIds = breakfastDoc.select("INPUT[TYPE=\"CHECKBOX\"]");

            lunchDoc = Jsoup.connect(URLPart1 + URLPart2s[k] + month + "%2F" + day +
                    "%2F" + year + URLPart3 + Util.meals[1]).get();
            lunchFoodNames = lunchDoc.select("div[class=\"pickmenucoldispname\"]");
            lunchNutIds = lunchDoc.select("INPUT[TYPE=\"CHECKBOX\"]");

            dinnerDoc = Jsoup.connect(URLPart1 + URLPart2s[k] + month + "%2F" + day +
                    "%2F" + year + URLPart3 + Util.meals[2]).get();
            dinnerFoodNames = dinnerDoc.select("div[class=\"pickmenucoldispname\"]");
            dinnerNutIds = dinnerDoc.select("INPUT[TYPE=\"CHECKBOX\"]");
        } catch (UnknownHostException e) {
            Log.v("ucscdining", "Internet connection missing");
            e.printStackTrace();
            return Util.GETLIST_INTERNET_FAILURE;
        } catch (IOException e) {
            Log.w("ucscdining","Unable to download dining menu");
            e.printStackTrace();
            return Util.GETLIST_OKHTTP_FAILURE;
        }

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
                (!(fullMenuObj.get(k).getLunch().isEmpty()) ||
                        !(fullMenuObj.get(k).getDinner().isEmpty()))){
            ArrayList<MenuItem> breakfastMessage = new ArrayList<MenuItem>();
            breakfastMessage.add(new MenuItem(Util.brunchMessage, "-1"));
            fullMenuObj.get(k).setBreakfast(breakfastMessage);
        }
        return Util.GETLIST_SUCCESS;
    }
    
    /**
     * Puts downloaded data from specified date (instead of today) into the full menu object.
     */
    public static int getMealList(int month, int day, int year) {
    	for (int i = 0; i < 5; i++) {
            int res = getSingleMealList(i, month, day, year);
            // Try three times to defeat OKHTTP error.
            if (res == Util.GETLIST_OKHTTP_FAILURE) {
                res = getSingleMealList(i, month, day, year);
                if (res == Util.GETLIST_OKHTTP_FAILURE) {
                    res = getSingleMealList(i, month, day, year);
                    if (res == Util.GETLIST_OKHTTP_FAILURE) {
                        return Util.GETLIST_INTERNET_FAILURE;
                    }
                }
            } else if (res != Util.GETLIST_SUCCESS) {
                return res;
            }
    	}
        return Util.GETLIST_SUCCESS;
    }
}
