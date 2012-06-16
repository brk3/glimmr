package com.bourke.glimmr;

import android.app.Activity;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.FrameLayout;

import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;

import com.gmail.yuyang226.flickr.photos.Photo;
import android.widget.TextView;

public final class PhotoFragment extends SherlockFragment {

    private static final String TAG = "Glimmr/PhotoFragment";

	private AQuery aq;

    private Activity mActivity;
    private Photo mPhoto;

    public PhotoFragment(Photo photo) {
        mPhoto = photo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getSherlockActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        FrameLayout layout = (FrameLayout) inflater.inflate(R.layout
                .photoviewer_fragment, container, false);
		aq = new AQuery(mActivity, layout);
        if (mPhoto != null) {
            String url = mPhoto.getUrl();
            aq.id(R.id.web).progress(R.id.progress).webImage(url);
            //aq.id(R.id.text).text(url);
        } else {
            Log.e(TAG, "onStart, mPhoto is null");
        }
        return layout;
    }
}
