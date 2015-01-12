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
		
        new RetrieveMenuTask().execute(0);
        
        setListAdapter(new ArrayAdapter<String>(getActivity(),
        		android.R.layout.simple_list_item_activated_1,
        		MenuParser.cowellMenu));
        
        getActivity().setTitle(college);
        return rootView;
	}
	
	public void onStart() {
		super.onStart();
	}
	
	private class RetrieveMenuTask extends AsyncTask<Integer, Integer, Long>{

		@Override
		protected Long doInBackground(Integer... arg0) {
			
			if(!needsRefresh){
				MenuParser.getCowellMealList();
			}
			
			return null;
		}
		
		protected void onPostExecute(Long result){
	        setListAdapter(new ArrayAdapter<String>(getActivity(),
	        		android.R.layout.simple_list_item_activated_1,
	        		MenuParser.cowellMenu));
		}
		
	}

}
