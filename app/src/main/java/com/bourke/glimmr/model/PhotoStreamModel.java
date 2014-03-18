package com.bourke.glimmr.model;

import android.content.Context;
import android.util.Log;

import com.bourke.glimmr.BuildConfig;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.event.Events;
import com.bourke.glimmr.tasks.LoadPhotostreamTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public class PhotoStreamModel extends DataModel {
    private static PhotoStreamModel ourInstance = new PhotoStreamModel();

    public static final String TAG = "Glimmr/PhotoStreamModel";

    public static final String PHOTO_LIST_FILE = "PhotoStreamModel_photolist.json";

    private static User mUserToView;

    public static PhotoStreamModel getInstance(Context context, OAuth oauth, User userToView) {
        mContext = context;
        mOAuth = oauth;
        mUserToView = userToView;
        return ourInstance;
    }

    public static PhotoStreamModel getInstance(Context context) {
        mContext = context;
        ourInstance.load();
        return ourInstance;
    }

    private PhotoStreamModel() {
    }

    @Override
    public synchronized void fetchNextPage(final Events.IPhotoListReadyListener listener) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "fetchNextPage: mPage=" + mPage); }
        new LoadPhotostreamTask(new Events.IPhotoListReadyListener() {
            /* update our list before calling the original callback */
            @Override
            public void onPhotosReady(List<Photo> photos, Exception e) {
                if (photos != null) {
                    mPhotos.addAll(photos);
                }
                listener.onPhotosReady(photos, e);
            }
        }, mUserToView, mPage++).execute(mOAuth);
    }

    @Override
    public synchronized void save() {

        if (!new GsonHelper(mContext).marshallObject(mPhotos, PHOTO_LIST_FILE)) {
            Log.e(TAG, "Error marshalling mPhotos to gson");
        }
    }

    @Override
    public synchronized void load() {
        GsonHelper gsonHelper = new GsonHelper(mContext);
        String json = gsonHelper.loadJson(PHOTO_LIST_FILE);
        if (json.length() > 0) {
            Type collectionType = new TypeToken<Collection<Photo>>(){}.getType();
            mPhotos.clear();
            mPhotos.addAll((List<Photo>)new Gson().fromJson(json, collectionType));
        } else {
            Log.e(TAG, String.format("Error reading '%s'", PHOTO_LIST_FILE));
        }
    }
}
