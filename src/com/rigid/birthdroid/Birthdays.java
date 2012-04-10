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
import java.text.DateFormat;
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

        
        

        /**
         * constructor
         */
        public Birthdays(Context c)
        {
                /* save activity */
                _c = c;
                
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
                Comparator c;
                
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
                        //ContactsContract.Contacts.PHOTO_URI,
                };

                String selection = new String(Data.MIMETYPE + "=? AND " + ContactsContract.CommonDataKinds.Event.TYPE + "=?");
                
                String[] args = new String[] 
                {
                        Event.CONTENT_ITEM_TYPE, 
                        Integer.toString(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
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
                /*final int photoColumn = c.getColumnIndex
                (
                        ContactsContract.Contacts.PHOTO_URI
                );*/
                
                /* try to get DateFormat to parse dates */
                DateFormat df;
                if((df = findDateFormat(c.getString(dateColumn))) == null)
                        return list;
                
                /* walk all birthdays */
                do 
                { 
                        /* get name of person */
                        String name = c.getString(nameColumn);

                        /* get date as string */
                        String date = c.getString(dateColumn); 

                        /* get lookup key */
                        String key = c.getString(keyColumn);

                        /* get contact id */
                        long id = c.getLong(idColumn);

                        /* get photo URI */
                        //String photo_uri = c.getString(photoColumn);
                        //Log.e(TAG, "-------------> "+photo_uri);
                        
                        /* parse date-string */
                        Date birthday;
                        try
                        {
                                birthday = df.parse(date);
                        }
                        catch(ParseException e)
                        {
                                Log.e(TAG, e.getMessage());
                                Log.e(TAG, "Failed to parse date! Please report bug.");
                                continue;
                        }

                        /* get contact photo */
                        Bitmap photo = null;
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
                        
                        
                        /* save birthday in list */
                        Birthday b = new Birthday(name, birthday, key, photo);
                        list.add(b);
                        
                } while (c.moveToNext()); 
        
               
                return list;
        }

        /** find date format for a supplied date-string */
        private DateFormat findDateFormat(String date)
        {
                /* preferences */
                Settings s = new Settings(_c);
                String prefLocale = s.getString("date_format_locale", "null");
                int prefStyle = s.getInt("date_format_style", -1);
                
                /* date-format not found, yet? */
                if(!prefLocale.equals("null") || prefStyle != -1)
                {
                        /* get date-format */
                        return DateFormat.getDateInstance(prefStyle, new Locale(prefLocale));
                }
                
                /* search date-format */
                for (Locale locale : DateFormat.getAvailableLocales()) 
                {
                        final int[] styles = 
                        {
                                DateFormat.SHORT,
                                DateFormat.MEDIUM,
                                DateFormat.LONG,
                                DateFormat.FULL,
                        };
                        
                        for(int style : styles)
                        {
                                DateFormat df = DateFormat.getDateInstance(style, locale);
                                try 
                                {
                                        /* try to parse date */
                                        df.parse(date);

                                        /* save date-format */
                                        s.putInt("date_format_style", style);
                                        s.putString("date_format_locale", locale.toString());
                                        s.commit();
                                        
                                        /* bingo! */
                                        return df;
                                }
                                catch (ParseException ex) 
                                {
                                        continue;
                                }
                        }
                }

                Log.e(TAG, "Could not find DateFormat for \""+date+"\"");
                        
                return null;
        }
        
        
        /** class to represent one birthday of one person */
        public class Birthday
        {
                /** the persons name */
                private String _name;
                /** the event */
                private Date _birthday;
                /** id of contact that belongs to this birthday */
                private String _key;
                /** contact photo */
                private Bitmap _photo;
                

                /** constructor */
                public Birthday(String name, Date birthday, String key, Bitmap photo)
                {
                        _name = name;
                        _birthday = birthday;
                        _key = key;
                        _photo = photo;
                }

                /** get contact photo */
                public Bitmap getPhoto() 
                {
                    return _photo;
                }
                
                /** return current age of person */
                public int getPersonAge()
                {
                        Calendar now = new GregorianCalendar();
                        Calendar bday = new GregorianCalendar();
                        bday.setTime(_birthday);

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
                                throw new IllegalArgumentException("Age < 0");
                        
                        return years+1;
                }
                
                /** return name of person */
                public String getPersonName()
                {
                        return _name;
                }

                /** return birthday date */
                public Date getDate()
                {
                        return _birthday;
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
                        b.setTime(_birthday);

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
                public String getMessage()
                {
                        /* amount of days until birthday */
                        int in_days = getDaysUntilFuture();

                        /* get age */
                        int age = getPersonAge();

                        
                        Resources res = _c.getResources();

                        
                        
                        /* special treatment for newborns */
                        if(in_days == 0 && age == 0)
                        {
                                String msg = res.getString(R.string.born_today);
                                return msg;
                        }
                        
                        /* build "in ... days" string */
                        String days;
                        switch(in_days)
                        {
                                /* today */
                                case 0:
                                {
                                        days = res.getString(R.string.days_zero);
                                        break;
                                }

                                /* tomorrow */
                                case 1:
                                {
                                        days = res.getString(R.string.days_one);
                                        break;
                                }

                                default:
                                {
                                        days = String.format(res.getString(R.string.days_more), in_days);
                                }
                                        
                        }

                                                
                        /* build message */
                        String msg;
                        msg = String.format(
                                        res.getString(R.string.upcoming),
                                        age, days);
                
                        
                        return msg;
                }

                /** open contact belonging to this birthday */
                public void openContact()
                {
                        Intent i;
                        i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(ContactsContract.Contacts.CONTENT_LOOKUP_URI + "/" + _key));
                        _c.startActivity(i);
                }
        }
        /**
         * comparator for "sort" method to sort birthdays 
         * by days-until-birthday
         */
        private class UpcomingDaysComparator implements Comparator
        {
                @Override
                public int compare(Object a, Object b) 
                {
                        int days_a = ((Birthday) a).getDaysUntilFuture();
                        int days_b = ((Birthday) b).getDaysUntilFuture();
                        return (days_a == days_b ? 0 : (days_a < days_b ? -1 : 1));
                }
        }


        /**
         * comparator comparator for "sort" method to sort
         * birthdays by age of person
         */
        public class AgeComparator implements Comparator
        {
                @Override
                public int compare(Object a, Object b) 
                {
                        int age_a = ((Birthday) a).getPersonAge();
                        int age_b = ((Birthday) b).getPersonAge();
                        return (age_a == age_b ? 0 : (age_a < age_b ? -1 : 1));
                }
        }
}

