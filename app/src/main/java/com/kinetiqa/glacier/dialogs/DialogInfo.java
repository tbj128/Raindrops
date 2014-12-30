package com.kinetiqa.glacier.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kinetiqa.glacier.R;

public class DialogInfo extends Dialog {

    private String title;
    private String desc;
    
    public DialogInfo(Context context, String title, String desc) {
        super(context, android.R.style.Theme_Holo_NoActionBar);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.title = title;
        this.desc = desc;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
		setCancelable(true);
		setContentView(R.layout.dialog_info);

		TextView msgTitle = (TextView) findViewById(R.id.info_title);
		msgTitle.setText(title);

		TextView msgBody = (TextView) findViewById(R.id.info_desc);
		msgBody.setText(desc);

		Button dismissButton = (Button) findViewById(R.id.info_dismiss);
		dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
	}
}
