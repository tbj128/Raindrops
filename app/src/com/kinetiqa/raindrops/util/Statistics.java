package com.kinetiqa.raindrops.util;

import android.content.Context;

import com.kinetiqa.raindrops.database.DatabaseHelper;
import com.kinetiqa.raindrops.menu.MenuComponent;
import com.kinetiqa.raindrops.menu.MenuComposite;
import com.kinetiqa.raindrops.menu.MenuRegistry;

/**
 * Contains static helper methods to determine statistics
 * 
 * @author Tom
 * 
 */
public class Statistics {

	/**
	 * Calculates the total number of items under a menu component
	 * 
	 * @param item
	 * @return
	 */
	public static int totalMenuItems(MenuComponent item) {
		if (item == null) {
			return 0;
		}
		Integer totItems = 0;
		if (item.getMediaType() == MenuRegistry.MEDIA_MENU) {
			MenuComposite menu = (MenuComposite) item;
			for (MenuComponent menuItem : menu.getMenuItems()) {
				totItems += totalMenuItems(menuItem);
			}
		} else {
			totItems++;
		}
		return totItems;
	}

	/**
	 * Calculates the total number of items under a menu component
	 * 
	 * @param item
	 * @return
	 */
	public static int totalMenuItemsCompleted(Context c, MenuComponent item) {
		if (item == null) {
			return 0;
		}
		Integer totVideos = 0;
		if (item.getMediaType() == MenuRegistry.MEDIA_MENU) {
			MenuComposite menu = (MenuComposite) item;
			for (MenuComponent menuItem : menu.getMenuItems()) {
				totVideos += numberItemsCompleted(c, menuItem);
			}
		} else {
			if (DatabaseHelper.getInstance(c).getStatisticsNumTimes(
					item.getID()) > 0) {
				totVideos++;
			}
		}
		return totVideos;
	}

	/**
	 * Calculates the cumulative amount of time (in seconds) spent watching
	 * videos under a menu component
	 */
	public static Integer amountOfTimeWatched(Context c, MenuComponent item) {
		if (item == null) {
			return 0;
		}
		Integer totTime = 0;
		if (item.getMediaType() == MenuRegistry.MEDIA_MENU) {
			MenuComposite menu = (MenuComposite) item;
			for (MenuComponent menuItem : menu.getMenuItems()) {
				totTime += amountOfTimeWatched(c, menuItem);
			}
		} else {
			totTime += DatabaseHelper.getInstance(c).getStatisticsTime(
					item.getID());
		}
		return totTime;
	}

	
	public static Integer pointsEarned(Context c, MenuComponent item) {
		if (item == null) {
			return 0;
		}
		Integer points = 0;
		if (item.getMediaType() == MenuRegistry.MEDIA_MENU) {
			MenuComposite menu = (MenuComposite) item;
			for (MenuComponent menuItem : menu.getMenuItems()) {
				points += pointsEarned(c, menuItem);
			}
		} else {
			points += DatabaseHelper.getInstance(c).getPoints(
					item.getID());
		}
		return points;
	}
	
	/**
	 * Calculates the total number of videos/activities under a menu component
	 * (Also is an indicator of how many stars there are available)
	 * 
	 * @param item
	 * @return
	 */
	public static Integer totNumberVideos(MenuComponent item) {
		if (item == null) {
			return 0;
		}
		Integer totVideos = 0;
		if (item.getMediaType() == MenuRegistry.MEDIA_MENU) {
			MenuComposite menu = (MenuComposite) item;
			for (MenuComponent menuItem : menu.getMenuItems()) {
				totVideos += totNumberVideos(menuItem);
			}
		} else {
			totVideos++;
		}
		return totVideos;
	}

	/**
	 * Calculates the total number of items completed under a menu component
	 * 
	 * @param item
	 * @return
	 */
	public static Integer numberItemsCompleted(Context c, MenuComponent item) {
		if (item == null) {
			return 0;
		}
		Integer totVideos = 0;
		if (item.getMediaType() == MenuRegistry.MEDIA_MENU) {
			MenuComposite menu = (MenuComposite) item;
			for (MenuComponent menuItem : menu.getMenuItems()) {
				totVideos += numberItemsCompleted(c, menuItem);
			}
		} else {
			if (DatabaseHelper.getInstance(c).getStatisticsNumTimes(
					item.getID()) > 0) {
				totVideos++;
			}
		}
		return totVideos;
	}

	/**
	 * Calculates the total number of videos under a menu component Also is an
	 * indicator of how many stars there are available
	 * 
	 * @param item
	 * @return
	 */
	public static Integer totNumberVideosOnly(MenuComponent item) {
		if (item == null) {
			return 0;
		}
		Integer totVideos = 0;
		if (item.getMediaType() == MenuRegistry.MEDIA_MENU) {
			MenuComposite menu = (MenuComposite) item;
			for (MenuComponent menuItem : menu.getMenuItems()) {
				totVideos += totNumberVideosOnly(menuItem);
			}
		} else if (!item.isActivity()) {
			totVideos++;
		}
		return totVideos;
	}

	/**
	 * Calculates the total number of activities watched under a menu component
	 * 
	 * @param item
	 * @return
	 */
	public static Integer totNumberActivitiesOnlyWatched(Context c,
			MenuComponent item) {
		if (item == null) {
			return 0;
		}
		Integer totVideos = 0;
		if (item.getMediaType() == MenuRegistry.MEDIA_MENU) {
			MenuComposite menu = (MenuComposite) item;
			for (MenuComponent menuItem : menu.getMenuItems()) {
				totVideos += totNumberActivitiesOnlyWatched(c, menuItem);
			}
		} else if (item.isActivity()) {

			if (DatabaseHelper.getInstance(c).getStatisticsNumTimes(
					item.getID()) > 0) {
				totVideos++;
			}

		}
		return totVideos;
	}

	/**
	 * Calculates the total number of activities under a menu component Also is
	 * an indicator of how many stars there are available
	 * 
	 * @param item
	 * @return
	 */
	public static Integer totNumberActivitiesOnly(MenuComponent item) {
		if (item == null) {
			return 0;
		}
		Integer totVideos = 0;
		if (item.getMediaType() == MenuRegistry.MEDIA_MENU) {
			MenuComposite menu = (MenuComposite) item;
			for (MenuComponent menuItem : menu.getMenuItems()) {
				totVideos += totNumberActivitiesOnly(menuItem);
			}
		} else if (item.isActivity()) {
			totVideos++;
		}
		return totVideos;
	}

}
