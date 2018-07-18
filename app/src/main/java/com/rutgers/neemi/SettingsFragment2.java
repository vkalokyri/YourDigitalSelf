package com.rutgers.neemi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class SettingsFragment2 extends PreferenceFragment {


    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals("facebook")) {
                    Preference connectionPref = findPreference(key);
                    boolean selected = prefs.getBoolean("facebook", true);
                    if(selected) {
                        connectionPref.setSummary("ITS SELECTED");
                        Intent myIntent = new Intent(getActivity(), FacebookActivity.class);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(myIntent);
                    }else{
                        connectionPref.setSummary("It's deselected");
                    }

                }
                else if (key.equals("gcal")) {
                    Preference connectionPref = findPreference(key);
                    boolean selected = prefs.getBoolean("gcal", true);
                    if(selected) {
                        connectionPref.setSummary("ITS SELECTED");
                        Intent myIntent = new Intent(getActivity(), GcalActivity.class);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(myIntent);
                    }else{
                        connectionPref.setSummary("It's deselected");
                    }
                }
                else if (key.equals("gmail")) {
                    Preference connectionPref = findPreference(key);
                    boolean selected = prefs.getBoolean("gmail", true);
                    if(selected) {
                        connectionPref.setSummary("ITS SELECTED");
                        Intent myIntent = new Intent(getActivity(), GmailActivity.class);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(myIntent);
                    }else{
                        connectionPref.setSummary("It's deselected");
                    }

                }
                else if (key.equals("instagram")) {
                    Preference connectionPref = findPreference(key);
                    boolean selected = prefs.getBoolean("instagram", true);
                    if(selected) {
                        connectionPref.setSummary("ITS SELECTED");
                        Intent myIntent = new Intent(getActivity(), InstagramActivity.class);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(myIntent);
                    }else{
                        connectionPref.setSummary("It's deselected");
                    }

                }

            }
    };


    @Override
    public void onCreate(Bundle savsavedInstanceState){
        super.onCreate(savsavedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(listener);


    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(listener);
    }

}