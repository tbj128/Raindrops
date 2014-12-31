package com.kinetiqa.glacier.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kinetiqa.glacier.R;
import com.kinetiqa.glacier.core.Config;
import com.kinetiqa.glacier.database.DatabaseHelper;
import com.kinetiqa.glacier.utils.TimeConversion;

import java.io.IOException;

public class DialogAudioPreview extends Dialog {

    private Context context;
    private String audioAttachmentLocation;
    private MediaPlayer audioPlayer;
    private TextView playbackTimerTextView;
    private SeekBar playbackSeekBar;
    private Button playButton;
    private Button pauseButton;
    private Handler seekHandler = new Handler();

    public DialogAudioPreview(Context context, String audioAttachmentLocation) {
        super(context, android.R.style.Theme_Holo_NoActionBar);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.context = context;
        this.audioAttachmentLocation = audioAttachmentLocation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
		setCancelable(true);
		setContentView(R.layout.dialog_audio_preview);

        initAudioPlayback();

        ImageButton closeButton = (ImageButton) findViewById(R.id.dialog_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dismiss();
            }
        });
	}

    private void initAudioPlayback() {
        playbackTimerTextView = (TextView) findViewById(R.id.mp_audio_time);

        playbackSeekBar = (SeekBar) findViewById(R.id.mp_seek);
        playbackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (audioPlayer != null && fromUser) {
                    audioPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        audioPlayer = new MediaPlayer();

        try {
            audioPlayer.setDataSource(audioAttachmentLocation);
            audioPlayer.prepare();

            playbackSeekBar.setMax(audioPlayer.getDuration());
            playbackSeekBar.setProgress(audioPlayer.getCurrentPosition());
            seekHandler.postDelayed(seekBarRunnable, 100);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        playButton = (Button) findViewById(R.id.mp_audio_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);

                if (audioPlayer == null) {
                    initAudioPlayback();
                }
                audioPlayer.start();
            }
        });

        pauseButton = (Button) findViewById(R.id.mp_audio_pause);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);

                audioPlayer.pause();
            }
        });

    }

    Runnable seekBarRunnable = new Runnable() {
        @Override
        public void run() {
            playbackSeekBar.setProgress(audioPlayer.getCurrentPosition());
            playbackTimerTextView.setText(TimeConversion.convertMillisecondsToTime(audioPlayer.getCurrentPosition()) + "/" + TimeConversion.convertMillisecondsToTime(audioPlayer.getDuration()));
            seekHandler.postDelayed(seekBarRunnable, 100);
        }
    };
}
