package com.bourke.glimmr.fragments.viewer;

import android.os.Bundle;

import android.support.v4.app.FragmentTransaction;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.ListView;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.BusProvider;
import com.bourke.glimmr.event.Events.IGroupInfoReadyListener;
import com.bourke.glimmr.fragments.base.BaseDialogFragment;
import com.bourke.glimmr.fragments.base.PhotoGridFragment.
           PhotoGridItemClickedEvent;
import com.bourke.glimmr.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadGroupInfoTask;

import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.groups.Throttle;

import com.squareup.otto.Subscribe;

// TODO
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
    private Throttle mThrottle;
    private int mCount = 0;
    private int mRemaining = 0;

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
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        new LoadGroupInfoTask(mGroup.getId(), this).execute(mOAuth);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onItemClicked(PhotoGridItemClickedEvent event) {
        if (event.isChecked()) {
            if (mRemaining > 0) {
                mRemaining--;
            }
        } else {
            if (mRemaining < mCount) {
                mRemaining++;
            }
        }
        String title = String.format("%s/%s remaining", mRemaining, mCount);
        getDialog().setTitle(title);
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

    @Override
    public void onGroupInfoReady(Group group) {
        if (Constants.DEBUG) Log.d(TAG, "onGroupInfoReady");
        if (group == null) {
            // TODO: handle retry or error case here
            return;
        }
        mThrottle = group.getThrottle();
        if (mThrottle == null) {
            // TODO: handle retry or error case here
            Log.e(TAG, "No group throttle found");
            return;
        }
        if (Constants.DEBUG) {
            Log.d(TAG, "Group throttle found");
            Log.d(TAG, String.format("Remaining: %d, Mode: %s, Count: %d",
                        mThrottle.getRemaining(), mThrottle.getMode(),
                        mThrottle.getCount()));
        }

        mRemaining = mThrottle.getRemaining();
        mCount = mThrottle.getCount();
        String title = String.format("%s/%s remaining", mRemaining, mCount);
        getDialog().setTitle(title);
    }
}
