package com.kinetiqa.glacier.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
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
import com.kinetiqa.glacier.ui.FragmentComposeMessage;
import com.kinetiqa.glacier.utils.TimeConversion;

import java.io.File;
import java.io.IOException;

public class DialogAudioRecorder extends Dialog {

    private FragmentComposeMessage h;

    private File audioFile;
    private String audioFileLocation;
    private MediaPlayer audioPlayer;
    private MediaRecorder audioRecorder;
    private Handler recorderTimeHandler = new Handler();
    private Handler seekHandler = new Handler();
    private long audioRecorderStartTime;

    private SeekBar playbackSeekBar;
    private TextView playbackTimerTextView;
    private TextView recordTimerTextView;
    private Button recordButton;
    private Button stopRecordingButton;
    private Button playButton;
    private Button pauseButton;
    private Button finishButton;

    public DialogAudioRecorder(Context context, FragmentComposeMessage h) {
        super(context, android.R.style.Theme_Holo_NoActionBar);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.h = h;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setCancelable(true);
        setContentView(R.layout.dialog_message_audio_recorder);
        init();
    }

    private void init() {
        recordTimerTextView = (TextView) findViewById(R.id.ar_timer);
        playbackTimerTextView = (TextView) findViewById(R.id.ar_playback_timer);

        playbackSeekBar = (SeekBar) findViewById(R.id.ar_seek);
        playbackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                audioPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        recordButton = (Button) findViewById(R.id.ar_record);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordButton.setVisibility(View.GONE);
                stopRecordingButton.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.GONE);
                finishButton.setVisibility(View.GONE);
                playbackSeekBar.setVisibility(View.GONE);
                recordTimerTextView.setVisibility(View.VISIBLE);
                playbackTimerTextView.setVisibility(View.GONE);

                audioPlayer = null;
                try {
                    startRecording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        stopRecordingButton = (Button) findViewById(R.id.ar_stop);
        stopRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordButton.setVisibility(View.VISIBLE);
                stopRecordingButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
                finishButton.setVisibility(View.VISIBLE);
                playbackSeekBar.setVisibility(View.VISIBLE);
                recordTimerTextView.setVisibility(View.GONE);
                playbackTimerTextView.setVisibility(View.VISIBLE);

                stopRecording();
            }
        });

        playButton = (Button) findViewById(R.id.ar_play);
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

        pauseButton = (Button) findViewById(R.id.ar_pause);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);

                audioPlayer.pause();
            }
        });

        finishButton = (Button) findViewById(R.id.ar_finish);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (audioPlayer != null) {
                    // audioPlayer.stop();
                    // audioPlayer.release();
                }
                h.attachAudioCallback(audioFileLocation);
                dismiss();
            }
        });

        ImageButton closeButton = (ImageButton) findViewById(R.id.dialog_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private void initAudioPlayback() {
        audioPlayer = new MediaPlayer();

        try {
            audioPlayer.setDataSource(audioFileLocation);
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
    }

    private void startRecording() throws IOException {
        File dir = new File(Config.MESSAGE_MEDIA_PATH_PREFIX);
        dir.mkdirs();

        File noMedia = new File(dir, ".nomedia");
        try {
            noMedia.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            audioFile = File.createTempFile("sound", ".mp4", dir);
        } catch (IOException e) {
            System.out.println(e.toString());
            return;
        }

        audioFileLocation = audioFile.getPath();
        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        audioRecorder.setOutputFile(audioFile.getAbsolutePath());
        audioRecorder.prepare();
        audioRecorder.start();

        audioRecorderStartTime = System.currentTimeMillis();
        recorderTimeHandler.postDelayed(recorderTimerRunnable, 100);
    }

    private void stopRecording() {
        audioRecorder.stop();
        audioRecorder.release();
        recorderTimeHandler.removeCallbacks(recorderTimerRunnable);
    }

    Runnable seekBarRunnable = new Runnable() {
        @Override
        public void run() {
            playbackSeekBar.setProgress(audioPlayer.getCurrentPosition());
            playbackTimerTextView.setText(TimeConversion.convertMillisecondsToTime(audioPlayer.getCurrentPosition()) + "/" + TimeConversion.convertMillisecondsToTime(audioPlayer.getDuration()));
            seekHandler.postDelayed(seekBarRunnable, 100);
        }
    };

    Runnable recorderTimerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsedTime = System.currentTimeMillis() - audioRecorderStartTime;
            recordTimerTextView.setText(TimeConversion.convertMillisecondsToTime(elapsedTime));
            recorderTimeHandler.postDelayed(recorderTimerRunnable, 100);
        }
    };

}
