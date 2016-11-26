package com.nickivy.slugfood;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import com.nickivy.slugfood.parser.MealDataFetcher;
import com.nickivy.slugfood.parser.MenuParser;
import com.nickivy.slugfood.util.Util;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        // MainActivity sets data, often it will read the displayed date and send it back in here
        collegeNum = getArguments().getInt(Util.TAG_COLLEGE);
        getActivity().setTitle(Util.collegeList[collegeNum]);

        displayedMonth = getArguments().getInt(Util.TAG_MONTH);
        displayedDay = getArguments().getInt(Util.TAG_DAY);
        displayedYear = getArguments().getInt(Util.TAG_YEAR);

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
            new RetrieveMenuInLargeFragmentTask(displayedMonth, displayedDay, displayedYear,
                    getActivity()).execute();
        }
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.large_refresh_layout);
        TypedArray a = view.getContext().getTheme().obtainStyledAttributes(new int[]
                {R.attr.tabSelector});
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(a.getResourceId(0, 0)));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MenuParser.manualRefresh = true;
                // When doing swipe refresh, reload to the displayed day
                new RetrieveMenuInLargeFragmentTask(displayedMonth, displayedDay,
                        displayedYear, getActivity()).execute();
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
        mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mFab.setBackgroundTintList(prefs.getBoolean("dark_theme",false)?
                ContextCompat.getColorStateList(getContext(), R.color.fab_ripple_color_dark) :
                ContextCompat.getColorStateList(getContext(), R.color.fab_ripple_color));
        mFab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                MainActivity.DatePicker.setSavedContext(view.getContext());
                DialogFragment newFragment = new MainActivity.DatePicker();
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });
    }

    public void selectItem(int position) {
        // update the main content by replacing listview adapters
        if (Util.fullMenuObj.get(position).getIsOpen()) {
            if (Util.fullMenuObj.get(position).getIsSet()) {
                collegeNum = position;
                // Set title to include date and color, based on events at the dining hall
                setTitleText(position, ((AppCompatActivity)getActivity()).getSupportActionBar());

                setAdapter((ListView) getActivity().findViewById(R.id.breakfast_list), collegeNum, Util.BREAKFAST);
                setAdapter((ListView) getActivity().findViewById(R.id.lunch_list), collegeNum, Util.LUNCH);
                setAdapter((ListView) getActivity().findViewById(R.id.dinner_list), collegeNum, Util.DINNER);

                mDrawerList = (ListView) getActivity().findViewById(R.id.left_drawer_list);
                mDrawerList.setAdapter(new ColorAdapter(getActivity(),
                        R.layout.drawer_list_item, Util.collegeList));
                mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                mDrawer = (RelativeLayout) getActivity().findViewById(R.id.left_drawer);
                mDrawerLayout.closeDrawer(mDrawer);
            }
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

        private Context mContext;

        /**
         * @param month Month of day to fetch
         * @param day Day of day to fetch
         * @param year Year of day to fetch
         */
        public RetrieveMenuInLargeFragmentTask(int month, int day, int year, Context context) {
            mAttemptedMonth = month;
            mAttemptedDay = day;
            mAttemptedYear = year;
            mContext = context;
        }

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected Long doInBackground(Void... voids) {
            int res = Util.GETLIST_SUCCESS;
            try {
                MealDataFetcher.fetchData(getActivity(), mAttemptedMonth, mAttemptedDay,
                        mAttemptedYear);
            } catch (UnknownHostException un) {
                un.printStackTrace();
                res = Util.GETLIST_INTERNET_FAILURE;
            } catch (IOException io) {
                io.printStackTrace();
                res = Util.GETLIST_OKHTTP_FAILURE;
            }
            return Double.valueOf(res).longValue();
        }

        protected void onPostExecute(Long result) {
            if (getActivity() == null) {
                // Rotation or something must have happened, etc. Spinners are gone.
                return;
            }
            // Only do secondary preference check here on init
            if (!initialRefreshed) {
                if (!(Util.fullMenuObj.get(collegeNum).getIsSet() &&
                        Util.fullMenuObj.get(collegeNum).getIsOpen())) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences
                            (mContext);
                    int possibleCollege = Integer.parseInt(prefs.getString("default_college_2nd",
                            "" + Util.NO_BACKUP_COLLEGE));
                    // If no value set on backup college, do nothing
                    if (possibleCollege != Util.NO_BACKUP_COLLEGE) {
                        collegeNum = possibleCollege;
                    }
                }
            }
            initialRefreshed = true;
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
            if(Util.fullMenuObj.get(collegeNum).getBreakfast().isEmpty() &&
                    Util.fullMenuObj.get(collegeNum).getLunch().isEmpty()
                    && Util.fullMenuObj.get(collegeNum).getDinner().isEmpty()) {
                mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                Toast.makeText(getActivity(), Util.collegeList[collegeNum] +
                        " dining hall closed today!", Toast.LENGTH_SHORT).show();
                mDrawerLayout.openDrawer(Gravity.START);
            }

            ListView listView = (ListView) getActivity().findViewById(R.id.breakfast_list);
            if (!Util.fullMenuObj.get(collegeNum).getBreakfast().isEmpty() &&
                    Util.fullMenuObj.get(collegeNum).getBreakfast().get(0).getItemName()
                            .equals(Util.brunchMessage)) {
                listView.setVisibility(View.INVISIBLE);
            } else {
                listView = (ListView) getActivity().findViewById(R.id.breakfast_list);
                listView.setVisibility(View.VISIBLE);
            }
            mDrawerList = (ListView) getActivity().findViewById(R.id.left_drawer_list);
            mDrawerList.setAdapter(new ColorAdapter(getActivity(),
                    R.layout.drawer_list_item, Util.collegeList));

            setAdapter((ListView) getActivity().findViewById(R.id.breakfast_list), collegeNum, Util.BREAKFAST);
            setAdapter((ListView) getActivity().findViewById(R.id.lunch_list), collegeNum, Util.LUNCH);
            setAdapter((ListView) getActivity().findViewById(R.id.dinner_list), collegeNum, Util.DINNER);

            // Set title text
            setTitleText(collegeNum, ((AppCompatActivity)getActivity()).getSupportActionBar());
        }

    }



    private void setAdapter(ListView view, final int college, final int meal) {
        if (view == null) {
            return;
        }
        ArrayList<String> array = null;
        switch(meal){
            case Util.BREAKFAST:
                array = Util.fullMenuObj.get(college).getBreakfastList();
                break;
            case Util.LUNCH:
                array = Util.fullMenuObj.get(college).getLunchList();
                break;
            case Util.DINNER:
                array = Util.fullMenuObj.get(college).getDinnerList();
                break;
            default:
                return;
        }
        if (array != null) {
            view.setAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1,
                    array));
            switch (meal) {
                case Util.BREAKFAST:
                    view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                            Intent intent = new Intent(getActivity().getApplicationContext(),
                                    NutritionViewActivity.class);
                            intent.putExtra(Util.TAG_URL, "http://nutrition.sa.ucsc.edu/label.asp" +
                                    MenuParser.URLPart2s[college] + displayedMonth +
                                    "%2F" + displayedDay + "%2F" + displayedYear +
                                    "&RecNumAndPort=" + Util.fullMenuObj.get(college)
                                    .getBreakfast().get(pos).getCode());
                            startActivity(intent);
                        }
                    });
                    break;
                case Util.LUNCH:
                    view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                            Intent intent = new Intent(getActivity().getApplicationContext(),
                                    NutritionViewActivity.class);
                            intent.putExtra(Util.TAG_URL, "http://nutrition.sa.ucsc.edu/label.asp" +
                                    MenuParser.URLPart2s[college] + displayedMonth +
                                    "%2F" + displayedDay + "%2F" + displayedYear +
                                    "&RecNumAndPort=" + Util.fullMenuObj.get(college)
                                    .getLunch().get(pos).getCode());
                            startActivity(intent);
                        }
                    });
                    break;
                case Util.DINNER:
                    view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                            Intent intent = new Intent(getActivity().getApplicationContext(),
                                    NutritionViewActivity.class);
                            intent.putExtra(Util.TAG_URL, "http://nutrition.sa.ucsc.edu/label.asp" +
                                    MenuParser.URLPart2s[college] + displayedMonth +
                                    "%2F" + displayedDay + "%2F" + displayedYear +
                                    "&RecNumAndPort=" + Util.fullMenuObj.get(college)
                                    .getDinner().get(pos).getCode());
                            startActivity(intent);
                        }
                    });
                    break;
            }
            final ArrayList<String> finalArray = array;
            view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                               int pos, long id) {

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(arg1.getContext());

                    Set<String> favorites = prefs.getStringSet("favorites_items_list",
                            new HashSet<String>());

                    favorites.add(finalArray.get(pos));
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove("favorites_items_list");
                    edit.commit();
                    edit.putStringSet("favorites_items_list", favorites);
                    edit.commit();

                    Toast.makeText(getActivity(), finalArray.get(pos)
                                    + " added to favorites",
                            Toast.LENGTH_SHORT).show();

                    return true;
                }
            });
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
        new RetrieveMenuInLargeFragmentTask(month, day, year, getActivity()).execute();
    }

    private void setTitleText(int college, ActionBar bar) {
        // Set title to include date and color, based on events at the dining hall
        Spannable text = new SpannableString(displayedMonth + "/" + displayedDay + " " +
                Util.collegeList[college]);

        if (Util.fullMenuObj.get(college).getIsCollegeNight()) {
            // Blue for College Night
            text.setSpan(new ForegroundColorSpan(Color.BLUE), 0, text.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else if (Util.fullMenuObj.get(college).getIsFarmFriday() ||
                Util.fullMenuObj.get(college).getIsHealthyMonday()) {
            // Green for Healthy Monday / Farm Friday
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.healthy)), 0,
                    text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            TypedArray a = bar.getThemedContext().getTheme().obtainStyledAttributes(new int[]
                    {R.attr.actionBarText});
            text.setSpan(new ForegroundColorSpan(getResources().getColor(a.getResourceId(0, 0))),
                    0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
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
            if (Util.fullMenuObj.get(position).getIsCollegeNight()) {
                ((TextView) v).setTextColor(Color.BLUE); //
            }
            // Grayed out if dining hall is closed
            if (!Util.fullMenuObj.get(position).getIsOpen()) {
                TypedArray a = v.getContext().getTheme().obtainStyledAttributes(new int[]
                        {R.attr.closedDiningHallText});
                ((TextView) v).setTextColor(getResources().getColor(a.getResourceId(0, 0)));
            }
            // Green for Healthy Monday / Farm Friday
            if (Util.fullMenuObj.get(position).getIsFarmFriday() ||
                    Util.fullMenuObj.get(position).getIsHealthyMonday()) {
                ((TextView) v).setTextColor(getResources().getColor(R.color.healthy));
            }
            if (collegeNum == position && Util.fullMenuObj.get(position).getIsOpen()) {
                TypedArray a = v.getContext().getTheme().obtainStyledAttributes(new int[]
                        {R.attr.tabBG});
                ((TextView) v).setBackgroundColor(getResources().getColor(a.getResourceId(0, 0)));
            }
            return v;
        }
    }

}
