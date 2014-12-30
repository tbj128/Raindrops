package com.kinetiqa.glacier.core;

import android.os.Environment;

import java.util.Locale;

/**
 * Created by Tom on 2014-10-31.
 */
public class Config {

    public static Locale locale = Locale.CANADA;

    public static String CONTENT_DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/glacier/content/";
    public static String MENU_MEDIA_PATH_PREFIX = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/glacier/content/media/";
    public static String MESSAGE_MEDIA_PATH_PREFIX = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/glacier/messages/media/";

    public static String DOWNLOAD_URL = "";

    // Messages
    public static int MAX_MESSAGES_FETCH = 12;
}
