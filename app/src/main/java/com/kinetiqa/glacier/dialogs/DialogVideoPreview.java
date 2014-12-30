package com.kinetiqa.glacier.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.MediaController;

import com.kinetiqa.glacier.R;
import com.kinetiqa.glacier.core.Config;
import com.kinetiqa.glacier.core.StatisticsManager;
import com.kinetiqa.glacier.ui.components.MediaVideoView;

public class DialogVideoPreview extends Dialog {

    private Context context;
    private String attachmentLocation;


    public DialogVideoPreview(Context context, String attachmentLocation) {
        super(context, android.R.style.Theme_Holo_NoActionBar);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.context = context;
        this.attachmentLocation = attachmentLocation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
		setCancelable(true);
		setContentView(R.layout.dialog_video_preview);

        ImageButton closeButton = (ImageButton) findViewById(R.id.dialog_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dismiss();
            }
        });

        initVideoPlayer();
	}

    private void initVideoPlayer() {
        MediaVideoView videoView = (MediaVideoView) findViewById(R.id.mp_video);
        videoView.setVideoPath(attachmentLocation);
        videoView.requestFocus();

        MediaController controller = new MediaController(context);
        controller.setAnchorView(videoView);
        controller.setMediaPlayer(videoView);

        videoView.setMediaController(controller);
        videoView.start();
    }

}
