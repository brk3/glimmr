package com.bourke.glimmr.fragments.home;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import android.os.Bundle;

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

import com.actionbarsherlock.app.SherlockDialogFragment;

import com.androidquery.AQuery;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.activities.GroupViewerActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.event.Events.GroupItemLongClickDialogListener;
import com.bourke.glimmr.event.Events.IGroupListReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.fragments.viewer.AddToGroupDialogFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadGroupsTask;

import com.googlecode.flickrjandroid.groups.Group;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.ArrayList;
import java.util.List;

public class GroupListFragment extends BaseFragment
        implements IGroupListReadyListener, GroupItemLongClickDialogListener {

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
            mListView.setOnItemLongClickListener(
                    new ListView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View v,
                        int position, long id) {
                    if (position < mGroups.size()) {
                        SherlockDialogFragment d =
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

            /* Show a tip on first run */
            final SharedPreferences prefs = mActivity.getSharedPreferences(
                    Constants.PREFS_NAME, Context.MODE_PRIVATE);
            final boolean isFirstRun = prefs.getBoolean(
                    Constants.KEY_IS_FIRST_RUN, true);
            if (isFirstRun) {
                Crouton.makeText(mActivity, R.string.tip_add_to_group,
                        Style.INFO).show();
            }
        }
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

            SherlockDialogFragment newFragment =
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
    }

    static class ViewHolder {
        TextView textViewGroupName;
        TextView textViewNumImages;
        ImageView imageViewGroupIcon;
    }

    static class GroupItemLongClickDialog extends SherlockDialogFragment {
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
