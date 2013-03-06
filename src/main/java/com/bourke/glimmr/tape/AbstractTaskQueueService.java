/*
 * Copyright 2012 Square, Inc.
 *
 * Based off Square's ImageUploadTaskService that ships as part of tape.
 */

package com.bourke.glimmrpro.tape;

import android.app.Service;
import com.bourke.glimmrpro.fragments.viewer.AddToGroupDialogFragment;

import android.content.Intent;

import android.os.IBinder;

import android.util.Log;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.event.Events.ITaskQueueServiceListener;
import com.bourke.glimmrpro.tape.AbstractTaskQueueService;
import com.bourke.glimmrpro.tape.GsonConverter;
import com.bourke.glimmrpro.tasks.AddItemToGroupTask;

import com.google.gson.Gson;

import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.FileObjectQueue.Converter;
import com.squareup.tape.Task;
import com.squareup.tape.TaskQueue;

import java.io.File;
import java.io.IOException;

public abstract class AbstractTaskQueueService extends Service
        implements ITaskQueueServiceListener {

    private static final String TAG = "Glimmr/AbstractTaskQueueService";

    private static final int MAX_RETRIES = 5;

    protected TaskQueue mQueue;
    private boolean mRunning;
    private int mNumRetries = 0;

    protected abstract void initTaskQueue();

    @Override
    public void onCreate() {
        if (Constants.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate();
        initTaskQueue();
    }

    @Override
    public void onDestroy() {
        if (Constants.DEBUG) Log.d(TAG, "onDestroy");
        super.onDestroy();
        mRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        executeNext();
        return START_STICKY;
    }

    private void executeNext() {
        /* Only one task at a time. */
        if (mRunning) {
            return;
        }

        Task task = mQueue.peek();
        if (task != null) {
            if (Constants.DEBUG) Log.d(TAG, "Attempt: " + mNumRetries);
            mRunning = true;
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
        mRunning = false;
        mQueue.remove();
        mNumRetries = 0;
        executeNext();
    }

    @Override
    public void onFailure(final String itemId, final boolean retry) {
        if (Constants.DEBUG) Log.d(TAG, "onFailure: " + itemId);
        mNumRetries++;
        mRunning = false;
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
