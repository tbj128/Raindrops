package com.kinetiqa.glacier.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kinetiqa.glacier.R;
import com.kinetiqa.glacier.components.CompletedLeader;
import com.kinetiqa.glacier.components.PointsLeader;
import com.kinetiqa.glacier.core.LeadersManager;

import java.util.ArrayList;
import java.util.List;


public class FragmentLeaders extends Fragment {

    private List<PointsLeader> pointsLeaders;
    private List<CompletedLeader> completedLeaders;

    private View fragmentView;
    private ListView pointsLeadersListView;
    private ListView completedLeadersListView;
    private TextView leadersPointsRankTextView;
    private TextView leadersCompletedRankTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.leaders_fragment, container, false);
        fragmentView = view;
        init();
        return view;
    }

    private void init() {
        leadersPointsRankTextView = (TextView) fragmentView.findViewById(R.id.leaders_points_rank);
        leadersCompletedRankTextView = (TextView) fragmentView.findViewById(R.id.leaders_completed_rank);

        pointsLeadersListView = (ListView) fragmentView.findViewById(R.id.leaders_points_list);
        pointsLeadersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            }
        });

        completedLeadersListView = (ListView) fragmentView.findViewById(R.id.leaders_completed_list);
        pointsLeadersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            }
        });

        UpdateLeaders leadersTask = new UpdateLeaders(getActivity());
        leadersTask.execute();
    }

    private BaseAdapter pointsLeadersAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
                return pointsLeaders.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_leader, parent, false);

            PointsLeader leader = pointsLeaders.get(position);

            TextView usernameTextView = (TextView) view.findViewById(R.id.item_leader_username);
            usernameTextView.setText(leader.getUsername());

            TextView pointsTextView = (TextView) view.findViewById(R.id.item_leader_alt);
            pointsTextView.setText(String.valueOf(leader.getPoints()) + " points");

            return view;
        }
    };

    private BaseAdapter completedLeadersAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return completedLeaders.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_leader, parent, false);

            CompletedLeader leader = completedLeaders.get(position);

            TextView usernameTextView = (TextView) view.findViewById(R.id.item_leader_username);
            usernameTextView.setText(leader.getUsername());

            TextView pointsTextView = (TextView) view.findViewById(R.id.item_leader_alt);
            pointsTextView.setText(String.valueOf(leader.getNumItemsCompleted()) + " items completed");

            return view;
        }
    };

    private class UpdateLeaders extends AsyncTask<Void, Void, Void> {

        private Context c;

        public UpdateLeaders(Context c) {
            this.c = c;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... voids) {
            LeadersManager.getInstance(c).refreshLeaders();
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            pointsLeaders = LeadersManager.getInstance(getActivity()).getPointsLeaders();
            if (pointsLeaders == null) {
                pointsLeaders = new ArrayList<PointsLeader>();
            }
            pointsLeadersListView.setAdapter(pointsLeadersAdapter);

            TextView leadersPointsEmptyTextView = (TextView) fragmentView.findViewById(R.id.leaders_points_empty);
            if (pointsLeaders.size() == 0) {
                leadersPointsEmptyTextView.setVisibility(View.VISIBLE);
                leadersPointsRankTextView.setText("-/-");
            } else {
                leadersPointsEmptyTextView.setVisibility(View.GONE);
                leadersPointsRankTextView.setText(LeadersManager.getInstance(getActivity()).getPointsLeadersRank());
            }


            completedLeaders = LeadersManager.getInstance(getActivity()).getCompletedLeaders();
            if (completedLeaders == null) {
                completedLeaders = new ArrayList<CompletedLeader>();
            }
            completedLeadersListView.setAdapter(completedLeadersAdapter);

            TextView leadersCompletedEmptyTextView = (TextView) fragmentView.findViewById(R.id.leaders_completed_empty);
            if (completedLeaders.size() == 0) {
                leadersCompletedEmptyTextView.setVisibility(View.VISIBLE);
                leadersCompletedRankTextView.setText("-/-");
            } else {
                leadersCompletedEmptyTextView.setVisibility(View.GONE);
                leadersCompletedRankTextView.setText(LeadersManager.getInstance(getActivity()).getCompletedLeadersRank());
            }
        }
    }

}
