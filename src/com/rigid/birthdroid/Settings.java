package com.rigid.birthdroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;



public class Settings
{
        /** our context */
        Context _c;
        /** our preferences */
        private SharedPreferences _prefs;
        private SharedPreferences.Editor _edit;



        /** constructor */
        public Settings(Context c)
        {
                /** save context */
                _c = c;
                
                /** read prefs */
                //_prefs = PreferenceManager.getDefaultSharedPreferences(c);
                _prefs = c.getSharedPreferences(c.getPackageName()+"_preferences", 
                                                Context.MODE_PRIVATE);
                _edit = _prefs.edit();
                commit();
        }


        /** commit recent edits */
        public void commit()
        {
                /* commit edits */
                _edit.commit();
        }

        
        /** getter */
        String getString(String key, String defValue)
        {
                return _prefs.getString(key, defValue);
        }

        int getInt(String key, int defValue)
        {
                return _prefs.getInt(key, defValue);
        }

        
        /** setter */
        void putString(String key, String value)
        {
                _edit.putString(key, value);
        }

        void putInt(String key, int value)
        {
                _edit.putInt(key, value);
        }
}
