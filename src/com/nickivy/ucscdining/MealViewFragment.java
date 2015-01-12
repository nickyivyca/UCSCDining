package com.nickivy.ucscdining;

import com.nickivy.ucscdining.parser.MenuParser;

import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;

/**
 * Fragment for displaying one meal. Currently just a simple
 * ListView with the Asynctask for fetching the menu.
 *
 * @author Nick Ivy parkedraccoon@gmail.com
 */

public class MealViewFragment extends ListFragment{
	
	final static String ARG_COLLEGE_NUMBER = "college_number";
	
	//TODO later this will be set by the clock
	private boolean needsRefresh = false;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.meal, container, false);
        int i = getArguments().getInt(ARG_COLLEGE_NUMBER);
        String college = MenuParser.collegeList[i];
        
        /*
         * If the menu has not been populated yet, load it
         * otherwise, there's no need to redownload
         */
        if(MenuParser.fullMenu.size() == 0){
        		new RetrieveMenuTask().execute(i);
        }else{
        	setListAdapter(new ArrayAdapter<String>(getActivity(),
        			android.R.layout.simple_list_item_activated_1,
        			MenuParser.fullMenu.get(i)));
        }
        
        getActivity().setTitle(college);
        return rootView;
	}
	
	public void onStart() {
		super.onStart();
	}
	
	private class RetrieveMenuTask extends AsyncTask<Integer, Integer, Long>{
		
		private int college;

		@Override
		protected Long doInBackground(Integer... arg0) {
			
			college = arg0[0];
			
			if(!needsRefresh){
//				MenuParser.getCowellMealList();
//				MenuParser.getMealList(arg0[0]);
				MenuParser.getFullMealList();
				
			}
			
			return null;
		}
		
		protected void onPostExecute(Long result){
/*	        setListAdapter(new ArrayAdapter<String>(getActivity(),
	        		android.R.layout.simple_list_item_activated_1,
	        		MenuParser.cowellMenu));*/
/*	        setListAdapter(new ArrayAdapter<String>(getActivity(),
	        		android.R.layout.simple_list_item_activated_1,
	        		MenuParser.menu));*/
			setListAdapter(new ArrayAdapter<String>(getActivity(),
    		android.R.layout.simple_list_item_activated_1,
    		MenuParser.fullMenu.get(college)));
		}
		
	}

}
