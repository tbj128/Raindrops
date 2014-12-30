package com.kinetiqa.glacier.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Tom on 2014-11-01.
 */
public class MenuItem extends Menu {

    private int mediaType = MenuManager.MEDIA_UNDEFINED;
    private String mediaName;
    private boolean isActivity;
    private int numViews = 0;
    private int totalTimeViewed = 0;
    private long videoLength = 0;

    public MenuItem(String id, String title, String desc, String requiresString, boolean isActivity, int mediaType, String mediaName) {
        String[] requiresStringArr = requiresString.split(",");
        List<String> requires = null;
        if (requiresStringArr.length > 0) {
            if (!"".equals(requiresStringArr[0])) {
                requires = new ArrayList<String>(Arrays.asList(requiresStringArr));
            }
        }

        this.id = id;
        this.title = title;
        this.desc = desc;
        this.requires = requires;
        this.mediaType = mediaType;
        this.isActivity = isActivity;
        this.mediaName = mediaName;
    }

    public MenuItem(String id, String title, String desc, List<String> requires, boolean isActivity, int mediaType, String mediaName) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.requires = requires;
        this.mediaType = mediaType;
        this.isActivity = isActivity;
        this.mediaName = mediaName;
    }

    public boolean isActivity() {
        return this.isActivity;
    }

    public int getMediaType() {
        return this.mediaType;
    }

    public String getMediaName() {
        return this.mediaName;
    }

    public void setNumViews(int numViews) {
        this.numViews = numViews;
    }

    public int getNumViews() {
        return this.numViews;
    }

    public long getTotalTimeViewed() {
        return this.totalTimeViewed;
    }


    public long getVideoLength() {
        return videoLength;
    }

    public void setVideoLength(long videoLength) {
        this.videoLength = videoLength;
    }

}
