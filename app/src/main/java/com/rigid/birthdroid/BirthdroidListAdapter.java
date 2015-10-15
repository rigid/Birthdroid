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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * ListAdapter that presents a complete list of birthdays
 */
public class BirthdroidListAdapter extends BaseAdapter
{
    private Context context;
    private Birthdays events;

    public BirthdroidListAdapter(Context context, Birthdays events)
    {
        this.context = context;
        this.events = events;
    }

    /**
     * @see android.widget.ListAdapter#getCount()
     */
    public int getCount()
    {
        return events.getCount();
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
     * @see android.widget.ListAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // Get the data item for this position
        Birthdays.Birthday event = events.get(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }
        // Get views for data population
        TextView name = (TextView) convertView.findViewById(R.id.list_name);
        TextView date = (TextView) convertView.findViewById(R.id.list_date);
        TextView daysLeft = (TextView) convertView.findViewById(R.id.list_daysLeft);
        TextView years = (TextView) convertView.findViewById(R.id.list_years);

        // Populate the data into the template view using the data object
        name.setText(event.personName);
        daysLeft.setText(event.getDaysLeft());
        date.setText(event.getFormattedDate());
        years.setText(event.getYearForNextEvent());

        return convertView;
    }
}
