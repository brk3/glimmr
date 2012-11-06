package com.bourke.glimmrpro.services;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.GsonHelper;
import com.bourke.glimmrpro.event.Events.IActivityItemsReadyListener;
import com.bourke.glimmrpro.tasks.LoadFlickrActivityTask;

import com.googlecode.flickrjandroid.activity.Item;
import com.googlecode.flickrjandroid.oauth.OAuth;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class refers to Flickr activity such as comments, faves, etc., not
 * Android Activities.
 */
public class ActivityNotificationHandler
        implements GlimmrNotificationHandler, IActivityItemsReadyListener {

    private static final String TAG = "Glimmr/ActivityNotificationHandler";

    private Context mContext;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mPrefsEditor;

    public ActivityNotificationHandler(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        mPrefsEditor = mPrefs.edit();
    }

    @Override
    public void startTask(OAuth oauth) {
        new LoadFlickrActivityTask(this).execute(oauth);
    }

    @Override
    public void onItemListReady(List<Item> items) {
        if (items != null) {
            Log.d(TAG, "onItemListReady: items.size: " + items.size());
            storeItemList(items);
        }
    }

    private void storeItemList(List<Item> items) {
        if (items == null) {
            Log.e(TAG, "storeItemList: items are null");
            return;
        }

        GsonHelper gson = new GsonHelper(mContext);

        boolean itemListStoreResult =
            gson.marshallObject(items, Constants.ACTIVITY_ITEMLIST_FILE);
        if (!itemListStoreResult) {
            Log.e(TAG, "Error marshalling itemlist");
            return;
        }

        if (Constants.DEBUG) {
            Log.d(TAG, String.format("Sucessfully wrote %d items to %s",
                    items.size(), Constants.ACTIVITY_ITEMLIST_FILE));
        }
    }

    public static List<Item> loadItemList(Context context) {
        List<Item> ret = new ArrayList<Item>();

        GsonHelper gson = new GsonHelper(context);
        String json = gson.loadJson(Constants.ACTIVITY_ITEMLIST_FILE);
        if (json.length() == 0) {
            Log.e(TAG, String.format("Error reading %s",
                        Constants.ACTIVITY_ITEMLIST_FILE));
            return ret;
        }
        Type collectionType = new TypeToken<Collection<Item>>(){}.getType();
        ret = new Gson().fromJson(json.toString(), collectionType);

        if (Constants.DEBUG) {
            Log.d(TAG, String.format("Sucessfully read %d items from %s",
                    ret.size(), Constants.ACTIVITY_ITEMLIST_FILE));
        }

        return ret;
    }
}
