package com.bourke.glimmr.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.bourke.glimmr.event.Events;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class DownloadPhotoTask extends AsyncTask<Void, Void, Bitmap> {

    private static final String TAG = "Glimmr/AddCommentTask";

    private final Events.IPhotoDownloadedListener mListener;
    private Context mContext;
    private final String mUrl;
    private Exception mException;

    public DownloadPhotoTask(Context context, Events.IPhotoDownloadedListener listener,
                             String url) {
        mContext = context;
        mListener = listener;
        mUrl = url;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        Bitmap bitmap = null;
        try {
            /* get the image synchronously */
            bitmap = Picasso.with(mContext).load(mUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
            mException = e;
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(final Bitmap result) {
        mListener.onPhotoDownloaded(result, mException);
    }
}
