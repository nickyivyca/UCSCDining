package com.nickivy.ucscdining.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.nickivy.ucscdining.R;
import com.nickivy.ucscdining.util.CollegeMenu;

import android.util.Log;

/**
 * Parses the incoming file.
 * 
 * <p>Data is stored in the static fullMenu arraylist of 
 * CollegeMenu objects. 
 * 
 * @author Nick Ivy parkedraccoon@gmail.com
 *
 */

public class MenuParser {
    
    public static final String[] collegeList = {
    	"Cowell/Stevenson",
    	"Crown/Merrill",
    	"Porter/Kresge",
    	"Eight/Oakes",
    	"Nine/Ten"
    };
    
    public static final String[] meals = {
    	"Breakfast",
    	"Lunch",
    	"Dinner"
    };
    
    public static final String[] URLList = {
    	"http://nutrition.sa.ucsc.edu/menuSamp.asp?locationNum=05&locationName=Cowell&sName=&naFlag=",
    	"http://nutrition.sa.ucsc.edu/menuSamp.asp?locationNum=20&locationName=Crown+Merrill&sName=&naFlag=",
    	"http://nutrition.sa.ucsc.edu/menuSamp.asp?locationNum=25&locationName=Porter&sName=&naFlag=",
    	"http://nutrition.sa.ucsc.edu/menuSamp.asp?locationNum=30&locationName=College+Eight&sName=&naFlag=",
    	"http://nutrition.sa.ucsc.edu/menuSamp.asp?locationNum=40&locationName=College+Nine+%26+Ten&sName=&naFlag="
    };

    
    public static final String dateURLPart1 = "http://nutrition.sa.ucsc.edu/menuSamp.asp?myaction=read&sName=&dtdate=";
    
    public static final String[] datedURLListParts2 = {
    	"&locationNum=05&locationName=%20Cowell&naFlag=1",
    	"&locationNum=20&locationName=Crown+Merrill&sName=&naFlag=1",
    	"&locationNum=25&locationName=Porter&sName=&naFlag=1",
    	"&locationNum=30&locationName=College+Eight&sName=&naFlag=1",
    	"&locationNum=40&locationName=College+Nine+%26+Ten&sName=&naFlag=1"
    };
    
    //4%2F24%2F2015
    		
//    public static final String cowellDateURLPart2 = "&locationNum=05&locationName=%20Cowell&naFlag=1";
    
    public static final String brunchMessage = "See lunch for today\'s brunch menu";
    
    public static boolean needsRefresh = false;
    
    private final static int maxReloads = 3;
    private static int reloadTries = 0;
    private static boolean failed = false;
    
    public static ArrayList<CollegeMenu> fullMenuObj = new ArrayList<CollegeMenu>(){{
    	add(new CollegeMenu());
    	add(new CollegeMenu());
    	add(new CollegeMenu());
    	add(new CollegeMenu());
    	add(new CollegeMenu());
    }};
    
    public static void getSingleMealList(int k, int month, int day, int year){
    	Document doc = null;
    	Elements names = null;
    	try{
    		reloadTries++;
    		doc = Jsoup.connect(dateURLPart1 + month + "%2F" + day + "%2F" + year + datedURLListParts2[k]).get();
    		
    		
//    		Log.v("ucscdining", "Reloadtries: " + reloadTries);
	
			names = doc.select("td[valign=\"top\"]");
    	} catch (IOException e) {
    		Log.w("ucscdining","Unable to download dining menu");
//    		Log.v("ucscdining", "Reloadtries: " + reloadTries);
    		//needsRefresh = true;
    		e.printStackTrace();
    		failed = true;
    	}
		
		/*
		 * In order to defeat an okhttp error, recurse
		 */
		if(failed && (reloadTries < maxReloads)){
			getSingleMealList(k, month, day, year);
		}
		if (reloadTries >= maxReloads) {
			return;
		}
	
		ArrayList<String> breakfastList = new ArrayList<String>(),
				lunchList = new ArrayList<String>(),
				dinnerList = new ArrayList<String>();
	
		//Catch if the dining hall is closed for that day
		if(names.size() > 0){
//			Log.v("ucscdining",collegeList[k] + names.get(0).text());
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
		}else{
			Log.w("ucscdining","array list empty!");
		}

		fullMenuObj.get(k).setBreakfast(breakfastList);
		fullMenuObj.get(k).setLunch(lunchList);
		fullMenuObj.get(k).setDinner(dinnerList);
		
		/*
		 *  If empty breakfast and not empty lunch or dinner, set brunch message
		 */
		if(fullMenuObj.get(k).getBreakfast().isEmpty() && 
				(!(fullMenuObj.get(k).getLunch().isEmpty()) || !(fullMenuObj.get(k).getDinner().isEmpty()))){
			ArrayList<String> breakfastMessage = new ArrayList<String>();
			breakfastMessage.add(brunchMessage);
			fullMenuObj.get(k).setBreakfast(breakfastMessage);
		}

	}
    
/*    public static void getMealList() {
    	for(int i = 0; i < 5; i++){
    		reloadTries = 0;
    		failed = false;
    		getSingleMealList(i);
    	}
    }*/
    
    /**
     * Puts downloaded data from specified date (instead of today) into the full menu object.
     * @param day
     * @param month
     * @param year
     */
    public static void getMealList(int month, int day, int year) {
    	for(int i = 0; i < 5; i++){
    		reloadTries = 0;
    		failed = false;
    		getSingleMealList(i, month, day, year);
    	}
    }
}
