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

    protected Activity mActivity;
	protected AQuery mGridAq;
    protected ViewGroup mLayout;
    protected PhotoList mPhotos = new PhotoList();
    protected OAuth mOAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getSherlockActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(R.layout.gridview_fragment,
                container, false);
        return mLayout;
    }

    /* The flickr Photo class isn't Serialisable, so construct a List of photo
     * urls to send it instead */
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
        bundle.putInt(Constants.KEY_PHOTO_LIST_INDEX, pos);
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
