package com.kinetiqa.glacier.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kinetiqa.glacier.R;
import com.kinetiqa.glacier.core.Config;
import com.kinetiqa.glacier.core.PointsManager;
import com.kinetiqa.glacier.core.StatisticsManager;
import com.kinetiqa.glacier.database.DatabaseHelper;
import com.kinetiqa.glacier.dialogs.DialogInfo;
import com.kinetiqa.glacier.dialogs.DialogStopwatch;
import com.kinetiqa.glacier.menu.Menu;
import com.kinetiqa.glacier.menu.MenuFolder;
import com.kinetiqa.glacier.menu.MenuItem;
import com.kinetiqa.glacier.menu.MenuManager;


public class MediaDocument extends Activity implements MediaViewer {

    private MenuItem document;
    private DialogStopwatch dialogStopwatch;
    private long timePracticed = 0;

    private ImageButton favButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_document);

        Intent intent = getIntent();
        String id = intent.getExtras().getString("id");
        Menu document = MenuManager.getInstance().findMenu(id);
        if (document == null) {
            finish();
            return;
        }

        if (document instanceof MenuFolder) {
            finish();
            return;
        }

        this.document = (MenuItem) document;

        initTitle();
        initButtons();
        initDocumentViewer();
        StatisticsManager.getInstance(getApplicationContext()).beginTiming();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatisticsManager.getInstance(getApplicationContext()).endPause();
        if (dialogStopwatch != null) {
            dialogStopwatch.resumeTimer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatisticsManager.getInstance(getApplicationContext()).beginPause(false);
        if (dialogStopwatch != null) {
            dialogStopwatch.pauseTimer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finishEvent();
    }

    private void finishEvent() {
        if (document == null) {
            return;
        }
        if (timePracticed < 0) {
            timePracticed = 0;
        }
        long totalTimeSeconds = StatisticsManager.getInstance(getApplicationContext()).finishTiming();
        PointsManager.getInstance(getApplicationContext()).addPoints(totalTimeSeconds);

        DatabaseHelper.getInstance(getApplicationContext()).setCompleted(
                document.getID());
        DatabaseHelper.getInstance(getApplicationContext()).addStatisticsTime(
                document.getID(), document.getMediaType(),
                (int) totalTimeSeconds, 1);
        DatabaseHelper.getInstance(getApplicationContext()).addStatisticsBreakdownTime(
                document.getID(), document.getMediaType(),
                (int) totalTimeSeconds, (int) timePracticed);

        finish();
    }

    private void initTitle() {
        TextView title = (TextView) findViewById(R.id.doc_title);
        title.setText(document.getTitle());
    }

    private void initButtons() {
        // Back button transitions between different menu layers
        Button backButton = (Button) findViewById(R.id.doc_back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishEvent();
            }
        });

        ImageButton infoButton = (ImageButton) findViewById(R.id.doc_info);
        infoButton.setVisibility(View.VISIBLE);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInfo dialogInfo = new DialogInfo(MediaDocument.this, document.getTitle(), document.getDesc());
                dialogInfo.show();
                dialogInfo.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        // TODO
                    }
                });
            }
        });

        ImageButton practiceButton = (ImageButton) findViewById(R.id.doc_practice);
        if (document.isActivity()) {
            practiceButton.setVisibility(View.VISIBLE);
            practiceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StatisticsManager.getInstance(getApplicationContext()).beginActivityTiming();
                    dialogStopwatch = new DialogStopwatch(MediaDocument.this, document.getID());
                    dialogStopwatch.show();
                    dialogStopwatch.startTimer();

                    dialogStopwatch.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            timePracticed = StatisticsManager.getInstance(getApplicationContext()).finishActivityTiming();
                            dialogStopwatch.stopTimer();
                        }
                    });
                }
            });
        } else {
            practiceButton.setVisibility(View.GONE);
        }

        boolean isBookmarked = DatabaseHelper.getInstance(getApplicationContext()).isBookmarked(document.getID());
        favButton = (ImageButton) findViewById(R.id.doc_favourite);
        favButton.setVisibility(View.VISIBLE);
        if (isBookmarked) {
            favButton.setImageResource(R.drawable.ic_action_important);
        } else {
            favButton.setImageResource(R.drawable.ic_action_not_important);
        }
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isBookmarked = DatabaseHelper.getInstance(getApplicationContext()).isBookmarked(document.getID());
                if (isBookmarked) {
                    DatabaseHelper.getInstance(getApplicationContext()).removeBookmark(document.getID());
                    favButton.setImageResource(R.drawable.ic_action_not_important);
                    Toast.makeText(getApplicationContext(), "Favourite removed", Toast.LENGTH_LONG).show();
                } else {
                    DatabaseHelper.getInstance(getApplicationContext()).addBookmark(
                            document.getID(), document.getMediaName());
                    favButton.setImageResource(R.drawable.ic_action_important);
                    Toast.makeText(getApplicationContext(), "Favourite added", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void initDocumentViewer() {
        WebView webview = (WebView) findViewById(R.id.doc_viewer);
        webview.getSettings().setBuiltInZoomControls(false);
        webview.getSettings().setSupportZoom(false);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setAllowFileAccess(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebChromeClient(new WebChromeClient() {
        });

        String pathToDocument = "file://" + Config.MENU_MEDIA_PATH_PREFIX + document.getMediaName();
        webview.loadUrl(pathToDocument);
    }
}
