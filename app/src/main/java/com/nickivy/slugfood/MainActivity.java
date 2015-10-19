package com.nickivy.slugfood;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.nickivy.slugfood.util.Util;

/**
 * App for viewing UCSC dining menus. Currently can
 * read all menus, display them based on time, with special colors displayed
 * for events such as College Nights, Healthy Mondays, or Farm Fridays. Also allows for
 * viewing days in the future and there is an automatically updating widget which displays the
 * same info. There are preferences for default college, and secondary college, which is used when
 * the default college is closed (only if it is open itself)
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author Nicky Ivy parkedraccoon@gmail.com
 */

public class MainActivity extends AppCompatActivity{

    private ListView mDrawerList;
    private ListView mAboutList;
    private RelativeLayout mDrawer;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar mActionBar;
    private MealViewFragment fragment;
    private MealViewFragmentLarge fragmentLarge;

    private int intentCollege = -1, intentMeal = -1, intentMonth = -1, intentDay = -1,
            intentYear = -1, mostRecentRotation = 0;

    private static final String KEY_ROTATION = "key_rotation",
    KEY_WIDGETINTENT = "key_widget";

    private boolean fromWidget = false;

    private int theme = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Set theme based on pref
        theme = prefs.getBoolean("dark_theme",false)? R.style.MainTheme_Dark :
                R.style.MainTheme_Colorful;
        setTheme(theme);
        mostRecentRotation = getResources().getConfiguration().orientation;
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mainview);

		//if(getFragmentManager().findFragmentById(R.id.fragment_container) == null) {

            /*
             * Three possible ways to launch activity:
             *
             * Intent is launch intent:
             * get data from today[], prefs
             *
             * Intent is from widget:
             * get data from intent
             *
             * savedinstancestate is not null:
             * use saved data in fragment
             * -be careful when fragment is rotated between 3-column and 1-column ('large' size)
             *
             * solution:
             *
             * priority:
             * 1. from widget
             *  - the onNewIntent overriding makes sure that a launch from the widget overrides the
             *    saved state.
             * 2. savedinstancestate
             *  - make sure rotation is the same, otherwise copy data saved into instancestate back
             *  out for use (this is in the case of a rotation)
             * 3. nothing (launch intent, get from [today[], prefs
             *
             */

            // If data in intent, it's from the widget. Highest priority
            if (!(getIntent().getIntExtra(Util.TAG_COLLEGE, -1) == -1)) {
                if (savedInstanceState != null && savedInstanceState.getBoolean(KEY_WIDGETINTENT) &&
                        !getIntent().getBooleanExtra(Util.TAG_FROMNOTIFICATION, false) ){
                    intentCollege = getCurrentCollege(this);
                    int date[] = getCurrentDispDate(this);
                    if (date[0] == 0) {
                        date = Util.getToday();
                    }
                    intentMonth = date[0];
                    intentDay = date[1];
                    intentYear = date[2];
                    intentMeal = Util.getCurrentMeal(intentCollege);
                } else {
                    // First time running intent from widget. Actually use it.
                    int today[] = Util.getToday();
                    intentCollege = getIntent().getIntExtra(Util.TAG_COLLEGE,
                            Integer.parseInt(prefs.getString("default_college", "0")));
                    intentMeal = getIntent().getIntExtra(Util.TAG_MEAL, Util.getCurrentMeal(intentCollege));
                    intentMonth = getIntent().getIntExtra(Util.TAG_MONTH, today[0]);
                    intentDay = getIntent().getIntExtra(Util.TAG_DAY, today[1]);
                    intentYear = getIntent().getIntExtra(Util.TAG_YEAR, today[2]);
                }
                fromWidget = true;
            } else if (savedInstanceState != null) {
                intentCollege = getCurrentCollege(this);
                int date[] = getCurrentDispDate(this);
                if (date[0] == 0) {
                    date = Util.getToday();
                }
                intentMonth = date[0];
                intentDay = date[1];
                intentYear = date[2];
                intentMeal = Util.getCurrentMeal(intentCollege);
            } else {
                int today[] = Util.getToday();
                intentCollege = Integer.parseInt(prefs.getString("default_college", "0"));
                intentMeal = Util.getCurrentMeal(intentCollege);
                intentMonth = today[0];
                intentDay = today[1];
                intentYear = today[2];
            }

            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
            mAboutList = (ListView) findViewById(R.id.drawer_settings_link);
            mDrawer = (RelativeLayout) findViewById(R.id.left_drawer);
            mActionBar = getSupportActionBar();

            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                    R.layout.drawer_list_item, Util.collegeList));

            ArrayList<String> infoLink = new ArrayList<String>();
            infoLink.add(getString(R.string.about));
            mAboutList.setAdapter(new ArrayAdapter<String>(this,
                    R.layout.drawer_list_item, infoLink));

            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.string.drawer_open, R.string.drawer_close);

            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
            mAboutList.setOnItemClickListener(new InfoItemClickListener());

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putInt(Util.TAG_COLLEGE, intentCollege);
            args.putInt(Util.TAG_MEAL, intentMeal);

            args.putInt(Util.TAG_MONTH, intentMonth);
            args.putInt(Util.TAG_DAY, intentDay);
            args.putInt(Util.TAG_YEAR, intentYear);

            /**
             * If layout xlarge (nexus 10-sized tablet) or layout large (nexus 7-sized tablet)
             * and view landscape, use large layout (displays all meals at once), otherwise use
             * normal layout
             */
            if ((getResources().getConfiguration().screenLayout &
                    Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE
                    || ( (getResources().getConfiguration().screenLayout &
                    Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE)
                    && ((getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_LANDSCAPE))) {
                fragmentLarge = new MealViewFragmentLarge();
                fragmentLarge.setArguments(args);
                transaction.replace(R.id.fragment_container, fragmentLarge);
            } else {
                fragment = new MealViewFragment();
                fragment.setArguments(args);
                transaction.replace(R.id.fragment_container, fragment);
            }

            transaction.commit();

            FragmentManager m = getSupportFragmentManager();
            m.executePendingTransactions();

/*	        if (savedInstanceState == null) {
//	            selectItem(0);
                fragment.selectItem(0);
            }*/
		//}

    }@Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save current rotation state, plus all other stuff
        savedInstanceState.putInt(KEY_ROTATION, mostRecentRotation);
        int date[] = getCurrentDispDate(this);
        savedInstanceState.putInt(Util.TAG_COLLEGE, getCurrentCollege(this));
        savedInstanceState.putInt(Util.TAG_MONTH, date[0]);
        savedInstanceState.putInt(Util.TAG_DAY, date[1]);
        savedInstanceState.putInt(Util.TAG_YEAR, date[2]);
        savedInstanceState.putInt(Util.TAG_MEAL, Util.getCurrentMeal(intentCollege));
        savedInstanceState.putBoolean(KEY_WIDGETINTENT, fromWidget);

        super.onSaveInstanceState(savedInstanceState);
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

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Set theme based on pref
        if (theme != (prefs.getBoolean("dark_theme",false)? R.style.MainTheme_Dark :
                R.style.MainTheme_Colorful)) {
            recreate();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(Util.LOGTAG, "new intent received");
        // Need to reset intent when new one is sent
        super.onNewIntent(intent);
        // If intent has no extras, it's a launch or return intent, do nothing
        if (intent.getExtras() == null) {
            return;
        }
        // If it contains college data, it's from widget, run new intent
        if (intent.getExtras().getInt(Util.TAG_COLLEGE, -1) != -1) {
            setIntent(intent);
            recreate();
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Need to act on whatever fragment is active
            try {
                MealViewFragment meal = (MealViewFragment)
                        getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                meal.selectItem(position);
            } catch (ClassCastException e) {
                MealViewFragmentLarge meal = (MealViewFragmentLarge)
                        getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                meal.selectItem(position);
            }
        }
    }

    private class InfoItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class DatePicker extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

        private static Context savedContext;

        @Override
        public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int today[] = getCurrentDispDate(savedContext);

            // Only return themed datepicker if on Lollipop
            if (Integer.valueOf(Build.VERSION.SDK_INT) >=
                    Integer.valueOf(Build.VERSION_CODES.LOLLIPOP)) {
                return new DatePickerDialog(getActivity(), R.style.DatePickerTheme, this, year,
                        today[0] - 1, today[1]);
            } else {
                return new DatePickerDialog(getActivity(), this, year,
                        today[0] - 1, today[1]);
            }
        }

        @Override
        public void onDateSet(android.widget.DatePicker view, int year,
            int monthOfYear, int dayOfMonth) {
            // Need to act on whatever fragment is active
            try {
                MealViewFragment meal = (MealViewFragment)
                        getFragmentManager().findFragmentById(R.id.fragment_container);
                meal.selectNewDate(monthOfYear + 1, dayOfMonth, year);
            } catch (ClassCastException e) {
                MealViewFragmentLarge meal = (MealViewFragmentLarge)
                        getFragmentManager().findFragmentById(R.id.fragment_container);
                meal.selectNewDate(monthOfYear + 1, dayOfMonth, year);
            }
        }

        public static void setSavedContext(Context context) {
            savedContext = context;
        }
    }

    private static int[] getCurrentDispDate(Context context) {
        // Need to act on whatever fragment is active
        try {
            MealViewFragment meal = (MealViewFragment)
                    ((AppCompatActivity)context).getSupportFragmentManager().findFragmentById(
                            R.id.fragment_container);
            int ret[] = {meal.displayedMonth, meal.displayedDay, meal.displayedYear};
            return ret;
        } catch (ClassCastException e) {
            MealViewFragmentLarge meal = (MealViewFragmentLarge)
                    ((AppCompatActivity)context).getSupportFragmentManager().findFragmentById(
                            R.id.fragment_container);
            int ret[] = {meal.displayedMonth, meal.displayedDay, meal.displayedYear};
            return ret;
        }
    }

    private static int getCurrentCollege(Context context) {
        // Need to act on whatever fragment is active
        try {
            MealViewFragment meal = (MealViewFragment)
                    ((AppCompatActivity)context).getSupportFragmentManager().findFragmentById(
                            R.id.fragment_container);
            return meal.collegeNum;
        } catch (ClassCastException e) {
            MealViewFragmentLarge meal = (MealViewFragmentLarge)
                    ((AppCompatActivity)context).getSupportFragmentManager().findFragmentById(
                            R.id.fragment_container);
            return meal.collegeNum;
        }
    }

}
