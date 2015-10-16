/*
 * Birthdroid - Android upcoming birthday App/Widget
 * Copyright (C) 2011-2015 Daniel Hiepler <daniel@niftylight.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.rigid.birthdroid;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;


/** main activity */
public class BirthdroidActivity extends AppCompatActivity
{
        private final static String TAG = "Birthdroid/BirthdroidActivity";
        /** object holding all birthdays */
        private Birthdays b;
        /** object to hold all our permanent settings */
        private Settings s;
        /** listview handle */
        private ListView l;

        
        
        /** called by OS when app is created initially */
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.main);

                /* create new settings-object */
                s = new Settings(this);
                /* create new birthday-list-object */
                b = new Birthdays(this);
                
                /* sort birthdays */
                b.sort(s.getString("birthdroid_sort_method", 
                        getResources().getString(R.string.birthdroid_sort_method)));

                /* Get the list view */
                l = (ListView) findViewById(R.id.main_listview);

                /* use our own list adapter */
                l.setAdapter(new BirthdroidListAdapter(this, b));

                /* enable text-filter */
                l.setTextFilterEnabled(true);

                /* register click-listener for list-items */
                l.setOnItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                                /* open this contact */
                                b.get(position).openContact();
                        }
                });
        }

		
        /** called by OS when this activity becomes active again */
        @Override
        public void onResume()
        {
                super.onResume();
                
                /* sort birthdays */
                b.sort(s.getString("birthdroid_sort_method", getResources().getString(R.string.birthdroid_sort_method)));

                ((BaseAdapter) l.getAdapter()).notifyDataSetInvalidated();
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


        /** options menu */
        @Override
        public boolean onCreateOptionsMenu(Menu menu)
        {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.birthdroid, menu);
                return super.onCreateOptionsMenu(menu);
        }

        
        /** item from options menu selected */
        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
                switch (item.getItemId())
                {
                        /** About */
                        case R.id.about:
                        {
                                startActivity(new Intent(this, AboutActivity.class));
                                break;
                        }
                                
                        /** Settings */
                        case R.id.settings:
                        {
                                startActivity(new Intent(this, SettingsActivity.class));
                                break;
                        }

                        /** wtf? */
                        default:
                        {
                                Log.w(TAG, "Unhandled menu-item. This is a bug!");
                                break;
                        }
                }

                return super.onOptionsItemSelected(item);
        }

}
