/*
 * Copyright 2012 Square, Inc.
 *
 * Based off Square's ImageUploadTaskService that ships as part of tape.
 */

package com.bourke.glimmr.tape;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.bourke.glimmr.BuildConfig;
import com.bourke.glimmr.event.Events.ITaskQueueServiceListener;
import com.squareup.tape.Task;
import com.squareup.tape.TaskQueue;

public abstract class AbstractTaskQueueService extends Service
        implements ITaskQueueServiceListener {

    private static final String TAG = "Glimmr/AbstractTaskQueueService";

    protected static final int MAX_RETRIES = 5;

    protected TaskQueue mQueue;
    protected int mNumRetries = 0;
    private boolean mRunning;

    /* used to notify user if the service did any work before finishing */
    protected abstract void initTaskQueue();

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate();
        initTaskQueue();
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy");
        super.onDestroy();
        mRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        executeNext();
        /* the service should run until explicitly stopped */
        return START_STICKY;
    }

    private void executeNext() {
        /* Only one task at a time. */
        if (mRunning) {
            return;
        }

        Task task = mQueue.peek();
        if (task != null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Attempt: " + mNumRetries);
            mRunning = true;
            task.execute(this);
        } else {
            /* No more tasks are present. Stop. */
            if (BuildConfig.DEBUG) Log.d(TAG, "Queue empty, service stopping");
            stopSelf();
        }
    }

    @Override
    public void onSuccess(final String itemId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onSuccess: " + itemId);
        mRunning = false;
        mQueue.remove();
        mNumRetries = 0;
        executeNext();
    }

    @Override
    public void onFailure(final String itemId, final boolean retry) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onFailure: " + itemId);
        mNumRetries++;
        mRunning = false;
        if (mNumRetries >= MAX_RETRIES) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Max retries reached, service stopping");
            }
            stopSelf();
        } else {
            if (!retry) {
                Log.e(TAG, "Task marked unrecoverable! Removing from queue");
                mQueue.remove();
            } else {
                int ms_wait = mNumRetries * 1000;
                Log.w(TAG, "Retrying in " + ms_wait + " ms.");
                try {
                    Thread.sleep(ms_wait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            executeNext();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
