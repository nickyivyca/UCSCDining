package com.nickivy.ucscdining.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.nickivy.ucscdining.util.CollegeMenu;

import android.util.Log;

/**
 * Parses the incoming file. Separate methods will return
 * certain things, such as the contents of each meal
 * or if there is a special event, such as a College Night
 * or Farm Friday/Healthy Monday.
 * 
 * Data is stored in the static fullMenu arraylist of 
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
    
    public static boolean needsRefresh = true;
    
    public static ArrayList<CollegeMenu> fullMenuObj = new ArrayList<CollegeMenu>(){{
    	add(new CollegeMenu());
    	add(new CollegeMenu());
    	add(new CollegeMenu());
    	add(new CollegeMenu());
    	add(new CollegeMenu());
    }};
    
    public static void getSingleMealList(int k){
    	Document doc = null;
    	Elements names = null;
    	try{
    		doc = Jsoup.connect(URLList[k]).get();
	
			names = doc.select("td[valign=\"top\"]");
    	} catch (IOException e) {
    		Log.w("ucscdining","Unable to download dining menu");
    		//needsRefresh = true;
    		e.printStackTrace();
    		
    		/*
    		 * In order to defeat an okhttp error, recurse
    		 * TODO: Probably should only let this happen a limited amount of times
    		 */
    		getSingleMealList(k);
    	}
	
			ArrayList<String> breakfastList = new ArrayList<String>(),
					lunchList = new ArrayList<String>(),
					dinnerList = new ArrayList<String>();
	
			//Catch if the dining hall is closed for that day
			if(names.size() > 0){
//				Log.v("ucscdining",collegeList[k] + names.get(0).text());
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
    	

	}
    
    public static void getMealList() {
    	for(int i = 0; i < 5; i++){
    		getSingleMealList(i);
    	}
    }
}
