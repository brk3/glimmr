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

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IAddItemToGroupListener;

import com.google.gson.GsonBuilder;

import com.squareup.tape.Task;

public class AddToGroupTaskQueueService extends Service
        implements IAddItemToGroupListener {

    public static boolean IS_RUNNING = false;

    private static final String TAG = "Glimmr/AddToGroupTaskQueueService";

    private static final int MAX_RETRIES = 5;

    private AddToGroupTaskQueue mQueue;
    private boolean running;
    private int mNumRetries = 0;

    @Override
    public void onCreate() {
        if (Constants.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate();
        mQueue = AddToGroupTaskQueue.newInstance(this,
                AddToGroupTaskQueue.QUEUE_FILE, new GsonBuilder().create());
        IS_RUNNING = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Constants.DEBUG) Log.d(TAG, "onDestroy");
        IS_RUNNING = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        executeNext();
        return START_STICKY;
    }

    private void executeNext() {
        /* Only one task at a time. */
        if (running) {
            return;
        }

        Task task = mQueue.peek();
        if (task != null) {
            if (Constants.DEBUG) Log.d(TAG, "Attempt: " + mNumRetries);
            running = true;
            task.execute(this);
        } else {
            if (Constants.DEBUG) Log.d(TAG, "Queue empty, service stopping");
            /* No more tasks are present. Stop. */
            stopSelf();
        }
    }

    @Override
    public void onSuccess(final String itemId) {
        if (Constants.DEBUG) Log.d(TAG, "onSuccess: " + itemId);
        running = false;
        mQueue.remove();
        mNumRetries = 0;
        executeNext();
    }

    @Override
    public void onFailure(final String itemId, final boolean retry) {
        if (Constants.DEBUG) Log.d(TAG, "onFailure: " + itemId);
        mNumRetries++;
        running = false;
        if (mNumRetries >= MAX_RETRIES) {
            if (Constants.DEBUG) {
                Log.d(TAG, "Max retries reached, service stopping");
            }
            stopSelf();
        } else {
            if (!retry) {
                Log.e(TAG, "Task marked unrecoverable! Removing from queue");
                mQueue.remove();
            }
            executeNext();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
