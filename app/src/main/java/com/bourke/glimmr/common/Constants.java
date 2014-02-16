package com.bourke.glimmr.common;

import java.util.HashSet;
import java.util.Set;

public class Constants {

    /* Important: set to false when doing release builds */
    public static final boolean DEBUG = false;

    public static final boolean PRO_VERSION = false;

    public static final String ERR_CODE_FLICKR_UNAVAILABLE = "105";

    /* Global app prefs */
    public static final String PREFS_NAME = "glimmr_prefs";

    /* General use SharedPreferences keys */
    public static final String KEY_IS_FIRST_RUN = "glimmr_is_first_run";
    public static final String KEY_OAUTH_TOKEN = "glimmr_oauthtoken";
    public static final String KEY_TOKEN_SECRET = "glimmr_tokensecret";
    public static final String KEY_ACCOUNT_USER_NAME = "glimmr_acc_user_name";
    public static final String KEY_ACCOUNT_USER_ID = "glimmr_acc_user_id";

    /* Attributes to fetch for a photo */
    public static final Set<String> EXTRAS = new HashSet<String>();
    static {
        EXTRAS.add("owner_name");
        EXTRAS.add("url_q");  /* large square 150x150 */
        EXTRAS.add("url_m");  /* small, 240 on longest side */
        EXTRAS.add("url_l");
        EXTRAS.add("views");
        EXTRAS.add("description");
        EXTRAS.add("tags");
    }

    /* Global preferences keys */
    public static final String KEY_INTERVALS_LIST_PREFERENCE
        = "notificationIntervals";
    public static final String KEY_INITIAL_TAB_LIST_PREFERENCE
        = "initialTab";
    public static final String KEY_ENABLE_NOTIFICATIONS
        = "enableNotifications";
    public static final String KEY_ENABLE_CONTACTS_NOTIFICATIONS
        = "enableContactsNotifications";
    public static final String KEY_ENABLE_ACTIVITY_NOTIFICATIONS
        = "enableActivityNotifications";
    public static final String KEY_ENABLE_USAGE_TIPS
        = "enableUsageTips";
    public static final String KEY_SLIDESHOW_INTERVAL
        = "slideshowInterval";
    public static final String KEY_HIGH_QUALITY_THUMBNAILS
        = "highQualityThumbnails";

    /* Number of items to fetch per page for calls that support pagination */
    public static final int FETCH_PER_PAGE = 20;

    /* Notification ids */
    public static final int NOTIFICATION_NEW_CONTACTS_PHOTOS = 0;
    public static final int NOTIFICATION_NEW_ACTIVITY = 1;
    public static final int NOTIFICATION_PHOTOS_UPLOADING = 2;

    /* Tape managed task queues */
    public static final String PHOTOSET_QUEUE = "photoset_task_queue.json";
    public static final String GROUP_QUEUE = "group_task_queue.json";
    public static final String UPLOAD_QUEUE = "upload_task_queue.json";

    public static final String PRO_MARKET_LINK =
        "market://details?id=com.bourke.glimmrpro";
}
