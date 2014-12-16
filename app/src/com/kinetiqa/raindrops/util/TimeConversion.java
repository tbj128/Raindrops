package com.kinetiqa.raindrops.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeConversion {

	public static int millsecondsToSeconds(float time) {
		return (int) (time / 1000);
	}

	public static int millsecondsToMinutes(float time) {
		return (int) ((time / 1000) / 60);

	}

	public static int millsecondsToHours(float time) {
		return (int) (((time / 1000) / 60) / 60);
	}

	public static double secondsToMinutesTwoDecimal(int time) {
		double timeInMinutes = (double) time / 60;
		return (double) Math.round(timeInMinutes * 10.00) / 10.00;
	}

	public static Date convertDateTimetoDate(Date dateTime) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
				Locale.CANADA);
		String currDateTimeStr = dateFormat.format(dateTime);
		try {
			return dateFormat.parse(currDateTimeStr);
		} catch (ParseException e) {
			return dateTime;
		}
	}

	public static String convertMillisecondsToTime(int milliseconds) {
		String sign = "";
		if (milliseconds < 0) {
			sign = "-";
			milliseconds = Math.abs(milliseconds);
		}

		long minutes = milliseconds / TimeUnit.MINUTES.toMillis(1);
		long seconds = milliseconds % TimeUnit.MINUTES.toMillis(1)
				/ TimeUnit.SECONDS.toMillis(1);

		final StringBuilder formatted = new StringBuilder(20);
		formatted.append(sign);
		formatted.append(String.format("%02d", minutes));
		formatted.append(String.format(":%02d", seconds));

		return formatted.toString();
	}

	// TODO make this better
	public static String convertDateToHumanReadable(String messageDate) {

		if (messageDate == null) {
			return "";
		}

		SimpleDateFormat dateFormatInput = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.CANADA);
		Date targetDate;
		try {
			targetDate = dateFormatInput.parse(messageDate);
		} catch (ParseException e1) {
			e1.printStackTrace();
			return messageDate;
		}

		long currentTime = System.currentTimeMillis();
		long millis = targetDate.getTime();
		long millisecondDifference = currentTime - millis;
		if (millisecondDifference < 5 * 60 * 1000) {
			// Message is less than 5 minutes old
			return "Just now";
		} else if (millisecondDifference < 24 * 60 * 60 * 1000) {
			try {
				String time = messageDate.split(" ")[1];
				String[] times = time.split(":");
				int hour = Integer.parseInt(times[0]);
				int minute = Integer.parseInt(times[1]);
				String minuteStr = String.valueOf(minute);
				if (minute < 10) {
					minuteStr = "0" + String.valueOf(minute);
				}
				if (hour == 0) {
					return "12:" + minuteStr + " am";
				} else if (hour >= 12) {
					if (hour > 12)
						return (hour - 12) + ":" + minuteStr + " pm";
					else
						return hour + ":" + minuteStr + " pm";
				} else {
					return hour + ":" + minuteStr + " am";
				}
			} catch (Exception e) {
				return messageDate;
			}
		} else {
			SimpleDateFormat dateFormatOutput = new SimpleDateFormat("MMM dd",
					Locale.CANADA);
			Date formattedDateObj;
			try {
				formattedDateObj = dateFormatInput.parse(messageDate);
				return dateFormatOutput.format(formattedDateObj);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return messageDate;
		}
	}

	/**
	 * Returns a string formatted in either seconds or minutes (depending on size of parameter)
	 * @param seconds
	 * @return
	 */
	public static String secondsToTime(int seconds) {
		if (seconds < 0) {
			return "0 sec";
		}
		if (seconds < 60) {
			return String.valueOf(seconds) + " sec";
		} else {
			double numMinutesWatched = TimeConversion
					.secondsToMinutesTwoDecimal(seconds);
			return String.valueOf(numMinutesWatched) + " min";
		}
	}
}
