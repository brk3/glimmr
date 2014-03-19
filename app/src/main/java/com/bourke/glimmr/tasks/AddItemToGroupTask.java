package com.bourke.glimmr.tasks;

import com.bourke.glimmr.BuildConfig;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.ITaskQueueServiceListener;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.squareup.tape.Task;
import org.json.JSONException;

import java.io.IOException;

public class AddItemToGroupTask
        implements Task<ITaskQueueServiceListener> {

    private static final String TAG = "Glimmr/AddItemToGroupTask";

    private static final Handler MAIN_THREAD = new Handler(
            Looper.getMainLooper());

    /* Error codes from
     * http://www.flickr.com/services/api/flickr.groups.pools.add.html */
    private static final String FLICKR_PHOTO_NOT_FOUND = "1";
    private static final String FLICKR_GROUP_NOT_FOUND = "2";
    private static final String FLICKR_ALREADY_IN_POOL = "3";
    private static final String FLICKR_PHOTO_IN_MAX_POOLS = "4";
    private static final String FLICKR_PHOTO_LIMIT_REACHED = "5";
    private static final String FLICKR_PHOTO_PENDING_MOD = "6";
    private static final String FLICKR_PHOTO_PENDING = "7";
    private static final String FLICKR_POOL_FULL = "10";

    private final String mGroupId;
    private final String mItemId;
    private final OAuth mOAuth;

    public AddItemToGroupTask(String groupId, String itemId, OAuth oauth) {
        mGroupId = groupId;
        mItemId = itemId;
        mOAuth = oauth;
    }

    @Override
    public void execute(final ITaskQueueServiceListener listener) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format(
                    "Processing photo id %s for group %s", mItemId,
                    mGroupId));
        }
        if (mOAuth == null) {
            Log.e(TAG, "AddItemToGroupTask requires authentication");
            MAIN_THREAD.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailure(mItemId, false);
                }
            });
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OAuthToken token = mOAuth.getToken();
                    Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                        token.getOauthToken(),
                        token.getOauthTokenSecret());
                    try {
                        f.getPoolsInterface().add(mItemId, mGroupId);
                        /* success */
                        postToMainThread(listener, true, false);
                    } catch (FlickrException e) {
                        e.printStackTrace();
                        final String errCode = e.getErrorCode();
                        /* any of the following warrants no retry */
                        if (FLICKR_PHOTO_NOT_FOUND.equals(errCode) ||
                                FLICKR_GROUP_NOT_FOUND.equals(errCode) ||
                                FLICKR_ALREADY_IN_POOL.equals(errCode) ||
                                FLICKR_PHOTO_IN_MAX_POOLS.equals(errCode) ||
                                FLICKR_PHOTO_LIMIT_REACHED.equals(errCode) ||
                                FLICKR_PHOTO_PENDING_MOD.equals(errCode) ||
                                FLICKR_PHOTO_PENDING.equals(errCode) ||
                                FLICKR_POOL_FULL.equals(errCode)) {
                            postToMainThread(listener, false, false);
                        } else {
                            Log.e(TAG, "Unknown FlickrException code: " +
                                    errCode);
                            postToMainThread(listener, false, false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        /* failure, queue for retry */
                        postToMainThread(listener, false, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                        /* failure, queue for retry */
                        postToMainThread(listener, false, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        /* shouldn't get here, don't retry */
                        postToMainThread(listener, false, false);
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    /* shouldn't get here, don't retry */
                    postToMainThread(listener, false, false);
                }
            }
        }).start();
    }

    /**
     * Calls to callback functions must be made on the main thread.
     */
    private void postToMainThread(final ITaskQueueServiceListener listener,
            final boolean success, final boolean retry) {
        MAIN_THREAD.post(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    listener.onSuccess(mItemId);
                } else {
                    listener.onFailure(mItemId, retry);
                }
            }
        });
    }
}
