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
 * <p>Work in progress! Currently only reads Cowell menus.
 * 
 * TODO: read menu specified by call from selectItem
 * 
 * 
 * @author Nick Ivy parkedraccoon@gmail.com
 *
 */

public class MenuParser {
	
    public static final String[] cowellDummyMenu = {
        "Hamburgers",
        "Hotdogs",
        "Pancakes"
    };
    
    public static final String[] collegeList = {
    	"Cowell/Stevenson",
    	"9/10",
    	"8/Oakes",
    	"Porter/Kresge",
    	"Crown/Merrill"
    };
    
    private static final String cowellURL = "http://nutrition.sa.ucsc.edu/"
    		+ "menuSamp.asp?locationNum=05&locationName=Cowell&sName=&naFlag=";
    
    public static ArrayList<String> cowellBreakfast = new ArrayList<String>();
    public static ArrayList<String> cowellLunch = new ArrayList<String>();
    public static ArrayList<String> cowellDinner = new ArrayList<String>();
    public static ArrayList<String> cowellMenu = new ArrayList<String>();

    
    public static void getCowellMealList(){
    	try {
			Document doc = Jsoup.connect(cowellURL).get();
	    	
	    	Elements names = doc.select("td[valign=\"top\"][width=50%]");
	    	
	    	cowellBreakfast.clear();
	    	cowellLunch.clear();
	    	cowellDinner.clear();
	    	
	    	
	    	for(int j = 0; j < names.size(); j++){
		    	//Breakfast
	    		if(names.get(j).text().contains("Breakfast")){
	    			Elements breakfast = names.get(j).select(" div[class=\"menusamprecipes\"]");
	    			cowellBreakfast.add("Breakfast: ");
	    			for(int i = 0; i < breakfast.size(); i++){
		    			cowellBreakfast.add(breakfast.get(i).text());
	    			}
	    			cowellBreakfast.add(""); //blank space for gap
	    		}
	    	
	    		//Lunch
	    		if(names.get(j).text().contains("Lunch")){
	    			Elements lunch = names.get(j).select(" div[class=\"menusamprecipes\"]");
	    			cowellLunch.add("Lunch: ");
	    			for(int i = 0; i < lunch.size(); i++){
		    			cowellLunch.add(lunch.get(i).text());
	    			}
	    			cowellLunch.add(""); //blank space for gap
	    		}
	    		
	    		//Dinner
	    		if(names.get(j).text().contains("Dinner")){
	    			Elements dinner = names.get(j).select(" div[class=\"menusamprecipes\"]");
	    			cowellDinner.add("Dinner: ");
	    			for(int i = 0; i < dinner.size(); i++){
	    				cowellDinner.add(dinner.get(i).text());
	    			}
	    		}
	    	}
	    	
	    	cowellMenu.addAll(cowellBreakfast);
	    	cowellMenu.addAll(cowellLunch);
	    	cowellMenu.addAll(cowellDinner);
	    	
		} catch (IOException e) {
			Log.w("ucscdining","Unable to download dining menu");
			e.printStackTrace();
		}
    }
}
