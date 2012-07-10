package com.bourke.glimmr.fragments.viewer;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import com.bourke.glimmr.common.PrettyDate;
import com.bourke.glimmr.event.ICommentsReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadCommentsTask;

import com.gmail.yuyang226.flickr.people.User;
import com.gmail.yuyang226.flickr.photos.comments.Comment;
import com.gmail.yuyang226.flickr.photos.Photo;

import java.util.ArrayList;
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
                R.layout.list_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);
        return mLayout;
    }

    @Override
    protected void startTask() {
        super.startTask();
        Log.d(TAG, "startTask()");
        new LoadCommentsTask(mActivity, this, mPhoto).execute(mOAuth);
    }

    public void itemClicked(AdapterView<?> parent, View view, int position,
            long id) {
        // TODO
    }

    public void onCommentsReady(List<Comment> comments, boolean cancelled) {
        Log.d(TAG, "onCommentsReady, comments.size(): " + comments.size());
        mGridAq = new AQuery(mActivity, mLayout);

        ArrayAdapter<Comment> adapter = new ArrayAdapter<Comment>(mActivity,
                R.layout.comment_list_row, (ArrayList<Comment>) comments) {

            // TODO: implement ViewHolder pattern
            // TODO: add aquery delay loading for fling scrolling
            @Override
            public View getView(final int position, View convertView,
                    ViewGroup parent) {

                if (convertView == null) {
                    convertView = mActivity.getLayoutInflater().inflate(
                            R.layout.comment_list_row, null);
                }

                final Comment comment = getItem(position);
                AQuery aq = mGridAq.recycle(convertView);

                // TODO: if your username replace with "You"
                aq.id(R.id.userName).text(comment.getAuthorName());
                aq.id(R.id.commentDate).text(new PrettyDate(comment
                            .getDateCreate()).toString());
                aq.id(R.id.commentText).text(comment.getText());
                // TODO aq.id(R.id.userIcon).image(group.getBuddyIconUrl(),
                //        true, true, 0, 0, null, AQuery.FADE_IN_NETWORK);

                return convertView;
            }
        };
        mGridAq.id(R.id.list).adapter(adapter).itemClicked(this,
                "itemClicked");
    }
}
