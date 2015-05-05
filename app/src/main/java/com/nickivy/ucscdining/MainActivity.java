package com.nickivy.ucscdining;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.nickivy.ucscdining.parser.MenuParser;
import com.nickivy.ucscdining.util.Util;

/**
 * App for viewing UCSC dining menus. Currently can 
 * read all menus, display them based on time, with special colors displayed 
 * for events such as College Nights, Healthy Mondays, or Farm Fridays.
 *
 * <p>TODO: tablet layout (display all 3 meals at once)
 *
 * @author Nick Ivy parkedraccoon@gmail.com
 */

public class MainActivity extends ActionBarActivity{

	private ListView mDrawerList;
	private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar mActionBar;
    private MealViewFragment fragment;
    
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
					R.layout.drawer_list_item, Util.collegeList));
			
	        mActionBar.setDisplayHomeAsUpEnabled(true);
	        mActionBar.setHomeButtonEnabled(true);	        
	        
	        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
	        		R.string.drawer_open, R.string.drawer_close);
	        
	        mDrawerLayout.setDrawerListener(mDrawerToggle);
	        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	        
	        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
	        fragment = new MealViewFragment();
	        Bundle args = new Bundle();
	        args.putInt(MealViewFragment.ARG_COLLEGE_NUMBER, currentCollege);
	        fragment.setArguments(args);
	        transaction.replace(R.id.fragment_container, fragment);
	        transaction.commit();
	        
/*	        if (savedInstanceState == null) {
//	            selectItem(0);
	        	fragment.selectItem(0);
	        }*/
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
        	fragment.selectItem(position);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.overflow_menu, menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       if (mDrawerToggle.onOptionsItemSelected(item)) {
           return true;
       }
       switch(item.getItemId()) {
       	case R.id.action_select_date:
       		DialogFragment newFragment = new DatePicker();
       	    newFragment.show(getSupportFragmentManager(), "datePicker");
        default:
           return super.onOptionsItemSelected(item);
       }
   }
    
    public static class DatePicker extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
   	
    	@Override
   		public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
           // Use the current date as the default date in the picker
           final Calendar c = Calendar.getInstance();
           int year = c.get(Calendar.YEAR);

           // Create a new instance of DatePickerDialog and return it
           // Default date to what is currently selected by user
           return new DatePickerDialog(getActivity(), this, year,
                   MealViewFragment.displayedMonth - 1, MealViewFragment.displayedDay);
    	}

		@Override
		public void onDateSet(android.widget.DatePicker view, int year,
				int monthOfYear, int dayOfMonth) {
			MealViewFragment meal = (MealViewFragment)
                    getFragmentManager().findFragmentById(R.id.fragment_container);
			meal.selectNewDate(monthOfYear + 1,  dayOfMonth,  year);
		}
   }

}
