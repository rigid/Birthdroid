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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.Data;
import android.util.Log;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.List;



public class Birthdays
{
        /** our log-tag */
        private final static String TAG = "Birthdroid/Birthdays";
        /** available sort-fields for sort-method */
        public static enum SortBy
        {
                UPCOMING_DAYS,
                AGE,
        };
        
        /** current context */
        private final Context _c;
        /** current list of birthdays */
        private List<Birthday> birthdays;
        /* preferences */
        private Settings s;
        
        

        /**
         * constructor
         */
        public Birthdays(Context c)
        {
                /* save activity */
                _c = c;

                /* settings */
                s = new Settings(_c);
                
                /* create new Birthday list */
                birthdays = refresh();

        }



        /**
         * sort current list of birtdays
         *
         * @param String representing a valid SortBy item
         */
        public void sort(String method)
        {
                if(method.equals("UPCOMING_DAYS"))
                {
                        sort(SortBy.UPCOMING_DAYS);
                }
                else if(method.equals("AGE"))
                {
                        sort(SortBy.AGE);
                }
                else
                {
                        Log.e(TAG, "invalid sort-method: \""+method+"\" Using default method.");
                        sort(SortBy.UPCOMING_DAYS);
                }
        }
        
        /**
         * sort current list of birthdays
         *
         * @param SortBy item to reflect method
         */
        public void sort(SortBy method)
        {
                Comparator<Birthday> c;
                
                switch(method)
                {
                        case UPCOMING_DAYS:
                        {
                                c = new UpcomingDaysComparator();
                                Log.v(TAG, "Sorting by UPCOMING_DAYS");
                                break;
                        }

                        case AGE:
                        {
                                c = new AgeComparator();
                                Log.v(TAG, "Sorting by AGE");
                                break;
                        }

                        default:
                        {
                                Log.e(TAG, "Invalid sort-field. Sorting by \"upcoming-days\"");
                                c = new UpcomingDaysComparator();
                        }
                }

                /* sort according to selected method */
                Collections.sort(birthdays, c);
                
        }

        
        /**
         * return amount of currently registered birthdays
         */
        public int getCount()
        {
                return birthdays.size();
        }

        /**
         * get birthday at position n
         */
        public Birthday get(int n)
        {
                /* n must not greater than amount of birthdays */
                if(n >= birthdays.size())
                    n = birthdays.size()-1;

                /* n must not be < 0 */
                if(n < 0)
                    n = 0;

                return birthdays.get(n);
        }
        
        /**
         * get complete list of birthdays
         *
         * @result List of all Birtday-objects
         */
        public List<Birthday> getAll()
        {
                return birthdays;
        }

        
        /** 
         * get list of upcoming birthdays 
         *
         * @param days return birthdays upcoming in this amount of days 
         * @result List of Birthday objects
         */
        public List<Birthday> getUpcoming(int days)
        {
                /* days must not be negative */
                if(days < 0)
                    days = 0;

                /* more than a year makes no sense */
                if(days > 366)
                    days = 366;

                /* result list */
                List<Birthday> list = new ArrayList<Birthday>();
                                
                
                /* walk all birthdays */
                for(Birthday b : birthdays)
                {
                        /* birthday too far in the future */
                        int d = b.getDaysUntilFuture();
                        //Log.v(TAG, b.getPersonName()+" in "+d+" days");
                        if(d > days)
                                continue;
                        
                        list.add(b);
                
                }

                return list;
        }

        
        /** this will crawl all contacts and return a list with all birthdays */
        public List<Birthday> refresh()
        {       

                /* create result-list */
                List<Birthday> list = new ArrayList<Birthday>();
                
                /* get content resolver */
                ContentResolver r = _c.getContentResolver();
                
                /* prepare query */
                String[] projection = new String[]
                { 
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.LOOKUP_KEY,
                        ContactsContract.CommonDataKinds.Event.START_DATE,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Event.TYPE,
                        //ContactsContract.Contacts.PHOTO_URI,
                };

                String selection = new String(Data.MIMETYPE + "=?");
                
                String[] args = new String[] 
                {
                        Event.CONTENT_ITEM_TYPE, 
                };
                
                /* get cursor from content resolver */
                Cursor c = r.query
                (       
                        Data.CONTENT_URI, 
                        projection, 
                        selection, 
                        args,
                        null
                );

                /* any birthdays in cursor? */
                if (!c.moveToFirst()) 
                {
                        Log.d(TAG, "No birthdays found.");
                        
                        /* return empty list */
                        return list;
                }
                
                
                /* get columns */
                final int idColumn = c.getColumnIndex
                (
                        ContactsContract.Contacts._ID
                );
                final int keyColumn = c.getColumnIndex
                (
                        ContactsContract.Contacts.LOOKUP_KEY
                );
                final int dateColumn = c.getColumnIndex
                (
                        ContactsContract.CommonDataKinds.Event.START_DATE
                );
                final int nameColumn = c.getColumnIndex
                (
                        ContactsContract.Contacts.DISPLAY_NAME
                );
                final int typeColumn = c.getColumnIndex
                (
                        ContactsContract.CommonDataKinds.Event.TYPE
                );
                /*final int photoColumn = c.getColumnIndex
                (
                        ContactsContract.Contacts.PHOTO_URI
                );*/

               				
                /* walk all birthdays */
                do 
                { 
                        /* get lookup key */
                        String key = c.getString(keyColumn);

                        /* get name of person */
                        String name = c.getString(nameColumn);

                        /* get date as string */
                        String date = c.getString(dateColumn); 
                        
                        /* get type of event (birthday, anniversary, etc. */
                        int type = c.getInt(typeColumn);

                        /* get photo URI */
                        //String photo_uri = c.getString(photoColumn);
                        //Log.e(TAG, "-------------> "+photo_uri);

						/* parse date-string */                        
						Birthday b;
						if((b = parse(date.trim())) == null)
								continue;

						/* fill in contact data */
						b.personName = name;
						b.contactId = key;
                        b.type = getReadableEventType(type);
						
                        /* get contact photo */
                        //~ Bitmap photo = null;
                        //~ Uri photo_uri = ContentUris.withAppendedId(
                                //~ ContactsContract.Contacts.CONTENT_URI, id);
                        
                        
                        //~ Log.v(TAG, "----> URI: "+photo_uri);
                        
                        //~ InputStream photo_input = 
                                //~ ContactsContract.
                                //~ Contacts.
                                //~ openContactPhotoInputStream(r, photo_uri);

                        //~ /* read bitmap */                        
                        //~ if(photo_input != null) 
                        //~ {
                                //~ photo = BitmapFactory.decodeStream(photo_input);
                                //~ Log.v(TAG, "PHOTO: "+photo);
                        //~ }
                        //~ else Log.v(TAG, "No photo!");
                        
                        
                        /* Add event to list, if not already included */
                        if (eventNotInList(b, list)) {
                                list.add(b);
                        }

                } while (c.moveToNext());       
				
                return list;
        }

        /**
         * Return a readable representation of the event type.
         *
         * Currently, there are these values supported:
         * birthday, anniversary, custom, other
         *
         * @param type
         * @return String
         */
        private String getReadableEventType(int type) {
                String readable = "";
                switch (type) {
                        case Event.TYPE_BIRTHDAY:
                                readable = "birthday";
                                break;
                        case Event.TYPE_ANNIVERSARY:
                                readable = "anniversary";
                                break;
                        case Event.TYPE_CUSTOM:
                                readable = "custom";
                                break;
                        case Event.TYPE_OTHER:
                                readable = "other";
                                break;
                }
                return readable;
        }

        /**
         * Checks if the given event is already included in the list.
         *
         * @param Birthday The event to search for
         * @return boolean
         */
        private boolean eventNotInList(Birthday birthday, List<Birthday> list) {
                for (Birthday b : list) {
                        if (b.contactId.equals(birthday.contactId)
                                && b.type.equals(birthday.type)
                                && b.date.equals(birthday.date)
                                ) {
                                return false;
                        }
                }
                return true;
        }

        /* parse date string */
        private Birthday parse(String date)
        {
                Date birthday = null;

                /* try various date formats */
                String[] formats = new String[]
                {
                        "yyyy-MM-dd hh:mm:ss.SSS",
                        "yyyy-MM-dd",
                        "--MM-dd",
                };

                for(String format : formats)
                {						
                        SimpleDateFormat df = new SimpleDateFormat(format);
						
                        /* we are strict */
                        df.setLenient(false);
						
                        try 
                        {
                                /* try to parse date */
                                birthday = df.parse(date);
							
                                /* create birthday */
								Birthday b = new Birthday(birthday);

								/* birthday has year? */
								if(format.contains("y"))
								{
										/* flag that birthday has valid year set */
										b.hasYear = true;
								}
								else
								{
										/* set year to 1900 for sorting */
										b.date.setYear(0);
										/* flag that birthday has no valid year set */
										b.hasYear = false;
								}

                                return b;
                        }
                        catch (ParseException ex) 
                        {
                                continue;
                        }
                }


                Log.e(TAG, "Could not find DateFormat for \""+date+"\"");
                        
                return null;
        }
        
        
        /** class to represent one birthday of one person */
        public class Birthday
        {
                /** the persons name */
                public String personName;
                /** the event */
                public Date date;
                /** the event type: birthday, anniversary, etc. */
                public String type;
                /** id of contact that belongs to this birthday */
                public String contactId;
                /** contact photo */
                public Bitmap photo;
                /* indicate if this Birthday has a year */
                public boolean hasYear;


                /** constructor */
                public Birthday(Date birthday)
                {
                        this.date = birthday;
                        this.type = "";
                        this.personName = "";
                        this.contactId = "";
                        this.photo = null;
                        this.hasYear = false;
				}

                /** return current age of person */
                public int getPersonAge()
                {
                        Calendar now = new GregorianCalendar();
                        Calendar bday = new GregorianCalendar();
                        bday.setTime(date);

                        
                        /* get amount of years between birthday and today */
                        int years = now.get(Calendar.YEAR) - bday.get(Calendar.YEAR);
                        
                        /* is this years birthday yet to come? */
                        if(
                            now.get(Calendar.MONTH) < bday.get(Calendar.MONTH) ||
                            (
                              (now.get(Calendar.MONTH) == bday.get(Calendar.MONTH)) &&
                              (now.get(Calendar.DAY_OF_MONTH) < bday.get(Calendar.DAY_OF_MONTH))
                            )
                          )
                        {
                                years--;
                        }

                        /* birthday today? */
                        if(now.get(Calendar.MONTH) == bday.get(Calendar.MONTH) &&
                           now.get(Calendar.DAY_OF_MONTH) == bday.get(Calendar.DAY_OF_MONTH))
                        {
                                years--;
                        }
                        
                        if(years < 0)
                        {
                                Log.e(TAG, "Illegal age: "+years+" Using: age = 0");
                                return 0;
                        }
                        
                        return years+1;
                }
                
                /** 
                 * return amount of days until birthday
                 * @result amount of days until birthday (negative if past)
                 */
                public int getDaysUntil()
                {
                        /* calendar for current time */
                        Calendar now = new GregorianCalendar();
                        
                        /* calendar for birthday */
                        Calendar b = new GregorianCalendar();
                        b.setTime(date);

                        /* set calendar to current year */
                        b.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                        
                        return b.get(Calendar.DAY_OF_YEAR)-now.get(Calendar.DAY_OF_YEAR);

                }

                
                /**
                 * return amount of days left until birthday
                 * @result amount of days until next birthday (always in the future)
                 */
                public int getDaysUntilFuture()
                {
                        int days_left = getDaysUntil();
                        
                        /* if birthday is already past, calculate amount of days
                           until birthday next year */
                        if(days_left < 0)
                        {
                                days_left += 
                                        Calendar.getInstance()
                                        .getActualMaximum(Calendar.DAY_OF_YEAR);
                        }
                        
                        return days_left;
                }
                
                /** return "upcoming" string */
                public String getDaysLeft()
                {
                        Resources res = _c.getResources();
                        /* amount of days until birthday */
                        int in_days = getDaysUntilFuture();

                        /* special treatment for newborns */
                        if(in_days == 0 && getPersonAge() == 0 && type.equals("birthday") && hasYear)
                        {
                                return res.getString(R.string.born_today);
                        }
                        
                        /* special case for today and tomorrow */
                        String days_left;
                        switch (in_days) {
                                case 0:
                                        days_left = res.getString(R.string.event_today);
                                        break;
                                case 1:
                                        days_left = res.getString(R.string.event_tomorrow);
                                        break;
                                default:
                                        days_left = String.format(res.getString(R.string.event_days_left), in_days);
                                        break;
                        }
                        days_left += getLeapYearMessage();
                        return days_left;
                }


                /* get special leap-year message if necessary */
                public String getLeapYearMessage()
                {
                        /* create temporary calendar for some calculations */
                        GregorianCalendar tmp = new GregorianCalendar();

                        /* is current year a leap year? */
                        boolean is_leap_year = tmp.isLeapYear(tmp.get(Calendar.YEAR));

                        /* set calendar to birthday */
                        tmp.setTime(date);
                        
                        /* is birthday on 29 of February and 
                           current year no leap-year? */                     
                        if(tmp.get(Calendar.MONTH) == Calendar.FEBRUARY &&
                           tmp.get(Calendar.DAY_OF_MONTH) == 29 &&
                           is_leap_year == false)
                        {
                                /* add no-leap-year message? */
                                return "\n"+_c.getResources().getString(R.string.no_leap_year);
                        }

                        return "";
                }

                
                /** open contact belonging to this birthday */
                public void openContact()
                {
                        Intent i;
                        i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(ContactsContract.Contacts.CONTENT_LOOKUP_URI + "/" + contactId));
                        _c.startActivity(i);
                }
        }
        
        /**
         * comparator for "sort" method to sort birthdays 
         * by days-until-birthday
         */
        private class UpcomingDaysComparator implements Comparator<Birthday>
        {
                @Override
                public int compare(Birthday a, Birthday b) 
                {
                        int days_a = a.getDaysUntilFuture();
                        int days_b = b.getDaysUntilFuture();
                        return (days_a == days_b ? 0 : (days_a < days_b ? -1 : 1));
                }
        }


        /**
         * comparator comparator for "sort" method to sort
         * birthdays by age of person
         */
        public class AgeComparator implements Comparator<Birthday>
        {
                @Override
                public int compare(Birthday a, Birthday b) 
                {
                        return (a.date.equals(b.date) ? 0 : (a.date.after(b.date) ? -1 : 1));
                }
        }

}

