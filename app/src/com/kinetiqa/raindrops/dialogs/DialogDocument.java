package com.kinetiqa.raindrops.dialogs;

import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinetiqa.raindrops.Home;
import com.kinetiqa.raindrops.R;
import com.kinetiqa.raindrops.Submenu;
import com.kinetiqa.raindrops.anim.AnimationManager;
import com.kinetiqa.raindrops.components.PointsManager;
import com.kinetiqa.raindrops.components.Stopwatch;
import com.kinetiqa.raindrops.database.DatabaseHelper;
import com.kinetiqa.raindrops.menu.MenuComponent;
import com.kinetiqa.raindrops.menu.MenuComposite;
import com.kinetiqa.raindrops.menu.MenuLeaf;
import com.kinetiqa.raindrops.util.TimeConversion;
import com.kinetiqa.raindrops.util.Toasted;

public class DialogDocument {

	/**
	 * DIALOG MANAGEMENT
	 */
	private Context context;
	private AnimationManager animationManager;
	private Dialog dialog;
	private PointsManager pointsManager;

	private MenuLeaf itemClicked;
	private boolean isBookmarked = false;
	private Date startVideoTime;
	private Date endVideoTime;
	private Date pauseVideoTime;
	private Date resumeVideoTime;
	private long durationOfPauseInVideo = 0;
	private long durationOfPauseInTimedActivity = 0;
	private Integer timedActivity = 0;

	private boolean inVideoFlag = false;
	private boolean inTimedActivityFlag = false;

	/**
	 * LAYOUT COMPONENTS
	 */
	private ImageButton startButton;
	private TextView startButtonDesc;
	private ImageButton aboutButton;
	private ImageButton bookmarkButton;
	private TextView bookmarkButtonDesc;

	// ---------- Stop Watch Fields ---------------
	private Dialog dialogTimer;
	private Handler mHandler = new Handler();
	private long startTime;
	private long elapsedTime;
	private final int REFRESH_RATE = 100;
	private String hours, minutes, seconds;
	private long secondsRaw, minRaw, hoursRaw;
	private TextView msgTimer;

	public DialogDocument(Context context) {
		this.context = context;
		animationManager = new AnimationManager(context);
		pointsManager = new PointsManager(context);

		pauseVideoTime = new Date(System.currentTimeMillis());
		resumeVideoTime = new Date(System.currentTimeMillis());
	}

	public void activityResumed() {
		if (!inVideoFlag) {
			// Dialog wasn't actually open
			return;
		}

		resumeVideoTime = new Date(System.currentTimeMillis());
		long timeVideoPaused = resumeVideoTime.getTime()
				- pauseVideoTime.getTime();
		timeVideoPaused = timeVideoPaused / 1000;
		durationOfPauseInVideo += timeVideoPaused;
		if (inTimedActivityFlag) {
			durationOfPauseInTimedActivity += timeVideoPaused;
			mHandler.postDelayed(startTimer, 0);
		}
	}

	public void activityPaused() {
		if (!inVideoFlag) {
			// Dialog wasn't actually open
			return;
		}

		pauseVideoTime = new Date(System.currentTimeMillis());
		if (inTimedActivityFlag) {
			mHandler.removeCallbacks(startTimer);
		}
	}

	/**
	 * Opens documents in its own dialog
	 * 
	 * @param position
	 * @requires: currMenu, gridMenu is not null
	 */
	@SuppressLint("SetJavaScriptEnabled")
	public void open(MenuComposite parentNode, MenuLeaf item,
			final RelativeLayout mainContainer,
			final TextView pointsEarnedAlertTextView) {
		dialog = new Dialog(context, android.R.style.Theme_Translucent);
		dialog.getWindow().setWindowAnimations(R.style.DialogFadeAnimation);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(true);
		dialog.setContentView(R.layout.rich_text_viewer);

		WebView webview = (WebView) ((Dialog) dialog)
				.findViewById(R.id.rt_viewer);
		webview.getSettings().setBuiltInZoomControls(false);
		webview.getSettings().setSupportZoom(false);
		webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webview.getSettings().setAllowFileAccess(true);
		webview.getSettings().setDomStorageEnabled(true);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebChromeClient(new WebChromeClient() {
		});

		itemClicked = item;
		inVideoFlag = true;

		String pathToDocument = "file://"
				+ Environment.getExternalStorageDirectory()
				+ "/raindrops/content/media/" + itemClicked.getPath();

		webview.loadUrl(pathToDocument);

		// Records item start time
		startVideoTime = new Date(System.currentTimeMillis());
		// Set in item flag
		inVideoFlag = true;
		durationOfPauseInVideo = 0;
		durationOfPauseInTimedActivity = 0;

		startButton = (ImageButton) dialog.findViewById(R.id.rt_practice);
		startButtonDesc = (TextView) dialog.findViewById(R.id.rt_practice_desc);
		aboutButton = (ImageButton) dialog.findViewById(R.id.rt_about);
		bookmarkButton = (ImageButton) dialog.findViewById(R.id.rt_bookmark);
		bookmarkButtonDesc = (TextView) dialog
				.findViewById(R.id.rt_bookmark_desc);

		if (itemClicked.isActivity()) {
			startButton.setVisibility(View.VISIBLE);
			startButtonDesc.setVisibility(View.VISIBLE);
		} else {
			startButton.setVisibility(View.GONE);
			startButtonDesc.setVisibility(View.GONE);
		}

		aboutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final Dialog dialogAbout = new Dialog(context,
						android.R.style.Theme_Translucent);
				dialogAbout.getWindow().setWindowAnimations(
						R.style.DialogFadeAnimation);
				dialogAbout.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialogAbout.setCancelable(true);
				dialogAbout.setContentView(R.layout.dialog_message);

				TextView dialogHeader = (TextView) dialogAbout
						.findViewById(R.id.dialog_title);
				dialogHeader.setText(itemClicked.getName());

				TextView dialogBody = (TextView) dialogAbout
						.findViewById(R.id.dialog_body);
				dialogBody.setText(itemClicked.getDesc());

				dialogAbout.show();

				ImageButton close = (ImageButton) dialogAbout
						.findViewById(R.id.dialog_close);
				close.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialogAbout.dismiss();
					}
				});

				Button dismiss = (Button) dialogAbout
						.findViewById(R.id.dialog_dismiss);
				dismiss.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialogAbout.dismiss();
					}
				});

			}
		});

		isBookmarked = DatabaseHelper.getInstance(context).isBookmarked(
				itemClicked.getID());
		if (isBookmarked) {
			bookmarkButton
					.setImageResource(R.drawable.item_unbookmark_selector);
			bookmarkButtonDesc.setText("Remove Bookmark");
		}
		bookmarkButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (isBookmarked) {
					DatabaseHelper.getInstance(context).removeBookmark(
							itemClicked.getID());
					bookmarkButton
							.setImageResource(R.drawable.item_bookmark_selector);
					bookmarkButtonDesc.setText("Bookmark");
					isBookmarked = false;
					Toasted.showToast("Bookmark removed");
				} else {
					DatabaseHelper.getInstance(context).addBookmark(
							itemClicked.getID(), itemClicked.getPath());
					bookmarkButton
							.setImageResource(R.drawable.item_unbookmark_selector);
					bookmarkButtonDesc.setText("Remove Bookmark");
					isBookmarked = true;
					Toasted.showToast("Bookmarked!");
				}
			}
		});

		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Starts activity timer
				openTrainingTimerDialog();
			}
		});

		ImageButton closeButton = (ImageButton) dialog
				.findViewById(R.id.rt_close);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {

				// Records the video end time
				endVideoTime = new Date(System.currentTimeMillis());
				inVideoFlag = false;

				long timeSpentWatchingVideo = endVideoTime.getTime()
						- startVideoTime.getTime();
				timeSpentWatchingVideo = timeSpentWatchingVideo / 1000;
				timeSpentWatchingVideo = timeSpentWatchingVideo
						- durationOfPauseInVideo;

				pointsEarnedAlertTextView.setText("+ " + timeSpentWatchingVideo
						+ " points");
				animationManager.playFireworks(mainContainer);
				animationManager.playPointsAlertAnim(pointsEarnedAlertTextView);

				DatabaseHelper.getInstance(context).setCompleted(
						itemClicked.getID());
				DatabaseHelper.getInstance(context).addStatisticsTime(
						itemClicked.getID(), itemClicked.getMediaType(),
						(int) timeSpentWatchingVideo, 1);
				DatabaseHelper.getInstance(context).addStatisticsBreakdownTime(
						itemClicked.getID(), itemClicked.getMediaType(),
						(int) timeSpentWatchingVideo, timedActivity);
				pointsManager.addPoints(timeSpentWatchingVideo);

				if (context instanceof Home) {
					((Home) context).onResume();
				} else if (context instanceof Submenu) {
					((Submenu) context).onResume();
				}

				// Resets any timed activities
				timedActivity = 0;
			}
		});

		if (parentNode != null) {
			// Check if all items under this menu have been completed
			boolean menuCompleted = true;
			for (MenuComponent component : parentNode.getMenuItems()) {
				boolean componentCompleted = DatabaseHelper
						.getInstance(context).isCompleted(component.getID());
				if (!componentCompleted) {
					menuCompleted = false;
				}
			}
			if (menuCompleted) {
				// All submenu items have been completed;
				// Set the parent menu as completed
				DatabaseHelper.getInstance(context).setCompleted(
						parentNode.getID());
			}

			// Resets any timed activities
			timedActivity = 0;
		}

		dialog.show();
	}

	// ---------- Timed Activity/Stopwatch Dialog Interactions
	// ------------------

	public void openTrainingTimerDialog() {

		dialogTimer = new Dialog(context, android.R.style.Theme_Translucent);
		dialogTimer.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogTimer.setCancelable(true);
		dialogTimer.getWindow()
				.setWindowAnimations(R.style.DialogFadeAnimation);
		dialogTimer.setContentView(R.layout.dialog_stopwatch);

		inTimedActivityFlag = true;

		msgTimer = (TextView) dialogTimer.findViewById(R.id.dialog_timer);

		TextView msgPreviousTimed = (TextView) dialogTimer
				.findViewById(R.id.dialog_previous);
		Integer previousTimedTime = DatabaseHelper.getInstance(context)
				.getTimedActivityTime(itemClicked.getID());
		msgPreviousTimed.setText("previous training time "
				+ Stopwatch.formatTime((float) previousTimedTime * 1000));

		startTime = System.currentTimeMillis();

		ImageButton closeButton = (ImageButton) dialogTimer
				.findViewById(R.id.dialog_close);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialogTimer.dismiss();
			}
		});

		Button finishedButton = (Button) dialogTimer
				.findViewById(R.id.dialog_dismiss);
		finishedButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialogTimer.dismiss();
			}
		});

		dialogTimer.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				inTimedActivityFlag = false;
				float timedActivityMilliseconds = (System.currentTimeMillis() - startTime);
				timedActivity = (int) (TimeConversion
						.millsecondsToSeconds(timedActivityMilliseconds) - durationOfPauseInTimedActivity);
				mHandler.removeCallbacks(startTimer);
			}
		});

		dialogTimer.show();

		// Start timer
		mHandler.removeCallbacks(startTimer);
		mHandler.postDelayed(startTimer, 0);

	}

	// ------- Timed Activity/Stopwatch Related Methods ----------------

	private void updateTimer(float time) {

		secondsRaw = (long) (time / 1000);
		secondsRaw = secondsRaw - durationOfPauseInTimedActivity;
		minRaw = (long) ((time / 1000) / 60);
		hoursRaw = (long) (((time / 1000) / 60) / 60);

		/*
		 * Convert the seconds to String and format to ensure it has a leading
		 * zero when required
		 */
		secondsRaw = secondsRaw % 60;
		seconds = String.valueOf(secondsRaw);
		if (secondsRaw == 0) {
			seconds = "00";
		}
		if (secondsRaw < 10 && secondsRaw > 0) {
			seconds = "0" + seconds;
		}

		/* Convert the minutes to String and format the String */

		minRaw = minRaw % 60;
		minutes = String.valueOf(minRaw);
		if (minRaw == 0) {
			minutes = "00";
		}
		if (minRaw < 10 && minRaw > 0) {
			minutes = "0" + minutes;
		}

		/* Convert the hours to String and format the String */

		hours = String.valueOf(hoursRaw);
		if (hoursRaw == 0) {
			hours = "00";
		}
		if (hoursRaw < 10 && hoursRaw > 0) {
			hours = "0" + hours;
		}

		/* Setting the timer text to the elapsed time */

		msgTimer.setText(hours + ":" + minutes + ":" + seconds);
	}

	private Runnable startTimer = new Runnable() {
		public void run() {
			elapsedTime = System.currentTimeMillis() - startTime;
			updateTimer(elapsedTime);
			mHandler.postDelayed(this, REFRESH_RATE);
		}
	};
}
