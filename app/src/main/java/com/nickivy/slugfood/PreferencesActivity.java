package com.nickivy.slugfood;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

import com.nickivy.slugfood.util.Util;

/**
 * Displays some info about the app, as well as option to look at license information for the app.
 *
 * Also display default college preferences.
 *
 * <p>Not using AppCompatActivity because preferencefragment isn't in support library.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author Nicky Ivy parkedraccoon@gmail.com
 */
public class PreferencesActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Set theme based on pref
        setTheme(prefs.getBoolean("dark_theme",false)? R.style.MainTheme_Dark :
                R.style.MainTheme_Colorful);
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);
            Preference pref = findPreference("mylicense");
            // Display the verison number once BuildConfig.VERSION_NAME works. Says 1.3 on bug page?
            //pref.setTitle(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
            pref.setTitle(getString(R.string.app_name)+ " 1.3");

            pref = findPreference("dark_theme");
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    getActivity().recreate();
                    return true;
                }
            });
            // Gray out background load preference if widget/notifications enabled
            pref = findPreference("background_load");
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Intent intent = new Intent(getActivity(), BackgroundLoader.class);
                    intent.setAction(((Boolean) o)? Util.TAG_ENABLEBACKGROUND :
                            Util.TAG_DISABLEBACKGROUND);
                    getActivity().sendBroadcast(intent);
                    return true;
                }
            });
            pref.setShouldDisableView(true);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            pref.setEnabled(!(prefs.getBoolean("widget_enabled", false) ||
                    prefs.getBoolean("notifications_enabled", false)));
        }
    }
}
