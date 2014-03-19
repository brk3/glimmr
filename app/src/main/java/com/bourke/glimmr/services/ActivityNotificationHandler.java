package com.bourke.glimmr.services;

import com.bourke.glimmr.BuildConfig;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bourke.glimmr.R;
import com.bourke.glimmr.activities.PhotoViewerActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.event.Events;
import com.bourke.glimmr.event.Events.IActivityItemsReadyListener;
import com.bourke.glimmr.event.Events.IPhotoInfoReadyListener;
import com.bourke.glimmr.tasks.DownloadPhotoTask;
import com.bourke.glimmr.tasks.LoadFlickrActivityTask;
import com.bourke.glimmr.tasks.LoadPhotoInfoTask;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.googlecode.flickrjandroid.activity.Event;
import com.googlecode.flickrjandroid.activity.Item;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.photos.Photo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    private static final String KEY_NOTIFICATION_NEWEST_ACTIVITY_EVENT_ID =
        "glimmr_notification_newest_activity_event_id";
    public static final String KEY_TIME_ACTIVITY_ITEMS_LAST_UPDATED =
        "glimmr_time_activity_items_last_updated";
    public static final String ACTIVITY_ITEMLIST_FILE =
        "glimmr_activity_items.json";

    private final Context mContext;
    private final SharedPreferences mPrefs;
    private final SharedPreferences.Editor mPrefsEditor;
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
    public void onItemListReady(List<Item> items, Exception e) {
        if (items != null && !items.isEmpty()) {
            if (BuildConfig.DEBUG) {
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
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Loading existing item list and comparing it against " +
                    "the ones we just fetched");
        }
        if (fetchedItems == null || fetchedItems.isEmpty()) {
            Log.d(TAG, "fetchedItems null or empty, returning");
            return;
        }
        List<Item> currentItems = loadItemList(mContext);
        if (currentItems == null || currentItems.isEmpty()) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "checkForNewItemEvents: couldn't load any " +
                        "existing items to compare against");
            }
            return;
        }

        /* (NOTE: we could look at all the fetchedItems here to check for other
         * new or updated ones.  But would need to be careful that wouldn't
         * spam a lot of notifications so will just check the latest for now.)
         * */
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
                    if (BuildConfig.DEBUG) {
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
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Found new item " + fetchedItem.getTitle());
            }
            if (!latestIdNotifiedAbout.equals(latestEventId)) {
                showNotification(fetchedItem, 0);
                storeLatestIdNotifiedAbout(latestEventId);
            }
        }
    }

    private void showNotification(final Item item, final int eventOffset) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "showNotification for " + item.getTitle() +
                    ", starting ajax task to fetch the item image");
        }

        /* fetch the photo the item refers to */
        new LoadPhotoInfoTask(new IPhotoInfoReadyListener() {
            @Override
            public void onPhotoInfoReady(final Photo photo, Exception e) {
                /* fetch the photo bitmap to be shown in the notification */
                String url = photo.getMediumUrl();
                new DownloadPhotoTask(mContext, new Events.IPhotoDownloadedListener() {
                    @Override
                    public void onPhotoDownloaded(Bitmap bitmap, Exception e) {
                        onItemPhotoReady(item, photo, bitmap, eventOffset);
                    }
                }, url).execute();
            }
        }, item.getId(), item.getSecret()).execute(mOAuth);
    }

    private void onItemPhotoReady(Item item, Photo photo, Bitmap bitmap,
            int eventOffset) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onItemPhotoReady");

        String tickerText = String.format("%s %s",
                mContext.getString(R.string.new_activity), item.getTitle());
        String titleText = item.getTitle();

        /* build the notification content text from the item events */
        StringBuilder contentText = new StringBuilder();
        List<Event> events = (List<Event>) item.getEvents();
        List<Event> newEvents = events.subList(eventOffset, events.size());
        if (BuildConfig.DEBUG) Log.d(TAG, "newEvents.size: " + newEvents.size());
        for (int i=0; i<newEvents.size(); i++) {
            Event e = newEvents.get(i);
            if ("comment".equals(e.getType())) {
                contentText.append(String.format("%s %s", e.getUsername(),
                           mContext.getString(R.string.added_a_comment)));
                if (i < newEvents.size()-1 && newEvents.size() > 1) {
                    contentText.append(", ");
                }
            } else if ("fave".equals(e.getType())) {
                contentText.append(String.format("%s %s", e.getUsername(),
                            mContext.getString(R.string.favorited)));
                if (i < newEvents.size()-1 && newEvents.size() > 1) {
                    contentText.append(", ");
                }
            }
            if (i == 1 && i < newEvents.size()) {
                contentText.append("+ ").append(newEvents.size() - 2)
                        .append(" others");
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
        return new NotificationCompat.BigPictureStyle(
                new NotificationCompat.Builder(mContext)
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
     * Passed to NotificationCompat.Builder.setContentIntent to start
     * MainActivity when the notification is pressed.
     */
    private PendingIntent getPendingIntent(Photo photo) {
        Intent photoViewer = new Intent(mContext, PhotoViewerActivity.class);
        photoViewer.setAction(PhotoViewerActivity.ACTION_VIEW_PHOTO_BY_ID);
        photoViewer.putExtra(PhotoViewerActivity.KEY_PHOTO_ID, photo.getId());
        photoViewer.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(mContext, 0, photoViewer, 0);
    }

    @Override
    public String getLatestIdNotifiedAbout() {
        String newestId = mPrefs.getString(
                KEY_NOTIFICATION_NEWEST_ACTIVITY_EVENT_ID, "");
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getLatestIdNotifiedAbout: " + newestId);
        }
        return newestId;
    }

    @Override
    public void storeLatestIdNotifiedAbout(String eventId) {
        mPrefsEditor.putString(KEY_NOTIFICATION_NEWEST_ACTIVITY_EVENT_ID,
                eventId);
        mPrefsEditor.commit();
        if (BuildConfig.DEBUG) {
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
            gson.marshallObject(items, ACTIVITY_ITEMLIST_FILE);
        if (!itemListStoreResult) {
            Log.e(TAG, "Error marshalling itemlist");
            return;
        }

        /* Store the current time so the menudrawer can check if it's fresh */
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong(KEY_TIME_ACTIVITY_ITEMS_LAST_UPDATED,
                (System.currentTimeMillis() / 1000L));
        prefsEditor.commit();

        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("Sucessfully wrote %d items to %s",
                    items.size(), ACTIVITY_ITEMLIST_FILE));
        }
    }

    public static List<Item> loadItemList(Context context) {
        List<Item> ret = new ArrayList<Item>();

        GsonHelper gson = new GsonHelper(context);
        String json = gson.loadJson(ACTIVITY_ITEMLIST_FILE);
        if (json.length() == 0) {
            Log.e(TAG, String.format("Error reading %s",
                        ACTIVITY_ITEMLIST_FILE));
            return ret;
        }
        Type collectionType = new TypeToken<Collection<Item>>(){}.getType();
        ret = new Gson().fromJson(json, collectionType);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("Sucessfully read %d items from %s",
                    ret.size(), ACTIVITY_ITEMLIST_FILE));
        }

        return ret;
    }
}
