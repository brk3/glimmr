package com.bourke.glimmr.fragments.viewer;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import com.bourke.glimmr.event.ICommentsReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.R;

import com.gmail.yuyang226.flickr.photos.comments.Comment;
import com.gmail.yuyang226.flickr.photos.Photo;

import java.util.List;

public final class CommentsFragment extends BaseFragment
        implements ICommentsReadyListener {

    protected String TAG = "Glimmr/CommentsFragment";

    private Photo mPhoto = new Photo();
    private AQuery mAq;

    public static CommentsFragment newInstance(Photo photo) {
        CommentsFragment newFragment = new CommentsFragment();
        newFragment.mPhoto = photo;
        return newFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.comments_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);
        return mLayout;
    }

    @Override
    protected void startTask() {
        super.startTask();
        Log.d(TAG, "startTask()");
        //TODO new LoadCommentsTask(mActivity, this, mPhoto).execute(mOAuth);
    }

    public void onCommentsReady(List<Comment> comments, boolean cancelled) {
        Log.d(TAG, "onCommentsReady, comments.size(): " + comments.size());
    }
}
