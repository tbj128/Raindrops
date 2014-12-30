package com.kinetiqa.glacier.components;

/**
 * Created by Tom on 2014-10-31.
 */
public class MenuErrorMessage {

    private String title;
    private String desc;

    public MenuErrorMessage(String title, String desc) {
        this.title = title; this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
