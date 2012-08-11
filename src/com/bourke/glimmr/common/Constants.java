package com.bourke.glimmr.common;

public class Constants {

    /* Global app prefs */
    public static final String PREFS_NAME = "glimmr_prefs";

    /* File and mem cache tuning params */
    public static final boolean USE_FILE_CACHE = true;
    public static final boolean USE_MEMORY_CACHE = true;
    public static final int CACHE_TRIM_TRIGGER_SIZE = 8000000;  // 8MB
    public static final int CACHE_TRIM_TARGET_SIZE = 5000000;  // 5MB

    /* Number of items to fetch per page for calls that support pagination */
    public static final int FETCH_PER_PAGE = 20;

    /* OAuth preferences */
    public static final String CALLBACK_SCHEME = "glimmr-oauth-callback";
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

    /* Contacts fragment state */
    public static final String NEWEST_CONTACT_PHOTO_ID =
        "glimmr_newest_contact_photo_id";

    /* Photostream fragment state */
    public static final String NEWEST_PHOTOSTREAM_PHOTO_ID =
        "glimmr_newest_photostream_photo_id";

    /* GroupPool fragment state */
    public static final String NEWEST_GROUPPOOL_PHOTO_ID =
        "glimmr_newest_grouppool_photo_id";

    /* Photoset fragment state */
    public static final String NEWEST_PHOTOSET_PHOTO_ID =
        "glimmr_newest_photoset_photo_id";

    /* Exif dialog state */
    public static final String KEY_EXIF_INFO_DIALOG_ACTIVITY_PHOTO =
        "glimmr_exif_info_dialog_activity_photo";

    /* Comments dialog state */
    public static final String COMMENTS_DIALOG_ACTIVITY_PHOTO =
        "glimmr_comments_dialog_activity_photo";
}
