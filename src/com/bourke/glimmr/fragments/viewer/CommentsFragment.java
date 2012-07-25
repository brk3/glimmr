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

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.PrettyDate;
import com.bourke.glimmr.event.Events.ICommentsReadyListener;
import com.bourke.glimmr.event.Events.IUserReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadCommentsTask;
import com.bourke.glimmr.tasks.LoadUserTask;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.comments.Comment;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public final class CommentsFragment extends BaseFragment
        implements ICommentsReadyListener, IUserReadyListener {

    protected String TAG = "Glimmr/CommentsFragment";

    private Photo mPhoto = new Photo();
    private AQuery mAq;

    private ArrayAdapter<Comment> mAdapter;

    private Map<String, UserItem> mUsers = Collections.synchronizedMap(
            new HashMap<String, UserItem>());

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
        Log.d(getLogTag(), "startTask()");
        new LoadCommentsTask(mActivity, this, mPhoto).execute(mOAuth);
    }

    public void itemClicked(AdapterView<?> parent, View view, int position,
            long id) {
        // TODO
    }

    @Override
    public void onUserReady(User user) {
        Log.d(getLogTag(), "onUserReady: " + user.getId());
        mUsers.put(user.getId(), new UserItem(user, false));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCommentsReady(List<Comment> comments) {
        Log.d(getLogTag(), "onCommentsReady, comments.size(): "
                + comments.size());
        mGridAq = new AQuery(mActivity, mLayout);

        mAdapter = new ArrayAdapter<Comment>(mActivity,
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

                UserItem author = mUsers.get(comment.getAuthor());
                if (author == null) {
                    mUsers.put(comment.getAuthor(), new UserItem(null, true));
                    new LoadUserTask(mActivity, CommentsFragment.this,
                            comment.getAuthor()).execute(mOAuth);
                } else {
                    if (!author.isLoading) {
                        aq.id(R.id.userIcon).image(
                                author.user.getBuddyIconUrl(),
                                Constants.USE_MEMORY_CACHE,
                                Constants.USE_FILE_CACHE, 0, 0, null,
                                AQuery.FADE_IN_NETWORK);
                    }
                }

                return convertView;
            }
        };
        mGridAq.id(R.id.list).adapter(mAdapter).itemClicked(this,
                "itemClicked");
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    class UserItem {
        public User user;
        public boolean isLoading = true;

        public UserItem(User user, boolean isLoading) {
            this.user = user;
            this.isLoading = isLoading;
        }
    }
}
