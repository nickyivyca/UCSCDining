package com.nickivy.ucscdining;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.nickivy.ucscdining.util.Util;


/**
 * WebView activity used to show nutritional info, instead of going to external browser.
 *
 * @author Nicky Ivy parkedraccoon@gmail.com
 */
public class NutritionWebpageActivity extends AppCompatActivity {

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new NutritionFragment())
                .commit();*/
        setContentView(R.layout.web_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        url = getIntent().getStringExtra(Util.TAG_URL);
        WebView myWebView = (WebView) findViewById(R.id.nutrition_webview);
        //myWebView.loadUrl("http://www.com");
        myWebView.loadUrl(url);
    }
    /*public static class NutritionFragment extends Fragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }*/
}
