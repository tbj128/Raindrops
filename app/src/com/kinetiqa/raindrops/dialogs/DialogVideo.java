package com.kinetiqa.raindrops.dialogs;

import java.util.Date;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Environment;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

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

public class DialogVideo {

	/**
	 * STATIC COMPONENTS
	 */
	public static final Integer VIDEO_PLAYING = 1;
	public static final Integer VIDEO_NOT_PLAYING = 0;

	/**
	 * DIALOG MANAGEMENT
	 */
	private Context context;
	private Dialog dialog;
	private PointsManager pointsManager;
	private AnimationManager animationManager;

	private MenuLeaf itemClicked;
	private boolean isBookmarked = false;
	private Date startVideoTime;
	private Date endVideoTime;
	private Date pauseVideoTime;
	private Date resumeVideoTime;
	private long durationOfPauseInVideo = 0;
	private long durationOfPauseInTimedActivity = 0;
	private Integer timedActivity = 0;

	private long durationOfVideo = 99999;
	private boolean inVideoFlag = false;
	private boolean inTimedActivityFlag = false;
	private Integer videoPlayerState = VIDEO_PLAYING;

	/**
	 * LAYOUT COMPONENTS
	 */
	private VideoView videoPlayer;
	private TextView mediaVisualTimer;
	private SeekBar seekBar;
	private ImageButton playButton;
	private ImageButton pauseButton;
	private ImageButton forwardButton;
	private ImageButton rewindButton;
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

	public DialogVideo(Context context) {
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

	public void open(MenuComposite parentNode, MenuLeaf item,
			final RelativeLayout mainContainer,
			final TextView pointsEarnedAlertTextView) {

		dialog = new Dialog(context, android.R.style.Theme_Translucent);
		dialog.getWindow().setWindowAnimations(R.style.DialogFadeAnimation);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(true);
		dialog.setContentView(R.layout.video_player);

		videoPlayer = (VideoView) ((Dialog) dialog).findViewById(R.id.vp_video);
		mediaVisualTimer = (TextView) ((Dialog) dialog)
				.findViewById(R.id.vp_video_time);
		seekBar = (SeekBar) ((Dialog) dialog).findViewById(R.id.vp_seekbar);

		// The particular item selected in menu structure
		this.itemClicked = item;
		inVideoFlag = true;

		videoPlayer.setVideoPath(Environment.getExternalStorageDirectory()
				+ "/raindrops/content/media/" + itemClicked.getPath());
		videoPlayer.requestFocus();
		videoPlayer.start();

		// Records video start time
		startVideoTime = new Date(System.currentTimeMillis());
		inVideoFlag = true;
		durationOfPauseInVideo = 0;
		durationOfPauseInTimedActivity = 0;

		playButton = (ImageButton) dialog.findViewById(R.id.vp_play);
		pauseButton = (ImageButton) dialog.findViewById(R.id.vp_pause);
		forwardButton = (ImageButton) dialog.findViewById(R.id.vp_forward);
		rewindButton = (ImageButton) dialog.findViewById(R.id.vp_rewind);
		startButton = (ImageButton) dialog.findViewById(R.id.vp_practice);
		startButtonDesc = (TextView) dialog.findViewById(R.id.vp_practice_desc);
		aboutButton = (ImageButton) dialog.findViewById(R.id.vp_about);
		bookmarkButton = (ImageButton) dialog.findViewById(R.id.vp_bookmark);
		bookmarkButtonDesc = (TextView) dialog.findViewById(R.id.vp_bookmark_desc);

		playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (videoPlayer.getCurrentPosition() == durationOfVideo) {
					videoPlayer.seekTo(0);
				}
				videoPlayer.start();
				playButton.setVisibility(View.GONE);
				pauseButton.setVisibility(View.VISIBLE);
				videoPlayerState = VIDEO_PLAYING;
			}
		});

		pauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				videoPlayer.pause();
				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
				videoPlayerState = VIDEO_NOT_PLAYING;
			}
		});

		forwardButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				videoPlayer.seekTo(videoPlayer.getCurrentPosition() + 500);
				return false;
			}
		});

		rewindButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				videoPlayer.seekTo(videoPlayer.getCurrentPosition() - 500);
				return false;
			}
		});

		aboutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				videoPlayer.pause();
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
						if (videoPlayerState == VIDEO_PLAYING) {
							// Video was playing beforehand; Let it continue
							// playing
							videoPlayer.start();
							videoPlayerState = VIDEO_PLAYING;
						}
						dialogAbout.dismiss();
					}
				});

				Button dismiss = (Button) dialogAbout
						.findViewById(R.id.dialog_dismiss);
				dismiss.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (videoPlayerState == VIDEO_PLAYING) {
							// Video was playing beforehand; Let it continue
							// playing
							videoPlayer.start();
							videoPlayerState = VIDEO_PLAYING;
						}
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
				videoPlayer.pause();
				// Starts activity timer
				openTrainingTimerDialog();
			}
		});

		videoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				durationOfVideo = videoPlayer.getDuration();
				seekBar.setMax(videoPlayer.getDuration());
				seekBar.postDelayed(updateSeekBar, 100);
			}
		});

		videoPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				// Video has finished
				mp.seekTo(0);
				playButton.setVisibility(View.VISIBLE);
				pauseButton.setVisibility(View.GONE);
				videoPlayerState = VIDEO_NOT_PLAYING;
			}
		});

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				if (fromUser) {
					// this is when actually seekbar has been seeked to a new
					// position
					videoPlayer.seekTo(progress);
				}
			}
		});

		ImageButton closeButton = (ImageButton) dialog
				.findViewById(R.id.vp_close);
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
				// All submenu items have been completed; Set the parent menu as
				// completed
				DatabaseHelper.getInstance(context).setCompleted(
						parentNode.getID());
			}
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
				if (videoPlayerState == VIDEO_PLAYING) {
					videoPlayer.start();
				}

			}
		});

		dialogTimer.show();

		// Start timer
		mHandler.removeCallbacks(startTimer);
		mHandler.postDelayed(startTimer, 0);

	}

	// Updates the seekbar of the video and checks if end if the video is near
	private Runnable updateSeekBar = new Runnable() {

		@Override
		public void run() {

			if (seekBar != null) {
				seekBar.setProgress(videoPlayer.getCurrentPosition());
				seekBar.postDelayed(updateSeekBar, 100);
			}

			if (itemClicked.isActivity()) {
				startButton.setVisibility(View.VISIBLE);
				startButtonDesc.setVisibility(View.VISIBLE);
			} else {
				startButton.setVisibility(View.GONE);
				startButtonDesc.setVisibility(View.GONE);
			}

			if (mediaVisualTimer != null) {
				mediaVisualTimer.setText(TimeConversion
						.convertMillisecondsToTime(videoPlayer
								.getCurrentPosition())
						+ "/"
						+ TimeConversion.convertMillisecondsToTime(videoPlayer
								.getDuration()));
			}

		}
	};

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
