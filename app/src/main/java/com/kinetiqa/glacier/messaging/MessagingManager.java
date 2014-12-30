package com.kinetiqa.glacier.messaging;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.kinetiqa.glacier.core.connection.ConnectionManager;
import com.kinetiqa.glacier.database.DatabaseHelper;

/**
 * Manages the messaging portal including creating, deleting, and viewing all
 * message types
 *
 * @author: Tom Jin
 * @date: June 12, 2013
 * @modified: Jan 12, 2014
 */

public class MessagingManager {

    private static MessagingManager mInstance;
    private Context context;

    private List<Message> inboxMessages = new ArrayList<Message>();
    private List<Message> outboxMessages = new ArrayList<Message>();

    private MessagingManager(Context c) {
        this.context = c;
    }

    public static MessagingManager getInstance(Context c) {
        if (mInstance == null) {
            mInstance = new MessagingManager(c);
        }
        return mInstance;
    }

    // --------- Compose --------
    public void sendMessage(Message message) {
        DatabaseHelper.getInstance(this.context).addOutgoingMessage(message);
        ConnectionManager cm = new ConnectionManager(context);
        cm.syncMessagesInBackground();
    }

    // --------- Inbox ----------

    public int getNumUnreadInboxMessages() {

        return DatabaseHelper.getInstance(this.context).getNumUnreadMessages();
    }

    public int getTotalNumInboxMessages() {
        return DatabaseHelper.getInstance(this.context).getNumTotalMessages();
    }

    public List<Message> getInboxMessages() {
        return DatabaseHelper.getInstance(this.context).getAllInbox();
    }

    // --------- Outbox ----------

    public int getTotalNumOutboxMessages() {
        return DatabaseHelper.getInstance(this.context).getNumTotalMessages();
    }

    public List<Message> getOutboxMessages() {
        return DatabaseHelper.getInstance(this.context).getAllOutbox();
    }

}
