package com.nickivy.ucscdining;

import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nickivy.ucscdining.parser.MealDataFetcher;
import com.nickivy.ucscdining.parser.MenuParser;
import com.nickivy.ucscdining.util.Util;

import java.util.ArrayList;

/**
 * Fragment for displaying all three menus at once - tablet layout.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author Nicky Ivy parkedraccoon@gmail.com
 */
public class MealViewFragmentLarge extends Fragment {

    public static int collegeNum,
    displayedMonth,
    displayedDay,
    displayedYear,
    initialMeal;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mDrawerList;
    private RelativeLayout mDrawer;
    private DrawerLayout mDrawerLayout;
    private ListView mMealList;
    private Bundle instance;

    private RetrieveMenuInLargeFragmentTask task;

    private boolean initialRefreshed = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        collegeNum = getArguments().getInt(Util.TAG_COLLEGE);
        Log.v(Util.LOGTAG, "received college in intent: " + collegeNum);
        getActivity().setTitle(Util.collegeList[collegeNum]);

        displayedMonth = getArguments().getInt(Util.TAG_MONTH);
        displayedDay = getArguments().getInt(Util.TAG_DAY);
        displayedYear = getArguments().getInt(Util.TAG_YEAR);

        instance = savedInstanceState;

        initialMeal = getArguments().getInt(Util.TAG_MEAL);

        return inflater.inflate(R.layout.meal_fragment, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.large_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MenuParser.manualRefresh = true;
                // When doing swipe refresh, reload to the displayed day
                if (task == null) {
                    task = new RetrieveMenuInLargeFragmentTask(displayedMonth, displayedDay,
                            displayedYear, false, 0);
                    task.execute();
                }
            }
        });

        /**
         * Since our ListViews are not direct children of swipe refresh layouts, we need to
         * manually manage its recognizing that we are at the top (which is the only place it should
         * engage a refresh)
         */
        final ListView listViewBreakfast = (ListView) getActivity().findViewById(R.id.breakfast_list);
        listViewBreakfast.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (listViewBreakfast == null || listViewBreakfast.getChildCount() == 0) ?
                                0 : listViewBreakfast.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(firstVisibleItem == 0 &&
                        topRowVerticalPosition >= 0);
            }
        });
        final ListView listViewLunch = (ListView) getActivity().findViewById(R.id.lunch_list);
        listViewLunch.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (listViewLunch == null || listViewLunch.getChildCount() == 0) ?
                                0 : listViewLunch.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(firstVisibleItem == 0 &&
                        topRowVerticalPosition >= 0);
            }
        });
        final ListView listViewDinner = (ListView) getActivity().findViewById(R.id.dinner_list);
        listViewDinner.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (listViewDinner == null || listViewDinner.getChildCount() == 0) ?
                                0 : listViewDinner.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(firstVisibleItem == 0 &&
                        topRowVerticalPosition >= 0);
            }
        });

        if (task == null && initialRefreshed == false) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int height = size.y;
            // manually try to recreate where the spinner ends up in a normal swipe
            mSwipeRefreshLayout.setProgressViewOffset(false, -50, height / 800);
            mSwipeRefreshLayout.setRefreshing(true);
            // Default loading to today
            initialRefreshed = true;
            int[] today = Util.getToday();
            task = new RetrieveMenuInLargeFragmentTask(today[0], today[1], today[2], true,
                    initialMeal);
            task.execute();
        }
    }

    public void selectItem(int position) {
        collegeNum = position;
        // update the main content by replacing listview adapters
        if (MenuParser.fullMenuObj.get(position).getIsOpen()) {
            if (MenuParser.fullMenuObj.get(position).getIsSet()) {
                // Set title to include date and color, based on events at the dining hall
                setTitleText(position, ((AppCompatActivity)getActivity()).getSupportActionBar());

                mMealList = (ListView) getActivity().findViewById(R.id.breakfast_list);
                ArrayList<String> testedArray = new ArrayList<String>();
                testedArray = MenuParser.fullMenuObj.get(position).getBreakfast();
                if (testedArray != null && mMealList != null) {
                    mMealList.setAdapter(new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_list_item_1,
                            MenuParser.fullMenuObj.get(position).getBreakfast()));
                }
                mMealList = (ListView) getActivity().findViewById(R.id.lunch_list);
                testedArray = MenuParser.fullMenuObj.get(position).getLunch();
                if (testedArray != null && mMealList != null) {
                    mMealList.setAdapter(new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_list_item_1,
                            MenuParser.fullMenuObj.get(position).getLunch()));
                }
                mMealList = (ListView) getActivity().findViewById(R.id.dinner_list);
                testedArray = MenuParser.fullMenuObj.get(position).getDinner();
                if (testedArray != null && mMealList != null) {
                    mMealList.setAdapter(new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_list_item_1,
                            MenuParser.fullMenuObj.get(position).getDinner()));
                }
            }

            mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
            mDrawerList = (ListView) getActivity().findViewById(R.id.left_drawer_list);
            mDrawer = (RelativeLayout) getActivity().findViewById(R.id.left_drawer);

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawer);
        } else {
            Toast.makeText(getActivity(), Util.collegeList[position] + " dining hall closed today!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("ResourceType")
    private class RetrieveMenuInLargeFragmentTask extends AsyncTask<Void, Void, Long> {

        private boolean mSetPage;
        private int mInitialMeal;

        /**
         * @param month Month of day to fetch
         * @param day Day of day to fetch
         * @param year Year of day to fetch
         * @param setPage If true, date will be set based on initMeal param, see below
         * @param initMeal Meal that should be viewed after data is loaded. Value will only be used
         *                 if setPage is true. Set to -1 to set date automatically (by getToday())
         */
        public RetrieveMenuInLargeFragmentTask(int month, int day, int year, boolean setPage,
                                          int initMeal) {
            displayedMonth = month;
            displayedDay = day;
            displayedYear = year;
            mSetPage = setPage;
            mInitialMeal = initMeal;
        }

        @Override
        protected void onPreExecute(){}

        @Override
        protected Long doInBackground(Void... voids) {
            int res = MealDataFetcher.fetchData(getActivity(), displayedMonth, displayedDay,
                    displayedYear);
            return new Double(res).longValue();
        }

        protected void onPostExecute(Long result) {
            MenuParser.manualRefresh = false;
            // For keeping track and only allowing one Asynctask to run at once
            task = null;
            // Post-execute: set array adapters, reload animation, set title


            /*
             *  Manually try to recreate what the swiperefresh layout has by default.
             *
             * Only 2 views exist at a time, so the third returns null, but
             * we don't know which one it is, so check each one. Try-catch
             * would work but is performance-inefficient.
             */

            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int height = size.y;

            mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.large_refresh_layout);
            mSwipeRefreshLayout.setProgressViewOffset(false, -100, height / 40);
            mSwipeRefreshLayout.setRefreshing(false);

            /*
             * We want the spinners canceled no matter what, but all the other stuff should not
             * be changed in the case of a data load failure
             */
            if (!result.equals(new Double(Util.GETLIST_SUCCESS).longValue())) {
                if (result.equals(new Double(Util.GETLIST_DATABASE_FAILURE).longValue())) {
                    Toast.makeText(getActivity(), getString(R.string.database_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.internet_failed),
                            Toast.LENGTH_SHORT).show();
                }
                return;
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
             *
             */
            ListView listView = (ListView) getActivity().findViewById(R.id.breakfast_list);
            if (!MenuParser.fullMenuObj.get(collegeNum).getBreakfast().isEmpty() &&
                    MenuParser.fullMenuObj.get(collegeNum).getBreakfast().get(0)
                            .equals(Util.brunchMessage)) {
                // hide breakfast?
                listView.setVisibility(View.INVISIBLE);
            } else {
                listView = (ListView) getActivity().findViewById(R.id.breakfast_list);
                listView.setVisibility(View.VISIBLE);
            }

            // If only dinner available, set to dinner
            // (rare occurence, pretty much only on return from holidays)
            if (MenuParser.fullMenuObj.get(collegeNum).getBreakfast().isEmpty() &&
                    MenuParser.fullMenuObj.get(collegeNum).getLunch().isEmpty() &&
                    !MenuParser.fullMenuObj.get(collegeNum).getDinner().isEmpty()) {
                // hide breakfast and lunch?
            }

            mDrawerList = (ListView) getActivity().findViewById(R.id.left_drawer_list);

            mDrawerList.setAdapter(new MealViewFragment.ColorAdapter(getActivity(),
                    R.layout.drawer_list_item, Util.collegeList));

            listView = (ListView) getActivity().findViewById(R.id.breakfast_list);
            listView.setAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_activated_1,
                    MenuParser.fullMenuObj.get(collegeNum).getBreakfast()));
            listView = (ListView) getActivity().findViewById(R.id.lunch_list);
            listView.setAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_activated_1,
                    MenuParser.fullMenuObj.get(collegeNum).getLunch()));
            listView = (ListView) getActivity().findViewById(R.id.dinner_list);
            listView.setAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_activated_1,
                    MenuParser.fullMenuObj.get(collegeNum).getDinner()));

            // Set title text
            setTitleText(collegeNum, ((AppCompatActivity)getActivity()).getSupportActionBar());
        }

    }

    public void selectNewDate(int month, int day, int year) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        /*
         *  Manually try to recreate what the swiperefresh layout has by default.
         *  No idea which layouts are active, so check for nulls on all.
         *  Also no idea why the -140 here is different than the -50 above,
         *  the whole thing makes no sense.
         */
        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.large_refresh_layout);
            mSwipeRefreshLayout.setProgressViewOffset(false, -150, height / 800);
            mSwipeRefreshLayout.setRefreshing(true);
        if (task == null) {
            task = new RetrieveMenuInLargeFragmentTask(month, day, year, false, 0);
            task.execute();
        }
    }

    private void setTitleText(int college, ActionBar bar) {
        // Set title to include date and color, based on events at the dining hall
        Spannable text = new SpannableString(
                Util.collegeList[college] + " " + displayedMonth + "/" + displayedDay);

        if (MenuParser.fullMenuObj.get(college).getIsCollegeNight()) {
            // Blue for College Night
            text.setSpan(new ForegroundColorSpan(Color.BLUE), 0, text.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else if (MenuParser.fullMenuObj.get(college).getIsFarmFriday() ||
                MenuParser.fullMenuObj.get(college).getIsHealthyMonday()) {
            // Green for Healthy Monday / Farm Friday
            text.setSpan(new ForegroundColorSpan(Color.rgb(0x4C, 0xC5, 0x52)), 0,
                    text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE); // 'Green Apple'
        } else {
            text.setSpan(new ForegroundColorSpan(Color.BLACK), 0, text.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        bar.setTitle(text);
    }

}
