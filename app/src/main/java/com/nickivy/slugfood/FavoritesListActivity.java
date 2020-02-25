package com.nickivy.slugfood;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.HashSet;
import java.util.Set;

/**
 * Fragment for displaying and modifying the favorite items in prefs.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author Nicky Ivy parkedraccoon@gmail.com
 */

public class FavoritesListActivity extends AppCompatActivity {

    int theme = 0;

    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Set theme based on pref
        theme = prefs.getBoolean("dark_theme", false) ? R.style.MainTheme_Dark :
                R.style.MainTheme_Colorful;
        setTheme(theme);
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.favorites_list);
        final ListView list = (ListView) findViewById(R.id.favorites_list);

        String[] favsList = prefs.getStringSet("favorites_items_list", new HashSet<String>()).toArray(new String[0]);
        list.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_activated_1,
                favsList));
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                final int fPos = pos;
                final Context fContext = arg0.getContext();
                AlertDialog.Builder alertBuild = new AlertDialog.Builder(arg0.getContext());
                alertBuild.setMessage(R.string.favorites_confirm_delete);
                alertBuild.setPositiveButton(R.string.favorites_confirm_delete_pos,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(fContext);

                                Set<String> favorites = prefs.getStringSet("favorites_items_list",
                                        new HashSet<String>());

                                favorites.remove(favorites.toArray()[fPos]);
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.remove("favorites_items_list");
                                edit.commit();
                                edit.putStringSet("favorites_items_list", favorites);
                                edit.commit();

                                String[] favsList = prefs.getStringSet("favorites_items_list", new HashSet<String>()).toArray(new String[0]);
                                list.setAdapter(new ArrayAdapter<String>(fContext,
                                        android.R.layout.simple_list_item_activated_1,
                                        favsList));
                            }
                        });
                alertBuild.setNegativeButton(R.string.favorites_confirm_delete_neg,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // just exit dialog
                            }
                        });

                AlertDialog alert11 = alertBuild.create();
                alert11.show();

                return true;
            }
        });

        final Context tContext = getSupportActionBar().getThemedContext();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(prefs.getBoolean("dark_theme",false)?
                ContextCompat.getColorStateList(this, R.color.fab_ripple_color_dark) :
                ContextCompat.getColorStateList(this, R.color.fab_ripple_color));
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alertBuild = new AlertDialog.Builder(tContext);
                final LayoutInflater inflater = getLayoutInflater();
                alertBuild.setView(R.layout.add_favorite);
                alertBuild.setMessage(R.string.favorites_add);
                alertBuild.setPositiveButton(R.string.favorites_add_pos,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(tContext);

                                Set<String> favorites = prefs.getStringSet("favorites_items_list",
                                        new HashSet<String>());
                                Dialog f = (Dialog) dialog;
                                EditText input = (EditText) f.findViewById(R.id.addFavDialogText);
                                favorites.add(input.getText().toString());
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.remove("favorites_items_list");
                                edit.commit();
                                edit.putStringSet("favorites_items_list", favorites);
                                edit.commit();

                                String[] favsList = prefs.getStringSet("favorites_items_list", new HashSet<String>()).toArray(new String[0]);
                                list.setAdapter(new ArrayAdapter<String>(tContext,
                                        android.R.layout.simple_list_item_activated_1,
                                        favsList));
                            }
                        });
                alertBuild.setNegativeButton(R.string.favorites_confirm_delete_neg,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // just exit dialog
                            }
                        });

                AlertDialog alert11 = alertBuild.create();
                alert11.show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Set theme based on pref
        if (theme != (prefs.getBoolean("dark_theme",false)? R.style.MainTheme_Dark :
                R.style.MainTheme_Colorful)) {
            recreate();
        }
    }
}