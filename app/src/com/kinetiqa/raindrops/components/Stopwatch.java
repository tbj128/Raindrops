package com.kinetiqa.raindrops.components;


public class Stopwatch {

	public static long secondsRaw;
	public static long minRaw;
	public static long hoursRaw;
	public static String seconds;
	public static String minutes;
	public static Object hours;

	/**
	 * Converts a time in milliseconds to the given stopwatch format
	 * @param time in milliseconds
	 * @return
	 */
	public static String formatTime(float time) {
		secondsRaw = (long) (time / 1000);
		minRaw = (long) ((time / 1000) / 60);
		hoursRaw = (long) (((time / 1000) / 60) / 60);

		/*
		 * Convert the seconds to String and format to ensure it has a leading
		 * zero when required
		 */
		secondsRaw = secondsRaw % 60;
		seconds = String.valueOf(secondsRaw);
		if (secondsRaw == 0) {
			seconds = "00";
		}
		if (secondsRaw < 10 && secondsRaw > 0) {
			seconds = "0" + seconds;
		}

		/* Convert the minutes to String and format the String */

		minRaw = minRaw % 60;
		minutes = String.valueOf(minRaw);
		if (minRaw == 0) {
			minutes = "00";
		}
		if (minRaw < 10 && minRaw > 0) {
			minutes = "0" + minutes;
		}

		/* Convert the hours to String and format the String */

		hours = String.valueOf(hoursRaw);
		if (hoursRaw == 0) {
			hours = "00";
		}
		if (hoursRaw < 10 && hoursRaw > 0) {
			hours = "0" + hours;
		}

		/* Setting the timer text to the elapsed time */

		return hours + ":" + minutes + ":" + seconds;
	}

}
