package com.kinetiqa.raindrops.util;

import android.widget.Toast;

import com.kinetiqa.raindrops.Home;

public class Toasted {

	public static void showToast(String msg) {
		if (Home.getAppContext() != null) {
			Toast.makeText(Home.getAppContext(), msg,
					Toast.LENGTH_SHORT).show();
		}
	}
}
