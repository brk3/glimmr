package com.bourke.glimmr.fragments.viewer;

import com.bourke.glimmr.common.Constants;
import android.os.Bundle;

import android.support.v4.app.FragmentTransaction;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.ListView;

import com.bourke.glimmr.event.Events.IGroupInfoReadyListener;
import com.bourke.glimmr.tasks.LoadGroupInfoTask;
import com.bourke.glimmr.fragments.base.BaseDialogFragment;
import com.bourke.glimmr.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.groups.Throttle;

// TODO
// * Group info task for title
// * Replace add text on button with icon
// * Persist selections on rotate
// * Start this dialog on + button from GroupViewerFragment
// * Some sort Service to handle the actual group additions
// * Make sure all works on Gingerbread
// * Add loading view to dialog

/**
 * Present a dialog to that allows the user to select photos from their
 * photo stream to add to the current group.
 *
 * This is really just a host for the main fragments, we could make it an
 * activity but ABS doesn't support Activity dialog themes.
 */
public class AddToGroupDialogFragment extends BaseDialogFragment
        implements IGroupInfoReadyListener {

    public static final String TAG = "Glimmr/AddToGroupDialogFragment";

    /* Can't see in api docs what the limit is when there's no throttle.  The
     * popular 'Black & White' group states 6, so go with that for now */
    private static final int ADD_AT_A_TIME = 6;

    private Group mGroup;

    public static AddToGroupDialogFragment newInstance(Group group) {
        AddToGroupDialogFragment newFragment = new AddToGroupDialogFragment();
        newFragment.mGroup = group;
        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (LinearLayout) inflater.inflate(
                R.layout.add_to_group_fragment, container, false);

        /* Nested fragments have to be added this way, not from xml */
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        boolean retainInstance = false;
        PhotoStreamGridFragment frag =
            PhotoStreamGridFragment.newInstance(retainInstance,
                    ListView.CHOICE_MODE_MULTIPLE);
        ft.replace(R.id.photoStreamFragment, frag);
        ft.commit();

        return mLayout;
    }

    public void onResume() {
        super.onResume();
        new LoadGroupInfoTask(mGroup.getId(), this).execute(mOAuth);
    }

    @Override
    public void onGroupInfoReady(Group group) {
        if (Constants.DEBUG) Log.d(TAG, "onGroupInfoReady");
        if (group != null) {
            setTitle(group);
        } else {
            getDialog().setTitle(
                    mActivity.getString(R.string.choose_some_photos));
        }
    }

    private void setTitle(Group group) {
        int remaining = ADD_AT_A_TIME;
        Throttle t = group.getThrottle();
        if (t != null) {
            remaining = t.getRemaining();
            if (Constants.DEBUG) {
                Log.d(TAG, "Group throttle found, remaining: " + remaining);
                Log.d(TAG, "Mode:" + t.getMode());
                Log.d(TAG, "Count:" + t.getCount());
            }
        } else {
            if (Constants.DEBUG) Log.d(TAG, "No group throttle found");
        }
        String title = String.format("%s (%d remaining)",
                mActivity.getString(R.string.choose_some_photos),
                remaining);
        getDialog().setTitle(title);
    }
}
