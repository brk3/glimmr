package com.bourke.glimmr.appwidget;

import com.bourke.glimmr.BuildConfig;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bourke.glimmr.R;
import com.bourke.glimmr.activities.PhotoViewerActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.common.OAuthUtils;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "Glimmr/StackWidgetService";

    /* For tasks that support pagination, just fetch the first page */
    private static final int PAGE = 0;

    private List<Photo> mPhotos;
    private final Context mContext;

    private int mWidgetType;

    private OAuth mOAuth;
    private User mUser;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
    }

    /**
     * In onCreate() you setup any connections / cursors to your data
     * source.
     * Heavy lifting, for example downloading or creating content
     * etc, should be deferred to onDataSetChanged() or getViewAt(). Taking
     * more than 20 seconds in this call will result in an ANR.
     */
    public void onCreate() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        mOAuth = OAuthUtils.loadAccessToken(mContext);
        mUser = null;
        if (OAuthUtils.isLoggedIn(mContext)) {
            mUser = mOAuth.getUser();
        }

        mPhotos = new ArrayList<Photo>();
        mWidgetType = StackViewWidgetConfigure.loadWidgetType(mContext);
    }

    /**
     * You can do heaving lifting in here, synchronously. For example, if you
     * need to process an image, fetch something from the network, etc., it is
     * ok to do it here, synchronously. A loading view will show up in lieu of
     * the actual contents in the interim.
     */
    public RemoteViews getViewAt(int position) {
        final RemoteViews rv = new RemoteViews(mContext.getPackageName(),
                R.layout.stackview_widget_item);

        if (mPhotos.size() == 0) {
            updatePhotos();
        }

        /* Fetch the photo synchronously */
        final Photo photo = mPhotos.get(position);
        Bitmap bitmap = null;
        try {
            bitmap = Picasso.with(mContext).load(photo.getSmallUrl()).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        rv.setImageViewBitmap(R.id.image_item, bitmap);

        /* Set the overlay views and owner info */
        rv.setViewVisibility(R.id.imageOverlay, View.VISIBLE);
        String viewsText = String.format("%s: %s",
                mContext.getString(R.string.views),
                String.valueOf(photo.getViews()));
        rv.setTextViewText(R.id.viewsText, viewsText);
        if (photo.getOwner() != null) {
            rv.setTextViewText(R.id.ownerText, photo.getOwner().getUsername());
        }

        /* Show ribbon in corner if photo is new */
        // TODO: create list of new photos to enable this
        //rv.setVisibility(R.id.imageNewRibbon, View.INVISIBLE);
        //if (mNewPhotos != null) {
            //for (Photo p : mNewPhotos) {
                //if (p.getId().equals(photo.getId())) {
                    //holder.imageNewRibbon.setVisibility(View.VISIBLE);
                //}
            //}
        //}

        /* Next, we set a fill-intent which will be used to fill-in the pending
         * intent template which is set on the collection view in
         * StackWidgetProvider. */
        Bundle extras = new Bundle();
        extras.putInt(StackWidgetProvider.VIEW_INDEX, position);
        extras.putString(PhotoViewerActivity.KEY_PHOTO_LIST_FILE,
                PhotoViewerActivity.PHOTO_LIST_FILE);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.image_layout, fillInIntent);

        return rv;
    }

    @Override
    public int getCount() {
        return mPhotos.size();
    }

    /**
     * This is triggered when you call AppWidgetManager
     * notifyAppWidgetViewDataChanged on the collection view corresponding
     * to this factory. You can do heaving lifting in here, synchronously.
     * For example, if you need to process an image, fetch something from
     * the network, etc., it is ok to do it here, synchronously. The widget
     * will remain in its current state while work is being done here, so
     * you don't need to worry about locking up the widget.
     */
    @Override
    public void onDataSetChanged() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onDataSetChanged");
        updatePhotos();
    }

    /**
     * You can create a custom loading view (for instance when getViewAt() is
     * slow.) If you return null here, you will get the default loading view.
     */
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDestroy() {
    }

    private void updatePhotos() {
        try {
            switch (mWidgetType) {
                default:
                case StackViewWidgetConfigure.WIDGET_TYPE_EXPORE:
                    mPhotos = getExplorePhotos();
                    break;

                case StackViewWidgetConfigure.WIDGET_TYPE_FAVORITES:
                    mPhotos = getFavoritePhotos();
                    break;

                case StackViewWidgetConfigure.WIDGET_TYPE_PHOTOS:
                    mPhotos = getUserPhotos();
                    break;

                case StackViewWidgetConfigure.WIDGET_TYPE_CONTACTS:
                    mPhotos = getContactsPhotos();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        new GsonHelper(mContext).marshallObject(mPhotos,
                PhotoViewerActivity.PHOTO_LIST_FILE);
    }

    private List<Photo> getExplorePhotos() throws Exception {
        Date day = null;
        return FlickrHelper.getInstance().getInterestingInterface().getList(
                day, Constants.EXTRAS, Constants.FETCH_PER_PAGE, PAGE);

    }

    private List<Photo> getFavoritePhotos() throws Exception {
        OAuthToken token = mOAuth.getToken();
        Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                token.getOauthToken(),
                token.getOauthTokenSecret());
        Date minFavDate = null;
        Date maxFavDate = null;
        return f.getFavoritesInterface().getList(mUser.getId(), minFavDate,
                maxFavDate, Constants.FETCH_PER_PAGE, PAGE, Constants.EXTRAS);
    }

    private List<Photo> getUserPhotos() throws Exception {
        OAuthToken token = mOAuth.getToken();
        Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                token.getOauthToken(), token.getOauthTokenSecret());
        return f.getPeopleInterface().getPhotos(mUser.getId(),
                Constants.EXTRAS, Constants.FETCH_PER_PAGE, PAGE);
    }

    private List<Photo> getContactsPhotos() throws Exception {
        OAuthToken token = mOAuth.getToken();
        Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                token.getOauthToken(), token.getOauthTokenSecret());
        boolean justFriends = false;
        boolean singlePhoto = false;
        boolean includeSelf = false;
        return f.getPhotosInterface().getContactsPhotos(
                Constants.FETCH_PER_PAGE, Constants.EXTRAS,
                justFriends, singlePhoto, includeSelf, PAGE,
                Constants.FETCH_PER_PAGE);
    }
}
