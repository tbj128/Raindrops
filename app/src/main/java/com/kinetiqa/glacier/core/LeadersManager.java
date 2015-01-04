package com.kinetiqa.glacier.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.kinetiqa.glacier.components.CompletedLeader;
import com.kinetiqa.glacier.components.PointsLeader;
import com.kinetiqa.glacier.core.connection.ConnectionManager;
import com.kinetiqa.glacier.database.DatabaseHelper;
import com.kinetiqa.glacier.messaging.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 2014-11-02.
 */
public class LeadersManager {

    private static LeadersManager mInstance;
    private SharedPreferences sharedPreferences;
    private Context context;

    private List<PointsLeader> pointsLeaders;
    private List<CompletedLeader> completedLeaders;

    private LeadersManager(Context c) {
        this.context = c;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
    }

    public static LeadersManager getInstance(Context c) {
        if (mInstance == null) {
            mInstance = new LeadersManager(c);
        }
        return mInstance;
    }

    public void refreshLeaders() {
        ConnectionManager cm = new ConnectionManager(context);
        pointsLeaders = cm.getPointsLeaders();
        completedLeaders = cm.getCompletedLeaders();
    }

    public List<PointsLeader> getPointsLeaders() {
        return pointsLeaders;
    }

    public List<CompletedLeader> getCompletedLeaders() {
        return completedLeaders;
    }

    public String getPointsLeadersRank() {
        int rank = -1;
        String username = sharedPreferences.getString("username", "");
        for (int i = 0; i < pointsLeaders.size(); i++) {
            if (username.equals(pointsLeaders.get(i).getUsername())) {
                rank = i + 1;
            }
        }
        if (rank != -1) {
            return rank + "/" + pointsLeaders.size();
        } else {
            return pointsLeaders.size() + "/" + pointsLeaders.size();
        }
    }

    public String getCompletedLeadersRank() {
        int rank = -1;
        String username = sharedPreferences.getString("username", "");
        for (int i = 0; i < completedLeaders.size(); i++) {
            if (username.equals(completedLeaders.get(i).getUsername())) {
                rank = i + 1;
            }
        }
        if (rank != -1) {
            return rank + "/" + completedLeaders.size();
        } else {
            return completedLeaders.size() + "/" + completedLeaders.size();
        }
    }


}
