package com.nickivy.ucscdining.parser;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.nickivy.ucscdining.util.CollegeMenu;
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

    private static final String dateURLPart1 = "http://nutrition.sa.ucsc.edu/menuSamp.asp?" +
            "myaction=read&sName=&dtdate=";
    
    private static final String[] datedURLListParts2 = {
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
    	Document doc;
    	Elements names = null;
    	try {
    		doc = Jsoup.connect(dateURLPart1 + month + "%2F" + day + "%2F" + year +
                    datedURLListParts2[k]).get();
	
			names = doc.select("td[valign=\"top\"]");
    	} catch (UnknownHostException e) {
            Log.v("ucscdining", "Internet connection missing");
            e.printStackTrace();
            return Util.GETLIST_INTERNET_FAILURE;
        } catch (IOException e) {
    		Log.w("ucscdining","Unable to download dining menu");
    		e.printStackTrace();
            return Util.GETLIST_OKHTTP_FAILURE;
    	}
	
		ArrayList<String> breakfastList = new ArrayList<String>(),
				lunchList = new ArrayList<String>(),
				dinnerList = new ArrayList<String>();
	
		//Catch if the dining hall is closed for that day
		if(names != null && names.size() > 0){
			for(int j = 0; j < names.size(); j++){
				//Breakfast
				if(names.get(j).text().contains("Breakfast")){
					Elements breakfast = names.get(j).select(" div[class=\"menusamprecipes\"]");
					for(int i = 0; i < breakfast.size(); i++){
						breakfastList.add(breakfast.get(i).text());
					}
				}
	
				//Lunch
				if(names.get(j).text().contains("Lunch")){
					Elements lunch = names.get(j).select(" div[class=\"menusamprecipes\"]");
					for(int i = 0; i < lunch.size(); i++){
						lunchList.add(lunch.get(i).text());
					}
				}
		
				//Dinner
				if(names.get(j).text().contains("Dinner")){
					Elements dinner = names.get(j).select(" div[class=\"menusamprecipes\"]");
					for(int i = 0; i < dinner.size(); i++){
						dinnerList.add(dinner.get(i).text());
					}
				}
			}
		}/*else{
			Log.w("ucscdining","array list empty!");
		}*/

		fullMenuObj.get(k).setBreakfast(breakfastList);
		fullMenuObj.get(k).setLunch(lunchList);
		fullMenuObj.get(k).setDinner(dinnerList);
		
		/*
		 *  If empty breakfast and not empty lunch or dinner, set brunch message
		 */
		if(fullMenuObj.get(k).getBreakfast().isEmpty() && 
				(!(fullMenuObj.get(k).getLunch().isEmpty()) ||
                        !(fullMenuObj.get(k).getDinner().isEmpty()))){
			ArrayList<String> breakfastMessage = new ArrayList<String>();
			breakfastMessage.add(Util.brunchMessage);
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
