package com.nickivy.slugfood;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.samples.apps.iosched.ui.widget.SlidingTabLayout;
import com.nickivy.slugfood.parser.MealDataFetcher;
import com.nickivy.slugfood.parser.MenuParser;
import com.nickivy.slugfood.util.Util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.ListFragment;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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
    LISTVIEW_ID4 = 15,
    SWIPEREF_ID1 = 16,
    SWIPEREF_ID2 = 17,
    SWIPEREF_ID3 = 18,
    SWIPEREF_ID4 = 19,
    FAB_ID1 = 20,
    FAB_ID2 = 21,
    FAB_ID3 = 22,
    FAB_ID4 = 23;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;
    private ListView mDrawerList;
    private RelativeLayout mDrawer;
    private DrawerLayout mDrawerLayout;
    private ListView mMealList;
    private FloatingActionButton mFab;

    public static int collegeNum = 0;

    public static int displayedMonth = 0,
    displayedDay = 0,
    displayedYear = 0,
    initialMeal = -1;

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

        initialMeal = getArguments().getInt(Util.TAG_MEAL);

        return inflater.inflate(R.layout.pager_fragment, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new MenuAdapter());
        // Otherwise pages will fall out of memory and this will cause errors
        mViewPager.setOffscreenPageLimit(5);
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(false);
        mSlidingTabLayout.setViewPager(mViewPager);
        TypedArray a = view.getContext().getTheme().obtainStyledAttributes(new int[] {R.attr.tabSelector});
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(a.getResourceId(0, 0)));
    }

    public void selectItem(int position) {
        // update the main content by replacing listview adapters
        if (Util.fullMenuObj.get(position).getIsOpen()) {
            if (Util.fullMenuObj.get(position).getIsSet()) {
                collegeNum = position;
                // Set title to include date and color, based on events at the dining hall
                setTitleText(position, ((AppCompatActivity) getActivity()).getSupportActionBar());

                setAdapter((ListView) getActivity().findViewById(LISTVIEW_ID1), collegeNum, Util.BREAKFAST);
                setAdapter((ListView) getActivity().findViewById(LISTVIEW_ID2), collegeNum, Util.LUNCH);
                setAdapter((ListView) getActivity().findViewById(LISTVIEW_ID3), collegeNum, Util.DINNER);
                setAdapter((ListView) getActivity().findViewById(LISTVIEW_ID4), collegeNum, Util.LATENIGHT);

                mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                mDrawerList = (ListView) getActivity().findViewById(R.id.left_drawer_list);
                mDrawerList.setAdapter(new ColorAdapter(getActivity(),
                        R.layout.drawer_list_item, Util.collegeList));
                mDrawer = (RelativeLayout) getActivity().findViewById(R.id.left_drawer);
                mDrawerLayout.closeDrawer(mDrawer);
            }
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
                // Rotation or something must have happened, etc.
                return;
            }
            // Only do secondary preference check here on init
            if (!initialRefreshed) {
                // swap if dining hall completely closed
                if (!(Util.fullMenuObj.get(collegeNum).getIsSet() &&
                        Util.fullMenuObj.get(collegeNum).getIsOpen())) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences
                            (mContext);
                    int possibleCollege = Integer.parseInt(prefs.getString("default_college_2nd",
                            "" + Util.NO_BACKUP_COLLEGE));
                    // If no value set on backup college, do nothing
                    if (possibleCollege != Util.NO_BACKUP_COLLEGE) {
                        collegeNum = possibleCollege;
                        selectItem(collegeNum);
                    }
                }
                // swap if open but not for late night
                if ( Util.fullMenuObj.get(collegeNum).getIsSet() && (Util.getCurrentMeal(collegeNum) == Util.LATENIGHT) &&
                        Util.fullMenuObj.get(collegeNum).getLateNight().isEmpty() ) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences
                            (mContext);
                    int possibleCollege = Integer.parseInt(prefs.getString("default_college_2nd",
                            "" + Util.NO_BACKUP_COLLEGE));

                    if (possibleCollege != Util.NO_BACKUP_COLLEGE &&
                            !Util.fullMenuObj.get(possibleCollege).getLateNight().isEmpty()) {
                        collegeNum = possibleCollege;
                        selectItem(collegeNum);
                    }
                }
            }
            MenuParser.manualRefresh = false;
            initialRefreshed = true;


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
            mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(SWIPEREF_ID4);
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
            // this block maybe deprecated by the one above?
            if(Util.fullMenuObj.get(collegeNum).getBreakfast().isEmpty() &&
                Util.fullMenuObj.get(collegeNum).getLunch().isEmpty()
                && Util.fullMenuObj.get(collegeNum).getDinner().isEmpty()) {
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
            if (!Util.fullMenuObj.get(collegeNum).getBreakfast().isEmpty() &&
                mViewPager.getCurrentItem() == 0 &&
                Util.fullMenuObj.get(collegeNum).getBreakfast().get(0).getItemName()
                .equals(Util.brunchMessage)) {
                mViewPager.setCurrentItem(1,false);
            }

            // If only dinner available, set to dinner
            // (rare occurence, pretty much only on return from holidays)
            // Updated to reflect late night addition
            if (Util.fullMenuObj.get(collegeNum).getBreakfast().isEmpty() &&
                    Util.fullMenuObj.get(collegeNum).getLunch().isEmpty() &&
                    !Util.fullMenuObj.get(collegeNum).getDinner().isEmpty() &&
                    (Util.getCurrentMeal(collegeNum) < Util.DINNER)) {
                mViewPager.setCurrentItem(2,false);
            }

            mDrawerList = (ListView) getActivity().findViewById(R.id.left_drawer_list);
            mDrawerList.setAdapter(new ColorAdapter(getActivity(),
                    R.layout.drawer_list_item, Util.collegeList));

            setAdapter((ListView) getActivity().findViewById(LISTVIEW_ID1), collegeNum, Util.BREAKFAST);
            setAdapter((ListView) getActivity().findViewById(LISTVIEW_ID2), collegeNum, Util.LUNCH);
            setAdapter((ListView) getActivity().findViewById(LISTVIEW_ID3), collegeNum, Util.DINNER);
            setAdapter((ListView) getActivity().findViewById(LISTVIEW_ID4), collegeNum, Util.LATENIGHT);

            // Set title text
            setTitleText(collegeNum, ((AppCompatActivity)getActivity()).getSupportActionBar());
        }

    }

    class MenuAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 4;
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
            try {
                container.addView(view, mealnum);
            } catch (IndexOutOfBoundsException e) {
                return view;
            }

            ListView mealList = (ListView) view.findViewById(android.R.id.list);
            /*
             * set ID - add 12 in case 0 is something
             * This is to reference it later for setting
             * its contents
             */
            mealList.setId(mealnum + LISTVIEW_ID1);

            mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.meal_refresh_layout);
            //Add 16 instead of 12 for swipelayout
            mSwipeRefreshLayout.setId(mealnum + SWIPEREF_ID1);
            TypedArray a = mealList.getContext().getTheme().obtainStyledAttributes(new int[] {R.attr.tabSelector});
            mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(a.getResourceId(0, 0)));
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
            //mFab.attachToListView(mealList);

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
                final ViewGroup cont = container;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout = (SwipeRefreshLayout) cont.findViewById(nummeal +
                                SWIPEREF_ID1);
                        if (mSwipeRefreshLayout != null) {
                            mSwipeRefreshLayout.setRefreshing(true);
                        }
                    }
                });
                //initialRefreshed = true;
                new RetrieveMenuInFragmentTask(displayedMonth, displayedDay, displayedYear, true,
                        initialMeal, getActivity()).execute();
            }

            setAdapter(mealList, collegeNum, mealnum);
            return view;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private void setAdapter(ListView view, final int college, final int meal) {
        if (view == null) {
            return;
        }
        ArrayList<String> array = null;
        switch(meal) {
            case Util.BREAKFAST:
                array = Util.fullMenuObj.get(college).getBreakfastList();
                break;
            case Util.LUNCH:
                array = Util.fullMenuObj.get(college).getLunchList();
                break;
            case Util.DINNER:
                array = Util.fullMenuObj.get(college).getDinnerList();
                break;
            case Util.LATENIGHT:
                array = Util.fullMenuObj.get(college).getLateNightList();
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
                            intent.putExtra(Util.TAG_URL, Util.nuturlpart1 +
                                    MenuParser.URLPart2s[college] + displayedMonth +
                                    "%2F" + displayedDay + "%2F" + displayedYear +
                                    "&RecNumAndPort=" + Util.fullMenuObj.get(college)
                                    .getBreakfast().get(pos).getCode());
                            intent.putExtra(Util.TAG_COOKIE_COLLEGE, college);
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
                            intent.putExtra(Util.TAG_URL, Util.nuturlpart1 +
                                    MenuParser.URLPart2s[college] + displayedMonth +
                                    "%2F" + displayedDay + "%2F" + displayedYear +
                                    "&RecNumAndPort=" + Util.fullMenuObj.get(college)
                                    .getLunch().get(pos).getCode());
                            intent.putExtra(Util.TAG_COOKIE_COLLEGE, college);
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
                            intent.putExtra(Util.TAG_URL, Util.nuturlpart1 +
                                    MenuParser.URLPart2s[college] + displayedMonth +
                                    "%2F" + displayedDay + "%2F" + displayedYear +
                                    "&RecNumAndPort=" + Util.fullMenuObj.get(college)
                                    .getDinner().get(pos).getCode());
                            intent.putExtra(Util.TAG_COOKIE_COLLEGE, college);
                            startActivity(intent);
                        }
                    });
                    break;
                case Util.LATENIGHT:
                    view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                            if (Util.fullMenuObj.get(college).getLateNight().get(pos).getCode().compareTo(Util.RCNONUT) == 0) {
                                Toast.makeText(getActivity(), getString(R.string.rc_nonut),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Intent intent = new Intent(getActivity().getApplicationContext(),
                                        NutritionViewActivity.class);
                                intent.putExtra(Util.TAG_URL, Util.nuturlpart1 +
                                        MenuParser.URLPart2s[college] + displayedMonth +
                                        "%2F" + displayedDay + "%2F" + displayedYear +
                                        "&RecNumAndPort=" + Util.fullMenuObj.get(college)
                                        .getLateNight().get(pos).getCode());
                                intent.putExtra(Util.TAG_COOKIE_COLLEGE, college);
                                startActivity(intent);
                            }
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
        handler.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(SWIPEREF_ID4);
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

        if (Util.fullMenuObj.get(college).getIsCollegeNight()) {
            // Blue for College Night
            text.setSpan(new ForegroundColorSpan(Color.BLUE), 0, text.length(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else if (Util.fullMenuObj.get(college).getIsFarmFriday() ||
            Util.fullMenuObj.get(college).getIsHealthyMonday()) {
            // Green for Healthy Monday / Farm Friday
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.healthy)), 0,
                text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE); // 'Green Apple'
        } else {
            TypedArray a = bar.getThemedContext().getTheme().obtainStyledAttributes(new int[]
                    {R.attr.actionBarText});
            text.setSpan(new ForegroundColorSpan(getResources().getColor(a.getResourceId(0, 0))),
                    0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        bar.setTitle(text);
    }

}
