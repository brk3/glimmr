package com.bourke.glimmr.fragments.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.bourke.glimmr.BuildConfig;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.model.FavoritesStreamModel;
import com.bourke.glimmr.model.IDataModel;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;

public class FavoritesGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/FavoritesGridFragment";

    private static final String KEY_NEWEST_FAVORITES_PHOTO_ID =
        "glimmr_newest_favorites_photo_id";
    private static final String KEY_USER =
            "com.bourke.glimmr.FavoritesGridFragment.KEY_USER";

    private User mUserToView;

    public static FavoritesGridFragment newInstance(User userToView) {
        FavoritesGridFragment f = new FavoritesGridFragment();
        f.mUserToView = userToView;
        return f;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataModel = FavoritesStreamModel.getInstance(mActivity, mOAuth, mUserToView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        new GsonHelper(mActivity).marshallObject(
                mUserToView, outState, KEY_USER);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && mUserToView == null) {
            String json = savedInstanceState.getString(KEY_USER);
            if (json != null) {
                mUserToView = new Gson().fromJson(json, User.class);
            } else {
                Log.e(TAG, "No stored user found in savedInstanceState");
            }
        }
    }

    /**
     * Once the parent binds the adapter it will trigger cacheInBackground
     * for us as it will be empty when first bound.
     */
    @Override
    protected boolean cacheInBackground() {
        super.startTask();
        mActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
        FavoritesStreamModel.getInstance(mActivity, mOAuth, mUserToView).fetchNextPage(this);
        return mMorePages;
    }

    @Override
    public String getNewestPhotoId() {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_NEWEST_FAVORITES_PHOTO_ID, null);
    }

    @Override
    public void storeNewestPhotoId(Photo photo) {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NEWEST_FAVORITES_PHOTO_ID, photo.getId());
        editor.commit();
        if (BuildConfig.DEBUG)
            Log.d(getLogTag(), "Updated most recent favorites photo id to " +
                photo.getId());
    }

    @Override
    protected int getModelType() {
        return IDataModel.TYPE_FAVORITES;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}
