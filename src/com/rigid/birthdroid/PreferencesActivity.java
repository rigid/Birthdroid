package com.rigid.birthdroid;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
//import android.content.SharedPreferences;
//import android.preference.Preference;
import android.preference.PreferenceActivity;
//import android.preference.Preference.OnPreferenceClickListener;
import android.os.Bundle;
import android.util.Log;




public class PreferencesActivity extends PreferenceActivity
{
        private final static String TAG = "Birthdroid/PreferencesActivity";
        
        
        
        /** called by OS when app is created initially */
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);

                /** create prefs from xml */
                addPreferencesFromResource(R.xml.preferences);
        }

        /** another activity comes over this activity */
        @Override
        public void onPause()
        {
                /** update widget */
                Intent i = new Intent(this, BirthdroidWidget.class);
                i.setAction("com.rigid.birthdroid.PREFS_UPDATE");
                sendBroadcast(i);

                super.onPause();
        }
        
}
