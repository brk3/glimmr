package com.bourke.glimmr;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import com.gmail.yuyang226.flickr.photosets.Photoset;
import com.gmail.yuyang226.flickr.photosets.Photosets;

/**
 *
 */
public class PhotosetListFragment extends BaseFragment
        implements IPhotosetListReadyListener {

    private static final String TAG = "Glimmr/PhotosetListFragment";

    private Photosets mPhotosets = new Photosets();

    public static PhotosetListFragment newInstance() {
        return new PhotosetListFragment();
    }

    @Override
    protected void startTask() {
        super.startTask();
        // TODO new LoadSetsTask(mActivity, this).execute(mOAuth);
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
        // TODO startSetViewer(mPhotosets.get(position));
    }

    @Override
    public void onPhotosetListReady(Photosets photoSets, boolean cancelled) {
        log(TAG, "onPhotosetListReady");
        mGridAq = new AQuery(mActivity, mLayout);
        mPhotosets = (Photosets) photoSets;
        // TODO
    }
}
