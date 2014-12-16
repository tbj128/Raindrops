package com.kinetiqa.raindrops.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.kinetiqa.raindrops.connection.ConnectionManager;

public class BackgroundDownloader extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("Raindrops", "Start Sync");
		ConnectionManager cm = new ConnectionManager(BackgroundDownloader.this);
		cm.sync();
		return Service.START_STICKY;
	}

}
