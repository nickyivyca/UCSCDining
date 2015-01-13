package com.nickivy.ucscdining;

import com.nickivy.ucscdining.parser.MenuParser;

import android.app.Activity;
import android.app.ListFragment;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
//import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

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
	
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private ListView mListView;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.meal, container, false);
        int i = getArguments().getInt(ARG_COLLEGE_NUMBER);
        String college = MenuParser.collegeList[i];
        
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.meal_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
        		new RetrieveMenuTask().execute(0);
        	}
        });

        mListView = (ListView) rootView.findViewById(android.R.id.list);
        /*
         * If the menu has not been populated yet, load it,
         * otherwise, there's no need to redownload.
         * 
         * Load circle *should* be shown, but currently there's
         * no proper way to manually trigger the reload animation. So 
         * we're stuck doing it in a hacky way.
         */
        if(MenuParser.fullMenu.size() == 0){
//			mSwipeRefreshLayout.setRefreshing(false);
        	Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int height = size.y;
            // manually try to recreate where the spinner ends up in a normal swipe
            mSwipeRefreshLayout.setProgressViewOffset(false, -50, height / 800);
        	new RetrieveMenuTask().execute(0);
        }else{
        	mListView.setAdapter(new ArrayAdapter<String>(getActivity(),
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
		protected void onPreExecute(){
			mSwipeRefreshLayout.setRefreshing(true);
		}

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
			mListView.setAdapter(new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_activated_1,
					MenuParser.fullMenu.get(college)));
			mSwipeRefreshLayout.setRefreshing(false);
        	Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int height = size.y;
            // manually try to recreate what the swiperefresh layout has by default
            mSwipeRefreshLayout.setProgressViewOffset(false, -100, height / 40);
		}
		
	}

}
