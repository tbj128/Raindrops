package com.kinetiqa.raindrops;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

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

import com.kinetiqa.raindrops.components.Bookmark;
import com.kinetiqa.raindrops.database.DatabaseHelper;
import com.kinetiqa.raindrops.dialogs.DialogDocument;
import com.kinetiqa.raindrops.dialogs.DialogManager;
import com.kinetiqa.raindrops.dialogs.DialogVideo;
import com.kinetiqa.raindrops.menu.MenuComponent;
import com.kinetiqa.raindrops.menu.MenuComposite;
import com.kinetiqa.raindrops.menu.MenuLeaf;
import com.kinetiqa.raindrops.menu.MenuRegistry;
import com.kinetiqa.raindrops.util.Fonts;
import com.kinetiqa.raindrops.util.Statistics;
import com.kinetiqa.raindrops.util.TimeConversion;
import com.kinetiqa.raindrops.util.Toasted;

/**
 * Handles interaction with submenu items
 * 
 * @author: Tom Jin
 */
public class Submenu extends Activity {

	/**
	 * STATIC COMPONENTS
	 */
	public static final Integer SUBMENU = 0;
	public static final Integer BOOKMARKS = 1;

	/**
	 * SUBMENU PAGE MANAGEMENT
	 */
	private SharedPreferences sharedPreferences;
	private Integer itemType = SUBMENU;
	private Stack<MenuComponent> currNode;
	private List<MenuComponent> currMenuItems;
	private MenuComposite currMenu;

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
	private GridView gridMenu;
	private ImageButton storeButton;
	private TextView setGoalsButton;
	private ImageButton openMessagesButton;
	private ImageButton openAwardsButton;
	private ImageButton openBookmarksButton;
	private ImageButton backButton;
	private RelativeLayout messageNotificationWrapper;
	private TextView messageNotificationNumber;
	private TextView pointsEarnedAlertTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		typeFaceHandwritten = Typeface.createFromAsset(getAssets(),
				Fonts.HANDWRITTEN);
		typeFaceAcme = Typeface.createFromAsset(getAssets(), Fonts.ACME);

		dialogManager = new DialogManager(Submenu.this);
		dialogVideo = new DialogVideo(Submenu.this);
		dialogDocument = new DialogDocument(Submenu.this);

		initialize();
		initializeAndroidComponents();
		initializeButtonEvents();
		initializeGridView();
		buildBreadcrumb();
	}

	@Override
	public void onResume() {
		super.onResume();

		dialogVideo.activityResumed();
		dialogDocument.activityResumed();

		initializeBackground();
		renderStatistics();
		renderNotifications();

		if (itemType != BOOKMARKS) {
			// Check if all items under this menu have been completed
			boolean menuCompleted = true;
			for (MenuComponent component : currMenu.getMenuItems()) {
				boolean componentCompleted = DatabaseHelper.getInstance(
						getApplicationContext()).isCompleted(component.getID());
				if (!componentCompleted) {
					menuCompleted = false;
				}
			}
			if (menuCompleted) {
				// All submenu items have been completed; Set the parent menu as
				// completed
				DatabaseHelper.getInstance(getApplicationContext())
						.setCompleted(currMenu.getID());
			}
		}

		mAdapter.notifyDataSetChanged();
	}

	@Override
	// Activity loses focus - manage pause time if in video
	public void onPause() {
		super.onPause();

		dialogVideo.activityPaused();
		dialogDocument.activityPaused();
	}

	private void initialize() {
		boolean isBookmarks = false;

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			isBookmarks = extras.getBoolean("bookmarks_view");
		}

		if (isBookmarks) {
			itemType = BOOKMARKS;
			currMenuItems = new LinkedList<MenuComponent>();
			List<Bookmark> bookmarks = DatabaseHelper.getInstance(
					getApplicationContext()).getAllBookmarks();
			for (Bookmark bookmark : bookmarks) {
				MenuComponent m = MenuRegistry.findNode(bookmark.getItemId(),
						Home.rootNode);
				if (m != null) {
					currMenuItems.add(m);
				}
			}
			if (currMenuItems.size() == 0) {
				TextView bookmarksWarning = (TextView) findViewById(R.id.no_bookmarks_warning);
				bookmarksWarning.setVisibility(View.VISIBLE);
			}
		} else {
			// This is a submenu...
			itemType = SUBMENU;
			// But the question is, does the app still know about it?
			currNode = MenuRegistry.currNode;
			if (currNode.isEmpty()) {
				finish();
				return;
			}
			currMenu = (MenuComposite) currNode.peek();
			currMenuItems = currMenu.getMenuItems();
		}
	}

	private void initializeAndroidComponents() {
		// Core Components
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(Submenu.this);
		dialogManager = new DialogManager(Submenu.this);

		// Visual Components
		mainContainer = (RelativeLayout) findViewById(R.id.main_container);
		pointsEarnedAlertTextView = (TextView) findViewById(R.id.points_earned_alert);
	}

	/**
	 * Builds breadcrumb path to root
	 */
	private void buildBreadcrumb() {
		TextView subMenuPath = (TextView) findViewById(R.id.main_submenu_breadcrumb);

		if (itemType == BOOKMARKS) {
			subMenuPath.setVisibility(View.VISIBLE);
			subMenuPath.setText("Bookmarks");
			return;
		}

		StringBuilder pathFromRoot = new StringBuilder();
		for (MenuComponent node : currNode) {
			pathFromRoot.append(node.getName());
			pathFromRoot.append(" / ");
		}

		subMenuPath.setVisibility(View.VISIBLE);
		subMenuPath.setText(pathFromRoot.toString());
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

	/**
	 * Sets up button events
	 */
	private void initializeButtonEvents() {
		// ---------- Button Events ----------------------------------

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
				dialogManager.showDialogSetGoals(Submenu.this);
			}
		});

		openBookmarksButton = (ImageButton) findViewById(R.id.bottom_nav_bookmarks);
		openBookmarksButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (itemType != BOOKMARKS) {
					Intent i = new Intent(getApplicationContext(),
							Submenu.class);
					i.putExtra("bookmarks_view", true);
					startActivity(i);
				}
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
				Intent i = new Intent(getApplicationContext(), Awards.class);
				startActivity(i);
			}
		});

		backButton = (ImageButton) findViewById(R.id.nav_back_button);
		backButton.setVisibility(View.VISIBLE);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Finished with the current menu element - modify stack to
				// reflect this
				finish();
				if (itemType != BOOKMARKS) {
					MenuRegistry.currNode.pop();
				}
			}
		});
	}

	private void initializeGridView() {
		// Populates with the contents of this menu
		gridMenu = (GridView) findViewById(R.id.main_submenu);
		gridMenu.setAdapter(mAdapter);
		gridMenu.setVisibility(View.VISIBLE);
		gridMenu.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				Intent i = new Intent(getApplicationContext(), Submenu.class);

				MenuComponent itemClicked = currMenuItems.get(position);
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
					if (!itemClicked.getRequires().equals("")) {
						// Menu/Video/Activity has a restriction - must go check

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
							// Required item has been completed

							if (itemClicked.getMediaType() == MenuRegistry.MEDIA_MENU) {
								// Open another sub menu
								MenuRegistry.currNode.add(currMenuItems
										.get(position));
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
						// Menu/Video/Activity has no inherit restriction -
						// directly
						// open item

						if (itemClicked.getMediaType() == MenuRegistry.MEDIA_MENU) {
							// Open another sub menu
							MenuRegistry.currNode.add(currMenuItems
									.get(position));
							startActivity(i);
						} else {
							launchItem(position);
						}

					}
				}

			}
		});

	}

	public void launchItem(int position) {
		switch (currMenuItems.get(position).getMediaType()) {
		case MenuRegistry.MEDIA_VIDEO:
			dialogVideo.open(currMenu, (MenuLeaf) currMenuItems.get(position),
					mainContainer, pointsEarnedAlertTextView);
			break;
		case MenuRegistry.MEDIA_RICH_TEXT:
			dialogDocument.open(currMenu,
					(MenuLeaf) currMenuItems.get(position), mainContainer,
					pointsEarnedAlertTextView);
			break;
		default:
			Toasted.showToast("Unknown Media Type");
		}
	}

	public void refreshGridView() {

		gridMenu.setAdapter(mAdapter);
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

	@Override
	public void onBackPressed() {
		// Finished with the current menu element - modify stack to reflect this
		finish();
		if (itemType != BOOKMARKS) {
			MenuRegistry.currNode.pop();
		}
	}

	// ------- Submenu Custom Grid Layout ----------------

	private BaseAdapter mAdapter = new BaseAdapter() {

		@Override
		public int getCount() {
			return currMenuItems.size();
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
					R.layout.main_menu_subitem, null);

			TextView title = (TextView) retval.findViewById(R.id.submenu_title);
			title.setText(currMenuItems.get(position).getName());

			ImageView typeIcon = (ImageView) retval
					.findViewById(R.id.submenu_type);
			TextView type = (TextView) retval
					.findViewById(R.id.submenu_type_text);
			RelativeLayout menuItem = (RelativeLayout) retval
					.findViewById(R.id.submenu_item);

			switch (currMenuItems.get(position).getMediaType()) {
			case MenuRegistry.MEDIA_MENU:
				type.setText("Folder");
				menuItem.setBackgroundResource(R.drawable.item_menu_bg);
				typeIcon.setBackgroundResource(R.drawable.light_folder);
				break;
			case MenuRegistry.MEDIA_VIDEO:
				type.setText("Video");
				menuItem.setBackgroundResource(R.drawable.item_bg);
				typeIcon.setBackgroundResource(R.drawable.light_video);
				break;
			case MenuRegistry.MEDIA_RICH_TEXT:
				type.setText("Document");
				menuItem.setBackgroundResource(R.drawable.item_bg);
				typeIcon.setBackgroundResource(R.drawable.light_list);
				break;
			default:
				type.setText("Other");
				menuItem.setBackgroundResource(R.drawable.item_menu_bg);
				typeIcon.setBackgroundResource(R.drawable.light_folder);
			}

			boolean hasCompletedItem = DatabaseHelper.getInstance(
					getApplicationContext()).isCompleted(
					currMenuItems.get(position).getID());
			if (hasCompletedItem) {
				ImageView imgCheck = (ImageView) retval
						.findViewById(R.id.submenu_completed_img);
				imgCheck.setVisibility(View.VISIBLE);
			}

			ImageView imgAlt = (ImageView) retval
					.findViewById(R.id.submenu_alt_img);

			boolean isBookmarked = DatabaseHelper.getInstance(
					getApplicationContext()).isBookmarked(
					currMenuItems.get(position).getID());
			if (isBookmarked) {
				imgAlt.setImageResource(R.drawable.blue_saved);
				imgAlt.setVisibility(View.VISIBLE);
			}

			boolean isPermitted = DatabaseHelper.getInstance(
					getApplicationContext()).isPermitted(
					currMenuItems.get(position).getID());
			if (!isPermitted) {
				imgAlt.setImageResource(R.drawable.yellow_lock);
				imgAlt.setVisibility(View.VISIBLE);
			}

			Integer timeWatchingSeconds = Statistics.amountOfTimeWatched(
					getApplicationContext(), currMenuItems.get(position));
			TextView achievement_minutes = (TextView) retval
					.findViewById(R.id.submenu_item_time);
			achievement_minutes.setText(TimeConversion
					.secondsToTime(timeWatchingSeconds));

			return retval;
		}

	};
}
