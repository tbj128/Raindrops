package com.kinetiqa.glacier.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.kinetiqa.glacier.database.DatabaseHelper;
import com.kinetiqa.glacier.dialogs.DialogInfo;
import com.kinetiqa.glacier.menu.Menu;
import com.kinetiqa.glacier.menu.MenuFolder;
import com.kinetiqa.glacier.menu.MenuItem;
import com.kinetiqa.glacier.utils.TimeConversion;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Tom on 2014-11-02.
 */
public class StatisticsManager {
    private static StatisticsManager mInstance;
    private Context context;
    private SharedPreferences sharedPreferences;

    private long itemStartTime = -1;
    private long itemStartPause = -1;
    private long itemTotalPause = 0;

    private long activityStartTime = -1;
    private long activityStartPause = -1;
    private long activityTotalPause = 0;
    private long activityTotalTime = 0;

    private StatisticsManager(Context c) {
        this.context = c;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
    }

    public static StatisticsManager getInstance(Context c) {
        if (mInstance == null) {
            mInstance = new StatisticsManager(c);
        }
        return mInstance;
    }

    public void beginTiming() {
        itemStartTime = System.currentTimeMillis();
        itemStartPause = -1;
        itemTotalPause = 0;
    }

    public void beginActivityTiming() {
        activityStartTime = System.currentTimeMillis();
        activityStartPause = -1;
        activityTotalPause = 0;
        activityTotalTime = 0;
    }

    public void beginPause(boolean isActivity) {
        itemStartPause = System.currentTimeMillis();

        if (isActivity) {
            activityStartPause = System.currentTimeMillis();
        }
    }

    public void endPause() {
        long itemEndPause = System.currentTimeMillis();
        if ((itemStartPause > -1) && (itemEndPause > itemStartPause)) {
            itemTotalPause += itemEndPause - itemStartPause;
        } else {
            System.out.println("Item has stopped pausing but no start pause indicated");
        }

        if ((activityStartPause > -1) && (itemEndPause > activityStartPause)) {
            activityTotalPause += itemEndPause - activityStartPause;
        } else {
            System.out.println("Activity has stopped pausing but no start pause indicated");
        }
    }

    public long finishActivityTiming() {
        long activityEndTime = System.currentTimeMillis();
        long activityTime = 0;
        if ((activityStartTime > -1) && (activityEndTime > activityStartTime)) {
            activityTime += (activityEndTime - activityStartTime) - activityTotalPause;
        } else {
            System.out.println("Item has stopped but no start time indicated");
        }

        if (activityTime > 0) {
            activityTotalTime += activityTime;
        } else {
            System.out.println("Item has negative time viewed");
        }

        return activityTotalTime;
    }

    /**
     *
     * @return the number of seconds that the manager
     */
    public long finishTiming() {
        long itemEndTime = System.currentTimeMillis();
        long totalTime = 0;
        if ((itemStartTime > -1) && (itemEndTime > itemStartTime)) {
            totalTime += (itemEndTime - itemStartTime) - itemTotalPause;
        } else {
            System.out.println("Item has stopped but no start time indicated");
        }

        if (totalTime > 0) {
            if (activityTotalTime < 0) {
                activityTotalTime = 0;
            } else {
                System.out.println("Item has negative activity time");
            }
        } else {
            System.out.println("Item has negative time viewed");
        }

        itemStartTime = -1;
        itemStartPause = -1;
        itemTotalPause = 0;

        activityStartTime = -1;
        activityStartPause = -1;
        activityTotalPause = 0;
        activityTotalTime = 0;

        return totalTime / 1000;
    }

    // ---

    public int getNumSecondsViewingMenu(Menu menu) {
        int numSecondsViewingMenu = 0;
        if (menu != null) {
            if (menu instanceof MenuFolder) {
                List<Menu> subMenus = ((MenuFolder) menu).getSubMenus();
                for (Menu subMenu : subMenus) {
                    numSecondsViewingMenu += getNumSecondsViewingMenu(subMenu);
                }
            } else {
                numSecondsViewingMenu += DatabaseHelper.getInstance(context).getStatisticsTime(menu.getID());
            }
        }
        return numSecondsViewingMenu;
    }

    public int getNumCompletedItemsUnderMenuFolder(MenuFolder menuFolder) {
        if (menuFolder == null) {
            return 0;
        }
        int totalNumCompletedItems = 0;
        for (Menu menu : menuFolder.getSubMenus()) {
            if (menu instanceof MenuItem) {
                if (DatabaseHelper.getInstance(context).getStatisticsNumTimes(menu.getID()) > 0) {
                    totalNumCompletedItems++;
                }
            } else if (menu instanceof MenuFolder) {
                totalNumCompletedItems += getNumCompletedItemsUnderMenuFolder((MenuFolder) menu);
            }
        }

        return totalNumCompletedItems;
    }

    public int getNumItemsUnderMenuFolder(MenuFolder menuFolder) {
        if (menuFolder == null) {
            return 0;
        }
        int totalNumItems = 0;
        for (Menu menu : menuFolder.getSubMenus()) {
            if (menu instanceof MenuItem) {
                totalNumItems++;
            } else if (menu instanceof MenuFolder) {
                totalNumItems += getNumItemsUnderMenuFolder((MenuFolder) menu);
            }
        }

        return totalNumItems;
    }

    public List<MenuItem> getMostViewedMenuItems() {
        return DatabaseHelper.getInstance(context).getMenuItemsByMostViewed();
    }


    /**
     * Checks if a daily award has been achieved
     */
    public void checkDailyAwards() {
        // Get current date (in milliseconds)
        Calendar calendar = Calendar.getInstance();
        long currTime = calendar.getTimeInMillis();
        Date currDate = TimeConversion.convertDateTimetoDate(new Date(currTime));
        long currDateMilli = currDate.getTime();

        // Get first and last consecutive usage date
        long firstConsecutiveDay = sharedPreferences.getLong(
                "first-consecutive-day", -1);
        long lastConsecutiveDay = sharedPreferences.getLong(
                "last-consecutive-day", -1);

        if (firstConsecutiveDay != -1 && lastConsecutiveDay != -1) {
            if (lastConsecutiveDay != currDateMilli) {

                if ((lastConsecutiveDay + 86400000) == currDateMilli) {
                    if (!sharedPreferences.getBoolean("two_consecutive_days", false)) {
                        // Consecutive day award achieved!
                        sharedPreferences.edit().putBoolean("two_consecutive_days", true).commit();
                        DialogInfo infoDialog = new DialogInfo(context, "Welcome Back", "You've achieved a consecutive day award! Click on the Stats tab to see all your awards.");
                        infoDialog.show();
                    }

                    sharedPreferences.edit().putLong("last-consecutive-day", currDateMilli).commit();

                    if ((firstConsecutiveDay + 7 * 86400000) == currDateMilli) {
                        // 7 days have passed
                        if (!sharedPreferences.getBoolean("seven_consecutive_days", false)) {
                            sharedPreferences.edit().putBoolean("seven_consecutive_days", true).commit();
                            DialogInfo infoDialog = new DialogInfo(context, "Week Long", "You've achieved the 7 day milestone award! Click on the Stats tab to see all your awards.");
                            infoDialog.show();
                        }
                    }
                } else {
                    // Not a consecutive day!
                    sharedPreferences.edit().putLong("last-consecutive-day", currDateMilli).commit();
                    sharedPreferences.edit().putLong("first-consecutive-day", currDateMilli).commit();
                }
            }

        }

        // Is this the first time recording a date?
        if (firstConsecutiveDay == -1) {
            sharedPreferences.edit()
                    .putLong("first-consecutive-day", currDateMilli).commit();
        }
        if (lastConsecutiveDay == -1) {
            sharedPreferences.edit()
                    .putLong("last-consecutive-day", currDateMilli).commit();
        }
    }

}
