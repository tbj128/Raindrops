package com.kinetiqa.glacier.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kinetiqa.glacier.R;
import com.kinetiqa.glacier.core.connection.ConnectionManager;
import com.kinetiqa.glacier.utils.security.Security;

import java.security.NoSuchAlgorithmException;

public class DialogLogin extends Dialog {

    private Context context;
    private SharedPreferences sharedPreferences;
    private boolean showSkip;

    private TextView loginStatusTextView;
    private EditText loginUsernameEditText;
    private EditText loginPasswordEditText;
    private Button loginSkipButton;


    public DialogLogin(Context context, boolean showSkip) {
        super(context, android.R.style.Theme_Holo_NoActionBar);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.showSkip = showSkip;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setCancelable(true);
        setContentView(R.layout.dialog_login);

        loginStatusTextView = (TextView) findViewById(R.id.login_status);
        loginUsernameEditText = (EditText) findViewById(R.id.login_username);
        loginPasswordEditText = (EditText) findViewById(R.id.login_password);

        loginSkipButton = (Button) findViewById(R.id.login_skip);
        loginSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showSkip) {
                    sharedPreferences.edit().putBoolean("skip_login", true).commit();
                }
                dismiss();
            }
        });
        if (!showSkip) {
            loginSkipButton.setText("Cancel");
        }

        Button loginContinue = (Button) findViewById(R.id.login_continue);
        loginContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String username = loginUsernameEditText.getText().toString().trim();
                String password = loginPasswordEditText.getText().toString().trim();
                if (username.equals("") || password.equals("")) {
                    loginStatusTextView.setText("Enter both a username and a password");
                    return;
                } else {
                    loginStatusTextView.setText("Logging in...");
                }

                String securePassword = null;
                try {
                    securePassword = Security.secureHash(password);
                } catch (NoSuchAlgorithmException e) {
                    loginStatusTextView.setText("Application error");
                    return;
                }

                LoginTask loginTask = new LoginTask(username, securePassword);
                loginTask.execute();
            }
        });
    }


    private class LoginTask extends AsyncTask<Void, Void, String> {

        private ConnectionManager cm;
        private String username;
        private String securePassword;

        public LoginTask(String username,
                         String securePassword) {
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
                return "Invalid username or password. Try again.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                loginStatusTextView.setText(result);
            } else {
                sharedPreferences.edit().putString("username", username).commit();
                sharedPreferences.edit().putString("password", securePassword)
                        .commit();
                loginStatusTextView.setText("Login successful!");
                sharedPreferences.edit().putBoolean("skip_login", true).commit();

                dismiss();

                ConnectionManager cm = new ConnectionManager(context);
                cm.sync();
            }
        }
    }

}
