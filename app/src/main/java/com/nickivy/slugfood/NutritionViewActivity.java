package com.nickivy.slugfood;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
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
            percent_vitamina, percent_vitb12, percent_vitaminc, percent_iron, percent_sodium,
            percent_fiber,
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
        view = (TextView) findViewById(R.id.nutrition_percent_vitaminc);
        view.setText("Vitamin C");
        view = (TextView) findViewById(R.id.nutrition_percent_iron);
        view.setText("Iron");
        view = (TextView) findViewById(R.id.nutrition_percent_vitb12);
        view.setText("Vitamin B12");
        view = (TextView) findViewById(R.id.nutrition_percent_sodium);
        view.setText("Sodium");
        view = (TextView) findViewById(R.id.nutrition_percent_fiber);
        view.setText("Dietary Fiber");
        view = (TextView) findViewById(R.id.nutrition_percent_vitamina);
        view.setText("Vitamin A - IU");
        view = (TextView) findViewById(R.id.nutrition_ingredients);
        view.setText("INGREDIENTS: ");
        view = (TextView) findViewById(R.id.nutrition_allergens);
        view.setText("ALLERGENS: ");
        view = (TextView) findViewById(R.id.nutrition_percentdvnotice);
        view.setText("*Percent Daily Values (DV) are based on a 2,000 calorie diet.");
        new RetrieveNutritionTask(this, getIntent().getStringExtra(Util.TAG_URL)).execute();
    }


    private class RetrieveNutritionTask extends AsyncTask<Void, Void, Integer> {

        private Activity mActivity;
        private String mUrl;

        /**
         * @param activity activity to set text in
         * @param url url for page of nutrition info
         */
        public RetrieveNutritionTask(Activity activity, String url) {
            mActivity = activity;
            mUrl = url;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            Document page;
            try {
                page = Jsoup.connect(mUrl).get();
            } catch (UnknownHostException e) {
                // Internet connection completely missing is a separate error from okhttp
                Log.v(Util.LOGTAG, "Internet connection missing");
                e.printStackTrace();
                return Util.GETLIST_INTERNET_FAILURE;
            } catch (IOException e) {
                Log.w(Util.LOGTAG, "connection error");
                try {
                    page = Jsoup.connect(mUrl).get();
                } catch (IOException e1) {
                    Log.w(Util.LOGTAG, "connection error");
                    try {
                        page = Jsoup.connect(mUrl).get();
                    } catch (IOException e2) {
                        Log.w(Util.LOGTAG, "connection error");
                        try {
                            page = Jsoup.connect(mUrl).get();
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
                 * EXCEPT for Chicken Nuggets. So since it's a well-liked item I figured I might
                 * as well make a fix for it.
                 *
                 * The difference is that on Chicken Nuggets, the Dietary Fiber and Sugars
                 * tags use different tag formatting. Dietary fiber uses 2 instead of 4, and sugars
                 * uses 1 instead of 2.
                 */
                if (title.equals("Chicken Nuggets")) {
                    servingsize = "Serving Size " + fonts.get(2).text();
                    calories = page.select("b").get(1).text();
                    calories_fat = fonts.get(4).text();
                    totalfat_amount = fonts.get(11).text();
                    satfat = "Saturated Fat " + fonts.get(18).text();
                    satfat_percent = fonts.get(52).text().substring(1);
                    transfat = "Trans Fat " + fonts.get(23).text();
                    cholesterol_amount = fonts.get(28).text();
                    cholesterol_percent = fonts.get(56).text().substring(1);
                    sodium_amount = fonts.get(34).text();
                    sodium_percent = fonts.get(35).text() + "%";
                    carbs_amount = fonts.get(14).text();
                    carbs_percent = fonts.get(15).text() + "%";
                    fiber_amount = "- - -";
                    fiber_percent = fonts.get(21).text() + "%";
                    sugars = "Sugars " + "- - - g";

                    protein_amount = fonts.get(31).text();
                    protein_percent = fonts.get(56).text().substring(1);

                    /*
                     * Percents appearing below normal list. Strangely, the page includes 2 often different
                     * amounts of sodium and fiber.
                     */
                    percent_vitamina = "Vitamin A - IU" + fonts.get(62).text().substring(1);
                    percent_vitb12 = "Vitamin B12" + fonts.get(66).text().substring(1);
                    percent_vitaminc = "Vitamin C" + fonts.get(64).text().substring(1);
                    percent_iron = "Iron" + fonts.get(58).text().substring(1);
                    percent_sodium = "Sodium" + fonts.get(60).text().substring(1);
                    percent_fiber = "Dietary Fiber" + fonts.get(46).text().substring(1);

                    ingredients_list = page.select("span").get(1).text();
                    allergens_list = page.select("span").get(3).text();
                } else {
                    servingsize = "Serving Size " + fonts.get(2).text();
                    calories = page.select("b").get(1).text();
                    calories_fat = fonts.get(4).text();
                    totalfat_amount = fonts.get(11).text();
                    satfat = "Saturated Fat " + fonts.get(18).text();
                    satfat_percent = fonts.get(55).text().substring(1);
                    transfat = "Trans Fat " + fonts.get(25).text();
                    cholesterol_amount = fonts.get(31).text();
                    cholesterol_percent = fonts.get(59).text().substring(1);
                    sodium_amount = fonts.get(37).text();
                    sodium_percent = fonts.get(38).text() + "%";
                    carbs_amount = fonts.get(14).text();
                    carbs_percent = fonts.get(15).text() + "%";
                    fiber_amount = fonts.get(21).text();
                    fiber_percent = fonts.get(22).text() + "%";
                    sugars = "Sugars " + fonts.get(28).text();

                    protein_amount = fonts.get(34).text();
                    protein_percent = fonts.get(59).text().substring(1);

                    /*
                     * Percents appearing below normal list. Strangely, the page includes 2 often different
                     * amounts of sodium and fiber.
                     */
                    percent_vitamina = "Vitamin A - IU" + fonts.get(65).text().substring(1);
                    percent_vitb12 = "Vitamin B12" + fonts.get(69).text().substring(1);
                    percent_vitaminc = "Vitamin C" + fonts.get(67).text().substring(1);
                    percent_iron = "Iron" + fonts.get(61).text().substring(1);
                    percent_sodium = "Sodium" + fonts.get(63).text().substring(1);
                    percent_fiber = "Dietary Fiber" + fonts.get(49).text().substring(1);

                    ingredients_list = page.select("span").get(1).text();
                    allergens_list = page.select("span").get(3).text();
                }


            } catch(java.lang.IndexOutOfBoundsException e) {
                e.printStackTrace();
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

                        view = (TextView) mActivity.findViewById(R.id.nutrition_percent_vitamina);
                        view.setText(percent_vitamina);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_percent_vitb12);
                        view.setText(percent_vitb12);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_percent_vitaminc);
                        view.setText(percent_vitaminc);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_percent_iron);
                        view.setText(percent_iron);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_percent_sodium);
                        view.setText(percent_sodium);
                        view = (TextView) mActivity.findViewById(R.id.nutrition_percent_fiber);
                        view.setText(percent_fiber);

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
