package com.kinetiqa.raindrops;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinetiqa.raindrops.components.HorizontalTiles;
import com.kinetiqa.raindrops.connection.ConnectionManager;
import com.kinetiqa.raindrops.database.DatabaseHelper;
import com.kinetiqa.raindrops.dialogs.DialogDocument;
import com.kinetiqa.raindrops.dialogs.DialogManager;
import com.kinetiqa.raindrops.dialogs.DialogVideo;
import com.kinetiqa.raindrops.menu.MenuComponent;
import com.kinetiqa.raindrops.menu.MenuComposite;
import com.kinetiqa.raindrops.menu.MenuLeaf;
import com.kinetiqa.raindrops.menu.MenuRegistry;
import com.kinetiqa.raindrops.receiver.OnAlarmReceive;
import com.kinetiqa.raindrops.util.Fonts;
import com.kinetiqa.raindrops.util.Statistics;
import com.kinetiqa.raindrops.util.TimeConversion;
import com.kinetiqa.raindrops.util.Toasted;

/**
 * Main Activity Class Called from Initialize.java
 * 
 * @author: Tom Jin
 * @date: May 8, 2013
 * @revised: Feb 4, 2014
 */
public class Home extends Activity {

	/**
	 * STATIC COMPONENTS
	 */
	public static Context context;
	public static Activity currActivity;

	/**
	 * HOME PAGE MANAGEMENT
	 */
	private SharedPreferences sharedPreferences;
	public static MenuComposite rootNode;
	private List<MenuComponent> rootMenu;
	
	/**
	 * LAYOUT STYLES
	 */
	private Typeface typeFaceHandwritten;
	private Typeface typeFaceAcme;

	/**
	 * LAYOUT COMPONENTS
	 */
	private DialogManager dialogManager;
	private DialogVideo dialogVideo;
	private DialogDocument dialogDocument;

	private RelativeLayout mainContainer;
	private HorizontalTiles horizontalMenu;
	private ImageButton settingsButton;
	private ImageButton storeButton;
	// private ImageButton forwardButton;
	// private ImageButton backButton;
	private TextView setGoalsButton;
	private ImageButton openMessagesButton;
	private ImageButton openAwardsButton;
	private ImageButton openBookmarksButton;
	private RelativeLayout messageNotificationWrapper;
	private TextView messageNotificationNumber;
	private TextView pointsEarnedAlertTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Home.currActivity = Home.this;
		setContentView(R.layout.home);
		typeFaceHandwritten = Typeface.createFromAsset(getAssets(), Fonts.HANDWRITTEN);
		typeFaceAcme = Typeface.createFromAsset(getAssets(), Fonts.ACME);
		mainContainer = (RelativeLayout) findViewById(R.id.main_container);

		// ------ Initialization of Key Fields ---------------------
		Home.context = getApplicationContext();
		dialogManager = new DialogManager(Home.this);
		dialogVideo = new DialogVideo(Home.this);
		dialogDocument = new DialogDocument(Home.this);
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(Home.this);
		pointsEarnedAlertTextView = (TextView) findViewById(R.id.points_earned_alert);

		// ---------- Initializes Recurring Background Updates ----------------
		setRecurringAlarm();

		// ------ Home Initialization -----
		initializeMenuStructure();
		initializeButtonEvents();
		contactServer();

		// Renders horizontal menu with components
		horizontalMenu.setAdapter(mAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();

		dialogVideo.activityResumed();
		dialogDocument.activityResumed();

		initializeBackground();
		renderNotifications();

		// Clears the activity stack because returned to home
		MenuRegistry.currNode.clear();

		// Renders sidebar (ie. num minutes watched, messages, etc.)
		renderStatistics();
		checkDailyAwards();

		// Check if all items under this menu have been completed
		boolean menuCompleted = true;
		for (MenuComponent component : rootNode.getMenuItems()) {
			boolean componentCompleted = DatabaseHelper.getInstance(
					getApplicationContext()).isCompleted(component.getID());
			if (!componentCompleted) {
				menuCompleted = false;
			}
		}
		if (menuCompleted) {
			// All submenu items have been completed; Set the parent menu as
			// completed
			DatabaseHelper.getInstance(getApplicationContext()).setCompleted(
					rootNode.getID());
		}

		// Refresh the tiles
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();

		dialogVideo.activityPaused();
		dialogDocument.activityPaused();
	}

	private void contactServer() {
		String username = sharedPreferences.getString("username", null);
		if (username == null) {
			// Never logged in
			dialogManager.login();
		} else {
			ConnectionManager cm = new ConnectionManager(Home.this);
			cm.sync();
		}
	}

	private void initializeBackground() {
		TextView logo_l = (TextView) findViewById(R.id.bottom_nav_logo_l);
		logo_l.setTypeface(typeFaceHandwritten);
		TextView logo_r = (TextView) findViewById(R.id.bottom_nav_logo_r);
		logo_r.setTypeface(typeFaceHandwritten);
		
		int selectedBackground = (int) sharedPreferences.getLong("bg_selected",
				Store.BG_ORIG);
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

	private void initializeMenuStructure() {
		// ------ Reads and creates menu structure ------------------
		MenuRegistry reg = MenuRegistry.getDefault(Home.this);
		rootNode = (MenuComposite) reg.getRootNode();
		rootMenu = rootNode.getMenuItems();

		horizontalMenu = (HorizontalTiles) findViewById(R.id.main_menu);
		horizontalMenu.setVisibility(View.VISIBLE);
		horizontalMenu.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				// Sound effect or something
				return false;
			}

		});
		horizontalMenu.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				MenuComponent itemClicked = rootMenu.get(position);
				boolean itemPermitted = DatabaseHelper.getInstance(
						getApplicationContext()).isPermitted(
						itemClicked.getID());

				if (!itemPermitted) {
					// Item is locked by trainer - No access
					dialogManager
							.openMessage(
									"Locked",
									"The item you are attempting to view has been locked by a trainer. Contact your trainer to learn more.");

				} else {
					if (itemClicked.getRequires() != null
							&& !itemClicked.getRequires().equals("")) {
						// Menu item clicked is valid and DOES have restrictions

						boolean itemsNotCompleted = false;
						String itemsRequiredStr = itemClicked.getRequires();
						String[] itemsRequired = itemsRequiredStr.split(",");
						List<String> itemsHaventCompleted = new LinkedList<String>();
						for (int s = 0; s < itemsRequired.length; s++) {
							boolean isComplete = DatabaseHelper.getInstance(
									getApplicationContext()).isCompleted(
									itemsRequired[s]);
							if (!isComplete) {
								itemsHaventCompleted.add(itemsRequired[s]);
								itemsNotCompleted = true;
							}
						}

						if (!itemsNotCompleted) {
							// Required item has been completed; Allow access

							if (itemClicked.getMediaType() == MenuRegistry.MEDIA_MENU) {
								// Keep track of new menu position in stack
								MenuRegistry.currNode.add(rootMenu
										.get(position));
								Intent i = new Intent(getApplicationContext(),
										Submenu.class);
								startActivity(i);

							} else {
								launchItem(position);
							}
						} else {
							// Required items have NOT been completed
							StringBuilder itemsToCompleteStr = new StringBuilder();
							for (int x = 0; x < itemsHaventCompleted.size(); x++) {
								itemsToCompleteStr.append(DatabaseHelper
										.getInstance(getApplicationContext())
										.itemNameLookup(
												itemsHaventCompleted.get(x)));
								if (x != (itemsHaventCompleted.size() - 1)) {
									itemsToCompleteStr.append(", ");
								}
							}
							dialogManager.openMessage("Warning!",
									"The following items must be completed first: "
											+ itemsToCompleteStr.toString());
						}
					} else {
						// No restrictions on menu item

						if (itemClicked.getMediaType() == MenuRegistry.MEDIA_MENU) {
							// Keep track of new menu position in stack
							MenuRegistry.currNode.add(rootMenu.get(position));
							Intent i = new Intent(getApplicationContext(),
									Submenu.class);
							startActivity(i);

						} else {
							launchItem(position);
						}
					}
				}
			}
		});
	}

	/**
	 * Sets up button events
	 */
	private void initializeButtonEvents() {
		// ---------- Button Events ----------------------------------
		settingsButton = (ImageButton) findViewById(R.id.nav_settings_button);
		settingsButton.setVisibility(View.VISIBLE);
		settingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), Settings.class);
				startActivity(i);
			}
		});

		storeButton = (ImageButton) findViewById(R.id.nav_store_button);
		storeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), Store.class);
				startActivity(i);
			}
		});

		setGoalsButton = (TextView) findViewById(R.id.bottom_progress_text_denom);
		setGoalsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialogManager.showDialogSetGoals(Home.this);
			}
		});

		// forwardButton = (ImageButton)
		// findViewById(R.id.horizontal_nav_forward);
		// forwardButton.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// // Manually scrolls horizontal menu to the right
		// horizontalMenu.scrollTo(horizontalMenu.mCurrentX + 280);
		// }
		// });
		//
		// backButton = (ImageButton) findViewById(R.id.horizontal_nav_back);
		// backButton.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// // Manually scrolls horizontal menu to the left
		// horizontalMenu.scrollTo(horizontalMenu.mCurrentX - 280);
		// }
		// });

		openBookmarksButton = (ImageButton) findViewById(R.id.bottom_nav_bookmarks);
		openBookmarksButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), Submenu.class);
				i.putExtra("bookmarks_view", true);
				startActivity(i);
			}
		});

		openMessagesButton = (ImageButton) findViewById(R.id.nav_mail_button);
		openMessagesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), Messages.class);
				startActivity(i);
			}
		});

		openAwardsButton = (ImageButton) findViewById(R.id.bottom_nav_awards);
		openAwardsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Home.this, Awards.class);
				startActivity(i);
			}
		});

	}

	/**
	 * Checks if a daily award has been achieved
	 */
	public void checkDailyAwards() {
		// Get current date (in milliseconds)
		Calendar calendar = Calendar.getInstance();
		long currTime = calendar.getTimeInMillis();
		Date currDate = TimeConversion
				.convertDateTimetoDate(new Date(currTime));
		long currDateMilli = currDate.getTime();

		// Get first and last consecutive usage date
		long firstConsecutiveDay = sharedPreferences.getLong(
				"first-consecutive-day", -1);
		long lastConsecutiveDay = sharedPreferences.getLong(
				"last-consecutive-day", -1);

		if (firstConsecutiveDay != -1 && lastConsecutiveDay != -1) {
			if (lastConsecutiveDay != currDateMilli) {

				if ((lastConsecutiveDay + 86400000) == currDateMilli) {
					if (!sharedPreferences.getBoolean("two_consecutive_days",
							false)) {
						// Consecutive day award achieved!
						sharedPreferences.edit()
								.putBoolean("two_consecutive_days", true)
								.commit();
						dialogManager
								.openMessage(
										"Welcome Back",
										"You've achieved a consecutive day award! Click on the awards tab to see all your awards.");
					}

					sharedPreferences.edit()
							.putLong("last-consecutive-day", currDateMilli)
							.commit();

					if ((firstConsecutiveDay + 7 * 86400000) == currDateMilli) {
						// 7 days have passed
						if (!sharedPreferences.getBoolean(
								"seven_consecutive_days", false)) {
							sharedPreferences.edit()
									.putBoolean("seven_consecutive_days", true)
									.commit();
							dialogManager
									.openMessage(
											"Week Long",
											"You've achieved the 7 day milestone award! Click on the awards tab to see all your awards.");
						}
					}
				} else {
					// Not a consecutive day!
					sharedPreferences.edit()
							.putLong("last-consecutive-day", currDateMilli)
							.commit();
					sharedPreferences.edit()
							.putLong("first-consecutive-day", currDateMilli)
							.commit();
				}
			}

		}

		// Is this the first time recording a date?
		if (firstConsecutiveDay == -1) {
			sharedPreferences.edit()
					.putLong("first-consecutive-day", currDateMilli).commit();
		}
		if (lastConsecutiveDay == -1) {
			sharedPreferences.edit()
					.putLong("last-consecutive-day", currDateMilli).commit();
		}
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

	private void renderNotifications() {

		messageNotificationWrapper = (RelativeLayout) findViewById(R.id.nav_mail_notification_wrapper);
		messageNotificationNumber = (TextView) findViewById(R.id.nav_mail_notification_number);

		int numUnreadMessages = DatabaseHelper.getInstance(
				getApplicationContext()).getNumUnreadMessages();
		if (numUnreadMessages <= 0) {
			messageNotificationWrapper.setVisibility(View.GONE);
		} else {
			messageNotificationWrapper.setVisibility(View.VISIBLE);
			messageNotificationNumber
					.setText(String.valueOf(numUnreadMessages));
		}

	}

	public void launchItem(int position) {
		switch (rootMenu.get(position).getMediaType()) {
		case MenuRegistry.MEDIA_VIDEO:
			dialogVideo.open(rootNode, (MenuLeaf) rootMenu.get(position),
					mainContainer, pointsEarnedAlertTextView);
			break;
		case MenuRegistry.MEDIA_RICH_TEXT:
			dialogDocument.open(rootNode, (MenuLeaf) rootMenu.get(position),
					mainContainer, pointsEarnedAlertTextView);
			break;
		default:
			Toasted.showToast("Unknown Media Type");
		}
	}

	// // ---------- Horizontal Menu Bridge/Interaction Methods
	// ------------------
	//
	// public static void hideLeftNav() {
	// Home.currActivity.findViewById(R.id.horizontal_nav_back).setVisibility(
	// View.GONE);
	// }
	//
	// public static void hideRightNav() {
	// Home.currActivity.findViewById(R.id.horizontal_nav_forward)
	// .setVisibility(View.GONE);
	// }
	//
	// public static void showLeftNav() {
	// Home.currActivity.findViewById(R.id.horizontal_nav_back).setVisibility(
	// View.VISIBLE);
	// }
	//
	// public static void showRightNav() {
	// Home.currActivity.findViewById(R.id.horizontal_nav_forward)
	// .setVisibility(View.VISIBLE);
	// }

	// --------------- General Helper Methods ----------------

	/**
	 * Returns the EpicWheelsMain context
	 * 
	 * @return
	 */
	public static Context getAppContext() {
		return Home.context;
	}

	/**
	 * Sets a service to sync with server every hour
	 * 
	 * @param context
	 *            is the containing instance of the class that's calling this
	 *            method
	 */
	private void setRecurringAlarm() {

		Calendar updateTime = Calendar.getInstance();
		updateTime.set(Calendar.MINUTE, 0);

		Intent downloader = new Intent(Home.this, OnAlarmReceive.class);
		PendingIntent recurringDownload = PendingIntent.getBroadcast(context,
				0, downloader, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				updateTime.getTimeInMillis() + 5 * 60 * 1000, 1000 * 60 * 30,
				recurringDownload);
	}

	// ============ Horizontal Menu Class Definitions =============

	private BaseAdapter mAdapter = new BaseAdapter() {

		@Override
		public int getCount() {
			return rootMenu.size();
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
					R.layout.main_menu_item, null);

			TextView title = (TextView) retval.findViewById(R.id.menu_title);
			title.setText(rootMenu.get(position).getName());

			ImageView typeIcon = (ImageView) retval
					.findViewById(R.id.menu_type_icon);

			RelativeLayout menuItem = (RelativeLayout) retval
					.findViewById(R.id.menu_item);

			switch (rootMenu.get(position).getMediaType()) {
			case MenuRegistry.MEDIA_MENU:
				menuItem.setBackgroundResource(R.drawable.item_menu_bg);
				typeIcon.setBackgroundResource(R.drawable.light_folder);
				break;
			case MenuRegistry.MEDIA_VIDEO:
				menuItem.setBackgroundResource(R.drawable.item_bg);
				typeIcon.setBackgroundResource(R.drawable.light_video);
				break;
			case MenuRegistry.MEDIA_RICH_TEXT:
				menuItem.setBackgroundResource(R.drawable.item_bg);
				typeIcon.setBackgroundResource(R.drawable.light_list);
				break;
			default:
				menuItem.setBackgroundResource(R.drawable.item_menu_bg);
				typeIcon.setBackgroundResource(R.drawable.light_folder);
			}

			boolean hasCompletedItem = DatabaseHelper.getInstance(
					getApplicationContext()).isCompleted(
					rootMenu.get(position).getID());
			if (hasCompletedItem) {
				ImageView imgCheck = (ImageView) retval
						.findViewById(R.id.completed_check_img);
				imgCheck.setVisibility(View.VISIBLE);
			}

			ImageView imgAlt = (ImageView) retval
					.findViewById(R.id.home_alt_img);

			boolean isBookmarked = DatabaseHelper.getInstance(
					getApplicationContext()).isBookmarked(
					rootMenu.get(position).getID());
			if (isBookmarked) {
				imgAlt.setImageResource(R.drawable.blue_saved);
				imgAlt.setVisibility(View.VISIBLE);
			}

			boolean isPermitted = DatabaseHelper.getInstance(
					getApplicationContext()).isPermitted(
					rootMenu.get(position).getID());
			if (!isPermitted) {
				imgAlt.setImageResource(R.drawable.yellow_lock);
				imgAlt.setVisibility(View.VISIBLE);
			}

			Integer timeWatchingSeconds = Statistics.amountOfTimeWatched(
					getApplicationContext(), rootMenu.get(position));
			TextView achievement_minutes = (TextView) retval
					.findViewById(R.id.main_menu_item_time);
			achievement_minutes.setText(TimeConversion
					.secondsToTime(timeWatchingSeconds));

			Integer itemsCompleted = Statistics.totalMenuItemsCompleted(
					getApplicationContext(), rootMenu.get(position));
			Integer totalItems = Statistics.totalMenuItems(rootMenu
					.get(position));
			TextView pointsTextView = (TextView) retval
					.findViewById(R.id.main_menu_item_completed);
			pointsTextView.setText(itemsCompleted + "/" + totalItems);

			return retval;
		}

	};
}
