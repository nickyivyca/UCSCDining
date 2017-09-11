package com.nickivy.slugfood;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.nickivy.slugfood.util.Util;

/**
 * Displays some info about the app, as well as option to look at license information for the app.
 *
 * Also display default college preferences.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author Nicky Ivy parkedraccoon@gmail.com
 */
public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Set theme based on pref
        setTheme(prefs.getBoolean("dark_theme", false) ? R.style.MainTheme_Dark :
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
            Preference namePref = findPreference("mylicense");
            namePref.setTitle(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);

            Preference darkPref = findPreference("dark_theme");
            darkPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    getActivity().recreate();
                    return true;
                }
            });
            // Gray out background load preference if widget/notifications enabled
            Preference bgLoadPref = findPreference("background_load");
            bgLoadPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Intent intent = new Intent(getActivity(), BackgroundLoader.class);
                    intent.setAction(((Boolean) o) ? Util.TAG_ENABLEBACKGROUND :
                            Util.TAG_DISABLEBACKGROUND);
                    getActivity().sendBroadcast(intent);
                    /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    Preference bgLoadPref = findPreference("background_load");
                    bgLoadPref.setEnabled(!(prefs.getBoolean("widget_enabled", false) ||
                            prefs.getBoolean("notifications_events", false) ||
                            prefs.getBoolean("notifications_favorites", false)));*/
                    return true;
                }
            });
            bgLoadPref.setShouldDisableView(true);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            bgLoadPref.setEnabled(!(prefs.getBoolean("widget_enabled", false) ||
                    prefs.getBoolean("notifications_events", false) ||
                    prefs.getBoolean("notifications_favorites", false)));
            //bgLoadPref.setEnabled(true);

            Preference notifEventsPref = findPreference("notifications_events");
            notifEventsPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Intent intent = new Intent(getActivity(), BackgroundLoader.class);
                    intent.setAction(((Boolean) o) ? Util.TAG_NOTIFICATIONSON :
                            Util.TAG_NOTIFICATIONSOFF);
                    getActivity().sendBroadcast(intent);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    CheckBoxPreference bgLoadPref = (CheckBoxPreference) findPreference("background_load");
                    if ((Boolean) o) {
                        bgLoadPref.setChecked(true);
                        intent = new Intent(getActivity(), BackgroundLoader.class);
                        intent.setAction(Util.TAG_ENABLEBACKGROUND);
                        getActivity().sendBroadcast(intent);
                    }
                    bgLoadPref.setEnabled(!(prefs.getBoolean("widget_enabled", false) ||
                            prefs.getBoolean("notifications_favorites", false) ||
                            (Boolean) o));
                    return true;
                }
            });

            Preference notifFavsPref = findPreference("notifications_favorites");
            notifFavsPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Intent intent = new Intent(getActivity(), BackgroundLoader.class);
                    intent.setAction(((Boolean) o) ? Util.TAG_NOTIFICATIONSON :
                            Util.TAG_NOTIFICATIONSOFF);
                    getActivity().sendBroadcast(intent);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    CheckBoxPreference bgLoadPref = (CheckBoxPreference) findPreference("background_load");
                    if ((Boolean) o) {
                        bgLoadPref.setChecked(true);
                        intent = new Intent(getActivity(), BackgroundLoader.class);
                        intent.setAction(Util.TAG_ENABLEBACKGROUND);
                        getActivity().sendBroadcast(intent);
                    }
                    bgLoadPref.setEnabled(!(prefs.getBoolean("widget_enabled", false) ||
                            prefs.getBoolean("notifications_events", false) ||
                            (Boolean) o));
                    return true;
                }
            });

            Preference favsListPref = findPreference("favslist");
            favsListPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity().getApplicationContext(),
                            FavoritesListActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }
    }
}