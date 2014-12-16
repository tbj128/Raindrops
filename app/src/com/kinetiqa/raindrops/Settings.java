package com.kinetiqa.raindrops;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.kinetiqa.raindrops.connection.ConnectionManager;
import com.kinetiqa.raindrops.dialogs.DialogManager;

public class Settings extends PreferenceActivity {

	private DialogManager dialogManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);

		dialogManager = new DialogManager(Settings.this);
		
		// ---------------------------------------------------------------
		
		PreferenceScreen changeUserAccount = (PreferenceScreen) findPreference("ChangeUserAccount");
		changeUserAccount.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(android.preference.Preference arg0) {
				dialogManager.login();
				return false;
			}
		});
		
		PreferenceScreen syncNow = (PreferenceScreen) findPreference("SyncNow");
		syncNow.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(android.preference.Preference arg0) {
				ConnectionManager cm = new ConnectionManager(Settings.this);
				cm.sync();
				return false;
			}
		});

		PreferenceScreen about = (PreferenceScreen) findPreference("About");
		Intent intent = new Intent(this, About.class);
		about.setIntent(intent);

	}
}