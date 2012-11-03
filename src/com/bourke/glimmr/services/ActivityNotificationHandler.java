package com.bourke.glimmrpro.services;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.event.Events.IActivityItemsReadyListener;
import com.bourke.glimmrpro.tasks.LoadFlickrActivityTask;

import com.googlecode.flickrjandroid.activity.ItemList;
import com.googlecode.flickrjandroid.oauth.OAuth;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This class refers to Flickr activity such as comments, faves, etc., not
 * Android Activities.
 */
public class ActivityNotificationHandler
        implements GlimmrNotificationHandler, IActivityItemsReadyListener {

    private static final String TAG =
        "Glimmr/ActivityNotificationHandler";

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
    public void onItemListReady(ItemList items) {
        if (items != null) {
            Log.d(TAG, "onItemListReady: items.size: " + items.size());
            storeItemList(items);
        }
    }

    private void storeItemList(ItemList items) {
        if (items == null) {
            Log.e(TAG, "storeItemList: items are null");
            return;
        }
        try {
            FileOutputStream fos = mContext.openFileOutput(
                    Constants.ACTIVITY_ITEMLIST_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(items);
            oos.close();
            if (Constants.DEBUG) {
                Log.d(TAG, String.format("Sucessfully wrote %d items to %s",
                        items.size(), Constants.ACTIVITY_ITEMLIST_FILE));
            }
        } catch (IOException e) {
            Log.e(TAG, "storeItemList: error storing ItemList");
            e.printStackTrace();
        }
    }

    public static ItemList loadItemList(Context context) {
        ItemList ret = new ItemList();
        try {
            FileInputStream fis =
                context.openFileInput(Constants.ACTIVITY_ITEMLIST_FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            try {
                ret = (ItemList) ois.readObject();
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Couldn't read ItemList object from " +
                        Constants.ACTIVITY_ITEMLIST_FILE);
                e.printStackTrace();
            }
            ois.close();
            if (Constants.DEBUG) {
                Log.d(TAG, String.format("Sucessfully read %d items from %s",
                        ret.size(), Constants.ACTIVITY_ITEMLIST_FILE));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
