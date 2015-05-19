package com.nickivy.ucscdining;

import java.util.ArrayList;
import java.util.List;

import com.google.samples.apps.iosched.ui.widget.SlidingTabLayout;
import com.melnykov.fab.FloatingActionButton;
import com.nickivy.ucscdining.parser.MealDataFetcher;
import com.nickivy.ucscdining.parser.MenuParser;
import com.nickivy.ucscdining.util.Util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Fragment for displaying one menu. Uses sliding tab
 * layout from Google's examples.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author Nicky Ivy parkedraccoon@gmail.com
 */

@SuppressWarnings("ResourceType")
public class MealViewFragment extends ListFragment{

    final static String ARG_COLLEGE_NUMBER = "college_number";

    private static final int LISTVIEW_ID1 = 12,
    LISTVIEW_ID2 = 13,
    LISTVIEW_ID3 = 14,
    SWIPEREF_ID1 = 15,
    SWIPEREF_ID2 = 16,
    SWIPEREF_ID3 = 17,
    FAB_ID1 = 18,
    FAB_ID2 = 19,
    FAB_ID3 = 20;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;
    private ListView mDrawerList;
    private RelativeLayout mDrawer;
    private DrawerLayout mDrawerLayout;
    private ListView mMealList;
    private FloatingActionButton mFab;

    private static int collegeNum = 0;

    public static int displayedMonth = 0,
    displayedDay = 0,
    displayedYear = 0,
    initialMeal = -1;

    private boolean initialRefreshed = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {

        // If USESAVED true this means we are returning to view the fragment without changing it
        if (!getArguments().getBoolean(Util.TAG_USESAVED)) {
            collegeNum = getArguments().getInt(Util.TAG_COLLEGE);
            getActivity().setTitle(Util.collegeList[collegeNum]);

            displayedMonth = getArguments().getInt(Util.TAG_MONTH);
            displayedDay = getArguments().getInt(Util.TAG_DAY);
            displayedYear = getArguments().getInt(Util.TAG_YEAR);

            initialMeal = getArguments().getInt(Util.TAG_MEAL);
        }

        return inflater.inflate(R.layout.pager_fragment, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new MenuAdapter());
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.primary));
    }

    public void selectItemWithSecondary(int position) {
        if (MenuParser.fullMenuObj.get(position).getIsOpen()) {
            if (MenuParser.fullMenuObj.get(position).getIsSet()) {
                selectItem(position);
                return;
            }
        } else {
            /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences
                    (container.getContext());
            selectItem(Integer.parseInt(prefs.getString("default_college_2nd", "0")));*/
        }
    }

    public void selectItem(int position) {
        Log.v(Util.LOGTAG, "selecting item " + position);
        collegeNum = position;
        mDrawerList = (ListView) getActivity().findViewById(R.id.left_drawer_list);
        mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // update the main content by replacing listview adapters
        if (MenuParser.fullMenuObj.get(position).getIsOpen()) {
            if (MenuParser.fullMenuObj.get(position).getIsSet()) {
                // Set title to include date and color, based on events at the dining hall
                setTitleText(position, ((AppCompatActivity)getActivity()).getSupportActionBar());

                mMealList = (ListView) getActivity().findViewById(MealViewFragment.LISTVIEW_ID1);
                final int collegePos = position;
                ArrayList<String> testedArray = new ArrayList<String>();
                testedArray = MenuParser.fullMenuObj.get(position).getBreakfastList();
                if (testedArray != null && mMealList != null) {
                    mMealList.setAdapter(new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1,
                        MenuParser.fullMenuObj.get(position).getBreakfastList()));
                        // Set onclick for leading to nutritional info
                        mMealList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                                Intent intent = new Intent(getActivity().getApplicationContext(),
                                        NutritionWebpageActivity.class);
                                intent.putExtra(Util.TAG_URL, "http://nutrition.sa.ucsc.edu/label.asp" +
                                        MenuParser.URLPart2s[collegePos] + displayedMonth +
                                        "%2F" + displayedDay + "%2F" + displayedYear +
                                        "&RecNumAndPort=" + MenuParser.fullMenuObj.get(collegePos)
                                        .getBreakfast().get(pos).getCode());
                                startActivity(intent);
                            }
                        });
                }
                mMealList = (ListView) getActivity().findViewById(MealViewFragment.LISTVIEW_ID2);
                testedArray = MenuParser.fullMenuObj.get(position).getLunchList();
                if (testedArray != null && mMealList != null) {
                    mMealList.setAdapter(new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1,
                        MenuParser.fullMenuObj.get(position).getLunchList()));
                    mMealList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                            Intent intent = new Intent(getActivity().getApplicationContext(),
                                    NutritionWebpageActivity.class);
                            intent.putExtra(Util.TAG_URL, "http://nutrition.sa.ucsc.edu/label.asp" +
                                    MenuParser.URLPart2s[collegePos] + displayedMonth +
                                    "%2F" + displayedDay + "%2F" + displayedYear +
                                    "&RecNumAndPort=" + MenuParser.fullMenuObj.get(collegePos)
                                    .getLunch().get(pos).getCode());
                            startActivity(intent);
                        }
                    });
                }
                mMealList = (ListView) getActivity().findViewById(MealViewFragment.LISTVIEW_ID3);
                testedArray = MenuParser.fullMenuObj.get(position).getDinnerList();
                if (testedArray != null && mMealList != null) {
                    mMealList.setAdapter(new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1,
                        MenuParser.fullMenuObj.get(position).getDinnerList()));
                    mMealList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                            Intent intent = new Intent(getActivity().getApplicationContext(),
                                    NutritionWebpageActivity.class);
                            intent.putExtra(Util.TAG_URL, "http://nutrition.sa.ucsc.edu/label.asp" +
                                    MenuParser.URLPart2s[collegePos] + displayedMonth +
                                    "%2F" + displayedDay + "%2F" + displayedYear +
                                    "&RecNumAndPort=" + MenuParser.fullMenuObj.get(collegePos)
                                    .getDinner().get(pos).getCode());
                            startActivity(intent);
                        }
                    });
                }
            }

            mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
            mDrawerList = (ListView) getActivity().findViewById(R.id.left_drawer_list);
            mDrawer = (RelativeLayout) getActivity().findViewById(R.id.left_drawer);

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            mDrawerList.setSelection(position);
            mDrawerList.setAdapter(new ColorAdapter(getActivity(),
                    R.layout.drawer_list_item, Util.collegeList));
            mDrawerLayout.closeDrawer(mDrawer);
        } else {
            Toast.makeText(getActivity(), Util.collegeList[position] + " dining hall closed today!",
                Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("ResourceType")
    private class RetrieveMenuInFragmentTask extends AsyncTask<Void, Void, Long> {

        private boolean mSetPage;
        private int mInitialMeal,
        mAttemptedMonth,
        mAttemptedDay,
        mAttemptedYear;

        private Context mContext;

        /**
         * @param month Month of day to fetch
         * @param day Day of day to fetch
         * @param year Year of day to fetch
         * @param setPage If true, date will be set based on initMeal param, see below
         * @param initMeal Meal that should be viewed after data is loaded. Value will only be used
         *                 if setPage is true. Set to -1 to set date automatically (by getToday())
         */
        public RetrieveMenuInFragmentTask(int month, int day, int year, boolean setPage,
                                          int initMeal, Context context) {
            mAttemptedMonth = month;
            mAttemptedDay = day;
            mAttemptedYear = year;
            mSetPage = setPage;
            mInitialMeal = initMeal;
            mContext = context;
        }

        @Override
        protected void onPreExecute(){}

        @Override
        protected Long doInBackground(Void... voids) {
            int res = MealDataFetcher.fetchData(getActivity(), mAttemptedMonth, mAttemptedDay,
                mAttemptedYear);
            return Double.valueOf(res).longValue();
        }

        protected void onPostExecute(Long result) {
            if (getActivity() == null) {
                // Rotation or something must have happened, etc.
                return;
            }
            // Only do secondary preference check here on init
            if (!initialRefreshed) {
                if (!(MenuParser.fullMenuObj.get(collegeNum).getIsSet() &&
                        MenuParser.fullMenuObj.get(collegeNum).getIsOpen())) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                    collegeNum = Integer.parseInt(prefs.getString("default_college_2nd", "0"));
                }
            }
            initialRefreshed = true;
            MenuParser.manualRefresh = false;


            // Post-execute: set array adapters, reload animation, set title


            /*
             *  Manually try to recreate what the swiperefresh layout has by default.
             *
             * Only 2 views exist at a time, so the third returns null, but
             * we don't know which one it is, so check each one. Try-catch
             * would work but is performance-inefficient.
             */

            mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(SWIPEREF_ID1);
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
            mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(SWIPEREF_ID2);
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
            mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(SWIPEREF_ID3);
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(false);
            }

            /*
             * We want the spinners canceled no matter what, but all the other stuff should not
             * be changed in the case of a data load failure
             */
            if (!result.equals(Double.valueOf(Util.GETLIST_SUCCESS).longValue())) {
                if (result.equals(Double.valueOf(Util.GETLIST_DATABASE_FAILURE).longValue())) {
                    Toast.makeText(getActivity(), getString(R.string.database_failed),
                        Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.internet_failed),
                        Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // Only set displayed day to what we attempted once data fetch successful
            displayedMonth = mAttemptedMonth;
            displayedDay = mAttemptedDay;
            displayedYear = mAttemptedYear;

            if (mSetPage) {
                if (mInitialMeal == -1) {
                    mViewPager.setCurrentItem(Util.getCurrentMeal(collegeNum), false);
                } else {
                    mViewPager.setCurrentItem(mInitialMeal, false);
                }
            }

            // if all meals empty (dining hall closed), pop open nav drawer
            if(MenuParser.fullMenuObj.get(collegeNum).getBreakfast().isEmpty() &&
                MenuParser.fullMenuObj.get(collegeNum).getLunch().isEmpty()
                && MenuParser.fullMenuObj.get(collegeNum).getDinner().isEmpty()) {
                mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                Toast.makeText(getActivity(), Util.collegeList[collegeNum] +
                                " dining hall closed today!", Toast.LENGTH_SHORT).show();
                mDrawerLayout.openDrawer(Gravity.START);
            }

            /*
             * If breakfast is in brunch (on weekends), set active tab to lunch - brunch message
             * will be displayed in breakfast tabs
             * Also will only move tab if breakfast is selected
             */
            if (!MenuParser.fullMenuObj.get(collegeNum).getBreakfast().isEmpty() &&
                mViewPager.getCurrentItem() == 0 &&
                MenuParser.fullMenuObj.get(collegeNum).getBreakfast().get(0).getItemName()
                .equals(Util.brunchMessage)) {
                mViewPager.setCurrentItem(1,false);
            }

            // If only dinner available, set to dinner
            // (rare occurence, pretty much only on return from holidays)
            if (MenuParser.fullMenuObj.get(collegeNum).getBreakfast().isEmpty() &&
                MenuParser.fullMenuObj.get(collegeNum).getLunch().isEmpty() &&
                !MenuParser.fullMenuObj.get(collegeNum).getDinner().isEmpty()) {
                mViewPager.setCurrentItem(2,false);
            }

            mDrawerList = (ListView) getActivity().findViewById(R.id.left_drawer_list);

            mDrawerList.setAdapter(new ColorAdapter(getActivity(),
            R.layout.drawer_list_item, Util.collegeList));

            final int collegePos = collegeNum;

            ListView listView = (ListView) getActivity().findViewById(LISTVIEW_ID1);
            if (listView != null) {
                listView.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_activated_1,
                MenuParser.fullMenuObj.get(collegeNum).getBreakfastList()));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                        Intent intent = new Intent(getActivity().getApplicationContext(),
                                NutritionWebpageActivity.class);
                        intent.putExtra(Util.TAG_URL, "http://nutrition.sa.ucsc.edu/label.asp" +
                                MenuParser.URLPart2s[collegePos] + displayedMonth +
                                "%2F" + displayedDay + "%2F" + displayedYear +
                                "&RecNumAndPort=" + MenuParser.fullMenuObj.get(collegePos)
                                .getBreakfast().get(pos).getCode());
                        startActivity(intent);
                    }
                });
            }
            listView = (ListView) getActivity().findViewById(LISTVIEW_ID2);
            if (listView != null) {
                listView.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_activated_1,
                MenuParser.fullMenuObj.get(collegeNum).getLunchList()));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                        Intent intent = new Intent(getActivity().getApplicationContext(),
                                NutritionWebpageActivity.class);
                        intent.putExtra(Util.TAG_URL, "http://nutrition.sa.ucsc.edu/label.asp" +
                                MenuParser.URLPart2s[collegePos] + displayedMonth +
                                "%2F" + displayedDay + "%2F" + displayedYear +
                                "&RecNumAndPort=" + MenuParser.fullMenuObj.get(collegePos)
                                .getLunch().get(pos).getCode());
                        startActivity(intent);
                    }
                });
            }
            listView = (ListView) getActivity().findViewById(LISTVIEW_ID3);
            if (listView != null) {
                listView.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_activated_1,
                MenuParser.fullMenuObj.get(collegeNum).getDinnerList()));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                        Intent intent = new Intent(getActivity().getApplicationContext(),
                                NutritionWebpageActivity.class);
                        intent.putExtra(Util.TAG_URL, "http://nutrition.sa.ucsc.edu/label.asp" +
                                MenuParser.URLPart2s[collegePos] + displayedMonth +
                                "%2F" + displayedDay + "%2F" + displayedYear +
                                "&RecNumAndPort=" + MenuParser.fullMenuObj.get(collegePos)
                                .getDinner().get(pos).getCode());
                        startActivity(intent);
                    }
                });
            }

            // Set title text
            setTitleText(collegeNum, ((AppCompatActivity)getActivity()).getSupportActionBar());
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
            return Util.meals[position];
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
            mealList.setId(mealnum + LISTVIEW_ID1);

            mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.meal_refresh_layout);
            //Add 15 instead of 12 for swipelayout
            mSwipeRefreshLayout.setId(mealnum + SWIPEREF_ID1);
            mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary));
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    MenuParser.manualRefresh = true;
                    // When doing swipe refresh, reload to the displayed day
                    new RetrieveMenuInFragmentTask(displayedMonth, displayedDay,
                            displayedYear, false, 0, getActivity()).execute();
                }
            });
            mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            mFab.setId(mealnum + FAB_ID1);
            mFab.setOnClickListener(new FloatingActionButtonListener());
            mFab.attachToListView(mealList);

            /*
             * ASynctask to load menu, either from web
             * or from SQLite db.
             *
             * Load circle *should* be shown, but currently there's
             * no proper way to manually trigger the reload animation. So
             * we're stuck doing it in a hacky way.
             */
            if (!initialRefreshed) {
                Handler handler = new Handler();
                final int nummeal = mealnum;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity()
                                .findViewById(nummeal + SWIPEREF_ID1);
                        if (mSwipeRefreshLayout != null) {
                            mSwipeRefreshLayout.setRefreshing(true);
                        }
                    }
                });
                new RetrieveMenuInFragmentTask(displayedMonth, displayedDay, displayedYear, true,
                        initialMeal, getActivity()).execute();
            }

            ArrayList<String> testedArray = new ArrayList<String>();
            final int collegePos = collegeNum;
            switch(mealnum){
                case 0:
                testedArray = MenuParser.fullMenuObj.get(collegeNum).getBreakfastList();
                if (testedArray != null) {
                    mealList.setAdapter(new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1,
                        MenuParser.fullMenuObj.get(collegeNum).getBreakfastList()));
                        mealList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                                Intent intent = new Intent(getActivity().getApplicationContext(),
                                        NutritionWebpageActivity.class);
                                intent.putExtra(Util.TAG_URL, "http://nutrition.sa.ucsc.edu/label.asp" +
                                        MenuParser.URLPart2s[collegePos] + displayedMonth +
                                        "%2F" + displayedDay + "%2F" + displayedYear +
                                        "&RecNumAndPort=" + MenuParser.fullMenuObj.get(collegePos)
                                        .getBreakfast().get(pos).getCode());
                                startActivity(intent);
                            }
                        });
                }
                break;
            case 1:
                testedArray = MenuParser.fullMenuObj.get(collegeNum).getLunchList();
                if (testedArray != null){
                    mealList.setAdapter(new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1,
                        MenuParser.fullMenuObj.get(collegeNum).getLunchList()));
                        mealList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                                Intent intent = new Intent(getActivity().getApplicationContext(),
                                        NutritionWebpageActivity.class);
                                intent.putExtra(Util.TAG_URL, "http://nutrition.sa.ucsc.edu/label.asp" +
                                        MenuParser.URLPart2s[collegePos] + displayedMonth +
                                        "%2F" + displayedDay + "%2F" + displayedYear +
                                        "&RecNumAndPort=" + MenuParser.fullMenuObj.get(collegePos)
                                        .getLunch().get(pos).getCode());
                                startActivity(intent);
                            }
                        });
                }
                break;
            case 2:
                testedArray = MenuParser.fullMenuObj.get(collegeNum).getDinnerList();
                if (testedArray != null){
                    mealList.setAdapter(new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1,
                        MenuParser.fullMenuObj.get(collegeNum).getDinnerList()));
                        mealList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                                Intent intent = new Intent(getActivity().getApplicationContext(),
                                        NutritionWebpageActivity.class);
                                intent.putExtra(Util.TAG_URL, "http://nutrition.sa.ucsc.edu/label.asp" +
                                        MenuParser.URLPart2s[collegePos] + displayedMonth +
                                        "%2F" + displayedDay + "%2F" + displayedYear +
                                        "&RecNumAndPort=" + MenuParser.fullMenuObj.get(collegePos)
                                        .getDinner().get(pos).getCode());
                                startActivity(intent);
                            }
                        });
                }
                break;
            default:
            }
            return view;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }


    /**
     * Allows us to set colors of entries in the college list to denote
     * events
     */
    private class ColorAdapter extends ArrayAdapter<String> {

        public ColorAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
        }

        public ColorAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View v = super.getView(position,  convertView,  parent);
            // Blue text for college night
            if (MenuParser.fullMenuObj.get(position).getIsCollegeNight()) {
                ((TextView) v).setTextColor(Color.BLUE); //
            }
            // Grayed out if dining hall is closed
            if (!MenuParser.fullMenuObj.get(position).getIsOpen()) {
                ((TextView) v).setTextColor(Color.LTGRAY); //
            }
            // Green for Healthy Monday / Farm Friday
            if (MenuParser.fullMenuObj.get(position).getIsFarmFriday() ||
                MenuParser.fullMenuObj.get(position).getIsHealthyMonday()) {
                //((TextView) v).setTextColor(Color.rgb(0x4C, 0xC5, 0x52)); // 'Green Apple'
                ((TextView) v).setTextColor(getResources().getColor(R.color.healthy)); // 'Green Apple'
            }
            if (collegeNum == position) {
                //((TextView) v).setTypeface(null, Typeface.BOLD);
                ((TextView) v).setBackgroundColor(getResources().getColor(R.color.primary_light));
            }
            return v;
        }
    }

    public void selectNewDate(int month, int day, int year) {
        Handler handler = new Handler();
        // We have to use the runnable method, otherwise the spinner will not show up
        handler.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(SWIPEREF_ID1);
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        handler.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(SWIPEREF_ID2);
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        handler.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(SWIPEREF_ID3);
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        new RetrieveMenuInFragmentTask(month, day, year, false, 0, getActivity()).execute();
    }

    /**
     * Sets the title text in the action bar automatically based on college parameter and data
     * stored in meals
     * @param college number of college
     * @param bar actionbar instance to set
     */
    private void setTitleText(int college, ActionBar bar) {
        // Set title to include date and color, based on events at the dining hall
        Spannable text = new SpannableString(displayedMonth + "/" + displayedDay + " " +
        Util.collegeList[college]);

        if (MenuParser.fullMenuObj.get(college).getIsCollegeNight()) {
            // Blue for College Night
            text.setSpan(new ForegroundColorSpan(Color.BLUE), 0, text.length(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else if (MenuParser.fullMenuObj.get(college).getIsFarmFriday() ||
            MenuParser.fullMenuObj.get(college).getIsHealthyMonday()) {
            // Green for Healthy Monday / Farm Friday
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.healthy)), 0,
                text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE); // 'Green Apple'
        } else {
            text.setSpan(new ForegroundColorSpan(R.color.primary_text), 0, text.length(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        bar.setTitle(text);
    }

    public class FloatingActionButtonListener implements FloatingActionButton.OnClickListener {

        @Override
        public void onClick(View view) {
            DialogFragment newFragment = new MainActivity.DatePicker();
            newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
        }
    }

}
