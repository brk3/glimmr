package com.bourke.glimmr.fragments.home;

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

import com.bourke.glimmr.activities.GroupViewerActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IGroupListReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadGroupsTask;

import com.gmail.yuyang226.flickr.groups.Group;
import com.gmail.yuyang226.flickr.groups.GroupList;

import java.util.ArrayList;

/**
 *
 */
public class GroupListFragment extends BaseFragment
        implements IGroupListReadyListener {

    private static final String TAG = "Glimmr/GroupListFragment";

    private GroupList mGroups = new GroupList();

    public static GroupListFragment newInstance() {
        return new GroupListFragment();
    }

    @Override
    protected void startTask() {
        super.startTask();
        new LoadGroupsTask(mActivity, this).execute(mOAuth);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(R.layout.list_fragment,
                container, false);
        return mLayout;
    }

    private void startGroupViewer(Group group) {
        if (group == null) {
            Log.e(getLogTag(),
                    "Cannot start GroupViewerActivity, group is null");
            return;
        }
        Log.d(getLogTag(), "Starting GroupViewerActivity for " +
                group.getName());
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_GROUPVIEWER_GROUP, group);
        Intent groupViewer = new Intent(mActivity, GroupViewerActivity
                .class);
        groupViewer.putExtras(bundle);
        mActivity.startActivity(groupViewer);
    }

    public void itemClicked(AdapterView<?> parent, View view, int position,
            long id) {
        startGroupViewer(mGroups.get(position));
    }

    @Override
    public void onGroupListReady(GroupList groups) {
        Log.d(getLogTag(), "onGroupListReady");
        mGridAq = new AQuery(mActivity, mLayout);
        mGroups = (GroupList) groups;

        ArrayAdapter<Group> adapter = new ArrayAdapter<Group>(mActivity,
                R.layout.group_list_row, (ArrayList<Group>)groups) {

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
                AQuery aq = mGridAq.recycle(convertView);

                aq.id(R.id.groupName).text(group.getName());
                aq.id(R.id.numImagesText).text(""+group.getPhotoCount());
                aq.id(R.id.groupIcon).image(group.getBuddyIconUrl(),
                        true, true, 0, 0, null, AQuery.FADE_IN_NETWORK);

                return convertView;
            }
        };
        mGridAq.id(R.id.list).adapter(adapter).itemClicked(this,
                "itemClicked");
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}
