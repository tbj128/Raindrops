package com.kinetiqa.raindrops.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Settings.Secure;

import com.kinetiqa.raindrops.Messages;
import com.kinetiqa.raindrops.components.Bookmark;
import com.kinetiqa.raindrops.components.Message;
import com.kinetiqa.raindrops.menu.MenuComponent;
import com.kinetiqa.raindrops.menu.MenuLeaf;
import com.kinetiqa.raindrops.menu.MenuRegistry;
import com.kinetiqa.raindrops.util.RandomAlphaNumeric;
import com.kinetiqa.raindrops.util.TimeConversion;

/**
 * Single point of entry for all database related methods Notes: Singleton
 * design pattern used to prevent concurrent database access
 * 
 * @author: Tom Jin
 * @date: May 8, 2013
 */

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final int DATABASE_CORRUPT = 0;
	public static final int DATABASE_EMPTY = 1;
	public static final int DATABASE_ITEMS_NOT_DOWNLOADED = 2;
	public static final int DATABASE_READY = 3;

	public static final int MEDIA_NOT_ACTIVITY = 0;
	public static final int MEDIA_IS_ACTIVITY = 1;
	public static final int MEDIA_NOT_DOWNLOADED = 0;
	public static final int MEDIA_DOWNLOADED = 1;
	public static final int MEDIA_ACCESS_DENIED = 0;
	public static final int MEDIA_ACCESS_GRANTED = 1;
	public static final int MEDIA_NOT_COMPLETED = 0;
	public static final int MEDIA_COMPLETED = 1;

	public static Integer EXTRA_TRAINING = 0;
	public static Integer EXTRA_WHEELING = 1;

	private static DatabaseHelper mInstance;
	private SQLiteDatabase mdb;

	public static final String TABLE_APP_MEDIA_INFO = "media_info";
	public static final String TABLE_APP_BOOKMARKS = "bookmarks";
	public static final String TABLE_APP_ACHIEVEMENTS_POINTS = "achievements_points";
	public static final String TABLE_APP_STATISTICS = "statistics";
	public static final String TABLE_APP_STATISTICS_BREAKDOWN = "statistics_breakdown";
	public static final String TABLE_APP_STATISTICS_NONTABLET = "statistics_nontablet";
	public static final String TABLE_APP_INBOX = "inbox";
	public static final String TABLE_APP_OUTBOX = "outbox";

	private static final String DATABASE_NAME = "content.db";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE_MEDIA_INFO = "CREATE TABLE "
			+ TABLE_APP_MEDIA_INFO
			+ "(item_id TEXT, item_name TEXT, type INTEGER, requires TEXT, path TEXT, is_activity INTEGER, downloaded INTEGER, completed INTEGER, permission INTEGER, version_number INTEGER);";

	private static final String DATABASE_CREATE_BOOKMARKS = "CREATE TABLE "
			+ TABLE_APP_BOOKMARKS + "(date DATETIME, item_id TEXT, path TEXT);";

	private static final String DATABASE_CREATE_ACHIEVEMENTS_POINTS = "CREATE TABLE "
			+ TABLE_APP_ACHIEVEMENTS_POINTS + "(item_id TEXT, points INTEGER);";

	private static final String DATABASE_CREATE_STATISTICS = "CREATE TABLE "
			+ TABLE_APP_STATISTICS
			+ "(item_id TEXT, item_type TEXT, total_time INTEGER, num_times_completed INTEGER);";

	private static final String DATABASE_CREATE_STATISTICS_BREAKDOWN = "CREATE TABLE "
			+ TABLE_APP_STATISTICS_BREAKDOWN
			+ "(date DATETIME, item_id TEXT, item_type TEXT, time_spent INTEGER, timed_activity INTEGER);";

	private static final String DATABASE_CREATE_STATISTICS_NONTABLET = "CREATE TABLE "
			+ TABLE_APP_STATISTICS_NONTABLET
			+ "(date DATETIME, falls_incidents INTEGER, wheeling_time INTEGER, training_time INTEGER);";

	private static final String DATABASE_CREATE_INBOX = "CREATE TABLE "
			+ TABLE_APP_INBOX
			+ "(id TEXT, sender TEXT, date DATETIME, type INTEGER, location TEXT, title TEXT, description TEXT, read INTEGER);";

	private static final String DATABASE_CREATE_OUTBOX = "CREATE TABLE "
			+ TABLE_APP_OUTBOX
			+ "(id TEXT, target TEXT, date DATETIME, type INTEGER, location TEXT, title TEXT, description TEXT, sent INTEGER);";

	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static DatabaseHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DatabaseHelper(context.getApplicationContext());
		}
		return mInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE_MEDIA_INFO);
		database.execSQL(DATABASE_CREATE_BOOKMARKS);
		database.execSQL(DATABASE_CREATE_ACHIEVEMENTS_POINTS);
		database.execSQL(DATABASE_CREATE_STATISTICS);
		database.execSQL(DATABASE_CREATE_STATISTICS_BREAKDOWN);
		database.execSQL(DATABASE_CREATE_STATISTICS_NONTABLET);
		database.execSQL(DATABASE_CREATE_INBOX);
		database.execSQL(DATABASE_CREATE_OUTBOX);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Upgrade instructions here
	}

	// ============ Database Status =====================
	/**
	 * Checks if database is ready
	 */
	public int databaseStatus() {
		mdb = this.getWritableDatabase();

		int status = DatabaseHelper.DATABASE_EMPTY;

		Cursor b = mdb.rawQuery("SELECT item_id,  type FROM media_info", null);
		try {
			if (b.getCount() > 0) {
				Cursor c = mdb.rawQuery(
						"SELECT item_id, type FROM media_info WHERE downloaded="
								+ DatabaseHelper.MEDIA_NOT_DOWNLOADED
								+ " AND type !=" + MenuRegistry.MEDIA_MENU,
						null);
				try {
					if (c.getCount() > 0) {
						status = DatabaseHelper.DATABASE_ITEMS_NOT_DOWNLOADED;
						if (c.moveToFirst()) {
							System.out.println("Not downloaded: "
									+ c.getString(c.getColumnIndex("item_id")));
						}
					} else {
						status = DatabaseHelper.DATABASE_READY;
					}
				} catch (Exception e) {
					// Do nothing
					e.printStackTrace();
				} finally {
					c.close();
				}
			} else {
				status = DatabaseHelper.DATABASE_EMPTY;
			}

		} catch (Exception e) {
			status = DatabaseHelper.DATABASE_CORRUPT;
		} finally {
			b.close();
		}
		return status;
	}

	// ============ Media Info Management ===============
	// Keeps track of downloaded media content, their permissions, and
	// completion

	/**
	 * Adds reference to a particular media item
	 * 
	 * @param item
	 */
	public void addItem(MenuComponent item) {
		if (item != null) {
			mdb = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("item_id", item.getID());
			values.put("item_name", item.getName());
			values.put("type", item.getMediaType());
			values.put("requires", item.getRequires());
			values.put("path", item.getPath());
			values.put("is_activity", item.isActivity());
			values.put("downloaded", DatabaseHelper.MEDIA_NOT_DOWNLOADED);
			values.put("completed", DatabaseHelper.MEDIA_NOT_COMPLETED);
			values.put("permission", DatabaseHelper.MEDIA_ACCESS_GRANTED);
			values.put("version_number", item.getVersionNumber());
			mdb.insert("media_info", null, values);
		}
	}

	/**
	 * Updates database when changes occur on the server
	 */
	public void updateMediaInfo() {
		// TODO
		return;
	}

	/**
	 * Finds the name of an item given its ID
	 * 
	 * @param item_id
	 */
	public String itemNameLookup(String item_id) {
		mdb = this.getWritableDatabase();
		Cursor c = mdb.rawQuery(
				"SELECT item_name FROM media_info WHERE item_id='" + item_id
						+ "'", null);

		String itemName = "";
		try {
			if (c.moveToFirst()) {
				itemName = c.getString(c.getColumnIndex("item_name"));
			}
		} finally {
			c.close();
		}
		return itemName;
	}

	/**
	 * Sets an item as completed whether it be a menu, video, etc
	 * 
	 * @param item_id
	 */
	public void setCompleted(String item_id) {
		mdb = this.getWritableDatabase();
		mdb.execSQL("UPDATE media_info SET completed="
				+ DatabaseHelper.MEDIA_COMPLETED + " WHERE item_id='" + item_id
				+ "'");
	}

	/**
	 * Checks if an item has been completed or not
	 * 
	 * @param item_id
	 * @return true if item is completed
	 */
	public boolean isCompleted(String item_id) {
		mdb = this.getWritableDatabase();
		Cursor c = mdb.rawQuery(
				"SELECT completed FROM media_info WHERE item_id='" + item_id
						+ "' AND completed=" + DatabaseHelper.MEDIA_COMPLETED,
				null);

		boolean completed = false;
		try {
			if (c.getCount() > 0) {
				completed = true;
			}
		} finally {
			c.close();
		}

		return completed;
	}

	/**
	 * Each user has their own "permitted" files as directed by trainer Sets a
	 * permission for a given item
	 * 
	 * @param item_id
	 * @param permission
	 */
	public void setPermission(String item_id, Integer locked) {
		mdb = this.getWritableDatabase();
		int permission = DatabaseHelper.MEDIA_ACCESS_GRANTED;
		if (locked == 1) {
			permission = DatabaseHelper.MEDIA_ACCESS_DENIED;
		}
		mdb.execSQL("UPDATE media_info SET permission=" + permission
				+ " WHERE item_id='" + item_id + "'");
	}

	/**
	 * Determines whether a user is allowed to access a given item
	 * 
	 * @param item_id
	 * @return true is item is accessible
	 */
	public boolean isPermitted(String item_id) {
		mdb = this.getWritableDatabase();
		Cursor c = mdb.rawQuery(
				"SELECT permission FROM media_info WHERE item_id='" + item_id
						+ "' AND permission="
						+ DatabaseHelper.MEDIA_ACCESS_GRANTED, null);

		boolean locked = false;
		try {
			if (c.moveToFirst()) {
				if (c.getCount() > 0) {
					locked = true;
				}
			}
		} finally {
			c.close();
		}
		return locked;
	}

	/**
	 * Sets a media item as downloaded
	 */
	public void setDownloaded(String item_id) {
		mdb = this.getWritableDatabase();
		mdb.execSQL("UPDATE media_info SET downloaded="
				+ DatabaseHelper.MEDIA_DOWNLOADED + " WHERE item_id='"
				+ item_id + "'");
	}

	/**
	 * Gets a list of not downloaded media items
	 * 
	 * @param item_id
	 * @return true if item is completed
	 */
	public List<MenuLeaf> getItemsNotDownloaded() {
		mdb = this.getWritableDatabase();
		Cursor c = mdb.rawQuery("SELECT * FROM media_info WHERE downloaded="
				+ DatabaseHelper.MEDIA_NOT_DOWNLOADED, null);

		List<MenuLeaf> mediaItems = new LinkedList<MenuLeaf>();
		try {
			if (c.moveToFirst()) {

				for (int i = 0; i < c.getCount(); i++) {
					int versionNumber = c.getInt(c
							.getColumnIndex("version_number"));
					boolean isActivity = c.getInt(c
							.getColumnIndex("is_activity")) > 0;
					String itemID = c.getString(c.getColumnIndex("item_id"));
					int mediaType = c.getInt(c.getColumnIndex("type"));
					String requires = c.getString(c.getColumnIndex("requires"));
					String path = c.getString(c.getColumnIndex("path"));

					if (mediaType != MenuRegistry.MEDIA_MENU) {
						MenuLeaf leaf = new MenuLeaf(versionNumber, isActivity,
								mediaType, itemID, "", requires, path);
						mediaItems.add(leaf);
					}

					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}
		return mediaItems;
	}

	// ===================== Statistics ================================

	// Get number of videos in statistics
	public int getNumVideosWatched() {
		mdb = this.getWritableDatabase();
		Cursor c = mdb.rawQuery("SELECT num_times_completed FROM statistics",
				null);
		int numVideosAchieved = 0;
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {
					numVideosAchieved++;
				}
			}

		} finally {
			c.close();
		}
		return numVideosAchieved;
	}

	// Returns the number of times the participant has watched the
	// video/activity
	public Integer getStatisticsNumTimes(String item_id) {
		mdb = this.getWritableDatabase();
		Cursor c = mdb.rawQuery(
				"SELECT num_times_completed FROM statistics WHERE item_id='"
						+ item_id + "'", null);
		Integer num_times_completed = 0;
		try {
			if (c.getCount() > 0) {
				if (c.moveToFirst()) {
					num_times_completed = c.getInt(c
							.getColumnIndex("num_times_completed"));
				}
			}
		} finally {
			c.close();
		}
		return num_times_completed;
	}

	// Returns the time in seconds spent watching the video/activity
	public Integer getStatisticsTime(String item_id) {
		mdb = this.getWritableDatabase();
		Cursor c = mdb.rawQuery(
				"SELECT total_time FROM statistics WHERE item_id='" + item_id
						+ "'", null);
		Integer total_time_existing = 0;
		try {
			if (c.getCount() > 0) {
				if (c.moveToFirst()) {
					total_time_existing = c.getInt(c
							.getColumnIndex("total_time"));
				}
			}
		} finally {
			c.close();
		}
		return total_time_existing;
	}

	// Increases the number of times watched and total time watched for
	// video/activity
	public void addStatisticsTime(String item_id, int item_type,
			Integer timeWatched, Integer num_times_completed) {
		mdb = this.getWritableDatabase();
		Cursor c = mdb.rawQuery(
				"SELECT total_time, num_times_completed FROM statistics WHERE item_id='"
						+ item_id + "'", null);
		try {
			if (c.getCount() > 0) {
				if (c.moveToFirst()) {
					// Get existing statistics
					Integer total_time_existing = c.getInt(c
							.getColumnIndex("total_time"));
					Integer num_times_existing = c.getInt(c
							.getColumnIndex("num_times_completed"));
					total_time_existing = total_time_existing + timeWatched;
					num_times_existing = num_times_existing + 1;

					mdb.execSQL("UPDATE statistics SET total_time="
							+ total_time_existing + " WHERE item_id='"
							+ item_id + "'");
					mdb.execSQL("UPDATE statistics SET num_times_completed="
							+ num_times_existing + " WHERE item_id='" + item_id
							+ "'");
				}
			} else {
				addStatistics(item_id, item_type, timeWatched,
						num_times_completed);
			}
		} finally {
			c.close();
		}
	}

	// Add app permissions
	public void addStatistics(String item_id, int item_type,
			Integer total_time, Integer num_times_completed) {
		mdb = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("item_id", item_id);
		values.put("item_type", item_type); // TODO
		values.put("total_time", total_time);
		values.put("num_times_completed", num_times_completed);
		mdb.insert("statistics", null, values);
	}

	// ============== Statistics Breakdown =======

	// Records the video/activity watched
	// timeSpent is amount of time spent watching in seconds
	public void addStatisticsBreakdownTime(String item_id, int item_type,
			Integer timeSpent, Integer timedActivity) {
		mdb = this.getWritableDatabase();

		Date currDate = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.CANADA);

		ContentValues values = new ContentValues();
		values.put("date", dateFormat.format(currDate));
		values.put("item_id", item_id);
		values.put("item_type", item_type); // TODO
		values.put("time_spent", timeSpent);
		values.put("timed_activity", timedActivity);

		// Inserting Row
		mdb.insert("statistics_breakdown", null, values);

	}

	// Returns the number of different days that a particular item was accessed
	public Integer getNumberDaysAccessedForItem(String item_id) {
		mdb = this.getWritableDatabase();

		Cursor c = mdb.rawQuery(
				"SELECT date FROM statistics_breakdown WHERE item_id='"
						+ item_id + "'", null);

		Date lastDateProcessed = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
				Locale.CANADA);

		Integer numDaysAccessed = 0;
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {
					String date = c.getString(c.getColumnIndex("date"));
					try {
						Date dateObj = dateFormat.parse(date);
						if (i == 0) {
							lastDateProcessed = dateObj;
							numDaysAccessed++;
						} else {
							if (dateObj.compareTo(lastDateProcessed) == 0) {
								// Same date as before
							} else {
								lastDateProcessed = dateObj;
								numDaysAccessed++;
							}
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}
		return numDaysAccessed;
	}

	// Returns the amount of time (in seconds) spent doing timed activity for a
	// particular item
	public Integer getTimedActivityTime(String item_id) {
		mdb = this.getWritableDatabase();

		Cursor c = mdb.rawQuery(
				"SELECT timed_activity FROM statistics_breakdown WHERE item_id='"
						+ item_id + "'", null);

		Integer totalTimedActivity = 0;
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {
					Integer timeSpent = c.getInt(c
							.getColumnIndex("timed_activity"));
					totalTimedActivity += timeSpent;
					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}
		return totalTimedActivity;
	}


	/**
	 *  Returns the amount of time (in seconds) spent watching today
	 * @return
	 */
	public Integer getTimeSpentWatchingToday() {
		mdb = this.getWritableDatabase();

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.CANADA);

		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);

		Date currTimeDate = new Date(System.currentTimeMillis());
		String todayNow = dateFormat.format(currTimeDate);
		String todayStart = dateFormat.format(today.getTime());

		Cursor c = mdb.rawQuery(
				"SELECT time_spent FROM statistics_breakdown WHERE date BETWEEN '"
						+ todayStart + "' AND '" + todayNow + "'", null);

		Integer totalTime = 0;
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {
					Integer timeSpent = c
							.getInt(c.getColumnIndex("time_spent"));
					totalTime += timeSpent;
					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}
		return totalTime;
	}
	
	// Returns the amount of time (in seconds) spent watching videos/activities
	// in the past week
	public Integer getTimeSpentWatchingPastWeek() {
		mdb = this.getWritableDatabase();

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.CANADA);

		Calendar lastSunday = Calendar.getInstance();
		lastSunday.set(Calendar.HOUR, 0);
		lastSunday.set(Calendar.MINUTE, 0);
		lastSunday.set(Calendar.SECOND, 0);
		while (lastSunday.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			lastSunday.add(Calendar.DAY_OF_WEEK, -1);
		}

		Date currTimeDate = new Date(System.currentTimeMillis());
		String currTime = dateFormat.format(currTimeDate);
		String lastWeekTime = dateFormat.format(lastSunday.getTime());

		Cursor c = mdb.rawQuery(
				"SELECT time_spent FROM statistics_breakdown WHERE date BETWEEN '"
						+ lastWeekTime + "' AND '" + currTime + "'", null);

		Integer totalTime = 0;
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {
					Integer timeSpent = c
							.getInt(c.getColumnIndex("time_spent"));
					totalTime += timeSpent;
					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}
		return totalTime;
	}

	public Integer getTimeSpentLifetime() {
		mdb = this.getWritableDatabase();

		Cursor c = mdb.rawQuery("SELECT time_spent FROM statistics_breakdown",
				null);

		Integer totalTime = 0;
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {
					Integer timeSpent = c
							.getInt(c.getColumnIndex("time_spent"));
					totalTime += timeSpent;
					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}
		return totalTime;
	}

	public long getNumberDaysPracticed() {
		mdb = this.getWritableDatabase();

		Cursor c = mdb
				.rawQuery(
						"SELECT date, item_id, item_type, time_spent, timed_activity FROM statistics_breakdown ORDER BY date ASC LIMIT 1",
						null);

		try {
			if (c.moveToFirst()) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd", Locale.CANADA);
				String date = c.getString(c.getColumnIndex("date"));
				long dateStart = dateFormat.parse(date).getTime();
				long dateEnd = new Date().getTime();
				return Math.abs((dateEnd - dateStart) / (1000 * 60 * 60 * 24));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
		return 0;
	}

	// =========================================================================
	// Statistics Incidents/Falls/Additional Training

	// Records the falls/incidents/training
	public void addStatisticsIncidentsAndAdditionalTraining(
			boolean fallOrIncidents, Integer trainingTime, Integer wheelingTime) {
		mdb = this.getWritableDatabase();

		Date currDate = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.CANADA);

		int falls_incidents = 0; // 0 = false
		if (fallOrIncidents) {
			falls_incidents = 1; // 1 = true
		}

		ContentValues values = new ContentValues();
		values.put("date", dateFormat.format(currDate));
		values.put("falls_incidents", falls_incidents);
		values.put("wheeling_time", wheelingTime);
		values.put("training_time", trainingTime);

		// Inserting Row
		mdb.insert("statistics_nontablet", null, values);

		// Adds Total Additional Time as a Statistics Breakdown Item
		// Indicates to system that there was activity on this day
		addStatisticsBreakdownTime("", MenuRegistry.MEDIA_OTHER, wheelingTime
				+ trainingTime, 0);

	}

	// Returns whether falls/incidents occurred for a particular day
	public Integer didFall(String date) {
		mdb = this.getWritableDatabase();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
				Locale.CANADA);
		SimpleDateFormat fullDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.CANADA);

		Date dateToFind;
		Integer didFall = -1;
		try {
			dateToFind = dateFormat.parse(date);

			Date nextDay = new Date(dateToFind.getTime() + 24 * 60 * 59 * 1000);

			String dateToFindStr = fullDateFormat.format(dateToFind);
			String nextDayStr = fullDateFormat.format(nextDay);

			Cursor c = mdb.rawQuery(
					"SELECT falls_incidents FROM statistics_nontablet WHERE date BETWEEN '"
							+ dateToFindStr + "' AND '" + nextDayStr + "'",
					null);
			try {
				if (c.moveToFirst()) {
					for (int i = 0; i < c.getCount(); i++) {
						Integer fall = c.getInt(c
								.getColumnIndex("falls_incidents"));
						if (fall == 1) {
							didFall = 1;
						} else if (fall == 0) {
							didFall = 0;
						}
						c.moveToNext();
					}
				}
			} finally {
				c.close();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return didFall;
	}

	/**
	 * Get additional training for a particular day
	 * 
	 * @param date
	 * @param type
	 *            - either 0 for extra training time or 1 for extra wheeling
	 * @return
	 */
	public int getStatisticsAdditionalTraining(String date, Integer type) {
		mdb = this.getWritableDatabase();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
				Locale.CANADA);
		SimpleDateFormat fullDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.CANADA);

		Date dateToFind;
		int additionalTrainingTime = 0;
		try {
			dateToFind = dateFormat.parse(date);

			Date nextDay = new Date(dateToFind.getTime() + 24 * 60 * 59 * 1000);

			String dateToFindStr = fullDateFormat.format(dateToFind);
			String nextDayStr = fullDateFormat.format(nextDay);

			Cursor c = mdb
					.rawQuery(
							"SELECT training_time, wheeling_time FROM statistics_nontablet WHERE date BETWEEN '"
									+ dateToFindStr
									+ "' AND '"
									+ nextDayStr
									+ "'", null);
			try {
				if (c.moveToFirst()) {
					for (int i = 0; i < c.getCount(); i++) {
						Integer training = c.getInt(c
								.getColumnIndex("training_time"));
						Integer wheeling = c.getInt(c
								.getColumnIndex("wheeling_time"));

						if (type == DatabaseHelper.EXTRA_TRAINING) {
							additionalTrainingTime += training;
						}

						if (type == DatabaseHelper.EXTRA_WHEELING) {
							additionalTrainingTime += wheeling;
						}

						c.moveToNext();
					}
				}
			} finally {
				c.close();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return additionalTrainingTime;
	}

	// =========================================================================
	// Achievement Points

	public int getTotalPoints() {
		mdb = this.getWritableDatabase();
		Cursor c = mdb.rawQuery("SELECT points FROM achievements_points", null);
		int numPoints = 0;
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {
					numPoints += c.getInt(c.getColumnIndex("points"));
				}
			}
		} finally {
			c.close();
		}
		return numPoints;
	}

	public int getPoints(String item_id) {
		mdb = this.getWritableDatabase();
		Cursor c = mdb.rawQuery(
				"SELECT points FROM achievements_points WHERE item_id='"
						+ item_id + "' LIMIT 1", null);
		int numPoints = 0;
		try {
			if (c.getCount() > 0) {
				numPoints = c.getInt(c.getColumnIndex("points"));
			}
		} finally {
			c.close();
		}
		return numPoints;
	}

	public void addPoints(String item_id, Integer newPoints) {
		mdb = this.getWritableDatabase();
		Cursor c = mdb.rawQuery(
				"SELECT points FROM achievements_points WHERE item_id='"
						+ item_id + "' LIMIT 1", null);

		try {
			if (c.getCount() > 0) {
				if (c.moveToFirst()) {
					Integer origPoints = c.getInt(c.getColumnIndex("points"));
					newPoints = origPoints + newPoints;

					mdb.execSQL("UPDATE achievements_points SET points = "
							+ newPoints + " WHERE item_id = '" + item_id + "'");
				}
			} else {
				ContentValues values = new ContentValues();
				values.put("item_id", item_id);
				values.put("points", newPoints);
				mdb.insert("achievements_points", null, values);
			}
		} finally {
			c.close();
		}
	}

	// =========================================================================
	// Bookmarks

	public List<Bookmark> getAllBookmarks() {
		mdb = this.getWritableDatabase();
		Cursor c = mdb.rawQuery("SELECT date, item_id, path FROM bookmarks",
				null);
		List<Bookmark> bookmarks = new LinkedList<Bookmark>();
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {
					String date = c.getString(c.getColumnIndex("date"));
					String item_id = c.getString(c.getColumnIndex("item_id"));
					String path = c.getString(c.getColumnIndex("path"));
					Bookmark b = new Bookmark(date, item_id, path);
					bookmarks.add(b);
					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}
		return bookmarks;
	}

	public boolean isBookmarked(String item_id) {
		mdb = this.getWritableDatabase();
		Cursor c = mdb.rawQuery("SELECT path FROM bookmarks WHERE item_id = '"
				+ item_id + "' LIMIT 1", null);
		try {
			if (c.moveToFirst()) {
				if (c.getCount() > 0) {
					return true;
				}
			}
		} finally {
			c.close();
		}
		return false;
	}

	public void addBookmark(String item_id, String path) {
		mdb = this.getWritableDatabase();
		Date currDate = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.CANADA);
		ContentValues values = new ContentValues();
		values.put("date", dateFormat.format(currDate));
		values.put("item_id", item_id);
		values.put("path", path);
		mdb.insert("bookmarks", null, values);
	}

	public void removeBookmark(String item_id) {
		mdb = this.getWritableDatabase();
		mdb.execSQL("DELETE FROM bookmarks WHERE item_id='" + item_id + "'");
	}
	
	// =========================================================================
	// Messages

	/**
	 * Update a message as sent
	 */
	public void setMessageAsSent(String id) {
		mdb = this.getWritableDatabase();
		mdb.execSQL("UPDATE outbox SET sent = " + Message.SENT
				+ " WHERE id = '" + id + "'");
	}

	/**
	 * @return List of messages that have not been sent
	 */
	public List<Message> getUnsentMessages() {
		mdb = this.getWritableDatabase();

		Cursor c = mdb
				.rawQuery(
						"SELECT id, target, date, type, location, title, description FROM outbox WHERE sent = "
								+ Message.UNSENT, null);

		List<Message> messages = new ArrayList<Message>();
		try {
			if (c.moveToFirst()) {

				for (int i = 0; i < c.getCount(); i++) {

					String id = c.getString(c.getColumnIndex("id"));
					String to = c.getString(c.getColumnIndex("target"));
					String date = c.getString(c.getColumnIndex("date"));
					int type = c.getInt(c.getColumnIndex("type"));
					String location = c.getString(c.getColumnIndex("location"));
					String title = c.getString(c.getColumnIndex("title"));
					String description = c.getString(c
							.getColumnIndex("description"));

					Message msg = new Message(id, to, date, type, location,
							title, description, Message.READ, Message.UNSENT);
					messages.add(msg);

					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}
		return messages;
	}

	public int getNumUnreadMessages() {
		mdb = this.getWritableDatabase();

		Cursor c = mdb.rawQuery("SELECT id FROM inbox WHERE read = "
				+ Message.UNREAD, null);

		int numUnreadMessages = 0;
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {
					numUnreadMessages++;
					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}
		return numUnreadMessages;
	}

	public int getNumTotalMessages() {
		mdb = this.getWritableDatabase();

		Cursor c = mdb.rawQuery("SELECT id FROM inbox", null);

		int numMessages = 0;
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {
					numMessages++;
					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}
		return numMessages;
	}

	public String addOutgoingMessage(int type, String to, String location,
			String title, String description) {
		mdb = this.getWritableDatabase();

		Date currDate = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.CANADA);
		String messageID = RandomAlphaNumeric.getAlphaNumeric(6);
		ContentValues values = new ContentValues();
		values.put("id", messageID);
		values.put("target", to);
		values.put("date", dateFormat.format(currDate));
		values.put("type", type);
		values.put("location", location);
		values.put("title", title);
		values.put("description", description);
		values.put("sent", Message.UNSENT);

		mdb.insert("outbox", null, values);
		return messageID;
	}

	// Updates a message as read
	public void updateMessageAsRead(String item_id) {
		mdb = this.getWritableDatabase();
		mdb.execSQL("UPDATE inbox SET read = " + Message.READ + " WHERE id='"
				+ item_id + "'");
	}

	// Updates a message as sent
	public void updateMessageAsSent(String item_id) {
		mdb = this.getWritableDatabase();
		mdb.execSQL("UPDATE outbox SET sent =  " + Message.SENT + " WHERE id='"
				+ item_id + "'");
	}

	// Records an incoming message
	public void addIncomingMessage(int type, String from, String location,
			String title, String description) {
		mdb = this.getWritableDatabase();

		Date currDate = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.CANADA);

		ContentValues values = new ContentValues();
		values.put("id", RandomAlphaNumeric.getAlphaNumeric(6));
		values.put("sender", from);
		values.put("date", dateFormat.format(currDate));
		values.put("type", type);
		values.put("location", location);
		values.put("title", title);
		values.put("description", description);
		values.put("read", Message.UNREAD);

		mdb.insert("inbox", null, values);
	}

	// Returns all inbox messages
	public List<Message> getAllInbox() {
		mdb = this.getWritableDatabase();

		Cursor c = mdb
				.rawQuery(
						"SELECT id, sender, date, type, location, title, description, read FROM inbox ORDER BY DATE DESC",
						null);

		List<Message> messages = new ArrayList<Message>();

		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {

					String id = c.getString(c.getColumnIndex("id"));
					String from = c.getString(c.getColumnIndex("sender"));
					String date = c.getString(c.getColumnIndex("date"));
					int type = c.getInt(c.getColumnIndex("type"));
					String location = c.getString(c.getColumnIndex("location"));
					String title = c.getString(c.getColumnIndex("title"));
					String description = c.getString(c
							.getColumnIndex("description"));
					int read = c.getInt(c.getColumnIndex("read"));

					Message msg = new Message(id, from, date, type, location,
							title, description, read, Message.UNSENT);
					messages.add(msg);

					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}
		return messages;
	}

	// Returns all outbox messages
	public List<Message> getAllOutbox() {
		mdb = this.getWritableDatabase();

		Cursor c = mdb
				.rawQuery(
						"SELECT id, target, date, type, location, title, description, sent FROM outbox ORDER BY DATE DESC",
						null);

		List<Message> messages = new ArrayList<Message>();
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {

					String id = c.getString(c.getColumnIndex("id"));
					String to = c.getString(c.getColumnIndex("target"));
					String date = c.getString(c.getColumnIndex("date"));
					int type = c.getInt(c.getColumnIndex("type"));
					String location = c.getString(c.getColumnIndex("location"));
					String title = c.getString(c.getColumnIndex("title"));
					String description = c.getString(c
							.getColumnIndex("description"));
					int sent = c.getInt(c.getColumnIndex("sent"));

					Message msg = new Message(id, to, date, type, location,
							title, description, Message.READ, sent);
					messages.add(msg);

					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}
		return messages;
	}

	public void deleteAllInbox() {
		mdb = this.getWritableDatabase();
		mdb.execSQL("DELETE FROM inbox");
	}

	public void deleteMsg(int inboxOrOutbox, String id) {
		mdb = this.getWritableDatabase();
		if (inboxOrOutbox == Messages.INBOX) {
			mdb.execSQL("DELETE FROM inbox WHERE id='" + id + "'");
		} else {
			mdb.execSQL("DELETE FROM outbox WHERE id='" + id + "'");
		}
	}

	public void deleteAllOutbox() {
		mdb = this.getWritableDatabase();
		mdb.execSQL("DELETE FROM outbox");
	}

	// =========================================================================
	// JSON Generation

	// Generates a JSON object representation of "Individual Participant Data"
	public JSONObject getIndividualParticipantDataJSON(Context context,
			Date dataStartDate, Date dataEndDate, boolean isExtraWheeling) {
		mdb = this.getWritableDatabase();

		SimpleDateFormat fullDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.CANADA);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
				Locale.CANADA);

		String dataStartDateString = fullDateFormat.format(dataStartDate);
		String dataEndDateString = fullDateFormat.format(dataEndDate);

		Cursor c = mdb
				.rawQuery(
						"SELECT date, item_id, item_type, time_spent, timed_activity FROM statistics_breakdown WHERE date > '"
								+ dataStartDateString
								+ "' AND date < '"
								+ dataEndDateString + "' ORDER BY date ASC",
						null);

		String androidID = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);
		JSONObject rootIdentifier = new JSONObject();

		JSONArray rootParent = new JSONArray();
		JSONArray itemsForDay = new JSONArray();
		int trainingTime = 0;
		int viewingTime = 0;
		int activityTime = 0;
		int timedActivityTime = 0;
		int additionalTraining = 0;
		int extraWheeling = -1; // Default is 'n/a'
		int sessionNumber = 1;
		String fall = "N/A";

		Date lastFullDateProcessed = null;
		Date lastDateProcessed = null;
		try {
			if (c.moveToFirst()) {
				try {
					for (int i = 0; i < c.getCount(); i++) {
						// Iterate through each video/activity action taken

						// Get date of entry
						String date = c.getString(c.getColumnIndex("date"));
						Date dateObj = dateFormat.parse(date);
						Date fullDateObj = fullDateFormat.parse(date);

						if (i == 0) {
							lastDateProcessed = dateObj;
							lastFullDateProcessed = fullDateObj;
						} else {
							if (dateObj.compareTo(lastDateProcessed) == 0) {
								// Date in the database log is same as the
								// current
								// date we're looking at
								// Look if new session is needed
								long diffInMs = fullDateObj.getTime()
										- lastFullDateProcessed.getTime();
								long diffInMin = TimeUnit.MILLISECONDS
										.toMinutes(diffInMs);
								if ((diffInMin) > 120) {
									// Greater than 2 hours since last
									// video/activity; New session required
									// Write previous session into JSON
									if (sessionNumber == 1) {
										additionalTraining = getStatisticsAdditionalTraining(
												dateFormat
														.format(lastDateProcessed),
												DatabaseHelper.EXTRA_TRAINING);
										if (isExtraWheeling) {
											extraWheeling = getStatisticsAdditionalTraining(
													dateFormat
															.format(lastDateProcessed),
													DatabaseHelper.EXTRA_WHEELING);
										} else {
											extraWheeling = -1;
										}
									} else {
										additionalTraining = 0;
										extraWheeling = -1;
									}
									Integer didFall = didFall(dateFormat
											.format(lastDateProcessed));
									if (didFall == 1) {
										fall = "Yes";
									} else if (didFall == 0) {
										fall = "No";
									}

									JSONObject rowObject = new JSONObject();
									rowObject.put("date", dateFormat
											.format(lastDateProcessed));
									rowObject.put("session_number",
											sessionNumber);
									rowObject
											.put("training_time",
													TimeConversion
															.secondsToMinutesTwoDecimal(trainingTime));
									rowObject
											.put("viewing_time",
													TimeConversion
															.secondsToMinutesTwoDecimal(viewingTime));
									rowObject
											.put("activity_time",
													TimeConversion
															.secondsToMinutesTwoDecimal(activityTime));
									rowObject
											.put("timed_activity_time",
													TimeConversion
															.secondsToMinutesTwoDecimal(timedActivityTime));
									rowObject.put("additional_training",
											additionalTraining);
									rowObject.put("extra_wheeling",
											extraWheeling);
									rowObject.put("fall", fall);

									lastDateProcessed = dateObj;
									lastFullDateProcessed = fullDateObj;

									itemsForDay.put(rowObject);
									sessionNumber++;
									// Reset values
									trainingTime = 0;
									viewingTime = 0;
									activityTime = 0;
									timedActivityTime = 0;
									additionalTraining = 0;
									extraWheeling = -1;
									fall = "N/A";
								}
							} else {
								// Current item is another day's item
								// Write previous session in JSON

								if (sessionNumber == 1) {
									additionalTraining = getStatisticsAdditionalTraining(
											dateFormat
													.format(lastDateProcessed),
											DatabaseHelper.EXTRA_TRAINING);
									if (isExtraWheeling) {
										extraWheeling = getStatisticsAdditionalTraining(
												dateFormat
														.format(lastDateProcessed),
												DatabaseHelper.EXTRA_WHEELING);
									} else {
										extraWheeling = -1;
									}
								} else {
									additionalTraining = 0;
									extraWheeling = -1;
								}
								Integer didFall = didFall(dateFormat
										.format(lastDateProcessed));
								if (didFall == 1) {
									fall = "Yes";
								} else if (didFall == 0) {
									fall = "No";
								}

								JSONObject rowObject = new JSONObject();
								rowObject.put("date",
										dateFormat.format(lastDateProcessed));
								rowObject.put("session_number", sessionNumber);
								rowObject
										.put("training_time",
												TimeConversion
														.secondsToMinutesTwoDecimal(trainingTime));
								rowObject
										.put("viewing_time",
												TimeConversion
														.secondsToMinutesTwoDecimal(viewingTime));
								rowObject
										.put("activity_time",
												TimeConversion
														.secondsToMinutesTwoDecimal(activityTime));
								rowObject
										.put("timed_activity_time",
												TimeConversion
														.secondsToMinutesTwoDecimal(timedActivityTime));
								rowObject.put("additional_training",
										additionalTraining);
								rowObject.put("extra_wheeling", extraWheeling);
								rowObject.put("fall", fall);

								itemsForDay.put(rowObject);
								rootParent.put(itemsForDay);

								itemsForDay = new JSONArray();

								lastDateProcessed = dateObj;
								lastFullDateProcessed = fullDateObj;

								// Reset session and values
								sessionNumber = 1;
								trainingTime = 0;
								viewingTime = 0;
								activityTime = 0;
								timedActivityTime = 0;
								additionalTraining = 0;
								extraWheeling = -1;
								fall = "N/A";
							}
						}

						String item_type = c.getString(c
								.getColumnIndex("item_type"));

						Integer time_spent = c.getInt(c
								.getColumnIndex("time_spent"));

						Integer timed_activity = c.getInt(c
								.getColumnIndex("timed_activity"));

						trainingTime += timed_activity;
						trainingTime += time_spent;
						if (item_type.equals("video")) {
							viewingTime += time_spent;
						} else if (item_type.equals("activity")) {
							activityTime += time_spent;
						}
						timedActivityTime += timed_activity;

						c.moveToNext();

					}

					// Write any leftover values (eg. happens when querying only
					// one
					// day)
					if (trainingTime > 0) {
						if (sessionNumber == 1) {
							additionalTraining = getStatisticsAdditionalTraining(
									dateFormat.format(lastDateProcessed),
									DatabaseHelper.EXTRA_TRAINING);
							if (isExtraWheeling) {
								extraWheeling = getStatisticsAdditionalTraining(
										dateFormat.format(lastDateProcessed),
										DatabaseHelper.EXTRA_WHEELING);
							} else {
								extraWheeling = -1;
							}
						} else {
							additionalTraining = 0;
							extraWheeling = -1;
						}
						Integer didFall = didFall(dateFormat
								.format(lastDateProcessed));
						if (didFall == 1) {
							fall = "Yes";
						} else if (didFall == 0) {
							fall = "No";
						}

						JSONObject rowObject = new JSONObject();
						rowObject.put("date",
								dateFormat.format(lastDateProcessed));
						rowObject.put("session_number", sessionNumber);
						rowObject.put("training_time", TimeConversion
								.secondsToMinutesTwoDecimal(trainingTime));
						rowObject.put("viewing_time", TimeConversion
								.secondsToMinutesTwoDecimal(viewingTime));
						rowObject.put("activity_time", TimeConversion
								.secondsToMinutesTwoDecimal(activityTime));
						rowObject.put("timed_activity_time", TimeConversion
								.secondsToMinutesTwoDecimal(timedActivityTime));
						rowObject
								.put("additional_training", additionalTraining);
						rowObject.put("extra_wheeling", extraWheeling);
						rowObject.put("fall", fall);

						itemsForDay.put(rowObject);
						rootParent.put(itemsForDay);
					}

					rootIdentifier.put(androidID, rootParent);

				} catch (JSONException e) {
				} catch (ParseException e) {
				}
			}
		} finally {
			c.close();
		}
		return rootIdentifier;
	}

	// Generates a JSON object representation of "Individual Training Behaviour"
	public JSONObject getIndividualTrainingBehaviourJSON(Context context,
			Date dataStartDate) {
		mdb = this.getWritableDatabase();

		SimpleDateFormat fullDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.CANADA);
		String dataStartDateString = fullDateFormat.format(dataStartDate);

		Cursor c = mdb
				.rawQuery(
						"SELECT date, item_id, item_type, time_spent, timed_activity FROM statistics_breakdown WHERE date > '"
								+ dataStartDateString + "'", null);

		String androidID = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);

		JSONObject rootIdentifier = new JSONObject();
		JSONArray rootParent = new JSONArray();
		JSONArray itemsForCurrentDate = new JSONArray();

		Date lastDateProcessed = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
				Locale.CANADA);

		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {

					String date = c.getString(c.getColumnIndex("date"));
					Date dateObj = null;
					try {
						dateObj = dateFormat.parse(date);
					} catch (ParseException e1) {
						e1.printStackTrace();
					}

					if (i == 0) {
						// Save the date of the current row so we can check
						// later if
						// it's a different day
						lastDateProcessed = dateObj;
					} else {
						if (dateObj.compareTo(lastDateProcessed) == 0) {
							// Same date as before
						} else {
							// Different day; Save JSON array to last date
							try {
								JSONObject dayObjects = new JSONObject();
								dayObjects.put(
										dateFormat.format(lastDateProcessed),
										itemsForCurrentDate);
								rootParent.put(dayObjects);
								itemsForCurrentDate = new JSONArray();
								lastDateProcessed = dateObj;
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
					String item_id = c.getString(c.getColumnIndex("item_id"));

					String item_type = c.getString(c
							.getColumnIndex("item_type"));

					Integer time_spent = c.getInt(c
							.getColumnIndex("time_spent"));

					Integer timed_activity = c.getInt(c
							.getColumnIndex("timed_activity"));

					if (!item_type.equals("extra")) {
						try {
							JSONObject rowObject = new JSONObject();
							rowObject.put("item_id", item_id);
							rowObject.put("item_type", item_type);
							rowObject.put("time_spent", TimeConversion
									.secondsToMinutesTwoDecimal(time_spent));
							rowObject
									.put("timed_activity",
											TimeConversion
													.secondsToMinutesTwoDecimal(timed_activity));
							itemsForCurrentDate.put(rowObject);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}

		if (itemsForCurrentDate.length() > 0) {
			try {
				JSONObject dayObjects = new JSONObject();
				dayObjects.put(dateFormat.format(lastDateProcessed),
						itemsForCurrentDate);
				rootParent.put(dayObjects);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		try {
			rootIdentifier.put(androidID, rootParent);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return rootIdentifier;
	}

	// Generates a JSON object representation of "Summary of Training Behaviour"
	public JSONObject getSummaryTrainingBehaviourJSON(Context context) {
		mdb = this.getWritableDatabase();

		Cursor c = mdb
				.rawQuery(
						"SELECT item_id, item_type, total_time, num_times_completed FROM statistics",
						null);

		String androidID = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);

		JSONObject rootIdentifier = new JSONObject();
		JSONObject rootParent = new JSONObject();
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {

					String item_id = c.getString(c.getColumnIndex("item_id"));

					String item_type = c.getString(c
							.getColumnIndex("item_type"));

					Integer total_time = c.getInt(c
							.getColumnIndex("total_time"));

					Integer num_times_completed = c.getInt(c
							.getColumnIndex("num_times_completed"));

					try {
						JSONObject rowObject = new JSONObject();
						rowObject.put("item_type", item_type);
						rowObject.put("total_time", TimeConversion
								.secondsToMinutesTwoDecimal(total_time));
						rowObject.put("num_times_completed",
								num_times_completed);
						rowObject.put("num_days_accessed",
								getNumberDaysAccessedForItem(item_id));
						rootParent.put(item_id, rowObject);
					} catch (JSONException e) {
						e.printStackTrace();
					}

					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}

		try {
			rootIdentifier.put(androidID, rootParent);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return rootIdentifier;
	}

	// --------- App Permissions --------------

	// Generates a JSON object representation of "App Permissions table"
	public JSONObject getAppPermissionsJSON(Context context) {
		mdb = this.getWritableDatabase();

		Cursor c = mdb
				.rawQuery(
						"SELECT item_id, completed, permission, requires FROM media_info",
						null);

		JSONObject rootIdentifier = new JSONObject();
		JSONArray rootParent = new JSONArray();
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {

					String item_id = c.getString(c.getColumnIndex("item_id"));

					Integer completed = c.getInt(c.getColumnIndex("completed"));

					Integer permission = c.getInt(c
							.getColumnIndex("permission"));

					String permission_required_id = c.getString(c
							.getColumnIndex("requires"));

					try {
						JSONObject rowObject = new JSONObject();
						rowObject.put("item_id", item_id);
						rowObject.put("completed", completed);
						rowObject.put("permission", permission);
						rowObject.put("requires", permission_required_id);
						rootParent.put(rowObject);
					} catch (JSONException e) {
						e.printStackTrace();
					}

					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}

		try {
			rootIdentifier.put("media_info", rootParent);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return rootIdentifier;
	}

}