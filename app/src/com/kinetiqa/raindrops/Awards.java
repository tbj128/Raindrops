package com.kinetiqa.raindrops;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinetiqa.raindrops.database.DatabaseHelper;
import com.kinetiqa.raindrops.dialogs.DialogManager;
import com.kinetiqa.raindrops.util.Fonts;
import com.kinetiqa.raindrops.util.Statistics;
import com.kinetiqa.raindrops.util.TimeConversion;

public class Awards extends Activity {

	public static final int NUM_AWARDS = 8;
	public static final int FIRST_ITEM = 0;
	public static final int CONSECUTIVE_DAYS_2 = 1;
	public static final int CONSECUTIVE_DAYS_7 = 2;
	public static final int POINTS_50 = 3;
	public static final int POINTS_100 = 4;
	public static final int POINTS_500 = 5;
	public static final int POINTS_1000 = 6;
	public static final int POINTS_10000 = 7;

	private Context context;
	private SharedPreferences sharedPreferences;

	/**
	 * LAYOUT STYLES
	 */
	private Typeface typeFaceHandwritten;
	private Typeface typeFaceAcme;
	
	private ListView awardsList;
	private ImageButton backButton;

	private int totalItemsCompleted = 0;
	private int lifeTimeMinutes = 0;
	private int lifeTimePoints = 0;
	private DialogManager dialogManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.awards);

		typeFaceHandwritten = Typeface.createFromAsset(getAssets(), Fonts.HANDWRITTEN);
		typeFaceAcme = Typeface.createFromAsset(getAssets(), Fonts.ACME);
		
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(Awards.this);

		dialogManager = new DialogManager(Awards.this);

		awardsList = (ListView) findViewById(R.id.awards_list);
		awardsList.setAdapter(mAdapter);
		awardsList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				// Do nothing for now
			}
		});

		initializeButtonEvents();

	}

	@Override
	public void onResume() {
		super.onResume();
		initializeBackground();
		awardsList.setAdapter(mAdapter);
		renderStatistics();
		renderStatisticsMain();
	}

	private void initializeBackground() {
		TextView logo_l = (TextView) findViewById(R.id.bottom_nav_logo_l);
		logo_l.setTypeface(typeFaceHandwritten);
		TextView logo_r = (TextView) findViewById(R.id.bottom_nav_logo_r);
		logo_r.setTypeface(typeFaceHandwritten);
		
		int selectedBackground = (int) sharedPreferences.getLong("bg_selected",
				Store.BG_ORIG);
		RelativeLayout mainContainer = (RelativeLayout) findViewById(R.id.main_container);
		switch (selectedBackground) {
		case Store.BG_ORIG:
			mainContainer.setBackgroundResource(R.drawable.bg_mountains);
			break;
		case Store.BG_SAILS:
			mainContainer.setBackgroundResource(R.drawable.bg_sails);
			break;
		default:
			mainContainer.setBackgroundResource(R.drawable.main_bg);
		}
	}
	
	/**
	 * Sets up button events
	 */
	private void initializeButtonEvents() {

		TextView setGoalsButton = (TextView) findViewById(R.id.bottom_progress_text_denom);
		setGoalsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialogManager.showDialogSetGoals(Awards.this);
			}
		});

		ImageButton openBookmarksButton = (ImageButton) findViewById(R.id.bottom_nav_bookmarks);
		openBookmarksButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), Submenu.class);
				i.putExtra("bookmarks_view", true);
				startActivity(i);
			}
		});

		ImageButton openAwardsButton = (ImageButton) findViewById(R.id.bottom_nav_awards);
		openAwardsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Do Nothing
			}
		});

		backButton = (ImageButton) findViewById(R.id.nav_back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * Method to render all the statistics on the right hand side
	 */
	private void renderStatisticsMain() {

		lifeTimeMinutes = DatabaseHelper.getInstance(getApplicationContext())
				.getTimeSpentLifetime();
		lifeTimeMinutes = Math.round(lifeTimeMinutes / 60);

		lifeTimePoints = (int) sharedPreferences.getLong("points", 0);

		long numberDaysPracticed = DatabaseHelper.getInstance(
				getApplicationContext()).getNumberDaysPracticed();
		if (numberDaysPracticed == 0) {
			numberDaysPracticed = 1;
		}
		float numberWeeksPracticed = (float) numberDaysPracticed / 7;
		if (numberWeeksPracticed == 0) {
			numberWeeksPracticed = 1;
		}
		Integer lifetimeMessages = DatabaseHelper.getInstance(
				getApplicationContext()).getNumTotalMessages();

		TextView award_stat_lifetime_text = (TextView) findViewById(R.id.award_stat_lifetime_text);
		award_stat_lifetime_text.setText(String.valueOf(lifeTimeMinutes));

		TextView award_stat_avgtimeweekly_text = (TextView) findViewById(R.id.award_stat_avgtimeweekly_text);
		Integer averageWeeklyPracticed = Math.round(lifeTimeMinutes
				/ numberWeeksPracticed);
		award_stat_avgtimeweekly_text.setText(String
				.valueOf(averageWeeklyPracticed));

		TextView award_stat_avgpointsweekly_text = (TextView) findViewById(R.id.award_stat_avgpointsweekly_text);
		Integer averageWeeklyPoints = Math.round(lifeTimePoints
				/ numberWeeksPracticed);
		award_stat_avgpointsweekly_text.setText(String
				.valueOf(averageWeeklyPoints));

		TextView award_stat_avgtimedaily_text = (TextView) findViewById(R.id.award_stat_avgtimedaily_text);
		Integer averageDailyPracticed = Math.round(lifeTimeMinutes
				/ numberDaysPracticed);
		award_stat_avgtimedaily_text.setText(String
				.valueOf(averageDailyPracticed));

		TextView award_stat_avgpointsdaily_text = (TextView) findViewById(R.id.award_stat_avgpointsdaily_text);
		Integer averageDailyPoints = Math.round(lifeTimePoints
				/ numberDaysPracticed);
		award_stat_avgpointsdaily_text.setText(String
				.valueOf(averageDailyPoints));

		TextView award_stat_messages_text = (TextView) findViewById(R.id.award_stat_messages_text);
		award_stat_messages_text.setText(String.valueOf(lifetimeMessages));

		renderItemCompletionProgressBar();
	}

	/**
	 * Renders the item progress bar
	 */
	private void renderItemCompletionProgressBar() {
		totalItemsCompleted = Statistics.totalMenuItemsCompleted(context,
				Home.rootNode);
		int totalItems = Statistics.totalMenuItems(Home.rootNode);
		TextView award_stat_item_progress_text = (TextView) findViewById(R.id.award_stat_item_progress_text);
		award_stat_item_progress_text.setText(totalItemsCompleted + "/"
				+ totalItems);

		float decimalPercentage = (totalItemsCompleted / (float) totalItems);
		int percentage = Math.round(decimalPercentage * 100);
		if (percentage > 100) {
			percentage = 100;
		}

		// Sets progress bar layout width
		LinearLayout progress_bar = (LinearLayout) findViewById(R.id.main_progress_bar_progress);
		progress_bar.setLayoutParams(new LinearLayout.LayoutParams(0, 25,
				decimalPercentage));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Renders statistics in the bottom bar
	 */
	public void renderStatistics() {
		TextView bottom_nav_today = (TextView) findViewById(R.id.bottom_practiced_today);
		Integer timePracticedToday = DatabaseHelper.getInstance(
				getApplicationContext()).getTimeSpentWatchingToday();
		bottom_nav_today.setText(String.valueOf(TimeConversion
				.secondsToMinutesTwoDecimal(timePracticedToday)));

		Integer timePracticedLastWeek = DatabaseHelper.getInstance(
				getApplicationContext()).getTimeSpentWatchingPastWeek();
		renderProgressBar(TimeConversion
				.secondsToMinutesTwoDecimal(timePracticedLastWeek));
	}

	private void renderProgressBar(double timePracticedLastWeek) {
		TextView time_goal_weekly = (TextView) findViewById(R.id.bottom_progress_text_denom);
		long weekly_goal_minutes = sharedPreferences.getLong(
				"weekly_goal_minutes", 60);
		time_goal_weekly.setText(" of " + String.valueOf(weekly_goal_minutes)
				+ " min goal");

		float decimalPercentage = (float) (timePracticedLastWeek / (double) weekly_goal_minutes);
		float decimalPercentage100 = decimalPercentage * 100;
		int percentage = Math.round(decimalPercentage100);
		if (percentage > 100) {
			percentage = 100;
		}

		TextView progress_bar_text = (TextView) findViewById(R.id.bottom_progress_text_minutes);
		if (decimalPercentage100 == 0) {
			progress_bar_text.setText("0%");
		} else if (decimalPercentage100 <= 1) {
			progress_bar_text.setText(String.format("%.1f",
					decimalPercentage100) + "%");
		} else {
			progress_bar_text.setText(percentage + "%");
		}

		// Sets progress bar layout width
		LinearLayout progress_bar = (LinearLayout) findViewById(R.id.bottom_progress_bar_progress);
		progress_bar.setLayoutParams(new LinearLayout.LayoutParams(0, 25,
				decimalPercentage));
	}

	// =========================================

	private BaseAdapter mAdapter = new BaseAdapter() {

		@Override
		public int getCount() {
			return Awards.NUM_AWARDS;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View retval = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.awards_item, null);

			TextView awardTitle = (TextView) retval
					.findViewById(R.id.award_title);
			TextView awardDesc = (TextView) retval
					.findViewById(R.id.award_desc);
			ImageView awardImg = (ImageView) retval
					.findViewById(R.id.award_img);

			switch (position) {
			case Awards.FIRST_ITEM:
				awardTitle.setText("Rookie Trainee");
				awardDesc.setText("Earned when you complete your first item.");
				if (totalItemsCompleted >= 1) {
					awardImg.setImageResource(R.drawable.award_1st_video);
				}
				break;
			case Awards.CONSECUTIVE_DAYS_2:
				awardTitle.setText("Double Double");
				awardDesc
						.setText("Earned when you use this app two consecutive days.");
				if (sharedPreferences.getBoolean("two_consecutive_days", false)) {
					awardImg.setImageResource(R.drawable.award_2_days);
				}
				break;
			case Awards.CONSECUTIVE_DAYS_7:
				awardTitle.setText("Sky High");
				awardDesc
						.setText("Earned when you use this app seven consecutive days.");
				if (sharedPreferences.getBoolean("seven_consecutive_days",
						false)) {
					awardImg.setImageResource(R.drawable.award_7_days);
				}
				break;
			case Awards.POINTS_50:
				awardTitle.setText("Fifty-0");
				awardDesc.setText("Earned when you collect 50 points.");
				if (sharedPreferences.getBoolean("award_50", false)) {
					awardImg.setImageResource(R.drawable.award_50);
				}
				break;
			case Awards.POINTS_100:
				awardTitle.setText("Hundred Zen");
				awardDesc.setText("Earned when you collect 100 points.");
				if (sharedPreferences.getBoolean("award_100", false)) {
					awardImg.setImageResource(R.drawable.award_100);
				}
				break;
			case Awards.POINTS_500:
				awardTitle.setText("Halftime");
				awardDesc.setText("Earned when you collect 500 points.");
				if (sharedPreferences.getBoolean("award_500", false)) {
					awardImg.setImageResource(R.drawable.award_500);
				}
				break;
			case Awards.POINTS_1000:
				awardTitle.setText("Mille to Spare?");
				awardDesc.setText("Earned when you collect 1000 points.");
				if (sharedPreferences.getBoolean("award_1000", false)) {
					awardImg.setImageResource(R.drawable.award_1000);
				}
				break;
			case Awards.POINTS_10000:
				awardTitle.setText("10K Bonus");
				awardDesc.setText("Earned when you collect 10000 points.");
				if (sharedPreferences.getBoolean("award_10000", false)) {
					awardImg.setImageResource(R.drawable.award_10000);
				}
				break;
			}

			return retval;
		}

	};
}
