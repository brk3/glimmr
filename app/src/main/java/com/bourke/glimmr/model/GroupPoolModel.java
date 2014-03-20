package com.bourke.glimmr.model;

import android.content.Context;
import android.util.Log;

import com.bourke.glimmr.BuildConfig;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.event.Events;
import com.bourke.glimmr.tasks.LoadGroupPoolTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.photos.Photo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupPoolModel implements IDataModel {

    private static GroupPoolModel ourInstance = new GroupPoolModel();

    public static final String TAG = "Glimmr/GroupPoolModel";

    public static final String PHOTO_LIST_FILE = "GroupPoolModel_photolist.json";

    private static List<Photo> mPhotos = new ArrayList<Photo>();
    private static int mPage = 1;
    private static OAuth mOAuth;
    private static Context mContext;
    private static Group mGroup;

    public static GroupPoolModel getInstance(Context context, OAuth oauth, Group group) {
        mContext = context;
        mOAuth = oauth;
        mGroup = group;
        ourInstance.load();
        return ourInstance;
    }


    public static GroupPoolModel getInstance(Context context) {
        mContext = context;
        ourInstance.load();
        return ourInstance;
    }

    private GroupPoolModel() {
    }

    @Override
    public synchronized void fetchNextPage(final Events.IPhotoListReadyListener listener) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "fetchNextPage: mPage=" + mPage); }
        new LoadGroupPoolTask(new Events.IPhotoListReadyListener() {
            /* update our list before calling the original callback */
            @Override
            public void onPhotosReady(List<Photo> photos, Exception e) {
                if (photos != null) {
                    mPhotos.addAll(photos);
                }
                listener.onPhotosReady(photos, e);
            }
        }, mGroup, mPage++).execute(mOAuth);
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