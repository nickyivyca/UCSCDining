package com.nickivy.ucscdining.util;

import java.util.ArrayList;

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
		return new ArrayList<String>();
	}
	
	public ArrayList<String> getLunch(){
		if(mLunch != null && mLunch.size() > 0){
			return mLunch;
		}
		return new ArrayList<String>();
	}
	
	public ArrayList<String> getDinner(){
		if(mDinner != null && mDinner.size() > 0){
			return mDinner;
		}
		return new ArrayList<String>();
	}
	
	/**
	 * Get if data has been written to the 
	 * object at all
	 */
	public boolean getIsSet(){
		return isSet;
	}
	
	
	/**
	 * Get if the dining hall is open
	 * 
	 * <p>At this point assume open if nothing has been loaded
	 */
	public boolean getIsOpen(){
    	if(isSet) {
            if (!getBreakfast().isEmpty() && !getLunch().isEmpty() && !getDinner().isEmpty()) {
                return true;
            }
    	}
    	return false;
	}
	
	/**
	 * @return If the first entry in Dinner contains "college night" (case insensitive) then it's
     * college night
	 */
	public boolean getIsCollegeNight(){
        return isSet && getDinner().size() > 0 &&
                getDinner().get(0).toLowerCase().contains("college night");
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
