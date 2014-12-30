package com.kinetiqa.glacier.ui.components;

/**
 * Created by Tom on 2014-10-30.
 */
public class SidebarItem {

    private int id;
    private int icon;
    private String title;

    public SidebarItem(int id, String title, int icon) {
        this.id = id;
        this.icon = icon;
        this.title = title;
    }

    public int getId() {
        return this.id;
    }

    public int getIcon() {
        return this.icon;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return this.title;
    }

}
