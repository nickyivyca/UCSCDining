package com.nickivy.ucscdining.util;

import java.util.ArrayList;

import android.util.Log;

/**
 * College menu object. Keeps things organized,
 * as I would end up using 3-dimensional arraylists otherwise.
 *
 * @author Nick Ivy parkedraccoon@gmail.com
 */

public class CollegeMenu {
	
	private ArrayList<String> mBreakfast = new ArrayList<String>();
	private ArrayList<String> mLunch = new ArrayList<String>();
	private ArrayList<String> mDinner = new ArrayList<String>();
	
	private boolean isSet = false;
	
	public CollegeMenu(){}
	
	public CollegeMenu(ArrayList<String> breakfast, ArrayList<String> lunch, ArrayList<String> dinner){
		mBreakfast = breakfast;
		mLunch = lunch;
		mDinner = dinner;
	}
	
	public void setBreakfast(ArrayList<String> meal){
		mBreakfast = meal;
		isSet = true;
	}
	
	public void setLunch(ArrayList<String> meal){
		mLunch = meal;
		isSet = true;
	}
	
	public void setDinner(ArrayList<String> meal){
		mDinner = meal;
		isSet = true;
	}
	
	public ArrayList<String> getBreakfast(){
		if(mBreakfast != null && mBreakfast.size() > 0){
			return mBreakfast;
		}
		ArrayList<String> ret = new ArrayList<String>();
		return ret;
	}
	
	public ArrayList<String> getLunch(){
		if(mLunch != null && mLunch.size() > 0){
			return mLunch;
		}
		ArrayList<String> ret = new ArrayList<String>();
		return ret;
	}
	
	public ArrayList<String> getDinner(){
		if(mDinner != null && mDinner.size() > 0){
			return mDinner;
		}
		ArrayList<String> ret = new ArrayList<String>();
		return ret;
	}
	
	/**
	 * Get if data has been written to the 
	 * object at all
	 * @return
	 */
	public boolean getIsSet(){
		return isSet;
	}
	
	
	/**
	 * Get if the dining hall is open
	 * 
	 * <p>At this point assume open if nothing has been loaded
	 * @return
	 */
	public boolean getIsOpen(){
        //Log.v("ucscdining", "checking open: " getLunch)
    	if(isSet) {
            if (!getBreakfast().isEmpty() && !getLunch().isEmpty() && !getDinner().isEmpty()) {
                return true;
            }
    	}
    	return false;
	}
	
	/**
	 * If the first entry in Dinner is
	 * "College Night" then it's a college night	
	 * @return
	 */
	public boolean getIsCollegeNight(){
    	if(isSet && getDinner().size() > 0 &&
                getDinner().get(0).toLowerCase().contains("college night")) {
    		return true;
    	}
    	return false;
	}
	
	/**
	 * @return If first entry in dinner or lunch is "Healthy Mondays"
	 */
	public boolean getIsHealthyMonday(){
    	if(isSet){
    		if((getDinner().size() > 0 && getDinner().get(0).equals("Healthy Mondays")) || 
    			getLunch().size() > 0 && getLunch().get(0).equals("Healthy Mondays")){
    			return true;
    		}
    	}
    	return false;		
	}

	/**
	 * @return If first entry in dinner or lunch is "Farm Fridays"
	 */
	public boolean getIsFarmFriday(){
    	if(isSet){
    		if((getDinner().size() > 0 && getDinner().get(0).equals("Farm Fridays")) || 
    			getLunch().size() > 0 && getLunch().get(0).equals("Farm Fridays")){    			
    			return true;
    		}
    	}
    	return false;		
	}
}
