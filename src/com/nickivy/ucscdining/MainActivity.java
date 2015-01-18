package com.nickivy.ucscdining;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.nickivy.ucscdining.R;
import com.nickivy.ucscdining.MealViewFragment;
import com.nickivy.ucscdining.parser.MenuParser;
import com.nickivy.ucscdining.util.CollegeMenu;

/**
 * App for viewing UCSC dining menus. Eventually will be able to 
 * read all menus, display them based on time, with special colors displayed 
 * for events such as College Nights, Healthy Mondays, or Farm Fridays.
 * Additionally, will eventually allow the user to fast forward to see planned menus for
 * days in the future.
 * 
 * <p>Currently just displays the whole menu at once, with extra entries to 
 * denote the separation between each meal.
 * 
 * TODO: smart refresh
 * TODO: tablet layout (display all 3 meals at once)
 *
 * @author Nick Ivy parkedraccoon@gmail.com
 */

public class MainActivity extends ActionBarActivity{
	
	static final int ITEMS = 3;
	
	private ListView mDrawerList;
	private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar mActionBar;
    
    private static int currentCollege = 0;
	
    @Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.mainview);
		
//		if(findViewById(R.id.fragment_container) != null) {
			
/*			if (savedInstanceState != null) {
				return;
			}*/
			
			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	        mDrawerList = (ListView) findViewById(R.id.left_drawer);
	        mActionBar = getSupportActionBar();

	        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
			mDrawerList.setAdapter(new ArrayAdapter<String>(this,
					R.layout.drawer_list_item, MenuParser.collegeList));
			
	        mActionBar.setDisplayHomeAsUpEnabled(true);
	        mActionBar.setHomeButtonEnabled(true);
	        
	        
	        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
	        		R.string.drawer_open, R.string.drawer_close);
	        
	        mDrawerLayout.setDrawerListener(mDrawerToggle);
	        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	        
	        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
	        MealViewFragment fragment =  new MealViewFragment();
	        Bundle args = new Bundle();
	        args.putInt(MealViewFragment.ARG_COLLEGE_NUMBER, currentCollege);
	        fragment.setArguments(args);
	        transaction.replace(R.id.fragment_container, fragment);
	        transaction.commit();
	        
	        if (savedInstanceState == null) {
	            selectItem(0);
	        }
//		}
		
	}
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       if (mDrawerToggle.onOptionsItemSelected(item)) {
           return true;
       }
       return super.onOptionsItemSelected(item);
   }
    
    private void selectItem(int position) {
    	currentCollege = position;
        // update the main content by replacing fragments
    	if(getDiningHallOpen(position)){
    		Fragment fragment = new MealViewFragment();
    		Bundle args = new Bundle();
    		args.putInt(MealViewFragment.ARG_COLLEGE_NUMBER, currentCollege);
    		fragment.setArguments(args);

    		FragmentManager fragmentManager = getSupportFragmentManager();
    		fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();

    		// update selected item and title, then close the drawer
    		mDrawerList.setItemChecked(position, true);
    		mDrawerLayout.closeDrawer(mDrawerList);
    	}else{
    		Toast.makeText(this, MenuParser.collegeList[position] + " dining hall closed today!", Toast.LENGTH_SHORT).show();
    	}
    }
    
    /**
     * If the dining hall meal is not null, but contains nothing,
     * it is closed. If it's null, data hasn't been loaded yet.
     * 
     * @param college college number
     * @return
     */
    private boolean getDiningHallOpen(int college) {
    	/*
    	 *  If meal is null, collegemenu class will return dummyemptyreturn,
    	 *  which means no data has been set yet
    	 *  
    	 *  For some reason isEmpty() isn't working
    	 */
    	if(MenuParser.fullMenuObj.get(college).getIsSet()){
    		if(MenuParser.fullMenuObj.get(college).getBreakfast().isEmpty() &&
    				MenuParser.fullMenuObj.get(college).getLunch().isEmpty() &&
    				MenuParser.fullMenuObj.get(college).getDinner().isEmpty()){
    			return false;
    		}
    	}
    	return true;
    }

}
