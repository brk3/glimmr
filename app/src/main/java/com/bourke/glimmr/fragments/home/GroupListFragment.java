package com.bourke.glimmr.fragments.home;

import com.bourke.glimmr.BuildConfig;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bourke.glimmr.R;
import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.activities.GroupViewerActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.event.Events.GroupItemLongClickDialogListener;
import com.bourke.glimmr.event.Events.IGroupListReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.fragments.group.AddToGroupDialogFragment;
import com.bourke.glimmr.tasks.LoadGroupsTask;
import com.googlecode.flickrjandroid.groups.Group;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GroupListFragment extends BaseFragment
        implements IGroupListReadyListener, GroupItemLongClickDialogListener {

    private static final String TAG = "Glimmr/GroupListFragment";

    private final List<Group> mGroups = new ArrayList<Group>();
    private LoadGroupsTask mTask;
    private ViewGroup mNoConnectionLayout;
    private AdapterView mListView;
    private GroupListAdapter mAdapter;

    public static GroupListFragment newInstance() {
        return new GroupListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.group_list_fragment, container, false);
        mNoConnectionLayout =
            (ViewGroup) mLayout.findViewById(R.id.no_connection_layout);
        mListView = (AdapterView) mLayout.findViewById(R.id.list);
        initAdapterView();
        return mLayout;
    }

    private void initAdapterView() {
        mAdapter = new GroupListAdapter(mActivity, R.layout.group_list_row,
                (ArrayList<Group>) mGroups);
        mListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Intent groupViewer = new Intent(mActivity,
                        GroupViewerActivity.class);
                groupViewer.putExtra(GroupViewerActivity.KEY_GROUP_ID,
                        mGroups.get(position).getId());
                groupViewer.setAction(GroupViewerActivity
                        .ACTION_VIEW_GROUP_BY_ID);
                startActivity(groupViewer);
            }
        });
        mListView.setOnItemLongClickListener(
                new ListView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView <?> parent, View v,
                                                   int position, long id) {
                        if (position < mGroups.size()) {
                            DialogFragment d =
                                    GroupItemLongClickDialog.newInstance(mActivity,
                                            GroupListFragment.this, mGroups.get(position));
                            d.show(mActivity.getSupportFragmentManager(),
                                    "group_item_long_click");
                        } else {
                            Log.e(getLogTag(), String.format(
                                    "Cannot call showGridItemContextMenu, " +
                                            "mGroups.size(%d) != position:(%d)",
                                    mGroups.size(), position));
                        }
                /* True indicates we're finished with event and triggers
                 * haptic feedback */
                        return true;
                    }
                });
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void startTask() {
        super.startTask();
        mTask = new LoadGroupsTask(this);
        mTask.execute(mOAuth);
    }

    @Override
    public void onGroupListReady(List<Group> groups, Exception e) {
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "onGroupListReady");
        if (FlickrHelper.getInstance().handleFlickrUnavailable(mActivity, e) ||
                groups == null) {
            mNoConnectionLayout.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            return;
        }
        mListView.setVisibility(View.VISIBLE);
        mNoConnectionLayout.setVisibility(View.GONE);
        mGroups.clear();
        mGroups.addAll(groups);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLongClickDialogSelection(Group group, int which) {
        Log.d(TAG, "onLongClickDialogSelection()");
        FragmentTransaction ft =
            mActivity.getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        if (group != null) {
            Fragment prev = mActivity.getSupportFragmentManager()
                .findFragmentByTag(AddToGroupDialogFragment.TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            DialogFragment newFragment =
                AddToGroupDialogFragment.newInstance(group);
            newFragment.show(ft, AddToGroupDialogFragment.TAG);
        } else {
            Log.e(TAG, "onLongClickDialogSelection: group is null");
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

            holder.textViewNumImages.setText("" + group.getPhotoCount());
            Picasso.with(mActivity).load(group.getBuddyIconUrl()).into(holder.imageViewGroupIcon);

            return convertView;
        }
    }

    static class ViewHolder {
        TextView textViewGroupName;
        TextView textViewNumImages;
        ImageView imageViewGroupIcon;
    }

    static class GroupItemLongClickDialog extends DialogFragment {
        private GroupItemLongClickDialogListener mListener;
        private Context mContext;
        private Group mGroup;

        public static GroupItemLongClickDialog newInstance(Context context,
                GroupItemLongClickDialogListener listener, Group group) {
            GroupItemLongClickDialog d = new GroupItemLongClickDialog();
            d.mListener = listener;
            d.mContext = context;
            d.mGroup = group;
            return d;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setItems(R.array.group_list_item_long_click_dialog_items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onLongClickDialogSelection(mGroup, which);
                    }
                });
            return builder.create();
        }
    }
}
