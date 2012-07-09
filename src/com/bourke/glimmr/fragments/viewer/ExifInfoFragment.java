package com.bourke.glimmr.fragments.viewer;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ScrollView;

import com.androidquery.AQuery;

import com.bourke.glimmr.event.IExifInfoReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadExifInfoTask;

import com.gmail.yuyang226.flickr.photos.Exif;
import com.gmail.yuyang226.flickr.photos.Photo;

import java.util.List;

public final class ExifInfoFragment extends BaseFragment
        implements IExifInfoReadyListener {

    protected String TAG = "Glimmr/ExifInfoFragment";

    private Photo mPhoto = new Photo();
    private AQuery mAq;

    public static ExifInfoFragment newInstance(Photo photo) {
        ExifInfoFragment photoFragment = new ExifInfoFragment();
        photoFragment.mPhoto = photo;
        return photoFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (ScrollView) inflater.inflate(
                R.layout.exif_info_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);
        return mLayout;
    }

    @Override
    protected void startTask() {
        super.startTask();
        new LoadExifInfoTask(mActivity, this, mPhoto).execute(mOAuth);
    }

    public void onExifInfoReady(List<Exif> exifInfo, boolean cancelled) {
        Log.d(TAG, "onExifInfoReady, exifInfo.size(): " + exifInfo.size());
    }
}
