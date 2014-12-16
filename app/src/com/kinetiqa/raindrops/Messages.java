package com.kinetiqa.raindrops;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

import com.kinetiqa.raindrops.components.Message;
import com.kinetiqa.raindrops.connection.ConnectionManager;
import com.kinetiqa.raindrops.database.DatabaseHelper;
import com.kinetiqa.raindrops.dialogs.DialogManager;
import com.kinetiqa.raindrops.util.Fonts;
import com.kinetiqa.raindrops.util.TimeConversion;
import com.kinetiqa.raindrops.util.Toasted;

/**
 * Manages the messaging portal including creating, deleting, and viewing all
 * message types
 * 
 * @author: Tom Jin
 * @date: June 12, 2013
 * @modified: Jan 12, 2014
 */

public class Messages extends Activity {

	public static final int INBOX = 1;
	public static final int OUTBOX = 0;

	public static Context context;
	public static Activity currActivity;
	private SharedPreferences sharedPreferences;
	private ConnectionManager cm;

	/**
	 * LAYOUT STYLES
	 */
	private Typeface typeFaceHandwritten;
	private Typeface typeFaceAcme;

	private InboxAdapter inboxAdapter;
	private OutboxAdapter outboxAdapter;

	// ----- Dialogs -----
	private DialogManager dialogManager;
	private Dialog composeMessage;
	private Dialog composeMessageBody;
	private Dialog composeMessageTypePrompt;
	private Dialog composeVideoMessage;
	private Dialog dialogDeleteAll;

	// ----- Compose Message -----
	private ListView messages;
	private TextView messageFrom;
	private TextView messageTitle;
	private TextView messageDesc;
	private String composeMessageTitle = "";
	private String composeMessageLocation;

	private MediaPlayer videoRecorderPreviewPlayer;
	private ImageButton composeButton;
	private Button playbackButton;
	private Button playbackPauseButton;;
	private Button sendButton;
	private Button startRecordingButton;
	private Button stopRecordingButton;
	private TextView voiceRecordingPrompt;

	// ----- Inbox/Outbox -----
	private TextView inboxOutboxHeader;
	private TextView inboxOutboxSwitchButton;

	private int inboxOrOutboxFlag = Messages.INBOX;
	private List<Message> inbox;
	private List<Message> outbox;
	private Message currOpenMessage;

	// ----- Message Pane -----
	private RelativeLayout messageContainer;
	private MediaPlayer audioMessagePlayer;
	private TextView mediaVisualTimer;
	private SeekBar mediaSlider;
	private RelativeLayout messageMediaContainer;
	private LinearLayout videoMessagePlayerContainer;
	private VideoView videoMessagePlayer;
	private ImageButton messagePlayButton;
	private ImageButton messagePauseButton;
	private ImageButton messageDeleteButton;

	private ImageButton backButton;
	private TextView messageRefreshButton;

	// ----- Audio Recorder -----
	private MediaRecorder audioRecorder;
	private File audiofile = null;
	private boolean isRecordingAudio = false;

	// ---- Video Recorder -----
	final static int REQUEST_VIDEO_CAPTURED = 1;
	private Uri uriVideo = null;
	private VideoView composeVideoPlayer;

	private TextView messageEmptyPrompt;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messages);

		typeFaceHandwritten = Typeface.createFromAsset(getAssets(),
				Fonts.HANDWRITTEN);
		typeFaceAcme = Typeface.createFromAsset(getAssets(), Fonts.ACME);

		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(Messages.this);
		cm = new ConnectionManager(Messages.this);

		initializeComponents();
	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeBackground();
		refreshMessages();
		renderStatistics();
		long lastInboxSync = sharedPreferences.getLong("inbox_sync", -1);
		if (lastInboxSync == -1
				|| (System.currentTimeMillis() - lastInboxSync) > 1000 * 60 * 10) {
			SyncMessages syncTask = new SyncMessages();
			syncTask.execute();
		}
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
	 * Associates fields with their respective Android components
	 */
	private void initializeComponents() {
		dialogManager = new DialogManager(Messages.this);

		messageContainer = (RelativeLayout) findViewById(R.id.message_wrapper);
		messageFrom = (TextView) findViewById(R.id.message_from);
		messageTitle = (TextView) findViewById(R.id.message_title);
		messageDesc = (TextView) findViewById(R.id.message_desc);
		messageEmptyPrompt = (TextView) findViewById(R.id.message_none);
		messages = (ListView) findViewById(R.id.message_list);

		inboxOutboxHeader = (TextView) findViewById(R.id.message_list_pane_top_header);
		inboxOutboxSwitchButton = (TextView) findViewById(R.id.message_list_pane_bottom_switch);
		messageRefreshButton = (TextView) findViewById(R.id.message_list_pane_bottom_refresh);
		initializeHeaders();
		attachListeners();
		initializeButtonEvents();
	}

	/**
	 * Calculates headers
	 */
	private void initializeHeaders() {
		if (inboxOrOutboxFlag == Messages.INBOX) {
			int numUnread = DatabaseHelper.getInstance(getApplicationContext())
					.getNumUnreadMessages();
			inboxOutboxHeader.setText("Inbox (" + numUnread + ")");
		} else {
			inboxOutboxHeader.setText("Outbox");
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
				dialogManager.showDialogSetGoals(Messages.this);
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

	/**
	 * Gives functionality to the Android components
	 */
	private void attachListeners() {

		messageRefreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SyncMessages syncTask = new SyncMessages();
				syncTask.execute();
			}
		});

		messages.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {

				messageContainer.setVisibility(View.VISIBLE);

				if (inboxOrOutboxFlag == Messages.INBOX) {
					currOpenMessage = inbox.get(position);
				} else {
					currOpenMessage = outbox.get(position);
				}

				// Updates message as read in database
				DatabaseHelper.getInstance(getApplicationContext())
						.updateMessageAsRead(currOpenMessage.getId());
				refreshMessages();

				File sdCard = Environment.getExternalStorageDirectory();

				if (currOpenMessage.getType() == Message.TYPE_AUDIO) {
					mediaVisualTimer.setVisibility(View.VISIBLE);
					messageMediaContainer.setVisibility(View.VISIBLE);
					messagePlayButton.setVisibility(View.VISIBLE);
					videoMessagePlayerContainer.setVisibility(View.GONE);
					mediaSlider.setVisibility(View.VISIBLE);

					audioMessagePlayer = new MediaPlayer();
					try {
						if (inboxOrOutboxFlag == 1) {
							audioMessagePlayer.setDataSource(sdCard
									.getAbsolutePath()
									+ "/raindrops/messages/media/"
									+ currOpenMessage.getLocation());
						} else {
							audioMessagePlayer.setDataSource(sdCard
									.getAbsolutePath()
									+ "/raindrops/messages/media/"
									+ currOpenMessage.getLocation());
						}
						audioMessagePlayer.prepare();

						audioMessagePlayer
								.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
									@Override
									public void onPrepared(MediaPlayer mp) {
										mediaSlider.setMax(mp.getDuration());
										mediaSlider.postDelayed(updateSeekBar,
												100);
									}
								});

						audioMessagePlayer
								.setOnCompletionListener(new OnCompletionListener() {

									@Override
									public void onCompletion(MediaPlayer mp) {
										// Message has finished
										messagePauseButton
												.setVisibility(View.GONE);
										messagePlayButton
												.setVisibility(View.VISIBLE);
										mp.seekTo(0);
									}

								});

						mediaSlider
								.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

									@Override
									public void onStopTrackingTouch(
											SeekBar seekBar) {
									}

									@Override
									public void onStartTrackingTouch(
											SeekBar seekBar) {
									}

									@Override
									public void onProgressChanged(
											SeekBar seekBar, int progress,
											boolean fromUser) {

										if (fromUser) {
											// If user touches slider, move
											// video player
											audioMessagePlayer.seekTo(progress);
										}
									}
								});
					} catch (Exception e) {
						e.printStackTrace();
					}

					// Sets Message Title
					if (currOpenMessage.getTitle().equals("")) {
						messageTitle.setText("Message");
					} else {
						messageTitle.setText(currOpenMessage.getTitle());
					}

				} else if (currOpenMessage.getType() == Message.TYPE_VIDEO) {
					mediaVisualTimer.setVisibility(View.GONE);
					messageMediaContainer.setVisibility(View.VISIBLE);
					messagePlayButton.setVisibility(View.VISIBLE);
					videoMessagePlayerContainer.setVisibility(View.VISIBLE);
					mediaSlider.setVisibility(View.GONE);
					System.out.println(sdCard.getAbsolutePath()
							+ "/raindrops/messages/media/"
							+ currOpenMessage.getLocation());
					videoMessagePlayer.setVideoPath(sdCard.getAbsolutePath()
							+ "/raindrops/messages/media/"
							+ currOpenMessage.getLocation());
					videoMessagePlayer.requestFocus();
					videoMessagePlayer.seekTo(10);
					videoMessagePlayer
							.setOnCompletionListener(new OnCompletionListener() {

								@Override
								public void onCompletion(MediaPlayer mp) {
									// Video Message has finished
									messagePauseButton.setVisibility(View.GONE);
									messagePlayButton
											.setVisibility(View.VISIBLE);
									videoMessagePlayer.seekTo(0);
								}

							});
				} else {
					mediaVisualTimer.setVisibility(View.GONE);
					messageMediaContainer.setVisibility(View.GONE);
					messagePlayButton.setVisibility(View.GONE);
					videoMessagePlayerContainer.setVisibility(View.GONE);
					mediaSlider.setVisibility(View.GONE);
				}

				// Sets Message Title
				if (currOpenMessage.getTitle().equals("")) {
					messageTitle.setText("Message");
				} else {
					messageTitle.setText(currOpenMessage.getTitle());
				}
				if (inboxOrOutboxFlag == Messages.OUTBOX)
					messageFrom.setText("from me");
				else
					messageFrom.setText("from " + currOpenMessage.getTarget());

				messageDesc.setText(currOpenMessage.getDescription());

			}
		});

		composeButton = (ImageButton) findViewById(R.id.message_compose);
		composeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				openComposeMessage();
			}
		});

		messageMediaContainer = (RelativeLayout) findViewById(R.id.message_media_container);

		messagePlayButton = (ImageButton) findViewById(R.id.message_play);
		messagePlayButton.setVisibility(View.GONE);
		messagePlayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				messagePauseButton.setVisibility(View.VISIBLE);
				messagePlayButton.setVisibility(View.GONE);

				if (currOpenMessage.getType() == Message.TYPE_AUDIO) {
					audioMessagePlayer.start();
				} else if (currOpenMessage.getType() == Message.TYPE_VIDEO) {
					videoMessagePlayer.start();
				}
			}
		});

		mediaVisualTimer = (TextView) findViewById(R.id.message_video_time);
		mediaVisualTimer.setVisibility(View.GONE);

		mediaSlider = (SeekBar) findViewById(R.id.message_video_seekbar);
		mediaSlider.setVisibility(View.GONE);

		videoMessagePlayer = (VideoView) findViewById(R.id.message_video);
		videoMessagePlayerContainer = (LinearLayout) findViewById(R.id.message_video_container_inner);
		videoMessagePlayerContainer.setVisibility(View.GONE);

		messagePauseButton = (ImageButton) findViewById(R.id.message_pause);
		messagePauseButton.setVisibility(View.GONE);
		messagePauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				messagePlayButton.setVisibility(View.VISIBLE);
				messagePauseButton.setVisibility(View.GONE);

				if (currOpenMessage.getType() == Message.TYPE_AUDIO) {
					audioMessagePlayer.pause();
				} else if (currOpenMessage.getType() == Message.TYPE_VIDEO) {
					videoMessagePlayer.pause();
				}

			}
		});

		messageDeleteButton = (ImageButton) findViewById(R.id.message_trash);
		messageDeleteButton.setVisibility(View.GONE);
		messageDeleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Deletes from database
				DatabaseHelper.getInstance(getApplicationContext()).deleteMsg(
						inboxOrOutboxFlag, currOpenMessage.getId());

				// Deletes from physical hard drive
				File file = new File(currOpenMessage.getLocation());
				file.delete();

				// Resets message pane
				messageDesc.setVisibility(View.VISIBLE);
				messageTitle.setText("No Messages Selected");
				messageDeleteButton.setVisibility(View.GONE);
				messagePauseButton.setVisibility(View.GONE);
				messagePlayButton.setVisibility(View.GONE);
				mediaSlider.setVisibility(View.GONE);

				// Refresh message list
				refreshMessages();
			}
		});

		inboxOutboxSwitchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				toggleInboxOutbox();
			}
		});

		// TODO
		// deleteAllButton = (Button) findViewById(R.id.message_delete_all);
		// if(!sharedPreferences.getBoolean(
		// "deleteMessages", false)) {
		// deleteAllButton.setVisibility(View.GONE);
		// }

		// deleteAllButton.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// openDeleteAllMessageConfirm();
		// }
		// });

		backButton = (ImageButton) findViewById(R.id.nav_back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void toggleInboxOutbox() {
		messageContainer.setVisibility(View.GONE);

		if (inboxOrOutboxFlag == Messages.INBOX) {
			inboxOrOutboxFlag = Messages.OUTBOX;

			inboxOutboxSwitchButton.setText("Inbox");
			initializeHeaders();

			resetMessagePane();
			refreshMessages();

		} else {
			inboxOrOutboxFlag = Messages.INBOX;

			inboxOutboxSwitchButton.setText("Outbox");
			initializeHeaders();

			resetMessagePane();
			refreshMessages();
		}
	}

	private void resetMessagePane() {
		messageDesc.setVisibility(View.VISIBLE);
		messageTitle.setText("No Messages Selected");
		messageDeleteButton.setVisibility(View.GONE);
		messagePauseButton.setVisibility(View.GONE);
		messagePlayButton.setVisibility(View.GONE);
		mediaSlider.setVisibility(View.GONE);
		videoMessagePlayerContainer.setVisibility(View.GONE);
	}

	/**
	 * Retrieves either inbox or outbox messages and populates the list and
	 * headings accordingly
	 */
	public void refreshMessages() {

		if (inboxOrOutboxFlag == Messages.INBOX) {

			inbox = DatabaseHelper.getInstance(getApplicationContext())
					.getAllInbox();
			if (inbox.size() == 0) {
				messageEmptyPrompt.setVisibility(View.VISIBLE);
			} else {
				messageEmptyPrompt.setVisibility(View.GONE);
			}

			int numUnread = DatabaseHelper.getInstance(getApplicationContext())
					.getNumUnreadMessages();
			inboxOutboxHeader.setText("Inbox (" + numUnread + ")");

			if (messages.getAdapter() == null) {
				inboxAdapter = new InboxAdapter();
				messages.setAdapter(inboxAdapter);
			} else {
				if (messages.getAdapter() instanceof InboxAdapter) {
					((InboxAdapter) messages.getAdapter())
							.notifyDataSetChanged();
				} else {
					inboxAdapter = new InboxAdapter();
					messages.setAdapter(inboxAdapter);
				}
			}
		} else {
			// Get all outbox messages
			outbox = DatabaseHelper.getInstance(getApplicationContext())
					.getAllOutbox();
			if (outbox.size() == 0) {
				messageEmptyPrompt.setVisibility(View.VISIBLE);
			} else {
				messageEmptyPrompt.setVisibility(View.GONE);
			}

			if (messages.getAdapter() == null) {
				outboxAdapter = new OutboxAdapter();
				messages.setAdapter(outboxAdapter);
			} else {
				if (messages.getAdapter() instanceof OutboxAdapter) {
					((OutboxAdapter) messages.getAdapter())
							.notifyDataSetChanged();
				} else {
					outboxAdapter = new OutboxAdapter();
					messages.setAdapter(outboxAdapter);
				}
			}
		}
	}

	// ==========================================
	// Composing New Messages

	/**
	 * Opens a dialog to choose between the type of message that the user wishes
	 * to create
	 */
	public void openComposeMessage() {
		composeMessageTypePrompt = new Dialog(Messages.this,
				android.R.style.Theme_Translucent);
		composeMessageTypePrompt.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
		composeMessageTypePrompt.requestWindowFeature(Window.FEATURE_NO_TITLE);
		composeMessageTypePrompt.setCancelable(true);
		composeMessageTypePrompt
				.setContentView(R.layout.dialog_message_type_prompt);

		Button textButton = (Button) composeMessageTypePrompt
				.findViewById(R.id.dialog_text);
		textButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				composeMessageTypePrompt.hide();
				openComposeTextMessage();
			}
		});

		Button voicemailButton = (Button) composeMessageTypePrompt
				.findViewById(R.id.dialog_voice);
		voicemailButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				composeMessageTypePrompt.hide();
				openComposeVoicemail();
			}
		});

		Button videoButton = (Button) composeMessageTypePrompt
				.findViewById(R.id.dialog_video);
		videoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				composeMessageTypePrompt.hide();
				openComposeVideoMessage();
			}
		});

		ImageButton closeButton = (ImageButton) composeMessageTypePrompt
				.findViewById(R.id.dialog_close);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				composeMessageTypePrompt.hide();
			}
		});

		composeMessageTypePrompt.show();
	}

	/**
	 * Composes a new typed message with titles and message body
	 */
	public void openComposeTextMessage() {
		composeMessage = new Dialog(Messages.this,
				android.R.style.Theme_Translucent);
		composeMessage.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
		composeMessage.requestWindowFeature(Window.FEATURE_NO_TITLE);
		composeMessage.setCancelable(true);
		composeMessage.setContentView(R.layout.dialog_new_message_text);
		Window window = composeMessage.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.TOP;
		window.setAttributes(wlp);

		Button sendButton = (Button) composeMessage
				.findViewById(R.id.new_message_send);
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				EditText messageTitleEditText = (EditText) composeMessage
						.findViewById(R.id.new_message_title);
				EditText messageDescEditText = (EditText) composeMessage
						.findViewById(R.id.new_message_desc);

				String title = messageTitleEditText.getText().toString();
				String desc = messageDescEditText.getText().toString();

				DatabaseHelper.getInstance(getApplicationContext())
						.addOutgoingMessage(Message.TYPE_TEXT, "", "", title,
								desc);

				SendMessageTask sendMessageTask = new SendMessageTask();
				sendMessageTask.execute();
				composeMessage.dismiss();
			}
		});

		ImageButton closeButton = (ImageButton) composeMessage
				.findViewById(R.id.new_message_close);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				composeMessage.dismiss();
			}
		});

		composeMessage.show();
	}

	/**
	 * Composes a new video message
	 */
	public void openComposeVideoMessage() {
		Intent intent = new Intent(
				android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
		if (sharedPreferences.getBoolean("lowerVideoQuality", true)) {
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
		}

		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath()
				+ "/raindrops/messages/media");
		dir.mkdirs();

		File nomedia = new File(dir, ".nomedia");
		try {
			nomedia.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File videoFile;
		try {
			videoFile = File.createTempFile("video", ".mp4", dir);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile((videoFile)));
		} catch (IOException e) {
			Toasted.showToast("Error in saving video");
		}
		startActivityForResult(intent, REQUEST_VIDEO_CAPTURED);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_VIDEO_CAPTURED) {
				uriVideo = data.getData();
				composeMessageLocation = uriVideo.toString();
				composeMessageLocation = uriVideo.toString().replace("file://",
						"");

				composeVideoMessage = new Dialog(Messages.this,
						android.R.style.Theme_Translucent);
				composeVideoMessage
						.requestWindowFeature(Window.FEATURE_NO_TITLE);
				composeVideoMessage.setCancelable(true);
				composeVideoMessage
						.setContentView(R.layout.dialog_new_message_video);

				ImageButton closeButton = (ImageButton) composeVideoMessage
						.findViewById(R.id.new_message_close);
				closeButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						composeVideoMessage.dismiss();
					}
				});

				composeVideoPlayer = (VideoView) composeVideoMessage
						.findViewById(R.id.new_message_video);

				composeVideoPlayer.setVideoPath(composeMessageLocation);
				composeVideoPlayer.requestFocus();
				composeVideoPlayer.seekTo(10);

				composeVideoPlayer
						.setOnCompletionListener(new OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {
								playbackPauseButton.setVisibility(View.GONE);
								playbackButton.setVisibility(View.VISIBLE);
								composeVideoPlayer.seekTo(10);
							}

						});

				playbackPauseButton = (Button) composeVideoMessage
						.findViewById(R.id.record_playback_pause);
				playbackPauseButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						playbackButton.setVisibility(View.VISIBLE);
						playbackPauseButton.setVisibility(View.GONE);
						composeVideoPlayer.pause();
					}
				});

				playbackButton = (Button) composeVideoMessage
						.findViewById(R.id.record_playback);
				playbackButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						playbackPauseButton.setVisibility(View.VISIBLE);
						playbackButton.setVisibility(View.GONE);
						composeVideoPlayer.start();
					}
				});

				sendButton = (Button) composeVideoMessage
						.findViewById(R.id.new_message_send);
				sendButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {

						composeMessageTitle = "Video Message";

						String filename = composeMessageLocation
								.substring(composeMessageLocation
										.lastIndexOf('/') + 1);
						DatabaseHelper.getInstance(getApplicationContext())
								.addOutgoingMessage(Message.TYPE_VIDEO, "",
										filename, composeMessageTitle, "");

						SendMessageTask sendMessageTask = new SendMessageTask();
						sendMessageTask.execute();

						refreshMessages();

						composeVideoMessage.dismiss();
					}
				});

				composeVideoMessage.show();
			}
		} else if (resultCode == RESULT_CANCELED) {
			uriVideo = null;
		}
	}

	public void openComposeVoicemail() {
		composeMessageBody = new Dialog(Messages.this,
				android.R.style.Theme_Translucent);
		composeMessageBody.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
		composeMessageBody.requestWindowFeature(Window.FEATURE_NO_TITLE);
		composeMessageBody.setCancelable(true);
		composeMessageBody.setContentView(R.layout.dialog_new_message_audio);

		voiceRecordingPrompt = (TextView) composeMessageBody
				.findViewById(R.id.new_message_body);

		startRecordingButton = (Button) composeMessageBody
				.findViewById(R.id.new_message_record_start);
		startRecordingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				isRecordingAudio = true;
				startRecordingButton.setVisibility(View.GONE);
				stopRecordingButton.setVisibility(View.VISIBLE);
				voiceRecordingPrompt
						.setText("Press 'Finished' when you are done.");
				try {
					startRecording();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		stopRecordingButton = (Button) composeMessageBody
				.findViewById(R.id.new_message_record_finished);
		stopRecordingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				isRecordingAudio = false;
				stopRecordingButton.setVisibility(View.GONE);
				playbackButton.setVisibility(View.VISIBLE);
				sendButton.setVisibility(View.VISIBLE);
				voiceRecordingPrompt
						.setText("Press 'Send' when you are ready to send.");
				stopRecording();
			}
		});

		playbackPauseButton = (Button) composeMessageBody
				.findViewById(R.id.new_message_record_playback_pause);
		playbackPauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				playbackButton.setVisibility(View.VISIBLE);
				playbackPauseButton.setVisibility(View.GONE);
				videoRecorderPreviewPlayer.pause();
			}
		});

		playbackButton = (Button) composeMessageBody
				.findViewById(R.id.new_message_record_playback);
		playbackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				playbackButton.setVisibility(View.GONE);
				playbackPauseButton.setVisibility(View.VISIBLE);
				try {
					videoRecorderPreviewPlayer = new MediaPlayer();
					videoRecorderPreviewPlayer
							.setDataSource(composeMessageLocation);
					videoRecorderPreviewPlayer.prepare();
					videoRecorderPreviewPlayer.start();
					videoRecorderPreviewPlayer
							.setOnCompletionListener(new OnCompletionListener() {
								@Override
								public void onCompletion(MediaPlayer mp) {
									playbackButton.setVisibility(View.VISIBLE);
									playbackPauseButton
											.setVisibility(View.GONE);
								}
							});
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		sendButton = (Button) composeMessageBody
				.findViewById(R.id.new_message_send);
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				composeMessageBody.dismiss();

				composeMessageTitle = "Voicemail";

				String filename = composeMessageLocation
						.substring(composeMessageLocation.lastIndexOf('/') + 1);
				DatabaseHelper.getInstance(getApplicationContext())
						.addOutgoingMessage(Message.TYPE_AUDIO, "", filename,
								composeMessageTitle, "");

				SendMessageTask sendMessageTask = new SendMessageTask();
				sendMessageTask.execute();

				refreshMessages();
			}
		});

		composeMessageBody.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (audioRecorder != null && isRecordingAudio) {
					stopRecording();
				}
			}
		});

		ImageButton closeButton = (ImageButton) composeMessageBody
				.findViewById(R.id.new_message_close);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				composeMessageBody.dismiss();
			}
		});

		composeMessageBody.show();
	}

	public void openDeleteAllMessageConfirm() {
		dialogDeleteAll = new Dialog(Messages.this,
				android.R.style.Theme_Translucent);
		dialogDeleteAll.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogDeleteAll.setCancelable(true);
		dialogDeleteAll.setContentView(R.layout.dialog_yes_no);

		TextView msgTitle = (TextView) dialogDeleteAll
				.findViewById(R.id.dialog_title);
		msgTitle.setText("Delete All");

		TextView msgBody = (TextView) dialogDeleteAll
				.findViewById(R.id.dialog_body);
		msgBody.setText("Are you sure you want to delete all messages on your tablet?");

		Button yesButton = (Button) dialogDeleteAll
				.findViewById(R.id.dialog_yes);
		yesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DatabaseHelper.getInstance(getApplicationContext())
						.deleteAllInbox();
				DatabaseHelper.getInstance(getApplicationContext())
						.deleteAllOutbox();

				// Deletes physical messages on device
				File sdCard = Environment.getExternalStorageDirectory();
				File dirMedia = new File(sdCard.getAbsolutePath()
						+ "/raindrops/messages/media");
				if (dirMedia.isDirectory()) {
					String[] children = dirMedia.list();
					for (int i = 0; i < children.length; i++) {
						new File(dirMedia, children[i]).delete();
					}
				}

				// Resets message pane
				messageDesc.setVisibility(View.VISIBLE);
				messageTitle.setText("No Messages Selected");
				messageDeleteButton.setVisibility(View.GONE);
				messagePauseButton.setVisibility(View.GONE);
				messagePlayButton.setVisibility(View.GONE);
				mediaSlider.setVisibility(View.GONE);
				refreshMessages();

				dialogDeleteAll.dismiss();

			}
		});

		Button noButton = (Button) dialogDeleteAll.findViewById(R.id.dialog_no);
		noButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialogDeleteAll.dismiss();
			}
		});

		dialogDeleteAll.show();
	}

	public void startRecording() throws IOException {

		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath()
				+ "/raindrops/messages/media");
		dir.mkdirs();

		File nomedia = new File(dir, ".nomedia");
		try {
			nomedia.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			audiofile = File.createTempFile("sound", ".mp4", dir);
		} catch (IOException e) {
			System.out.println(e.toString());
			return;
		}
		composeMessageLocation = audiofile.getPath();
		audioRecorder = new MediaRecorder();
		audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		audioRecorder.setOutputFile(audiofile.getAbsolutePath());
		audioRecorder.prepare();
		audioRecorder.start();
	}

	public void stopRecording() {
		audioRecorder.stop();
		audioRecorder.release();
	}

	// Updates the seekbar of the message
	private Runnable updateSeekBar = new Runnable() {

		@Override
		public void run() {
			if (audioMessagePlayer != null) {
				mediaSlider
						.setProgress(audioMessagePlayer.getCurrentPosition());
				mediaSlider.postDelayed(updateSeekBar, 100);
				mediaVisualTimer.setText(TimeConversion
						.convertMillisecondsToTime(audioMessagePlayer
								.getCurrentPosition())
						+ "/"
						+ TimeConversion
								.convertMillisecondsToTime(audioMessagePlayer
										.getDuration()));
			}
		}
	};

	private class SyncMessages extends AsyncTask<Void, Void, Void> {

		public SyncMessages() {
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(Void... voids) {
			cm.syncInbox();
			inbox = DatabaseHelper.getInstance(getApplicationContext())
					.getAllInbox();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			sharedPreferences.edit()
					.putLong("inbox_sync", System.currentTimeMillis()).commit();

			if (inboxOrOutboxFlag == Messages.INBOX) {
				if (messages.getAdapter() instanceof InboxAdapter) {
					((InboxAdapter) messages.getAdapter())
							.notifyDataSetChanged();
				} else {
					messages.setAdapter(inboxAdapter);
				}
			}
		}
	}

	private class SendMessageTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(Void... voids) {
			List<Message> messages = DatabaseHelper.getInstance(
					getApplicationContext()).getUnsentMessages();
			for (Message message : messages) {
				cm.sendMessage(message);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toasted.showToast("Message successfully sent!");
		}
	}

	// ------------- Inbox List Class ---------------

	private class InboxAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return inbox.size();
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
					R.layout.message_item, null);

			Message msg = inbox.get(position);

			RelativeLayout messageContainer = (RelativeLayout) retval
					.findViewById(R.id.message_container);

			TextView title = (TextView) retval
					.findViewById(R.id.message_list_title);
			title.setText(msg.getTitle());

			TextView from = (TextView) retval
					.findViewById(R.id.message_list_from);
			from.setText(msg.getTarget());

			TextView time = (TextView) retval
					.findViewById(R.id.message_list_time);
			time.setText(TimeConversion.convertDateToHumanReadable(msg
					.getDate()));

			if (msg.getRead() == Message.UNREAD) {
				title.setTypeface(null, Typeface.BOLD);
				from.setTypeface(null, Typeface.BOLD);
				time.setTypeface(null, Typeface.BOLD);
				messageContainer.setBackgroundColor(Color.rgb(255, 255, 255));
			}

			return retval;
		}

	};

	// ------------- Outbox List Class ---------------
	private class OutboxAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return outbox.size();
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
					R.layout.message_item, null);

			Message msg = outbox.get(position);

			TextView title = (TextView) retval
					.findViewById(R.id.message_list_title);
			title.setText(msg.getTitle());

			TextView from = (TextView) retval
					.findViewById(R.id.message_list_from);
			from.setText(msg.getTarget());

			TextView time = (TextView) retval
					.findViewById(R.id.message_list_time);
			time.setText(TimeConversion.convertDateToHumanReadable(msg
					.getDate()));

			if (msg.getRead() == Message.UNREAD) {
				title.setTypeface(null, Typeface.BOLD);
				from.setTypeface(null, Typeface.BOLD);
				time.setTypeface(null, Typeface.BOLD);
			}

			return retval;

		}

	};

}
