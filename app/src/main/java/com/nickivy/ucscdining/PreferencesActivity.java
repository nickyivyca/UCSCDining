package com.nickivy.ucscdining;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;

/**
 * Displays some info about the app, as well as option to look at license information for the app.
 *
 * <p>May actually hold preferences later, but currently only displays the info.
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
            pref.setTitle(getString(R.string.app_name)+ " 0.9.4");
        }
    }
}
