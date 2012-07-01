package com.bourke.glimmr.fragments.home;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import com.bourke.glimmr.event.IPhotosetsReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.R;

import com.gmail.yuyang226.flickr.photosets.Photoset;
import com.gmail.yuyang226.flickr.photosets.Photosets;

/**
 *
 */
public class PhotosetsFragment extends BaseFragment
        implements IPhotosetsReadyListener {

    private static final String TAG = "Glimmr/PhotosetsFragment";

    private Photosets mPhotosets = new Photosets();

    public static PhotosetsFragment newInstance() {
        return new PhotosetsFragment();
    }

    @Override
    protected void startTask() {
        super.startTask();
        // TODO new LoadPhotosetsTask(mActivity, this).execute(mOAuth);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(R.layout
                .standard_list_fragment, container, false);
        return mLayout;
    }

    private void startPhotosetViewer(Photoset photoset) {
        if (photoset == null) {
            Log.e(TAG, "Cannot start SetViewerActivity, photoset is null");
            return;
        }
        Log.d(TAG, "Starting SetViewerActivity for " + photoset.getTitle());
        // TODO
    }

    public void itemClicked(AdapterView<?> parent, View view, int position,
            long id) {
        // TODO startSetViewer(mPhotosets.getPhotosets().get(position));
    }

    @Override
    public void onPhotosetsReady(Photosets photoSets, boolean cancelled) {
        log(TAG, "onPhotosetListReady");
        mGridAq = new AQuery(mActivity, mLayout);
        mPhotosets = (Photosets) photoSets;
        // TODO
    }
}
