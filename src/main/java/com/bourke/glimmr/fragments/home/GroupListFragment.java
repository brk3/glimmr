package com.bourke.glimmrpro.fragments.home;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;

import com.bourke.glimmrpro.activities.BaseActivity;
import com.bourke.glimmrpro.activities.GroupViewerActivity;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.TextUtils;
import com.bourke.glimmrpro.event.Events.IGroupListReadyListener;
import com.bourke.glimmrpro.fragments.base.BaseFragment;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.tasks.LoadGroupsTask;

import com.googlecode.flickrjandroid.groups.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupListFragment extends BaseFragment
        implements IGroupListReadyListener {

    private static final String TAG = "Glimmr/GroupListFragment";

    private List<Group> mGroups = new ArrayList<Group>();
    private LoadGroupsTask mTask;
    private ViewGroup mNoConnectionLayout;
    private AdapterView mListView;
    private GroupListAdapter mAdapter;

    public static GroupListFragment newInstance() {
        GroupListFragment newFragment = new GroupListFragment();
        return newFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.group_list_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);
        mNoConnectionLayout =
            (ViewGroup) mLayout.findViewById(R.id.no_connection_layout);
        mListView = (AdapterView) mLayout.findViewById(R.id.list);
        return mLayout;
    }

    @Override
    public void onPause() {
        super.onPause();
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
        if (mTask != null) {
            mTask.cancel(true);
            if (Constants.DEBUG)
                Log.d(TAG, "onPause: cancelling task");
        }
    }

    @Override
    protected void startTask() {
        super.startTask();
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        mTask = new LoadGroupsTask(this);
        mTask.execute(mOAuth);
    }

    @Override
    public void onGroupListReady(List<Group> groups) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onGroupListReady");

        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
        if (groups == null) {
            mNoConnectionLayout.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            mListView.setVisibility(View.VISIBLE);
            mNoConnectionLayout.setVisibility(View.GONE);
            mGroups = (List<Group>) groups;
            mAdapter = new GroupListAdapter(mActivity, R.layout.group_list_row,
                    (ArrayList<Group>) groups);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    GroupViewerActivity.startGroupViewer(mActivity,
                        mGroups.get(position));
                }
            });
        }
    }

    /**
     * Override as no pagination
     */
    @Override
    protected void refresh() {
        mGroups.clear();
        mAdapter.notifyDataSetChanged();
        startTask();
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

        // TODO: add aquery delay loading for fling scrolling
        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mActivity.getLayoutInflater().inflate(
                        R.layout.group_list_row, null);
                holder = new ViewHolder();
                holder.textViewGroupName = (TextView)
                    convertView.findViewById(R.id.groupName);
                holder.textViewNumImages = (TextView)
                    convertView.findViewById(R.id.numImagesText);
                holder.imageViewGroupIcon = (ImageView)
                    convertView.findViewById(R.id.groupIcon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Group group = getItem(position);

            holder.textViewGroupName.setText(group.getName());
            mTextUtils.setFont(holder.textViewGroupName,
                    TextUtils.FONT_ROBOTOBOLD);

            holder.textViewNumImages.setText(""+group.getPhotoCount());
            mAq.id(holder.imageViewGroupIcon).image(group.getBuddyIconUrl(),
                    true, true, 0, 0, null, AQuery.FADE_IN_NETWORK);

            return convertView;
        }

        class ViewHolder {
            TextView textViewGroupName;
            TextView textViewNumImages;
            ImageView imageViewGroupIcon;
        }
    }
}
