package com.bourke.glimmr.common;

import android.content.Context;
import com.bourke.glimmr.tape.GsonConverter;
import com.google.gson.Gson;
import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.FileObjectQueue.Converter;

import java.io.File;
import java.io.IOException;

public class TaskQueueDelegateFactory<T> {

    private final Context mContext;

    public TaskQueueDelegateFactory(Context context) {
        mContext = context;
    }

    public FileObjectQueue<T> get(String fileName, Class<T> taskType) {
        final Converter<T> converter =
            new GsonConverter<T>(new Gson(), taskType);
        final File queueFile = new File(mContext.getFilesDir(), fileName);
        FileObjectQueue<T> delegate = null;
        try {
            delegate = new FileObjectQueue<T>(queueFile, converter);
        } catch (IOException e) {
            // TODO: how should we handle this
            e.printStackTrace();
        }
        return delegate;
    }
}
