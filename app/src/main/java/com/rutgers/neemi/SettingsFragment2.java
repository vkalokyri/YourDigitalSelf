package com.rutgers.neemi;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rutgers.neemi.util.ObscuredSharedPreferences;

import java.util.ArrayList;


public class SettingsFragment2 extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    private CheckBoxPreference fbPreference;
    private CheckBoxPreference instagramPreference;
    private CheckBoxPreference gmailPreference;
    private ListPreference gdrivePreference;
    private CheckBoxPreference gcalPreference;
    private CheckBoxPreference gpsPreference;
    private CheckBoxPreference plaidPreference;

    AlertDialog.Builder builder;



//    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
//        @Override
//        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
//                if (key.equals("facebook")) {
//                    Preference connectionPref = findPreference(key);
//
//                    boolean selected = prefs.getBoolean("facebook", true);
//                    if(selected) {
//                        connectionPref.setSummary("ITS SELECTED");
//                        Intent myIntent = new Intent(getActivity(), FacebookActivity.class);
//                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(myIntent);
//                    }else{
//                        connectionPref.setSummary("It's deselected");
//                    }
//
//                }
//                else if (key.equals("gcal")) {
//                    Preference connectionPref = findPreference(key);
//                    boolean selected = prefs.getBoolean("gcal", true);
//                    if(selected) {
//                        connectionPref.setSummary("ITS SELECTED");
//                        Intent myIntent = new Intent(getActivity(), GcalActivity.class);
//                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(myIntent);
//                    }else{
//                        connectionPref.setSummary("It's deselected");
//                    }
//                }
//                else if (key.equals("gmail")) {
//                    Preference connectionPref = findPreference(key);
//                    boolean selected = prefs.getBoolean("gmail", true);
//                    if(selected) {
//                        connectionPref.setSummary("ITS SELECTED");
//                        Intent myIntent = new Intent(getActivity(), GmailActivity.class);
//                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(myIntent);
//                    }else{
//                        connectionPref.setSummary("It's deselected");
//                    }
//
//                }
//                else if (key.equals("instagram")) {
//                    Preference connectionPref = findPreference(key);
//                    boolean selected = prefs.getBoolean("instagram", true);
//                    if(selected) {
//                        connectionPref.setSummary("YourDigitalSelf was authorized to access your Instagram account. If you want to revoke access please go to: www.instagram.com>Settings>AuthorizedApps and select \"Revoke Access\" ");
//                        connectionPref.setSelectable(false);
//                        Intent myIntent = new Intent(getActivity(), InstagramActivity.class);
//                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(myIntent);
//                    }
//
//                }
//
//            }
//    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        boolean isEnabled = prefs.getBoolean("instagram", false);
        System.out.println("SETTINGS FRAGMENT = "+ isEnabled);
        instagramPreference = (CheckBoxPreference) getPreferenceScreen().findPreference("instagram");
        instagramPreference.setSelectable(!isEnabled);
        //instagramPreference.setSelectable(!isEnabled);

        fbPreference = (CheckBoxPreference) getPreferenceScreen().findPreference("facebook");
        gmailPreference = (CheckBoxPreference) getPreferenceScreen().findPreference("gmail");
        gdrivePreference = (ListPreference) getPreferenceScreen().findPreference("gdrive");
        gcalPreference = (CheckBoxPreference) getPreferenceScreen().findPreference("gcal");
        plaidPreference = (CheckBoxPreference) getPreferenceScreen().findPreference("plaid");
        gpsPreference = (CheckBoxPreference) getPreferenceScreen().findPreference("gps");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getActivity());
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("instagram")) {
            if (instagramPreference.isChecked()) {
                Intent myIntent = new Intent(getActivity(), InstagramActivity.class);
                myIntent.putExtra("action", "grant");
                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(myIntent);
            }

        }else if (key.equals("facebook")) {
            if (fbPreference.isChecked()) {
                Intent myIntent = new Intent(getActivity(), FacebookActivity.class);
                myIntent.putExtra("action", "grant");
                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(myIntent);
            } if (!fbPreference.isChecked()) {
                builder.setTitle("Revoke Permissions")
                        .setMessage("Are you sure you want to revoke all permissions?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent myIntent = new Intent(getActivity(), FacebookActivity.class);
                                myIntent.putExtra("action", "revoke");
                                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(myIntent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                fbPreference.setChecked(true);
                                Intent myIntent = new Intent(getActivity(), MainActivity.class);
                                myIntent.putExtra("key", "facebook");
                                myIntent.putExtra("items", 0);
                                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(myIntent);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }else if (key.equals("gmail")) {
            if (gmailPreference.isChecked()) {
                builder.setTitle("Grant Permissions")
                        .setMessage("If you grant permissions for the Gmail, access to Google Calendar will be granted as well. \n Are you sure you want to grant these permissions?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent myIntent = new Intent(getActivity(), GmailActivity.class);
                                myIntent.putExtra("action", "grant");
                                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(myIntent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                gmailPreference.setChecked(false);
                                Intent myIntent = new Intent(getActivity(), MainActivity.class);
                                myIntent.putExtra("key", "gmail");
                                myIntent.putExtra("items", 0);
                                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(myIntent);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } if (!gmailPreference.isChecked()) {
                builder.setTitle("Revoke Permissions")
                        .setMessage("If you revoke your permissions from Gmail, access to ALL Google apps will be revoked simultaneously. \n Are you sure you want to revoke all permissions?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent myIntent = new Intent(getActivity(), GmailActivity.class);
                                myIntent.putExtra("action", "revoke");
                                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(myIntent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                gmailPreference.setChecked(true);
                                Intent myIntent = new Intent(getActivity(), MainActivity.class);
                                myIntent.putExtra("key", "gmail");
                                myIntent.putExtra("items", 0);
                                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(myIntent);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }else if (key.equals("gcal")) {
            if (gcalPreference.isChecked()) {
                builder.setTitle("Grant Permissions")
                        .setMessage("If you grant permissions for Google Calendar, access to Gmail will be granted as well. \n Are you sure you want to grant these permissions?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent myIntent = new Intent(getActivity(), GcalActivity.class);
                                myIntent.putExtra("action", "grant");
                                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(myIntent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                gcalPreference.setChecked(false);
                                Intent myIntent = new Intent(getActivity(), MainActivity.class);
                                myIntent.putExtra("key", "gmail");
                                myIntent.putExtra("items", 0);
                                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(myIntent);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } if (!gcalPreference.isChecked()) {
                builder.setTitle("Revoke Permissions")
                        .setMessage("If you revoke your permissions from Google calendar, access to ALL Google apps will be revoked simultaneously. \n Are you sure you want to revoke all permissions?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent myIntent = new Intent(getActivity(), GcalActivity.class);
                                myIntent.putExtra("action", "revoke");
                                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(myIntent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                gcalPreference.setChecked(true);
                                Intent myIntent = new Intent(getActivity(), MainActivity.class);
                                myIntent.putExtra("key", "gcal");
                                myIntent.putExtra("items", 0);
                                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(myIntent);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }


    }



}