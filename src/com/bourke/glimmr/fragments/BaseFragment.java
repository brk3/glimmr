package com.bourke.glimmr;

import android.app.Activity;

import android.content.Intent;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.people.User;
import com.gmail.yuyang226.flickr.photos.Photo;
import com.gmail.yuyang226.flickr.photos.PhotoList;

import java.util.ArrayList;

/**
 *
 */
public abstract class BaseFragment extends SherlockFragment {

    private static final String TAG = "Glimmr/BaseFragment";

    /**
     * It's useful to keep a reference to the parent activity in our fragments.
     */
    protected Activity mActivity;

    /**
     * Most Glimmr fragments deal with a list of photos.
     */
    protected PhotoList mPhotos = new PhotoList();

    /**
     * Should contain current user and valid access token for that user.
     */
    protected OAuth mOAuth;

	protected AQuery mGridAq;
    protected ViewGroup mLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getSherlockActivity();
    }

    /**
     * Start the PhotoViewerActivity with a list of photos to view and an index
     * to start at in the list.
     *
     * Unfortunately can't use a PhotoList here as Photo isn't serialisable.
     */
    public void startPhotoViewer(int pos) {
        if (mPhotos == null) {
            Log.e(TAG, "Cannot start PhotoViewer, mPhotos is null");
            return;
        }
        ArrayList<String> photoUrls = new ArrayList<String>();
        for (Photo p : mPhotos) {
            photoUrls.add(p.getLargeUrl());
        }
        Log.d(TAG, "starting photo viewer with " + photoUrls.size() + " ids");
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_PHOTOVIEWER_LIST, photoUrls);
        bundle.putInt(Constants.KEY_PHOTOVIEWER_START_INDEX, pos);
        Intent photoViewer = new Intent(mActivity, PhotoViewerActivity.class);
        photoViewer.putExtras(bundle);
        mActivity.startActivity(photoViewer);
    }

    public void startProfileViewer(User user) {
        if (user == null) {
            Log.e(TAG, "Cannot start ProfileActivity, user is null");
            return;
        }
        Log.d(TAG, "Starting ProfileActivity for " + user.getUsername());
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_PROFILEVIEWER_USER, user);
        Intent profileViewer = new Intent(mActivity, ProfileActivity.class);
        profileViewer.putExtras(bundle);
        mActivity.startActivity(profileViewer);
    }
}
