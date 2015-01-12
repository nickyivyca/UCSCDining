package com.nickivy.ucscdining;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nickivy.ucscdining.R;
import com.nickivy.ucscdining.MealViewFragment;
import com.nickivy.ucscdining.parser.MenuParser;

/**
 * App for viewing UCSC dining menus. Eventually will be able to 
 * read all menus, display them based on time, with special colors displayed 
 * for events such as College Nights, Healthy Mondays, or Farm Fridays.
 * Additionally, will allow the user to fast forward to see planned menus for
 * days in the future.
 * 
 * <p>Currently just displays the whole menu at once, with extra entries to 
 * denote the separation between each meal.
 * 
 * TODO: Display each meal separately
 * TODO: smart refresh
 * 
 * 
 *
 * @author Nick Ivy parkedraccoon@gmail.com
 */

public class MainActivity extends ActionBarActivity{
	
	private ListView mDrawerList;
	private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainview);
		
//		if(findViewById(R.id.fragment_container) != null) {
			
/*			if (savedInstanceState != null) {
				return;
			}*/
			
			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	        mDrawerList = (ListView) findViewById(R.id.left_drawer);

	        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
			mDrawerList.setAdapter(new ArrayAdapter<String>(this,
					R.layout.drawer_list_item, MenuParser.collegeList));
			
	        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	        getSupportActionBar().setHomeButtonEnabled(true);
	        
	        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
	        		R.string.drawer_open, R.string.drawer_close);
	        
	        mDrawerLayout.setDrawerListener(mDrawerToggle);
	        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	        
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
        // update the main content by replacing fragments
        Fragment fragment = new MealViewFragment();
        Bundle args = new Bundle();
        args.putInt(MealViewFragment.ARG_COLLEGE_NUMBER, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

}
