package com.kinetiqa.raindrops;

import android.app.Activity;
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
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinetiqa.raindrops.database.DatabaseHelper;
import com.kinetiqa.raindrops.dialogs.DialogManager;
import com.kinetiqa.raindrops.util.Fonts;
import com.kinetiqa.raindrops.util.TimeConversion;
import com.kinetiqa.raindrops.util.Toasted;

public class Store extends Activity {

	public final static int NUM_ITEMS = 2;

	public final static int BG_ORIG = 0;
	public final static int BG_SAILS = 1;

	private SharedPreferences sharedPreferences;
	/**
	 * LAYOUT STYLES
	 */
	private Typeface typeFaceHandwritten;
	private Typeface typeFaceAcme;
	
	private GridView items;
	private ImageButton backButton;
	private TextView pointsAvailableTextView;

	private long points;

	private DialogManager dialogManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.store);

		typeFaceHandwritten = Typeface.createFromAsset(getAssets(), Fonts.HANDWRITTEN);
		typeFaceAcme = Typeface.createFromAsset(getAssets(), Fonts.ACME);
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(Store.this);
		points = sharedPreferences.getLong("points", 0);

		dialogManager = new DialogManager(Store.this);
		
		pointsAvailableTextView = (TextView) findViewById(R.id.points_available);
		pointsAvailableTextView.setText(String.valueOf(points));

		items = (GridView) findViewById(R.id.store_items);
		items.setAdapter(mAdapter);
		items.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				int selectedStoreItemPrice = 0;

				switch (position) {
				case Store.BG_ORIG:
					selectedStoreItemPrice = 0;
				case Store.BG_SAILS:
					selectedStoreItemPrice = 1200;
					break;
				}

				switch (position) {
				case Store.BG_ORIG:
					if (sharedPreferences.getBoolean("bg_orig", true)) {
						// Already owned
						Toasted.showToast("Background Selected");
					} else {
						if (selectedStoreItemPrice > points) {
							// not enough points to buy item
							DialogManager dm = new DialogManager(Store.this);
							dm.openMessage("Uh oh...",
									"You don't have enough points to buy this item. Keep practicing!");
							break;
						}
						points -= selectedStoreItemPrice;
						sharedPreferences.edit().putLong("points", points)
								.commit();
						sharedPreferences.edit().putBoolean("bg_orig", true)
								.commit();
					}
					sharedPreferences.edit()
							.putLong("bg_selected", Store.BG_ORIG).commit();
					mAdapter.notifyDataSetChanged();
					break;
				case Store.BG_SAILS:
					if (sharedPreferences.getBoolean("bg_sails", true)) {
						// Already owned
						Toasted.showToast("Background Selected");
					} else {
						if (selectedStoreItemPrice > points) {
							// not enough points to buy item
							DialogManager dm = new DialogManager(Store.this);
							dm.openMessage("Uh oh...",
									"You don't have enough points to buy this item. Keep practicing!");
							break;
						}
						points -= selectedStoreItemPrice;
						sharedPreferences.edit().putLong("points", points)
								.commit();
						sharedPreferences.edit().putBoolean("bg_sails", true)
								.commit();
					}
					sharedPreferences.edit()
							.putLong("bg_selected", Store.BG_SAILS).commit();
					mAdapter.notifyDataSetChanged();
					break;
				}

				mAdapter.notifyDataSetChanged();
				initializeBackground();
			}
		});
		
		initializeButtonEvents();

	}

	@Override
	public void onResume() {
		super.onResume();
		initializeBackground();
		renderStatistics();
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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
				dialogManager.showDialogSetGoals(Store.this);
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
				Intent i = new Intent(getApplicationContext(), Awards.class);
				startActivity(i);
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
	 * Renders statistics in the bottom bar
	 */
	public void renderStatistics() {
		TextView bottom_nav_today = (TextView) findViewById(R.id.bottom_practiced_today);
		Integer timePracticedToday = DatabaseHelper.getInstance(
				getApplicationContext()).getTimeSpentWatchingToday();
		bottom_nav_today.setText(String.valueOf(TimeConversion
				.secondsToMinutesTwoDecimal(timePracticedToday)));

		TextView bottom_bar_total_points = (TextView) findViewById(R.id.points_available);
		bottom_bar_total_points.setTypeface(typeFaceAcme);
		bottom_bar_total_points.setText(String.valueOf(sharedPreferences
				.getLong("points", 0)));

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
			return Store.NUM_ITEMS;
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
					R.layout.store_item, null);

			ImageView imagePreview = (ImageView) retval
					.findViewById(R.id.store_item_preview);
			RelativeLayout dimImagePreview = (RelativeLayout) retval
					.findViewById(R.id.store_item_preview_dim);
			ImageView ownedImage = (ImageView) retval
					.findViewById(R.id.store_item_in_use);
			TextView type = (TextView) retval
					.findViewById(R.id.store_item_type);
			type.setTypeface(typeFaceHandwritten);
			TextView storePrice = (TextView) retval
					.findViewById(R.id.store_item_price);
			storePrice.setTypeface(typeFaceHandwritten);

			int selectedBackground = (int) sharedPreferences.getLong(
					"bg_selected", Store.BG_ORIG);

			switch (position) {
			case Store.BG_ORIG:
				imagePreview.setImageResource(R.drawable.bg_mountains);
				if (!sharedPreferences.getBoolean("bg_orig", true)) {
					dimImagePreview.setVisibility(View.VISIBLE);
					storePrice.setText("0 points");
				} else {
					storePrice.setText("Owned");
				}
				if (selectedBackground == position) {
					ownedImage.setVisibility(View.VISIBLE);
				}
				break;
			case Store.BG_SAILS:
				imagePreview.setImageResource(R.drawable.bg_sails);
				if (!sharedPreferences.getBoolean("bg_sails", false)) {
					dimImagePreview.setVisibility(View.VISIBLE);
					storePrice.setText("1200 points");
				} else {
					storePrice.setText("Owned");
				}
				if (selectedBackground == position) {
					ownedImage.setVisibility(View.VISIBLE);
				}
				break;
			}

			return retval;
		}

	};
}
