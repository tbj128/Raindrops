package com.kinetiqa.glacier.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Tom on 2014-11-02.
 */
public class PointsManager {

    private static PointsManager mInstance;
    private static Context context;
    private SharedPreferences sharedPreferences;

    private PointsManager(Context c) {
        this.context = c;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
    }

    public static PointsManager getInstance(Context c) {
        if (mInstance == null) {
            mInstance = new PointsManager(c);
        }
        return mInstance;
    }

    public long getPoints() {
        long points = (int) sharedPreferences.getLong("points", 0);
        return points;
    }

    public long getLifetimePoints() {
        long points = (int) sharedPreferences.getLong("lifetime_points", 0);
        return points;
    }

    /**
     *
     * @param points - (Time in seconds)
     */
    public void addPoints(long points) {
        long currentPoints = getPoints();
        currentPoints += points;
        sharedPreferences.edit().putLong("points", currentPoints).commit();

        long lifetimePoints = getLifetimePoints();
        lifetimePoints += points;
        sharedPreferences.edit().putLong("lifetime_points", lifetimePoints).commit();

        checkPointsAwards(lifetimePoints);
    }

    private void checkPointsAwards(long points) {
        if (points >= 50) {
            sharedPreferences.edit().putBoolean("award_50", true).commit();
        }
        if (points >= 100) {
            sharedPreferences.edit().putBoolean("award_100", true).commit();
        }
        if (points >= 500) {
            sharedPreferences.edit().putBoolean("award_500", true).commit();
        }
        if (points >= 1000) {
            sharedPreferences.edit().putBoolean("award_1000", true).commit();
        }
        if (points >= 1000) {
            sharedPreferences.edit().putBoolean("award_10000", true).commit();
        }
    }
}
