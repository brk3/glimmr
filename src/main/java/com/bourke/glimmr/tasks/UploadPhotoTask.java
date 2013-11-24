package com.bourke.glimmr.tasks;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.ITaskQueueServiceListener;
import com.bourke.glimmr.fragments.upload.LocalPhotosGridFragment;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.uploader.UploadMetaData;
import com.squareup.tape.Task;

import java.io.FileInputStream;
import java.io.IOException;

public class UploadPhotoTask implements Task<ITaskQueueServiceListener> {

    private static final String TAG = "Glimmr/UploadPhotoTask";

    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    /* http://www.flickr.com/services/api/upload.api.html */
    private static final String FLICKR_NO_PHOTO_SPECIFIED = "2";
    private static final String FLICKR_GENERAL_UPLOAD_FAILURE = "3";
    private static final String FLICKR_FILE_SIZE_ZERO = "4";
    private static final String FLICKR_UNKNOWN_FILETYPE = "5";
    private static final String FLICKR_UPLOAD_LIMIT_REACHED = "6";

    private final LocalPhotosGridFragment.LocalPhoto mPhoto;
    private final UploadMetaData mMetadata;
    private final OAuth mOAuth;

    public UploadPhotoTask(OAuth oauth, LocalPhotosGridFragment.LocalPhoto photo) {
        mOAuth = oauth;
        mPhoto = photo;
        mMetadata = photo.getMetadata();
    }

    @Override
    public void execute(final ITaskQueueServiceListener listener) {
        if (mOAuth == null) {
            Log.e(TAG, "UploadPhotoTask requires authentication");
            MAIN_THREAD.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailure(mPhoto.getUri(), false);
                }
            });
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OAuthToken token = mOAuth.getToken();
                    Flickr f = FlickrHelper.getInstance().getFlickrAuthed(token.getOauthToken(),
                            token.getOauthTokenSecret());
                    try {
                        f.getUploader().upload(mMetadata.getTitle(),
                                new FileInputStream(mPhoto.getUri()), mMetadata);

                        /* success */
                        postToMainThread(listener, true, false);

                    } catch (FlickrException e) {
                        e.printStackTrace();
                        final String errCode = e.getErrorCode();
                        /* retry */
                        if (FLICKR_GENERAL_UPLOAD_FAILURE.equals(errCode) ||
                                FLICKR_UPLOAD_LIMIT_REACHED.equals(errCode)) {
                            postToMainThread(listener, false, true);
                        } else {
                            postToMainThread(listener, false, false);
                        }
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
                    listener.onSuccess(mPhoto.getUri());
                } else {
                    listener.onFailure(mPhoto.getUri(), retry);
                }
            }
        });
    }
}
