package com.bourke.glimmrpro.fragments.home;

import android.content.Intent;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import com.bourke.glimmrpro.activities.BaseActivity;
import com.bourke.glimmrpro.activities.GroupViewerActivity;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.event.Events.IGroupListReadyListener;
import com.bourke.glimmrpro.fragments.base.BaseFragment;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.tasks.LoadGroupsTask;

import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.groups.GroupList;
import com.googlecode.flickrjandroid.people.User;

import java.util.ArrayList;

/**
 *
 */
public class GroupListFragment extends BaseFragment
        implements IGroupListReadyListener {

    private static final String TAG = "Glimmr/GroupListFragment";

    private GroupList mGroups = new GroupList();
    private LoadGroupsTask mTask;

    public static GroupListFragment newInstance() {
        GroupListFragment newFragment = new GroupListFragment();
        return newFragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTask != null) {
            mTask.cancel(true);
            if (Constants.DEBUG)
                Log.d(TAG, "onPause: cancelling task");
        }
    }

    @Override
    protected void startTask() {
        super.startTask();
        mTask = new LoadGroupsTask(this, this);
        mTask.execute(mOAuth);
    }

    @Override
    protected void refresh() {
        super.refresh();
        startTask();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.group_list_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);
        return mLayout;
    }

    private void startGroupViewer(Group group) {
        if (group == null) {
            if (Constants.DEBUG)
                Log.e(getLogTag(),
                    "Cannot start GroupViewerActivity, group is null");
            return;
        }
        if (Constants.DEBUG)
            Log.d(getLogTag(), "Starting GroupViewerActivity for " +
                group.getName());
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_GROUPVIEWER_GROUP, group);
        bundle.putSerializable(Constants.KEY_GROUPVIEWER_USER,
                mActivity.getUser());
        Intent groupViewer = new Intent(mActivity, GroupViewerActivity.class);
        groupViewer.putExtras(bundle);
        mActivity.startActivity(groupViewer);
    }

    public void itemClicked(AdapterView<?> parent, View view, int position,
            long id) {
        startGroupViewer(mGroups.get(position));
    }

    @Override
    public void onGroupListReady(GroupList groups) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onGroupListReady");

        if (groups == null) {
            mAq.id(R.id.no_connection_layout).visible();
            mAq.id(R.id.list).gone();
        } else {
            mAq.id(R.id.list).visible();
            mAq.id(R.id.no_connection_layout).gone();
            mGroups = (GroupList) groups;
            GroupListAdapter adapter = new GroupListAdapter(mActivity,
                    R.layout.group_list_row, (ArrayList<Group>)groups);
            mAq.id(R.id.list).adapter(adapter).itemClicked(this,
                    "itemClicked");
        }
        mAq.id(android.R.id.empty).invisible();
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    class GroupListAdapter extends ArrayAdapter<Group> {
        public GroupListAdapter(BaseActivity activity, int textViewResourceId,
                ArrayList<Group> objects) {
            super(activity, textViewResourceId, objects);
        }

        // TODO: implement ViewHolder pattern
        // TODO: add aquery delay loading for fling scrolling
        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            if (convertView == null) {
                convertView = mActivity.getLayoutInflater().inflate(
                        R.layout.group_list_row, null);
            }

            final Group group = getItem(position);
            AQuery aq = mAq.recycle(convertView);

            aq.id(R.id.groupName).text(group.getName());
            aq.id(R.id.numImagesText).text(""+group.getPhotoCount());
            aq.id(R.id.groupIcon).image(group.getBuddyIconUrl(),
                    true, true, 0, 0, null, AQuery.FADE_IN_NETWORK);

            return convertView;
        }
    }
}
