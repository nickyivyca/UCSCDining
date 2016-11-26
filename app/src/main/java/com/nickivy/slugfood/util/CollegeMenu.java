package com.nickivy.slugfood.util;

import java.util.ArrayList;

/**
 * College menu object. Keeps things organized,
 * as I would end up using 3-dimensional arraylists otherwise.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author Nicky Ivy parkedraccoon@gmail.com
 */

public class CollegeMenu {
	
	private ArrayList<MenuItem> mBreakfast = new ArrayList<MenuItem>();
	private ArrayList<MenuItem> mLunch = new ArrayList<MenuItem>();
	private ArrayList<MenuItem> mDinner = new ArrayList<MenuItem>();
	private ArrayList<MenuItem> mLateNight = new ArrayList<MenuItem>();

	private boolean isSet = false;
	
	public CollegeMenu(){}
	
	public CollegeMenu(ArrayList<MenuItem> breakfast, ArrayList<MenuItem> lunch, ArrayList<MenuItem> dinner){
		mBreakfast = breakfast;
		mLunch = lunch;
		mDinner = dinner;
	}
	
	public void setBreakfast(ArrayList<MenuItem> meal){
		mBreakfast = meal;
		isSet = true;
	}
	
	public void setLunch(ArrayList<MenuItem> meal){
		mLunch = meal;
		isSet = true;
	}

	public void setDinner(ArrayList<MenuItem> meal){
		mDinner = meal;
		isSet = true;
	}

	public void setLateNight(ArrayList<MenuItem> meal){
		mLateNight = meal;
		isSet = true;
	}
	
	public ArrayList<MenuItem> getBreakfast(){
		if(mBreakfast != null && mBreakfast.size() > 0){
			return mBreakfast;
		}
		return new ArrayList<MenuItem>();
	}
	
	public ArrayList<MenuItem> getLunch(){
		if(mLunch != null && mLunch.size() > 0){
			return mLunch;
		}
		return new ArrayList<MenuItem>();
	}

	public ArrayList<MenuItem> getDinner(){
		if(mDinner != null && mDinner.size() > 0){
			return mDinner;
		}
		return new ArrayList<MenuItem>();
	}

	public ArrayList<MenuItem> getLateNight(){
		if(mLateNight != null && mLateNight.size() > 0){
			return mLateNight;
		}
		return new ArrayList<MenuItem>();
	}

    /**
     * @return Titles of meals as arraylist of strings
     */
    public ArrayList<String> getBreakfastList(){
        ArrayList<String> ret = new ArrayList<String>();
        if(mBreakfast != null && mBreakfast.size() > 0){
            for (MenuItem m : mBreakfast) {
                ret.add(m.getItemName());
            }
        }
        return ret;
    }

    /**
     * @return Titles of meals as arraylist of strings
     */
    public ArrayList<String> getLunchList(){
        ArrayList<String> ret = new ArrayList<String>();
        if(mLunch != null && mLunch.size() > 0){
            for (MenuItem m : mLunch) {
                ret.add(m.getItemName());
            }
        }
        return ret;
    }

	/**
	 * @return Titles of meals as arraylist of strings
	 */
	public ArrayList<String> getDinnerList(){
		ArrayList<String> ret = new ArrayList<String>();
		if(mDinner != null && mDinner.size() > 0){
			for (MenuItem m : mDinner) {
				ret.add(m.getItemName());
			}
		}
		return ret;
	}

	/**
	 * @return Titles of meals as arraylist of strings
	 */
	public ArrayList<String> getLateNightList(){
		ArrayList<String> ret = new ArrayList<String>();
		if(mLateNight != null && mLateNight.size() > 0){
			for (MenuItem m : mLateNight) {
				ret.add(m.getItemName());
			}
		}
		return ret;
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
            if (!getBreakfast().isEmpty() || !getLunch().isEmpty() || !getDinner().isEmpty()) {
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
                getDinner().get(0).getItemName().toLowerCase().contains("college night");
    }
	
	/**
	 * @return If first entry in dinner or lunch is "Healthy Mondays"
	 */
	public boolean getIsHealthyMonday(){
    	if(isSet){
			for(MenuItem item : getDinner()) {
                if (item.getItemName().equals("Healthy Mondays")) {
                    return true;
                }
            }
            for(MenuItem item : getLunch()) {
                if (item.getItemName().equals("Healthy Mondays")) {
                    return true;
                }
            }
    	}
    	return false;		
	}

	/**
	 * @return If first entry in dinner or lunch is "Farm Fridays"
	 */
	public boolean getIsFarmFriday(){
    	if(isSet){
    		if((getDinner().size() > 0 && getDinner().get(0).getItemName().equals("Farm Fridays")) ||
    			getLunch().size() > 0 && getLunch().get(0).getItemName().equals("Farm Fridays")){
    			return true;
    		}
    	}
    	return false;		
	}
}
