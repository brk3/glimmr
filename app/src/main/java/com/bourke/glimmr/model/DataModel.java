package com.bourke.glimmr.model;

import android.content.Context;
import android.util.Log;

import com.bourke.glimmr.BuildConfig;
import com.bourke.glimmr.event.Events;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.ArrayList;
import java.util.List;

public abstract class DataModel {

    public static final String TAG = "Glimmr/DataModel";

    public static final int TYPE_PHOTOSTREAM = 0;

    // TODO: provide load/save methods for this data to tie to activity lifecycle
    protected static List<Photo> mPhotos = new ArrayList<Photo>();
    protected static int mPage = 1;
    protected static OAuth mOAuth;
    protected static Context mContext;

    public abstract void fetchNextPage(final Events.IPhotoListReadyListener listener);

    public abstract void save();

    public abstract void load();

    public List<Photo> getPhotos() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getPhotos(): " + mPhotos.size());
        }
        return mPhotos;
    }

    public void clear() {
        mPhotos.clear();
        mPage = 0;
    }

    public boolean isEmpty() {
        return mPhotos.isEmpty();
    }
}
