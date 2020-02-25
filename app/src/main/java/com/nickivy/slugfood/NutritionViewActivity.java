package com.nickivy.slugfood;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nickivy.slugfood.util.Util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Class for displaying the custom-formatted nutrition info instead of using a webview. The
 * webview does not work well on phones, and on any device the Back and Print buttons can be
 * confusing.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author @author Nicky Ivy parkedraccoon@gmail.com
 */
public class NutritionViewActivity extends AppCompatActivity {

    String items[] = new String[40];
    String title, servingsize, calories, calories_fat, totalfat_amount, satfat, satfat_percent,
            transfat, cholesterol_amount, cholesterol_percent, sodium_amount, sodium_percent,
            carbs_amount, carbs_percent, fiber_amount, fiber_percent, sugars, protein_amount,
            protein_percent,
            lowerpercent1, lowerpercent2, lowerpercent3, lowerpercent4, lowerpercent5,
            lowerpercent6, totalfat_percent,
            ingredients_list, allergens_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Set theme based on pref
        setTheme(prefs.getBoolean("dark_theme", false) ? R.style.MainTheme_Dark :
                R.style.MainTheme_Colorful);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nutrition_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView view = (TextView) findViewById(R.id.nutrition_item_title);
        view.setText("");
        view = (TextView) findViewById(R.id.nutrition_title);
        view.setText("Nutrition Facts");
        view = (TextView) findViewById(R.id.nutrition_servingsize);
        view.setText("Serving Size");
        view = (TextView) findViewById(R.id.nutrition_amountperserving);
        view.setText("Amount Per Serving");
        view = (TextView) findViewById(R.id.nutrition_calories);
        view.setText("Calories");
        view = (TextView) findViewById(R.id.nutrition_calories_fat);
        view.setText("Calories from Fat");
        view = (TextView) findViewById(R.id.nutrition_percentdailyvalues);
        view.setText("% Daily Values*");
        view = (TextView) findViewById(R.id.nutrition_totalfat);
        view.setText("Total Fat ");
        view = (TextView) findViewById(R.id.nutrition_satfat);
        view.setText("Saturated Fat");
        view = (TextView) findViewById(R.id.nutrition_transfat);
        view.setText("Trans Fat");
        view = (TextView) findViewById(R.id.nutrition_cholesterol);
        view.setText("Cholesterol ");
        view = (TextView) findViewById(R.id.nutrition_cholesterol_amount);
        //view.setText("99.1mg");
        view = (TextView) findViewById(R.id.nutrition_sodium);
        view.setText("Sodium ");
        view = (TextView) findViewById(R.id.nutrition_carbs);
        view.setText("Total Carb. ");
        view = (TextView) findViewById(R.id.nutrition_fiber);
        view.setText("Dietary Fiber ");
        view = (TextView) findViewById(R.id.nutrition_sugars);
        view.setText("Sugars");
        view = (TextView) findViewById(R.id.nutrition_protein);
        view.setText("Protein ");
        view = (TextView) findViewById(R.id.nutrition_lowerpercent1);
        view.setText("Vitamin D - mcg");
        view = (TextView) findViewById(R.id.nutrition_lowerpercent2);
        view.setText("Calcium");
        view = (TextView) findViewById(R.id.nutrition_lowerpercent3);
        view.setText("Iron");
        view = (TextView) findViewById(R.id.nutrition_lowerpercent4);
        view.setText("Potassium");
        view = (TextView) findViewById(R.id.nutrition_lowerpercent5);
        view.setText("Added Sugar");
        view = (TextView) findViewById(R.id.nutrition_lowerpercent6);
        view.setText("");
        view = (TextView) findViewById(R.id.nutrition_ingredients);
        view.setText("INGREDIENTS: ");
        view = (TextView) findViewById(R.id.nutrition_allergens);
        view.setText("ALLERGENS: ");
        view = (TextView) findViewById(R.id.nutrition_percentdvnotice);
        view.setText("*Percent Daily Values (DV) are based on a 2,000 calorie diet.");
        new RetrieveNutritionTask(this, getIntent().getStringExtra(Util.TAG_URL),
                getIntent().getIntExtra(Util.TAG_COOKIE_COLLEGE, 0)).execute();
    }


    private class RetrieveNutritionTask extends AsyncTask<Void, Void, Integer> {

        private Activity mActivity;
        private String mUrl;
        private int mCookieLoc;

        /**
         * @param activity activity to set text in
         * @param url url for page of nutrition info
         */
        public RetrieveNutritionTask(Activity activity, String url, int cookieloc) {
            mActivity = activity;
            mUrl = url;
            mCookieLoc = cookieloc;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            Document page;
            try {
                page = Jsoup.connect(mUrl).cookie("WebInaCartLocation", Util.CookieLocations[mCookieLoc])
                        .cookie("WebInaCartDates", "").cookie("WebInaCartMeals", "")
                        .cookie("WebInaCartQtys", "").cookie("WebInaCartRecipes", "").get();
            } catch (UnknownHostException e) {
                // Internet connection completely missing is a separate error from okhttp
                Log.v(Util.LOGTAG, "Internet connection missing");
                e.printStackTrace();
                return Util.GETLIST_INTERNET_FAILURE;
            } catch (IOException e) {
                Log.w(Util.LOGTAG, "connection error");
                try {
                    page = Jsoup.connect(mUrl).cookie("WebInaCartLocation", Util.CookieLocations[mCookieLoc])
                            .cookie("WebInaCartDates", "").cookie("WebInaCartMeals", "")
                            .cookie("WebInaCartQtys", "").cookie("WebInaCartRecipes", "").get();
                } catch (IOException e1) {
                    Log.w(Util.LOGTAG, "connection error");
                    try {
                        page = Jsoup.connect(mUrl).cookie("WebInaCartLocation", Util.CookieLocations[mCookieLoc])
                                .cookie("WebInaCartDates", "").cookie("WebInaCartMeals", "")
                                .cookie("WebInaCartQtys", "").cookie("WebInaCartRecipes", "").get();
                    } catch (IOException e2) {
                        Log.w(Util.LOGTAG, "connection error");
                        try {
                            page = Jsoup.connect(mUrl).cookie("WebInaCartLocation", Util.CookieLocations[mCookieLoc])
                                    .cookie("WebInaCartDates", "").cookie("WebInaCartMeals", "")
                                    .cookie("WebInaCartQtys", "").cookie("WebInaCartRecipes", "").get();
                        } catch (IOException e3) {
                            Log.w(Util.LOGTAG, "connection error");
                            // Give up after four times
                            return Util.GETLIST_OKHTTP_FAILURE;
                        }
                    }
                }
            }

            Elements fonts = page.select("font");

            /*
             * By looking carefully through the source page for
             * the nutrition info, you can find the instance of each tag where data is stored.
             */
            try {
                title = page.select("div").get(0).text();

                /**
                 * For whatever reason, the dining hall pages are all perfectly consistent -
                 * EXCEPT for a few items. So I set up 2 different ways to do this. Originally, this
                 * was only for Chicken Nuggets but it turns out this happens for more items than
                 * just chicken nuggets.
                 */

                servingsize = "Serving Size " + fonts.get(2).text();
                calories = page.select("b").get(1).text();
                calories_fat = fonts.get(4).text();
                totalfat_amount = fonts.get(11).text();
                totalfat_percent = fonts.get(12).text();
                satfat = "Saturated Fat " + fonts.get(19).text();
                satfat_percent = fonts.get(20).text() + "%";
                transfat = "Trans Fat " + fonts.get(27).text();
                cholesterol_amount = fonts.get(33).text();
                cholesterol_percent = fonts.get(34).text() + "%";
                sodium_amount = fonts.get(40).text();
                sodium_percent = fonts.get(41).text() + "%";
                carbs_amount = fonts.get(15).text();
                carbs_percent = fonts.get(16).text() + "%";
                fiber_amount = fonts.get(23).text();
                fiber_percent = fonts.get(24).text() + "%";
                sugars = "Sugars " + fonts.get(30).text();

                protein_amount = fonts.get(37).text();

                //protein_percent = fonts.get(47).text().substring(1); // protein percent not given anymore?
                /*
                 * Percents appearing below normal list. Strangely, the page includes 2 often different
                 * amounts of sodium and fiber.
                 */
                lowerpercent1 = "Vitamin D - mcg " + fonts.get(46).text();
                lowerpercent2 = "Calcium " + fonts.get(48).text().substring(1);
                lowerpercent3 = "Iron " + fonts.get(50).text();
                lowerpercent4 = "Potassium " + fonts.get(52).text();
                lowerpercent5 = "Added Sugar " + fonts.get(54).text();
                //lowerpercent6 = "Dietary Fiber " + fonts.get(51).text();

                ingredients_list = page.select("span").get(1).text();
                allergens_list = page.select("span").get(3).text();


            } catch (java.lang.IndexOutOfBoundsException e2) {
                e2.printStackTrace();
                return Util.GETNUT_NO_INFO;
            }
            return Util.GETLIST_SUCCESS;
        }

        protected void onPostExecute(Integer result) {
            if (result == Util.GETNUT_NO_INFO) {
                Toast.makeText(mActivity, getString(R.string.no_nutrition_info),
                        Toast.LENGTH_SHORT).show();
                // Hide only the spinner
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView view = (TextView) mActivity.findViewById(R.id.nutrition_item_title);
                        view.setText(title);
                        View spinner = mActivity.findViewById(R.id.nutrition_progresscircle);
                        spinner.setVisibility(View.INVISIBLE);
                    }
                });
            } else if (result == Util.GETLIST_OKHTTP_FAILURE || result == Util.GETLIST_INTERNET_FAILURE) {
                Toast.makeText(mActivity, getString(R.string.internet_failed),
                        Toast.LENGTH_SHORT).show();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Hide spinner after internet failure
                        View spinner = mActivity.findViewById(R.id.nutrition_progresscircle);
                        spinner.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView view = (TextView) mActivity.findViewById(R.id.nutrition_item_title);
                        view.setText(title);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_servingsize);
                        view.setText(servingsize);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_calories);
                        view.setText(calories);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_calories_fat);
                        view.setText(calories_fat);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_totalfat_amount);
                        view.setText(totalfat_amount);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_totalfat_percent);
                        view.setText(totalfat_percent);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_satfat);
                        view.setText(satfat);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_satfat_percent);
                        view.setText(satfat_percent);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_transfat);
                        view.setText(transfat);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_cholesterol_amount);
                        view.setText(cholesterol_amount);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_cholesterol_percent);
                        view.setText(cholesterol_percent);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_sodium_amount);
                        view.setText(sodium_amount);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_sodium_percent);
                        view.setText(sodium_percent);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_carbs_amount);
                        view.setText(carbs_amount);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_carbs_percent);
                        view.setText(carbs_percent);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_fiber_amount);
                        view.setText(fiber_amount);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_fiber_percent);
                        view.setText(fiber_percent);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_sugars);
                        view.setText(sugars);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_protein_amount);
                        view.setText(protein_amount);

                        view = (TextView) mActivity.findViewById(R.id.nutrition_lowerpercent1);
                        view.setText(lowerpercent1);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_lowerpercent2);
                        view.setText(lowerpercent2);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_lowerpercent3);
                        view.setText(lowerpercent3);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_lowerpercent4);
                        view.setText(lowerpercent4);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_lowerpercent5);
                        view.setText(lowerpercent5);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_lowerpercent6);
                        view.setText(lowerpercent6);

                        view = (TextView) mActivity.findViewById(R.id.nutrition_ingredients_list);
                        view.setText(ingredients_list);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_allergens_list);
                        view.setText(allergens_list);

                        View spinner = mActivity.findViewById(R.id.nutrition_progresscircle);
                        spinner.setVisibility(View.INVISIBLE);
                    }
                });
            }

        }
    }
}
