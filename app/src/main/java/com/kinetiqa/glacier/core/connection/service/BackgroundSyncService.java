package com.kinetiqa.glacier.core.connection.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.kinetiqa.glacier.core.connection.ConnectionManager;

public class BackgroundSyncService extends Service {

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
		Log.d("Glacier", "Start Sync");
		ConnectionManager cm = new ConnectionManager(BackgroundSyncService.this);
		cm.sync();
		return Service.START_STICKY;
	}

}
