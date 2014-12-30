package com.kinetiqa.glacier.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.kinetiqa.glacier.R;
import com.kinetiqa.glacier.core.connection.ConnectionManager;
import com.kinetiqa.glacier.dialogs.DialogLogin;

/**
 * Created by Tom on 2014-11-09.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settings);

        PreferenceScreen changeUserAccount = (PreferenceScreen) findPreference("change_user_account");
        changeUserAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference arg0) {
                DialogLogin dialogLogin = new DialogLogin(getActivity(), false);
                dialogLogin.show();
                return false;
            }
        });

        PreferenceScreen syncNow = (PreferenceScreen) findPreference("sync_now");
        syncNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference arg0) {
                ConnectionManager cm = new ConnectionManager(getActivity());
                cm.sync();
                return false;
            }
        });

        PreferenceScreen version = (PreferenceScreen) findPreference("version");
        version.setSummary("0.1"); // TODO
    }
}
