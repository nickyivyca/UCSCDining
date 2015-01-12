package com.nickivy.ucscdining.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.util.Log;

/**
 * Parses the incoming file. Separate methods return
 * certain things, such as the contents of each meal
 * or if there is a special event, such as a College Night
 * or Farm Friday/Healthy Monday.
 * 
 * Currently does read menu based on selectItem number - 
 * however it will need to be modified to eventually store the data
 * instead of having to download it each time, as well as returning
 * each meal separately. 
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
    
    private static final String[] URLList = {
    	"http://nutrition.sa.ucsc.edu/menuSamp.asp?locationNum=05&locationName=Cowell&sName=&naFlag=",
    	"http://nutrition.sa.ucsc.edu/menuSamp.asp?locationNum=20&locationName=Crown+Merrill&sName=&naFlag=",
    	"http://nutrition.sa.ucsc.edu/menuSamp.asp?locationNum=25&locationName=Porter&sName=&naFlag=",
    	"http://nutrition.sa.ucsc.edu/menuSamp.asp?locationNum=30&locationName=College+Eight&sName=&naFlag=",
    	"http://nutrition.sa.ucsc.edu/menuSamp.asp?locationNum=40&locationName=College+Nine+%26+Ten&sName=&naFlag="
    };
    
    public static ArrayList<String> cowellMenu = new ArrayList<String>();
    
    public static ArrayList<String> menu = new ArrayList<String>();
    
    public static ArrayList<ArrayList<String>> fullMenu = new ArrayList<ArrayList<String>>();
    
    public static void getFullMealList(){
    	try{
			fullMenu.clear();
    		for(int k = 0; k < collegeList.length; k++){
    			Document doc = Jsoup.connect(URLList[k]).get();
	
    			Elements names = doc.select("td[valign=\"top\"][width=50%]");
	
    			ArrayList<String> breakfastList = new ArrayList<String>(),
    					lunchList = new ArrayList<String>(),
    					dinnerList = new ArrayList<String>();
	
    			//Catch if the dining hall is closed for that day
    			if(names.size() > 0){
    				Log.v("ucscdining",collegeList[k] + names.get(0).text());
    				for(int j = 0; j < names.size(); j++){
    					//Breakfast
    					if(names.get(j).text().contains("Breakfast")){
    						Elements breakfast = names.get(j).select(" div[class=\"menusamprecipes\"]");
    						breakfastList.add("Breakfast: ");
    						for(int i = 0; i < breakfast.size(); i++){
    							breakfastList.add(breakfast.get(i).text());
    						}
    						breakfastList.add(""); //blank space for gap
    					}
	
    					//Lunch
    					if(names.get(j).text().contains("Lunch")){
    						Elements lunch = names.get(j).select(" div[class=\"menusamprecipes\"]");
    						lunchList.add("Lunch: ");
    						for(int i = 0; i < lunch.size(); i++){
    							lunchList.add(lunch.get(i).text());
    						}
    						lunchList.add(""); //blank space for gap
    					}
		
    					//Dinner
    					if(names.get(j).text().contains("Dinner")){
    						Elements dinner = names.get(j).select(" div[class=\"menusamprecipes\"]");
    						dinnerList.add("Dinner: ");
    						for(int i = 0; i < dinner.size(); i++){
    							dinnerList.add(dinner.get(i).text());
    						}
    					}
    				}
    			}
    			
    			fullMenu.add(new ArrayList<String>());
	
    			fullMenu.get(k).addAll(breakfastList);
    			fullMenu.get(k).addAll(lunchList);
    			fullMenu.get(k).addAll(dinnerList);
    		}
    	
    	} catch (IOException e) {
    		Log.w("ucscdining","Unable to download dining menu");
    		e.printStackTrace();
    	}
	}
}
