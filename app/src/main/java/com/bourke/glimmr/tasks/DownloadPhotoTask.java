package com.bourke.glimmr.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.bourke.glimmr.event.Events;
import com.squareup.picasso.Picasso;
import com.squareup.tape.Task;

import java.io.IOException;

public class DownloadPhotoTask implements Task<Events.ITaskQueueServiceListener> {

    private static final String TAG = "Glimmr/DownloadPhotoTask";

    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    private final String mUrl;
    private final Context mContext;
    private Bitmap mBitmap = null;

    public DownloadPhotoTask(Context context, String url) {
        mContext = context;
        mUrl = url;
    }

    @Override
    public void execute(final Events.ITaskQueueServiceListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        /* get the image synchronously */
                        mBitmap = Picasso.with(mContext).load(mUrl).get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    /* success */
                    postToMainThread(listener, true, false);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    /* shouldn't get here, don't retry */
                    postToMainThread(listener, false, false);
                }
            }
        }).start();
    }

    private void postSuccessToMainThread(final Events.ITaskQueueServiceListener listener) {
        MAIN_THREAD.post(new Runnable() {
            @Override
            public void run() {
                    listener.onSuccess(mBitmap);
            }
        });
    }

    /**
     * Calls to callback functions must be made on the main thread.
     */
    private void postToMainThread(final Events.ITaskQueueServiceListener listener,
                                  final boolean success, final boolean retry) {
//        MAIN_THREAD.post(new Runnable() {
//            @Override
//            public void run() {
//                if (success) {
//                    listener.onSuccess(mPhoto.getUri());
//                } else {
//                    listener.onFailure(mPhoto.getUri(), retry);
//                }
//            }
//        });
    }
}