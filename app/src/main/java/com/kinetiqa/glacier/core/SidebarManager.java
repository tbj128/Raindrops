package com.kinetiqa.glacier.core;

import com.kinetiqa.glacier.R;
import com.kinetiqa.glacier.messaging.MessagingManager;
import com.kinetiqa.glacier.ui.Home;
import com.kinetiqa.glacier.ui.components.SidebarItem;

/**
 * Created by Tom on 2014-11-02.
 */
public class SidebarManager {
    public static final int NUM_DISPLAYED_SIDEBAR_ITEMS = 7;

    public static final int HOME = 0;
    public static final int MENU = 1;
    public static final int FAVOURITES = 2;
    public static final int COMPOSE_NEW_MESSAGE = 3;
    public static final int INBOX = 4;
    public static final int OUTBOX = 5;
    public static final int LEADERS = 6;
    public static final int STATS = 7;

    public static SidebarItem[] sidebarItems = new SidebarItem[NUM_DISPLAYED_SIDEBAR_ITEMS];

    public static SidebarItem getSidebarItem(Home h, int pos) {
        if (sidebarItems.length == NUM_DISPLAYED_SIDEBAR_ITEMS) {
            sidebarItems[0] = new SidebarItem(MENU, "Content", R.drawable.ic_action_view_as_grid);
            sidebarItems[1] = new SidebarItem(FAVOURITES, "Favourites", R.drawable.ic_action_important);
            sidebarItems[2] = new SidebarItem(COMPOSE_NEW_MESSAGE, "Compose Message", R.drawable.ic_action_edit);
            sidebarItems[3] = new SidebarItem(INBOX, "Inbox", R.drawable.ic_action_unread);
            sidebarItems[4] = new SidebarItem(OUTBOX, "Sent", R.drawable.ic_action_reply);
            sidebarItems[5] = new SidebarItem(LEADERS, "Leaders", R.drawable.ic_action_view_as_list);
            sidebarItems[6] = new SidebarItem(STATS, "Stats", R.drawable.ic_action_favorite);
        }
        // We want Inbox items to indicate their unread messages
        if (pos == 3) {
            int numUnreadInboxMessages = MessagingManager.getInstance(h).getNumUnreadInboxMessages();
            sidebarItems[3].setTitle("Inbox (" + numUnreadInboxMessages + ")");
        }
        return sidebarItems[pos];
    }

    public static void setInFocus(Home h, int pos) {
        Home.IN_FOCUS = getSidebarItem(h, pos).getId();
    }
}
