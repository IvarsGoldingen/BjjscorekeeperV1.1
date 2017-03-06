package com.example.android.bjjscorekeeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Ivars on 2017.03.04..
 */

public class MyPreferensesClass {
    private static final String STARTED_TIME_ID = "My_start_time";
    private SharedPreferences myPreferences;

    public MyPreferensesClass(Context c) {
        //constructor: app is getting the file which is used by preferences
        //Gets a SharedPreferences instance that points to the default file that is used by the
        // preference framework in the given context
        myPreferences = PreferenceManager.getDefaultSharedPreferences(c);
    }

    public long getStartedTime() {
        //the value is found in the preference file by the id, the second variable is the defualt value
        return myPreferences.getLong(STARTED_TIME_ID, 0);
    }

    public void setStartedTime(long started) {
        //from developer android: Modifications to the preferences must go through an
        // SharedPreferences.Editor object to ensure the preference values remain in a consistent
        // state and control when they are committed to storage.
        SharedPreferences.Editor editor = myPreferences.edit();
        editor.putLong(STARTED_TIME_ID, started);
        editor.apply();
    }
}
