package com.nickivy.slugfood;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.nickivy.slugfood.util.Util;


/**
 * WebView activity used to show nutritional info, instead of going to external browser.
 *
 * @author Nicky Ivy parkedraccoon@gmail.com
 */
public class NutritionWebpageActivity extends AppCompatActivity {

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Set theme based on pref
        setTheme(prefs.getBoolean("dark_theme",false)? R.style.MainTheme_Dark :
                R.style.MainTheme_Colorful);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        url = getIntent().getStringExtra(Util.TAG_URL);
        WebView myWebView = (WebView) findViewById(R.id.nutrition_webview);
        myWebView.loadUrl(url);
    }
}
