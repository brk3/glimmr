package com.bourke.glimmr.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.preference.PreferenceManager;

import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import com.bourke.glimmr.activities.PhotoViewerActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.event.Events.IActivityItemsReadyListener;
import com.bourke.glimmr.event.Events.IPhotoInfoReadyListener;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadFlickrActivityTask;
import com.bourke.glimmr.tasks.LoadPhotoInfoTask;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import com.googlecode.flickrjandroid.activity.Event;
import com.googlecode.flickrjandroid.activity.Item;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.photos.Photo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.jakewharton.notificationcompat2.NotificationCompat2;

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.*;
import java.util.Date;

/**
 * This class refers to Flickr activity such as comments, faves, etc., not
 * Android Activities.
 *
 * A new event will bump that event's item to the top of the list, but it
 * will not reorder the event list itself.
 */
public class ActivityNotificationHandler
        implements GlimmrNotificationHandler<String>,
                   IActivityItemsReadyListener {

    private static final String TAG = "Glimmr/ActivityNotificationHandler";

    private Context mContext;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mPrefsEditor;
    private OAuth mOAuth;

    public ActivityNotificationHandler(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        mPrefsEditor = mPrefs.edit();
    }

    @Override
    public void startTask(OAuth oauth) {
        mOAuth = oauth;
        new LoadFlickrActivityTask(this).execute(oauth);
    }

    @Override
    public void onItemListReady(List<Item> items) {
        if (items != null) {
            if (Constants.DEBUG) {
                Log.d(TAG, "onItemListReady: items.size: " + items.size());
            }
            checkForNewItemEvents(items);
            storeItemList(mContext, items);
        }
    }

    @Override
    public boolean enabledInPreferences() {
        SharedPreferences defaultSharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(mContext);
        return defaultSharedPrefs.getBoolean(
                Constants.KEY_ENABLE_ACTIVITY_NOTIFICATIONS, false);
    }

    /**
     * The item at the head of the list is the latest.  Check if it's
     * either new, or has updated events.
     */
    private void checkForNewItemEvents(List<Item> fetchedItems) {
        assertNotNull(fetchedItems != null);
        assertTrue(!fetchedItems.isEmpty());

        if (Constants.DEBUG) {
            Log.d(TAG, "Loading existing item list and comparing it against " +
                    "the ones we just fetched");
        }
        List<Item> currentItems = loadItemList(mContext);
        if (currentItems == null || currentItems.isEmpty()) {
            if (Constants.DEBUG) {
                Log.d(TAG, "checkForNewItemEvents: couldn't load any " +
                        "existing items to compare against");
            }
            return;
        }

        /* (NOTE: we could look the fetchedItems here to check for other new or
         * updated ones.  But would need to be careful that wouldn't spam a lot
         * of noticiations so will just check the latest for now.) */
        Item fetchedItem = fetchedItems.get(0);
        List<Event> newEvents = (List<Event>)fetchedItem.getEvents();
        Event latestEvent = newEvents.get(newEvents.size()-1);
        String latestEventId = ""+latestEvent.getDateadded().getTime();
        String latestIdNotifiedAbout = getLatestIdNotifiedAbout();
        boolean isNewItem = true;

        for (Item curItem : currentItems) {
            if (fetchedItem.getId().equals(curItem.getId())) {
                isNewItem = false;
                List<Event> curEvents = (List<Event>)curItem.getEvents();
                if (newEvents.size() > curEvents.size()) {
                    /* we have new events */
                    if (Constants.DEBUG) {
                        Log.d(TAG, "Found update to existing item " +
                                fetchedItem.getTitle());
                    }
                    if (!latestIdNotifiedAbout.equals(latestEventId)) {
                        showNotification(fetchedItem, curEvents.size());
                        storeLatestIdNotifiedAbout(latestEventId);
                    }
                }
                break;
            }
        }

        if (isNewItem) {
            if (Constants.DEBUG) {
                Log.d(TAG, "Found new item " + fetchedItem.getTitle());
            }
            if (!latestIdNotifiedAbout.equals(latestEventId)) {
                showNotification(fetchedItem, 0);
                storeLatestIdNotifiedAbout(latestEventId);
            }
        }
    }

    private void showNotification(final Item item, final int eventOffset) {
        if (Constants.DEBUG) {
            Log.d(TAG, "showNotification for " + item.getTitle() +
                    ", starting ajax task to fetch the item image");
        }

        /* fetch the photo the item refers to */
        new LoadPhotoInfoTask(new IPhotoInfoReadyListener() {
            @Override
            public void onPhotoInfoReady(final Photo photo) {
                /* fetch the photo bitmap to be shown in the notication picture */
                String url = photo.getMediumUrl();
                new AQuery(mContext).ajax(url, Bitmap.class,
                        new AjaxCallback<Bitmap>() {
                    @Override
                    public void callback(final String url, final Bitmap bitmap,
                            final AjaxStatus status) {
                        /* we now have enough info to show the notification */
                        onItemPhotoReady(item, photo, bitmap, eventOffset);
                    }
                });
            }
        }, item.getId(), item.getSecret()).execute(mOAuth);
    }

    private void onItemPhotoReady(Item item, Photo photo, Bitmap bitmap,
            int eventOffset) {
        if (Constants.DEBUG) Log.d(TAG, "onItemPhotoReady");

        // TODO: add to strings
        String tickerText = "New activity on your photo " + item.getTitle();
        String titleText = item.getTitle();

        /* build the notification content text from the item events */
        StringBuilder contentText = new StringBuilder();
        List<Event> events = (List<Event>) item.getEvents();
        List<Event> newEvents = events.subList(eventOffset, events.size());
        if (Constants.DEBUG) Log.d(TAG, "newEvents.size: " + newEvents.size());
        for (int i=0; i<newEvents.size(); i++) {
            Event e = newEvents.get(i);
            if ("comment".equals(e.getType())) {
                // TODO: add to strings
                contentText.append(e.getUsername() + " added a comment");
                if (i < newEvents.size()-1 && newEvents.size() > 1) {
                    contentText.append(", ");
                }
            } else if ("fave".equals(e.getType())) {
                // TODO: add to strings
                contentText.append(e.getUsername() + " favorited");
                if (i < newEvents.size()-1 && newEvents.size() > 1) {
                    contentText.append(", ");
                }
            }
            if (i == 1 && i < newEvents.size()) {
                contentText.append("+ " + (newEvents.size()-2) + " others");
                break;
            }
        }

        /* finally, show the notification itself */
        int smallIcon = R.drawable.ic_social_chat_dark;
        if ("fave".equals(newEvents.get(0).getType())) {
            smallIcon = R.drawable.ic_action_rating_important_dark;
        }
        Notification n = getNotification(tickerText, titleText,
                contentText.toString(), newEvents.size(), smallIcon,
                bitmap, photo);
        final NotificationManager mgr = (NotificationManager)
            mContext.getSystemService(WakefulIntentService
                    .NOTIFICATION_SERVICE);
        mgr.notify(Constants.NOTIFICATION_NEW_ACTIVITY, n);
    }

    private Notification getNotification(final String tickerText,
            final String titleText, final String contentText,
            int number, int smallIcon, Bitmap image, Photo photo) {
        return new NotificationCompat2.BigPictureStyle(
                new NotificationCompat2.Builder(mContext)
                .setContentTitle(titleText)
                .setContentText(contentText)
                .setNumber(number)
                .setTicker(tickerText)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(getPendingIntent(photo))
                .setLargeIcon(BitmapFactory.decodeResource(
                        mContext.getResources(), R.drawable.ic_notification))
                .setSmallIcon(smallIcon))
            .bigPicture(image)
            .setSummaryText(contentText)
            .setBigContentTitle(titleText)
            .build();
    }

    /**
     * Passed to NotificationCompat2.Builder.setContentIntent to start
     * MainActivity when the notification is pressed.
     */
    private PendingIntent getPendingIntent(Photo photo) {
        List<Photo> photos = new ArrayList<Photo>();
        photos.add(photo);
        GsonHelper gsonHelper = new GsonHelper(mContext);
        gsonHelper.marshallObject(photos, Constants.PHOTOVIEWER_LIST_FILE);

        Intent photoViewer = new Intent(mContext, PhotoViewerActivity.class);
        photoViewer.putExtra(Constants.KEY_PHOTOVIEWER_START_INDEX, 0);
        photoViewer.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(mContext, 0, photoViewer, 0);
    }

    @Override
    public String getLatestIdNotifiedAbout() {
        String newestId = mPrefs.getString(
                Constants.NOTIFICATION_NEWEST_ACTIVITY_EVENT_ID, "");
        if (Constants.DEBUG) {
            Log.d(TAG, "getLatestIdNotifiedAbout: " + newestId);
        }
        return newestId;
    }

    @Override
    public void storeLatestIdNotifiedAbout(String eventId) {
        mPrefsEditor.putString(Constants.NOTIFICATION_NEWEST_ACTIVITY_EVENT_ID,
                eventId);
        mPrefsEditor.commit();
        if (Constants.DEBUG) {
            Log.d(TAG, "Updated most event id notified about to " + eventId);
        }
    }

    public static void storeItemList(Context context, List<Item> items) {
        if (items == null) {
            Log.e(TAG, "storeItemList: items are null");
            return;
        }

        GsonHelper gson = new GsonHelper(context);
        boolean itemListStoreResult =
            gson.marshallObject(items, Constants.ACTIVITY_ITEMLIST_FILE);
        if (!itemListStoreResult) {
            Log.e(TAG, "Error marshalling itemlist");
            return;
        }

        /* Store the current time so the menudrawer can check if it's fresh */
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong(Constants.TIME_ACTIVITY_ITEMS_LAST_UPDATED,
                new Date().getTime());
        prefsEditor.commit();

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
