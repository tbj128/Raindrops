package com.kinetiqa.raindrops.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PointsManager {
	private SharedPreferences sharedPreferences;

	public PointsManager(Context context) {
		this.sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
	}

	public void addPoints(long newPoints) {
		long points = sharedPreferences.getLong("points", 0);
		points += newPoints;
		sharedPreferences.edit().putLong("points", points).commit();
		checkPointsAwards(points);
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
