package com.nickivy.ucscdining;

import java.util.ArrayList;

import com.example.android.common.view.SlidingTabLayout;
import com.nickivy.ucscdining.parser.MenuParser;

import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Fragment for displaying one meal. Uses sliding tab
 * layout from Google's examples.
 * 
 * <p>TODO: fun colors for events such as college nights and Healthy Mondays
 *
 * @author Nick Ivy parkedraccoon@gmail.com
 */

public class MealViewFragment extends ListFragment{
	
	final static String ARG_COLLEGE_NUMBER = "college_number";
	
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private ViewPager mViewPager;
	private SlidingTabLayout mSlidingTabLayout;
	
	private int collegeNum = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		collegeNum = getArguments().getInt(ARG_COLLEGE_NUMBER);
        getActivity().setTitle(MenuParser.collegeList[collegeNum]);

    	collegeNum = getArguments().getInt(ARG_COLLEGE_NUMBER);
    	
		return inflater.inflate(R.layout.pager_fragment, container, false);
	}
	
	public void onViewCreated(View view, Bundle savedInstanceState){
		mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new MenuAdapter());
		mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
		mSlidingTabLayout.setViewPager(mViewPager);
	}
	
	

	
	private class RetrieveMenuTask extends AsyncTask<Integer, Integer, Long>{
		
		private int college;
		
		@Override
		protected void onPreExecute(){
			MenuParser.needsRefresh = false;
		}

		@Override
		protected Long doInBackground(Integer... arg0) {			
			college = arg0[0];
			MenuParser.getMealList();			
			
			return null;
		}
		
		protected void onPostExecute(Long result){
			/*
			 * only 2 views exist at a time, so the third returns null, but
			 * we don't know which one it is, so check each one. try catch
			 * would work but is performance-inefficient
			 */
			MenuParser.needsRefresh= false;			
			ListView listView = (ListView) getActivity().findViewById(12);
			if(listView != null){
				listView.setAdapter(new ArrayAdapter<String>(getActivity(),
						android.R.layout.simple_list_item_activated_1,
						MenuParser.fullMenuObj.get(college).getBreakfast()));
			}
			listView = (ListView) getActivity().findViewById(13);
			if(listView != null){
				listView.setAdapter(new ArrayAdapter<String>(getActivity(),
						android.R.layout.simple_list_item_activated_1,
						MenuParser.fullMenuObj.get(college).getLunch()));
			}
			listView = (ListView) getActivity().findViewById(14);
			if(listView != null){
				listView.setAdapter(new ArrayAdapter<String>(getActivity(),
						android.R.layout.simple_list_item_activated_1,
						MenuParser.fullMenuObj.get(college).getDinner()));
			}
			Display display = getActivity().getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			int height = size.y;
			/*
			 *  Manually try to recreate what the swiperefresh layout has by default.
			 *  Same deal as above with nulls.
			 */
			mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(15);
			if(mSwipeRefreshLayout != null){
				mSwipeRefreshLayout.setProgressViewOffset(false, -100, height / 40);
				mSwipeRefreshLayout.setRefreshing(false);
			}
			mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(16);
			if(mSwipeRefreshLayout != null){
				mSwipeRefreshLayout.setProgressViewOffset(false, -100, height / 40);
				mSwipeRefreshLayout.setRefreshing(false);
			}
			mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(17);
			if(mSwipeRefreshLayout != null){
				mSwipeRefreshLayout.setProgressViewOffset(false, -100, height / 40);
				mSwipeRefreshLayout.setRefreshing(false);
			}
		}
		
	}
	
	class MenuAdapter extends PagerAdapter {
		
        @Override
        public int getCount() {
            return 3;
        }
        
        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }
        
        @Override
        public CharSequence getPageTitle(int position) {
            return MenuParser.meals[position];
        }
        
        @Override
        public Object instantiateItem(ViewGroup container, int mealnum) {
            // Inflate a new layout from our resources
            View view = getActivity().getLayoutInflater().inflate(R.layout.meal,
                    container, false);
        	container.addView(view, mealnum);
        	
        	ListView mealList = (ListView) view.findViewById(android.R.id.list);
        	/*
        	 * set ID - add 12 in case 0 is something
        	 * This is to reference it later for setting
        	 * its contents
        	 */
        	mealList.setId(mealnum + 12);
        	
            mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.meal_refresh_layout);
            //Add 15 instead of 12 for swipelayout
            mSwipeRefreshLayout.setId(mealnum + 15);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
            		new RetrieveMenuTask().execute(collegeNum);
            	}
            });
            
            /*
             * Only refresh if actually necessary
             * I think I have more than enough checks for this,
             * but I might as well keep them.
             * 
             * If the menu has not been populated yet, load it,
             * otherwise, there's no need to redownload.
             * 
             * Load circle *should* be shown, but currently there's
             * no proper way to manually trigger the reload animation. So 
             * we're stuck doing it in a hacky way.
             */
    		if(MenuParser.needsRefresh){
    	        	Display display = getActivity().getWindowManager().getDefaultDisplay();
    	            Point size = new Point();
    	            display.getSize(size);
    	            int height = size.y;
    	            // manually try to recreate where the spinner ends up in a normal swipe
    	            mSwipeRefreshLayout.setProgressViewOffset(false, -50, height / 800);
    				mSwipeRefreshLayout.setRefreshing(true);
    	        	new RetrieveMenuTask().execute(collegeNum);
    	    }
    		ArrayList<String> testedArray = new ArrayList<String>();
        	switch(mealnum){
        	case 0:
        		testedArray = MenuParser.fullMenuObj.get(collegeNum).getBreakfast();
        		if (testedArray != null){
        			mealList.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
        					MenuParser.fullMenuObj.get(collegeNum).getBreakfast()));
        		}
        		break;
        	case 1:
        		testedArray = MenuParser.fullMenuObj.get(collegeNum).getLunch();
        		if (testedArray != null){
        			mealList.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
        					MenuParser.fullMenuObj.get(collegeNum).getLunch()));
        		}
        		break;
        	case 2:
        		testedArray = MenuParser.fullMenuObj.get(collegeNum).getDinner();
        		if (testedArray != null){
        			mealList.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
        				MenuParser.fullMenuObj.get(collegeNum).getDinner()));
        		}
        		break;
        	default:
        		Log.v("ucscdining","We have a problem");
        	}
            return view;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }
    
}
