package com.kinetiqa.glacier.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinetiqa.glacier.R;
import com.kinetiqa.glacier.core.Config;
import com.kinetiqa.glacier.core.connection.ConnectionManager;
import com.kinetiqa.glacier.database.DatabaseHelper;
import com.kinetiqa.glacier.messaging.Message;
import com.kinetiqa.glacier.messaging.MessagingManager;

import java.io.File;
import java.util.List;


public class FragmentInbox extends Fragment {

    private View fragmentView;
    private ListView mainMenuListView;
    private TextView emptyMessagesTextView;
    private ImageButton trashAllButton;
    private List<Message> messages;
    private Dialog deleteAllDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.messages_fragment, container, false);
        this.fragmentView = view;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
        SyncMessagesTask syncMessagesTask = new SyncMessagesTask();
        syncMessagesTask.execute();
    }

    private void init() {
        trashAllButton = (ImageButton) fragmentView.findViewById(R.id.messages_delete);
        trashAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDeleteAllMessageConfirm();
            }
        });

        mainMenuListView = (ListView) fragmentView.findViewById(R.id.message_list);
        mainMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                DatabaseHelper.getInstance(getActivity()).updateMessageAsRead(messages.get(position).getId());
                ((Home) getActivity()).initSidebar();
                Intent i = new Intent(getActivity(), MessageViewer.class);
                i.putExtra("id", messages.get(position).getId());
                i.putExtra("isInbox", true);
                startActivity(i);
            }
        });

        emptyMessagesTextView = (TextView) fragmentView.findViewById(R.id.messages_empty);

        FindMessagesTask findMessagesTask = new FindMessagesTask();
        findMessagesTask.execute();
    }

    public void openDeleteAllMessageConfirm() {
        deleteAllDialog = new Dialog(getActivity(),
                android.R.style.Theme_Translucent);
        deleteAllDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        deleteAllDialog.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        deleteAllDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        deleteAllDialog.setCancelable(true);
        deleteAllDialog.setContentView(R.layout.dialog_confirm);

        TextView msgTitle = (TextView) deleteAllDialog.findViewById(R.id.confirm_title);
        msgTitle.setText("Delete All Inbox");

        TextView msgBody = (TextView) deleteAllDialog.findViewById(R.id.confirm_desc);
        msgBody.setText("Are you sure you want to delete all the inbox messages on your tablet?");

        Button yesButton = (Button) deleteAllDialog
                .findViewById(R.id.confirm_okay);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                DatabaseHelper.getInstance(getActivity()).deleteAllInbox();
                DatabaseHelper.getInstance(getActivity()).deleteAllOutbox();

                // Deletes physical messages on device
                for (Message message : messages) {
                    if (message.getAttachmentName() != "") {
                        File dirMedia = new File(Config.MESSAGE_MEDIA_PATH_PREFIX + message.getAttachmentName());
                        if (!dirMedia.isDirectory()) {
                            dirMedia.delete();
                        }
                    }
                }
                init();
                deleteAllDialog.dismiss();
            }
        });

        Button noButton = (Button) deleteAllDialog.findViewById(R.id.confirm_cancel);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                deleteAllDialog.dismiss();
            }
        });

        deleteAllDialog.show();
    }

    private BaseAdapter messagesAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_message, parent, false);

            Message message = messages.get(position);

            TextView title = (TextView) view.findViewById(R.id.item_message_title);
            title.setText(message.getTitle());

            TextView preview = (TextView) view.findViewById(R.id.item_message_preview);
            preview.setText(message.getDescriptionPreview());

            RelativeLayout messageContainer = (RelativeLayout) view.findViewById(R.id.item_message_container);
            if (message.isRead()) {
                messageContainer.setBackgroundResource(R.drawable.bg_grey50_rounded_selector);
            } else {
                title.setTypeface(null, Typeface.BOLD);
                messageContainer.setBackgroundResource(R.drawable.bg_white_rounded_selector);
            }

            return view;
        }
    };

    private class FindMessagesTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            messages = MessagingManager.getInstance(getActivity()).getInboxMessages();
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            mainMenuListView.setAdapter(messagesAdapter);
            if (messages.size() == 0) {
                emptyMessagesTextView.setVisibility(View.VISIBLE);
                mainMenuListView.setVisibility(View.GONE);
                trashAllButton.setVisibility(View.GONE);
            } else {
                emptyMessagesTextView.setVisibility(View.GONE);
                mainMenuListView.setVisibility(View.VISIBLE);
                trashAllButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private class SyncMessagesTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            ConnectionManager cm = new ConnectionManager(getActivity());
            cm.syncMessages();
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (DatabaseHelper.getInstance(getActivity()).getNumUnreadMessages() > 0) {
                ((Home) getActivity()).initSidebar();
                init();
            }
        }
    }

}
