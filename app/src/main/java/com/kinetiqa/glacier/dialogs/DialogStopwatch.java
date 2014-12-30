package com.kinetiqa.glacier.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kinetiqa.glacier.R;
import com.kinetiqa.glacier.database.DatabaseHelper;
import com.kinetiqa.glacier.utils.TimeConversion;

public class DialogStopwatch extends Dialog {

    private Context context;
    private String menuID;

    private Handler timerHandler = new Handler();
    private long startTime;
    private long elapsedTime;
    private long pauseStartTime = -1;
    private long pauseTotalTime = 0;

    private TextView timerTextView;

    public DialogStopwatch(Context context, String menuID) {
        super(context, android.R.style.Theme_Holo_NoActionBar);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.context = context;
        this.menuID = menuID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
		setCancelable(true);
		setContentView(R.layout.dialog_stopwatch);

        timerTextView = (TextView) findViewById(R.id.sw_time);
        timerTextView.setText(TimeConversion.convertMillisecondsToTime(0));

        int previousTimedTime = DatabaseHelper.getInstance(context)
                .getTimedActivityTime(menuID);
        TextView msgPreviousTimed = (TextView) findViewById(R.id.sw_prev_time);
        msgPreviousTimed.setText("previously trained for "
                + TimeConversion.formatStopwatchTime((float) previousTimedTime * 1000));

        startTime = System.currentTimeMillis();

        ImageButton closeButton = (ImageButton) findViewById(R.id.sw_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dismiss();
            }
        });

        Button finishButton = (Button) findViewById(R.id.sw_finish);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dismiss();
            }
        });
	}

    public void startTimer() {
        timerHandler.removeCallbacks(startTimer);
        timerHandler.postDelayed(startTimer, 0);
    }

    public void pauseTimer() {
        timerHandler.removeCallbacks(startTimer);
        pauseStartTime = System.currentTimeMillis();
    }

    public void resumeTimer() {
        if (pauseStartTime > -1) {
            long currentTime = System.currentTimeMillis();
            pauseTotalTime += currentTime - pauseStartTime;
        }
        pauseStartTime = -1;
        timerHandler.removeCallbacks(startTimer);
        timerHandler.postDelayed(startTimer, 0);
    }

    public void stopTimer() {
        timerHandler.removeCallbacks(startTimer);
    }

    private Runnable startTimer = new Runnable() {
        public void run() {
            elapsedTime = System.currentTimeMillis() - startTime - pauseTotalTime;
            timerTextView.setText(TimeConversion.convertMillisecondsToTime(elapsedTime));
            timerHandler.postDelayed(this, 500);
        }
    };
}
