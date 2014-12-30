package com.kinetiqa.glacier.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by Tom on 2014-10-30.
 */
public class MediaVideoView extends VideoView {

    private PlayPauseListener mListener;

    public MediaVideoView(Context context) {
        super(context);
    }

    public MediaVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPlayPauseListener(PlayPauseListener listener) {
        mListener = listener;
    }

    @Override
    public void pause() {
        super.pause();
        if (mListener != null) {
            mListener.onPause();
        }
    }

    @Override
    public void start() {
        super.start();
        if (mListener != null) {
            mListener.onPlay();
        }
    }

    public static interface PlayPauseListener {
        void onPlay();
        void onPause();
    }
}
