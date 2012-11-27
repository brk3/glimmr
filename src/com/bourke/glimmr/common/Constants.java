package com.bourke.glimmr.common;

public class Constants {

    /* Important: set to false when doing release builds */
    public static final boolean DEBUG = false;

    /* Global app prefs */
    public static final String PREFS_NAME = "glimmr_prefs";

    /* File and mem cache tuning params */
    public static final boolean USE_FILE_CACHE = true;
    public static final boolean USE_MEMORY_CACHE = true;
    public static final int CACHE_TRIM_TRIGGER_SIZE = 8000000;  // 8MB
    public static final int CACHE_TRIM_TARGET_SIZE = 5000000;  // 5MB
    public static final int IMAGE_CACHE_LIMIT = 100;  // Images
    public static final int MEM_CACHE_PX_SIZE = 3000000;  // 3M pixels

    /* General use SharedPreferences keys */
    public static final String KEY_USER_NAME = "glimmr_user_name";
    public static final String KEY_USER_ID = "glimmr_user_id";
    public static final String KEY_IS_FIRST_RUN = "glimmr_is_first_run";
    public static final String KEY_OAUTH_TOKEN = "glimmr_oauthtoken";
    public static final String KEY_TOKEN_SECRET = "glimmr_tokensecret";
    public static final String KEY_ACCOUNT_USER_NAME = "glimmr_acc_user_name";
    public static final String KEY_ACCOUNT_USER_ID = "glimmr_acc_user_id";

    /* Fonts */
    public static final int FONT_SHADOWSINTOLIGHT = 0;
    public static final String FONT_PATH_SHADOWSINTOLIGHT =
        "fonts/ShadowsIntoLight.ttf";
    public static final int FONT_ROBOTOREGULAR = 1;
    public static final String FONT_PATH_ROBOTOREGULAR =
        "fonts/Roboto-Regular.ttf";
    public static final int FONT_ROBOTOTHIN = 2;
    public static final String FONT_PATH_ROBOTOTHIN =
        "fonts/Roboto-Thin.ttf";
    public static final int FONT_ROBOTOLIGHT = 3;
    public static final String FONT_PATH_ROBOTOLIGHT =
        "fonts/Roboto-Light.ttf";
    public static final int FONT_ROBOTOBOLD = 4;
    public static final String FONT_PATH_ROBOTOBOLD =
        "fonts/Roboto-Bold.ttf";

    /* Global preferences keys */
    public static final String KEY_INTERVALS_LIST_PREFERENCE
        = "notificationIntervals";
    public static final String KEY_ENABLE_NOTIFICATIONS
        = "enableNotifications";
    public static final String KEY_ENABLE_CONTACTS_NOTIFICATIONS
        = "enableContactsNotifications";
    public static final String KEY_ENABLE_ACTIVITY_NOTIFICATIONS
        = "enableActivityNotifications";

    /* Number of items to fetch per page for calls that support pagination */
    public static final int FETCH_PER_PAGE = 20;

    /* Notification ids */
    public static final int NOTIFICATION_NEW_CONTACTS_PHOTOS = 0;
    public static final int NOTIFICATION_NEW_ACTIVITY = 1;

    /* DialogBuilder dialog ids */
    public static final int DIALOG_ABOUT = 0;

    public static final String PRO_MARKET_LINK =
        "market://details?id=com.bourke.glimmrpro";
}
