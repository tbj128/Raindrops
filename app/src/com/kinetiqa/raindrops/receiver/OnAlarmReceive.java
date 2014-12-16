package com.kinetiqa.raindrops.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnAlarmReceive extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d("Alarm", "BroadcastReceiver, in onReceive:");

		context.startService(new Intent(context,
				com.kinetiqa.raindrops.service.BackgroundDownloader.class));
	}

}