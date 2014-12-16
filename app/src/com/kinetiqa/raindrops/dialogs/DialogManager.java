package com.kinetiqa.raindrops.dialogs;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.kinetiqa.raindrops.Home;
import com.kinetiqa.raindrops.R;
import com.kinetiqa.raindrops.Submenu;
import com.kinetiqa.raindrops.connection.ConnectionManager;
import com.kinetiqa.raindrops.database.DatabaseHelper;
import com.kinetiqa.raindrops.util.Secure;

public class DialogManager extends Activity {

	private Context context;

	private SharedPreferences sharedPreferences;

	private Dialog dialogLogin;
	private Dialog dialogFalls;
	private Dialog dialogFallContactTrainerReminder;
	private Dialog dialogTrainingQuestion;
	private Dialog dialogTrainingHours;
	private Dialog dialogWheelingHours;
	private Dialog dialogSetGoals;

	private NumberPicker npTraining;
	private NumberPicker npWheeling;

	private boolean nonTabletFall = false;
	private int nonTabletTraining = 0;
	private int nonTabletWheeling = 0;

	public DialogManager(Context context) {
		this.context = context;
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
	}

	public void login() {
		dialogLogin = new Dialog(context, android.R.style.Theme_Translucent);
		dialogLogin.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogLogin.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
		dialogLogin.setCancelable(true);
		dialogLogin.setContentView(R.layout.dialog_login);

		final TextView loginStatus = (TextView) dialogLogin
				.findViewById(R.id.dialog_login_password_label);
		final EditText loginUsername = (EditText) dialogLogin
				.findViewById(R.id.dialog_login_username);
		final EditText loginPassword = (EditText) dialogLogin
				.findViewById(R.id.dialog_login_password);
		Button loginContinue = (Button) dialogLogin
				.findViewById(R.id.dialog_continue);

		loginContinue.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String username = loginUsername.getText().toString().trim();
				String password = loginPassword.getText().toString().trim();
				if (username.equals("") || password.equals("")) {
					loginStatus.setText("Enter both a username and a password");
					return;
				}

				String securePassword = null;
				try {
					securePassword = Secure.secureHash(password);
				} catch (NoSuchAlgorithmException e) {
					loginStatus.setText("Application error");
					return;
				}

				LoginTask loginTask = new LoginTask(loginStatus, username,
						securePassword);
				loginTask.execute();
			}
		});

		ImageButton loginDismiss = (ImageButton) dialogLogin
				.findViewById(R.id.dialog_close);

		loginDismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialogLogin.dismiss();
			}
		});

		dialogLogin.show();
	}

	public void askInitialQuestionsIfApplicable() {
		long lastAskedQuestions = sharedPreferences.getLong(
				"last_asked_questions", 0);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
				Locale.CANADA);

		Date currDateTime = new Date(System.currentTimeMillis());
		String currDateStr = dateFormat.format(currDateTime);
		Date currDate = currDateTime;

		Date dateTimeToCompare = new Date(lastAskedQuestions);
		String dateToCompareStr = dateFormat.format(dateTimeToCompare);
		Date dateToCompare = dateTimeToCompare;

		try {
			currDate = dateFormat.parse(currDateStr);
			dateToCompare = dateFormat.parse(dateToCompareStr);
		} catch (ParseException e1) {
			Log.e("DialogSupport", "Date parsing error!");
		}

		if (currDate.compareTo(dateToCompare) != 0) {
			// Not same date as before; Show questions
			showDialogFallsIncidents();
		}

	}

	public void showDialogFallsIncidents() {
		try {
			// Obtains the date when the last question was asked
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
					Locale.CANADA);
			long lastTimeQuestionsAskedMilli = sharedPreferences.getLong(
					"last_questions", -1);
			String lastTimeQuestionsAsked = dateFormat
					.format(lastTimeQuestionsAskedMilli);
			Date lastDayQuestionsAsked = dateFormat
					.parse(lastTimeQuestionsAsked);

			// Obtains the current date
			Calendar calendar = Calendar.getInstance();
			long currTime = calendar.getTimeInMillis();
			String currDateString = dateFormat.format(currTime);
			Date currDateObj = dateFormat.parse(currDateString);

			if (currDateObj.compareTo(lastDayQuestionsAsked) != 0
					|| lastTimeQuestionsAskedMilli == -1) {
				// Not the same date as before; Show initial questions
				sharedPreferences.edit().putLong("last_questions", currTime)
						.commit();

				// Prevent multiple dialogs from showing
				if (dialogFalls != null) {
					if (dialogFalls.isShowing()) {
						return;
					}
				}

				dialogFalls = new Dialog(context,
						android.R.style.Theme_Translucent);
				dialogFalls.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialogFalls.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
				dialogFalls.setCancelable(true);
				dialogFalls.setContentView(R.layout.dialog_yes_no);
				TextView dialogHeader = (TextView) dialogFalls
						.findViewById(R.id.message_title);
				dialogHeader.setText("Welcome Back!");

				TextView dialogBody = (TextView) dialogFalls
						.findViewById(R.id.dialog_body);
				dialogBody.setText("Did you have any falls/incidents?");

				Button fallsYes = (Button) dialogFalls
						.findViewById(R.id.dialog_yes);
				fallsYes.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialogFalls.dismiss();
						nonTabletFall = true;
						showDialogContactTrainerReminder();
					}
				});

				Button fallsNo = (Button) dialogFalls
						.findViewById(R.id.dialog_no);
				fallsNo.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialogFalls.dismiss();
						nonTabletFall = false;
						showDialogAdditionalTraining();
					}
				});

				dialogFalls.show();
			}
		} catch (Exception e) {

		}
	}

	public void showDialogContactTrainerReminder() {

		dialogFallContactTrainerReminder = new Dialog(context,
				android.R.style.Theme_Translucent);
		dialogFallContactTrainerReminder.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
		dialogFallContactTrainerReminder
				.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogFallContactTrainerReminder.setCancelable(true);
		dialogFallContactTrainerReminder
				.setContentView(R.layout.dialog_message);

		TextView dialogHeader = (TextView) dialogFallContactTrainerReminder
				.findViewById(R.id.message_title);
		dialogHeader.setText("Important!");

		TextView dialogBody = (TextView) dialogFallContactTrainerReminder
				.findViewById(R.id.dialog_body);
		dialogBody
				.setText("Remember to contact your trainer about this fall/incident.");

		dialogFallContactTrainerReminder.show();

		Button dismiss = (Button) dialogFallContactTrainerReminder
				.findViewById(R.id.dialog_close);
		dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialogFallContactTrainerReminder.dismiss();
				showDialogAdditionalTraining();
			}
		});

	}

	public void showDialogAdditionalTraining() {
		dialogTrainingQuestion = new Dialog(context,
				android.R.style.Theme_Translucent);
		dialogTrainingQuestion.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogTrainingQuestion.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
		dialogTrainingQuestion.setCancelable(true);
		dialogTrainingQuestion.setContentView(R.layout.dialog_yes_no);
		TextView dialogHeader = (TextView) dialogTrainingQuestion
				.findViewById(R.id.message_title);
		dialogHeader.setText("Additional Training");

		TextView dialogBody = (TextView) dialogTrainingQuestion
				.findViewById(R.id.dialog_body);
		dialogBody
				.setText("Since your last session, did you do any training on your own?");

		Button fallsYes = (Button) dialogTrainingQuestion
				.findViewById(R.id.dialog_yes);
		fallsYes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialogTrainingQuestion.dismiss();
				showDialogHoursTraining();
			}
		});

		Button fallsNo = (Button) dialogTrainingQuestion
				.findViewById(R.id.dialog_no);
		fallsNo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Participant did no additional training;
				// Record in database
				dialogTrainingQuestion.dismiss();
				nonTabletTraining = 0;
				boolean extraWheelingQuestions = sharedPreferences.getBoolean(
						"ExtraWheeling", false);
				if (extraWheelingQuestions) {
					// Group needs to be asked extra wheeling questions
					showDialogWheelingHours();
				}
			}
		});

		dialogTrainingQuestion.show();
	}

	public void showDialogHoursTraining() {

		dialogTrainingHours = new Dialog(context,
				android.R.style.Theme_Translucent);
		dialogTrainingHours.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
		dialogTrainingHours.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogTrainingHours.setCancelable(true);
		dialogTrainingHours.setContentView(R.layout.dialog_amount_of_time);

		TextView dialogHeader = (TextView) dialogTrainingHours
				.findViewById(R.id.message_title);
		dialogHeader.setText("Practicing");

		TextView dialogBody = (TextView) dialogTrainingHours
				.findViewById(R.id.dialog_body);
		dialogBody.setText("How many minutes did you spend practicing?");

		String[] nums = new String[21];
		for (int i = 0; i < nums.length; i++)
			nums[i] = Integer.toString(i * 5);
		npTraining = (NumberPicker) dialogTrainingHours
				.findViewById(R.id.dialog_np);
		npTraining
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		npTraining.setMaxValue(nums.length - 1);
		npTraining.setMinValue(0);
		npTraining.setWrapSelectorWheel(false);
		npTraining.setDisplayedValues(nums);

		dialogTrainingHours.show();

		Button dismiss = (Button) dialogTrainingHours
				.findViewById(R.id.dialog_close);
		dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialogTrainingHours.dismiss();
				nonTabletTraining = npTraining.getValue() * 5;

				boolean extraWheelingQuestions = sharedPreferences.getBoolean(
						"ExtraWheeling", false);

				if (extraWheelingQuestions) {
					// Group needs to be asked extra wheeling questions
					showDialogWheelingHours();
				} else {
					// Not in the group that needs extra wheeling questions
					DatabaseHelper.getInstance(context.getApplicationContext())
							.addStatisticsIncidentsAndAdditionalTraining(
									nonTabletFall, nonTabletTraining, 0);
				}
			}
		});

	}

	public void showDialogWheelingHours() {

		dialogWheelingHours = new Dialog(context,
				android.R.style.Theme_Translucent);
		dialogWheelingHours.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
		dialogWheelingHours.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogWheelingHours.setCancelable(true);
		dialogWheelingHours.setContentView(R.layout.dialog_amount_of_time);

		TextView dialogHeader = (TextView) dialogWheelingHours
				.findViewById(R.id.message_title);
		dialogHeader.setText("Wheeling");

		TextView dialogBody = (TextView) dialogWheelingHours
				.findViewById(R.id.dialog_body);
		dialogBody.setText("How many minutes did you spend wheeling?");

		String[] nums = new String[21];
		for (int i = 0; i < nums.length; i++)
			nums[i] = Integer.toString(i * 5);
		npWheeling = (NumberPicker) dialogWheelingHours
				.findViewById(R.id.dialog_np);
		npWheeling
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		npWheeling.setMaxValue(nums.length - 1);
		npWheeling.setMinValue(0);
		npWheeling.setWrapSelectorWheel(false);
		npWheeling.setDisplayedValues(nums);

		dialogWheelingHours.show();

		Button dismiss = (Button) dialogWheelingHours
				.findViewById(R.id.dialog_close);
		dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialogWheelingHours.dismiss();
				nonTabletWheeling = npWheeling.getValue() * 5;
				DatabaseHelper.getInstance(context.getApplicationContext())
						.addStatisticsIncidentsAndAdditionalTraining(
								nonTabletFall, nonTabletTraining,
								nonTabletWheeling);
			}
		});

	}

	public void showDialogSetGoals(final Context c) {

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(c);

		dialogSetGoals = new Dialog(c, android.R.style.Theme_Translucent);
		dialogSetGoals.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
		dialogSetGoals.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogSetGoals.setCancelable(true);
		dialogSetGoals.setContentView(R.layout.dialog_amount_of_time);

		TextView dialogHeader = (TextView) dialogSetGoals
				.findViewById(R.id.dialog_title);
		dialogHeader.setText("Set Weekly Goals");

		TextView dialogBody = (TextView) dialogSetGoals
				.findViewById(R.id.dialog_body);
		dialogBody
				.setText("How many minutes are you aiming to spend training with the tablet?");

		// Allows increments of 5
		String[] nums = new String[61];
		for (int i = 0; i < nums.length; i++)
			nums[i] = Integer.toString(i * 5);
		final NumberPicker np = (NumberPicker) dialogSetGoals
				.findViewById(R.id.dialog_np);
		np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		np.setMaxValue(nums.length - 1);
		np.setMinValue(0);
		np.setWrapSelectorWheel(false);
		np.setDisplayedValues(nums);
		long weekly_goal_minutes = settings.getLong("weekly_goal_minutes", 60);
		np.setValue(Math.round(weekly_goal_minutes / 5));
		dialogSetGoals.show();

		ImageButton close = (ImageButton) dialogSetGoals
				.findViewById(R.id.dialog_close);
		close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialogSetGoals.dismiss();
			}

		});

		Button dismiss = (Button) dialogSetGoals
				.findViewById(R.id.dialog_dismiss);
		dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(context);
				settings.edit()
						.putLong("weekly_goal_minutes", np.getValue() * 5)
						.commit();

				// Sets statistics
				if (c instanceof Home) {
					((Home) c).renderStatistics();
				} else if (c instanceof Submenu) {
					((Submenu) c).renderStatistics();
				}
				dialogSetGoals.dismiss();
			}
		});

	}

	// Opens a new message
	public void openMessage(String messageTitle, String messageText) {
		final Dialog dialog = new Dialog(context,
				android.R.style.Theme_Translucent);
		dialog.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(true);
		dialog.setContentView(R.layout.dialog_message);

		TextView msgTitle = (TextView) dialog.findViewById(R.id.dialog_title);
		msgTitle.setText(messageTitle);

		TextView msgBody = (TextView) dialog.findViewById(R.id.dialog_body);
		msgBody.setText(messageText);

		Button dismissButton = (Button) dialog.findViewById(R.id.dialog_dismiss);
		dismissButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		ImageButton closeButton = (ImageButton) dialog.findViewById(R.id.dialog_close);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	private class LoginTask extends AsyncTask<Void, Void, String> {

		private ConnectionManager cm;
		private TextView loginStatus;
		private String username;
		private String securePassword;

		public LoginTask(TextView loginStatus, String username,
				String securePassword) {
			this.loginStatus = loginStatus;
			this.username = username;
			this.securePassword = securePassword;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			cm = new ConnectionManager(context);
		}

		@Override
		protected String doInBackground(Void... voids) {
			if (cm.checkLogin(username, securePassword)) {
				return null;
			} else {
				return "Username or password incorrect";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				loginStatus.setText(result);
				System.out.println(result);
			} else {
				sharedPreferences.edit().putString("username", username).commit();
				sharedPreferences.edit().putString("password", securePassword)
						.commit();
				loginStatus.setText("Login successful!");
				dialogLogin.dismiss();
				ConnectionManager cm = new ConnectionManager(context);
				cm.sync();
			}
		}
	}

}
