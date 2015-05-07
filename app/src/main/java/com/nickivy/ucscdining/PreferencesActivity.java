package com.nickivy.ucscdining;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;

/**
 * Displays some info about the app, as well as option to look at license information for the app.
 *
 * May actually hold preferences later, but currently only displays the info.
 *
 * Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author Nick Ivy parkedraccoon@gmail.com
 */
public class PreferencesActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            Preference pref = findPreference("mydesc");
            // Display the verison number once BuildConfig.VERSION_NAME works. Says 1.3 on bug page?
            //pref.setTitle(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
            pref.setTitle(getString(R.string.app_name));
        }
    }
}
