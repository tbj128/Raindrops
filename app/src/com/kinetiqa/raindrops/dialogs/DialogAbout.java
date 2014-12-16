package com.kinetiqa.raindrops.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kinetiqa.raindrops.R;

public class DialogAbout {

	private Context context;
	private Dialog dialog;
	
	public DialogAbout(Context context) {
			this.context = context;
	}
	
	// Opens a new message
	public void openMessage(String messageTitle, String messageText) {
		dialog = new Dialog(context,
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
}
