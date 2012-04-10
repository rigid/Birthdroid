package com.rigid.birthdroid;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

import java.text.DateFormat;
import java.util.List;




/** main activity */
public class BirthdroidActivity extends ListActivity
{
        private final static String TAG = "Birthdroid/BirthdroidActivity";
        /** object holding all birthdays */
        private static Birthdays _b;
        /** object to hold all our permanent settings */
        private static Settings _s;

        
        
        /** called by OS when app is created initially */
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);

                /* create new settings-object */
                _s = new Settings(this);
                /* create new birthday-list-object */
                _b = new Birthdays(this);
                
                /* sort birthdays */
                _b.sort(_s.getString("birthdroid_sort_method", 
                        getResources().getString(R.string.birthdroid_sort_method)));

                /* use our own list adapter */
                setListAdapter(new BirthdroidListAdapter(this));

                /* enable text-filter */
                getListView().setTextFilterEnabled(true);

                /* register click-listener for list-items */
                getListView().setOnItemClickListener(new OnItemClickListener() 
                {
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) 
                        {
                                /* open this contact */
                                _b.get(position).openContact();
                        }
                });
        }

        /** called by OS when this activity becomes active again */
        @Override
        public void onResume()
        {
                super.onResume();
                
                /* sort birthdays */
                _b.sort(_s.getString("birthdroid_sort_method", getResources().getString(R.string.birthdroid_sort_method)));

                ((BaseAdapter) getListAdapter()).notifyDataSetInvalidated();
        }

        
        /**
         * ListAdapter that presents a complete list of birthdays
         */
        private static class BirthdroidListAdapter extends BaseAdapter 
        {
                private Context _c;
                private DateFormat _df;
                private LayoutInflater _i;
                
                
                public BirthdroidListAdapter(Context context) 
                {
                        _c = context;
                        _i = LayoutInflater.from(context);
                        _df = DateFormat.getDateInstance(DateFormat.SHORT);
                }

                
                /**
                 * The number of items in the list is determined by the number of speeches
                 * in our array.
                 * 
                 * @see android.widget.ListAdapter#getCount()
                 */
                public int getCount() 
                {
                        return _b.getCount();
                }

                
        

                /**
                 * return id object represents one row in the
                 * list.
                 * 
                 * @see android.widget.ListAdapter#getItem(int)
                 */
                public Object getItem(int position) 
                {
                        return position;
                }

                /**
                 * Use the array index as a unique id.
                 * 
                 * @see android.widget.ListAdapter#getItemId(int)
                 */
                public long getItemId(int position) 
                {
                        return position;
                }

                /**
                 * Make a SpeechView to hold each row.
                 * 
                 * @see android.widget.ListAdapter#getView(int, android.view.View,
                 *      android.view.ViewGroup)
                 */
                public View getView(int position, View convertView, ViewGroup parent) 
                {
                        ViewHolder holder;


                        /* View not yet created? */
                        if(convertView == null) 
                        {
                                convertView = _i.inflate(R.layout.list_item, null);

                                /* create new ViewHolder with necessary stuff */
                                holder = new ViewHolder();
                                holder.name = (TextView) convertView.findViewById(R.id.list_person);
                                holder.msg = (TextView) convertView.findViewById(R.id.list_message);
                                holder.img = (ImageView) convertView.findViewById(R.id.list_image);
                                convertView.setTag(holder);
                        }
                        else 
                        {
                                /* recycle previously created view-holder */
                                holder = (ViewHolder) convertView.getTag();
                        }

                        /* get birthday */
                        Birthdays.Birthday bday = _b.get(position);
                        
                        /* set content of entry */
                        holder.name.setText(bday.getPersonName());
                        holder.msg.setText(bday.getMessage()+" ("+_df.format(bday.getDate())+")");
                        holder.img.setImageBitmap(bday.getPhoto());
                        
                        return convertView;
                }

                /* ViewHolder class to hold views of one list-entry */
                static class ViewHolder 
                {
                        TextView name;
                        TextView msg;
                        ImageView img;
                }
        }
        

        /** options menu */
        @Override
        public boolean onCreateOptionsMenu(Menu menu)
        {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.birthdroid, menu);
                return true;
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
                                return true;
                        }
                                
                        /** Settings */
                        case R.id.settings:
                        {
                                startActivity(new Intent(this, PreferencesActivity.class));
                                return true;
                        }

                        /** wtf? */
                        default:
                        {
                                Log.w(TAG, "Unhandled menu-item. This is a bug!");
                                break;
                        }
                }

                return false;
        }

}
