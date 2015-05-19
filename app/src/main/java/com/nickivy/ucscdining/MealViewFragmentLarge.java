package com.nickivy.ucscdining;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.nickivy.ucscdining.parser.MealDataFetcher;
import com.nickivy.ucscdining.parser.MenuParser;
import com.nickivy.ucscdining.util.Util;

import java.util.ArrayList;
import java.util.List;

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
    displayedYear;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mDrawerList;
    private RelativeLayout mDrawer;
    private DrawerLayout mDrawerLayout;
    private ListView mMealList;
    private FloatingActionButton mFab;

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
        }
        initialRefreshed = false;

        return inflater.inflate(R.layout.meal_fragment, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (!initialRefreshed) {
            /*
             * For some strange reason the runnable method used in other places does NOT work for
             * initial refresh on the large layout. It works fine in select new date.
             */
            mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity()
                    .findViewById(R.id.large_refresh_layout);
            TypedValue typed_value = new TypedValue();
            getActivity().getTheme().
                    resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value,
                            true);
            mSwipeRefreshLayout.setProgressViewOffset(false, 0,
                    getResources().getDimensionPixelSize(typed_value.resourceId));
            mSwipeRefreshLayout.setRefreshing(true);
            // Default loading to today
            initialRefreshed = true;
            new RetrieveMenuInLargeFragmentTask(displayedMonth, displayedDay, displayedYear)
                    .execute();
        }
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.large_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MenuParser.manualRefresh = true;
                // When doing swipe refresh, reload to the displayed day
                new RetrieveMenuInLargeFragmentTask(displayedMonth, displayedDay,
                        displayedYear).execute();
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
        mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab_large);
        mFab.setOnClickListener(new FloatingActionButtonListener());
        // Attach to dinner list view
        //fab.attachToListView(listViewDinner);
    }

    public void selectItem(int position) {
        int oldPosition = collegeNum;
        collegeNum = position;
        // update the main content by replacing listview adapters
        if (MenuParser.fullMenuObj.get(position).getIsOpen()) {
            if (MenuParser.fullMenuObj.get(position).getIsSet()) {
                // Set title to include date and color, based on events at the dining hall
                setTitleText(position, ((AppCompatActivity)getActivity()).getSupportActionBar());

                mMealList = (ListView) getActivity().findViewById(R.id.breakfast_list);
                final int collegePos = position;
                ArrayList<String> testedArray = new ArrayList<String>();
                testedArray = MenuParser.fullMenuObj.get(position).getBreakfastList();
                if (testedArray != null && mMealList != null) {
                    mMealList.setAdapter(new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_list_item_1,
                            MenuParser.fullMenuObj.get(position).getBreakfastList()));
                    // Set link to nutrition info
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
                mMealList = (ListView) getActivity().findViewById(R.id.lunch_list);
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
                mMealList = (ListView) getActivity().findViewById(R.id.dinner_list);
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

            mDrawerList.setAdapter(new ColorAdapter(getActivity(),
                    R.layout.drawer_list_item, Util.collegeList));

            mDrawerLayout.closeDrawer(mDrawer);
        } else {
            Toast.makeText(getActivity(), Util.collegeList[position] + " dining hall closed today!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("ResourceType")
    private class RetrieveMenuInLargeFragmentTask extends AsyncTask<Void, Void, Long> {

        private int mAttemptedMonth,
                mAttemptedDay,
                mAttemptedYear;

        /**
         * @param month Month of day to fetch
         * @param day Day of day to fetch
         * @param year Year of day to fetch
         */
        public RetrieveMenuInLargeFragmentTask(int month, int day, int year) {
            mAttemptedMonth = month;
            mAttemptedDay = day;
            mAttemptedYear = year;
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
                // Rotation or something must have happened, etc. Spinners are gone.
                return;
            }
            MenuParser.manualRefresh = false;
            // Post-execute: set array adapters, reload animation, set title


            /*
             * We want the spinners canceled no matter what, but all the other stuff should not
             * be changed in the case of a data load failure
             */
            mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity()
                    .findViewById(R.id.large_refresh_layout);
            mSwipeRefreshLayout.setRefreshing(false);
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

            // if all meals empty (dining hall closed), pop open nav drawer
            if(MenuParser.fullMenuObj.get(collegeNum).getBreakfast().isEmpty() &&
                    MenuParser.fullMenuObj.get(collegeNum).getLunch().isEmpty()
                    && MenuParser.fullMenuObj.get(collegeNum).getDinner().isEmpty()) {
                mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                Toast.makeText(getActivity(), Util.collegeList[collegeNum] +
                        " dining hall closed today!", Toast.LENGTH_SHORT).show();
                mDrawerLayout.openDrawer(Gravity.START);
            }

            ListView listView = (ListView) getActivity().findViewById(R.id.breakfast_list);
            if (!MenuParser.fullMenuObj.get(collegeNum).getBreakfast().isEmpty() &&
                    MenuParser.fullMenuObj.get(collegeNum).getBreakfast().get(0).getItemName()
                            .equals(Util.brunchMessage)) {
                listView.setVisibility(View.INVISIBLE);
            } else {
                listView = (ListView) getActivity().findViewById(R.id.breakfast_list);
                listView.setVisibility(View.VISIBLE);
            }

            // If only dinner available, set to dinner
            // (rare occurence, pretty much only on return from holidays)
            /*if (MenuParser.fullMenuObj.get(collegeNum).getBreakfast().isEmpty() &&
                    MenuParser.fullMenuObj.get(collegeNum).getLunch().isEmpty() &&
                    !MenuParser.fullMenuObj.get(collegeNum).getDinner().isEmpty()) {
                // hide breakfast and lunch? there is already nothing in the lists
            }*/

            mDrawerList = (ListView) getActivity().findViewById(R.id.left_drawer_list);

            mDrawerList.setAdapter(new ColorAdapter(getActivity(),
                    R.layout.drawer_list_item, Util.collegeList));

            final int collegePos = collegeNum;

            listView = (ListView) getActivity().findViewById(R.id.breakfast_list);
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
            listView = (ListView) getActivity().findViewById(R.id.lunch_list);
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
            listView = (ListView) getActivity().findViewById(R.id.dinner_list);
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

            // Set title text
            setTitleText(collegeNum, ((AppCompatActivity)getActivity()).getSupportActionBar());
        }

    }

    public void selectNewDate(int month, int day, int year) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity()
                        .findViewById(R.id.large_refresh_layout);
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        new RetrieveMenuInLargeFragmentTask(month, day, year).execute();
    }

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
            text.setSpan(new ForegroundColorSpan(Color.rgb(0x4C, 0xC5, 0x52)), 0,
                    text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE); // 'Green Apple'
        } else {
            text.setSpan(new ForegroundColorSpan(R.color.primary_text), 0, text.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        bar.setTitle(text);
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
                ((TextView) v).setTextColor(getResources().getColor(R.color.healthy));
            }
            if (collegeNum == position) {
                //((TextView) v).setTypeface(null, Typeface.BOLD);
                ((TextView) v).setBackgroundColor(getResources().getColor(R.color.primary_light));
            }
            return v;
        }
    }

    public class FloatingActionButtonListener implements FloatingActionButton.OnClickListener {

        @Override
        public void onClick(View view) {
            DialogFragment newFragment = new MainActivity.DatePicker();
            newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
        }
    }

}
