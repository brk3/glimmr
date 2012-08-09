package com.bourke.glimmr.common;

public class Constants {

    /* File and mem cache tuning params */
    public static final boolean USE_FILE_CACHE = true;
    public static final boolean USE_MEMORY_CACHE = true;
    public static final int CACHE_TRIM_TRIGGER_SIZE = 8000000;  // 8MB
    public static final int CACHE_TRIM_TARGET_SIZE = 5000000;  // 5MB

    /* Number of items to fetch per page for calls that support pagination */
    public static final int FETCH_PER_PAGE = 20;

    /* OAuth callback uri */
    public static final String CALLBACK_SCHEME = "glimmr-oauth-callback";

    /* Global app prefs */
    public static final String PREFS_NAME = "glimmr_prefs";

    public static final String KEY_OAUTH_TOKEN = "glimmr_oauthtoken";
    public static final String KEY_TOKEN_SECRET = "glimmr_tokensecret";
    public static final String KEY_USER_NAME = "glimmr_username";
    public static final String KEY_USER_ID = "glimmr_userid";

    /* Photo viewer state */
    public static final String KEY_PHOTOVIEWER_LIST = "glimmr_photolist";
    public static final String KEY_PHOTOVIEWER_URL = "glimmr_photoviewer_url";
    public static final String KEY_PHOTOVIEWER_START_INDEX =
        "glimmr_photoviewer_index";
    public static final String KEY_PROFILEVIEWER_USER =
        "glimmr_profileviewer_user";

    /* Group viewer state */
    public static final String KEY_GROUPVIEWER_GROUP =
        "glimmr_groupviewer_group";
    public static final String KEY_GROUPVIEWER_USER =
        "glimmr_groupviewer_user";

    /* Photoset viewer state */
    public static final String KEY_PHOTOSETVIEWER_PHOTOSET =
        "glimmr_photosetviewer_photoset";
    public static final String KEY_PHOTOSETVIEWER_USER =
        "glimmr_photosetviewer_user";

    /* Exif dialog state */
    public static final String KEY_EXIF_INFO_DIALOG_ACTIVITY_PHOTO =
        "glimmr_exif_info_dialog_activity_photo";

    /* Comments dialog state */
    public static final String COMMENTS_DIALOG_ACTIVITY_PHOTO =
        "glimmr_comments_dialog_activity_photo";
}
