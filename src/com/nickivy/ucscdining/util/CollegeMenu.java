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
	
	/*
	 *  Despite being available via get methods, those do null
	 *  checking, sometimes we want the raw data
	 */
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

}
