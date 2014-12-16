package com.kinetiqa.raindrops.anim;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.kinetiqa.raindrops.R;

public class AnimationManager {

	private Context context;
	
	public AnimationManager(Context context) {
		this.context = context;
	}

	public void playFireworks(RelativeLayout container) {
		DiamondView animSparks = new DiamondView(context);
		SparkView sparks = new SparkView(context);

		container.addView(animSparks);
		container.addView(sparks);

		playSuccessFX();
	}

	public void playPointsAlertAnim(View container) {
		container.setVisibility(View.VISIBLE);
		Animation animTextFlyIn = AnimationUtils.loadAnimation(context,
				R.anim.text_flyin);
		container.startAnimation(animTextFlyIn);
	}

	public void playSuccessFX() {
		try {
			MediaPlayer m = new MediaPlayer();
			AssetFileDescriptor descriptor = context.getAssets().openFd(
					"fx_success.mp3");
			m.setDataSource(descriptor.getFileDescriptor(),
					descriptor.getStartOffset(), descriptor.getLength());
			descriptor.close();

			m.prepare();
			m.setVolume(1f, 1f);
			m.setLooping(false);
			m.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
