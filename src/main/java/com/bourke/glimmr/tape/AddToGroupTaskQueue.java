/*
 * Copyright 2012 Square, Inc.
 *
 * Based off Square's ImageUploadTaskQueue that ships as part of tape.
 */

package com.bourke.glimmr.tape;

import android.content.Context;
import android.content.Intent;

import com.bourke.glimmr.event.BusProvider;
import com.bourke.glimmr.tasks.AddItemToGroupTask;

import com.google.gson.Gson;

import com.squareup.otto.Produce;
import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.FileObjectQueue.Converter;
import com.squareup.tape.ObjectQueue;
import com.squareup.tape.TaskQueue;

import java.io.File;
import java.io.IOException;

public class AddToGroupTaskQueue extends TaskQueue<AddItemToGroupTask> {

    private static final String TAG = "Glimmr/AddToGroupTaskQueue";

    public static final String QUEUE_FILE =
        "add_item_to_group_task_queue.json";

    private final Context mContext;

    public static AddToGroupTaskQueue newInstance(Context context,
            String filename, Gson gson) {
        Converter<AddItemToGroupTask> converter =
            new GsonConverter<AddItemToGroupTask>(
                    gson, AddItemToGroupTask.class);
        File queueFile = new File(context.getFilesDir(), filename);
        FileObjectQueue<AddItemToGroupTask> delegate = null;
        try {
            delegate = new FileObjectQueue<AddItemToGroupTask>(queueFile,
                    converter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new AddToGroupTaskQueue(delegate, context);
    }

    private AddToGroupTaskQueue(ObjectQueue<AddItemToGroupTask> delegate,
            Context c) {
        super(delegate);

        mContext = c;
        BusProvider.getInstance().register(mContext);

        if (size() > 0) {
            startService();
        }
    }

    private void startService() {
        mContext.startService(new Intent(mContext,
                    AddToGroupTaskQueueService.class));
    }

    @Override
    public void add(AddItemToGroupTask entry) {
        super.add(entry);
        BusProvider.getInstance().post(produceSizeChanged());
        startService();
    }

    @Override
    public void remove() {
        super.remove();
        BusProvider.getInstance().post(produceSizeChanged());
    }

    @Produce
    public TaskQueueSizeEvent produceSizeChanged() {
        return new TaskQueueSizeEvent(size());
    }

    public static class TaskQueueSizeEvent {
        public final int size;

        public TaskQueueSizeEvent(int size) {
            this.size = size;
        }
    }
}
