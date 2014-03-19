package com.bourke.glimmr.fragments.group;

import com.bourke.glimmr.BuildConfig;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bourke.glimmr.R;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.common.TaskQueueDelegateFactory;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.event.BusProvider;
import com.bourke.glimmr.event.Events.IGroupInfoReadyListener;
import com.bourke.glimmr.fragments.base.BaseDialogFragment;
import com.bourke.glimmr.fragments.base.PhotoGridFragment.PhotoGridItemClickedEvent;
import com.bourke.glimmr.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmr.tape.AddToGroupTaskQueueService;
import com.bourke.glimmr.tasks.AddItemToGroupTask;
import com.bourke.glimmr.tasks.LoadGroupInfoTask;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.groups.Throttle;
import com.googlecode.flickrjandroid.photos.Photo;
import com.squareup.otto.Subscribe;
import com.squareup.tape.TaskQueue;

import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

// TODO
// * Handle failing to create TaskQueue
// * Persist selections on rotate

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

    public static final String KEY_GROUP =
            "com.bourke.glimmr.AddToGroupDialogFragment.KEY_GROUP";

    /* Can't see in api docs what the limit is when there's no throttle.  The
     * popular 'Black & White' group states 6, so go with that for now */
    private static final int ADD_AT_A_TIME = 6;

    private Group mGroup;
    private TaskQueue mQueue;
    private ProgressBar mProgressBar;
    private TextView mTitleView;

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
        TaskQueueDelegateFactory<AddItemToGroupTask> factory =
            new TaskQueueDelegateFactory<AddItemToGroupTask>(mActivity);
        mQueue = new TaskQueue(factory.get(Constants.GROUP_QUEUE, AddItemToGroupTask.class));
        BusProvider.getInstance().register(this);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        new GsonHelper(mActivity).marshallObject(mGroup, outState, KEY_GROUP);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && mGroup == null) {
            String json = savedInstanceState.getString(KEY_GROUP);
            if (json != null) {
                mGroup = new Gson().fromJson(json, Group.class);
            } else {
                Log.e(TAG, "No stored group found in savedInstanceState");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        new LoadGroupInfoTask(mGroup.getId(), this).execute(mOAuth);
    }

    @Subscribe
    public void onItemClicked(PhotoGridItemClickedEvent event) {
        if (event.isChecked()) {
            mRemaining--;
        } else {
            if (mRemaining < mCount) {
                mRemaining++;
            }
        }
        String title = String.format("%s/%s %s", mRemaining, mCount,
                getString(R.string.remaining));
        mTitleView.setText(title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (LinearLayout) inflater.inflate(
                R.layout.add_to_group_fragment, container, false);

        mTitleView = (TextView) mLayout.findViewById(R.id.titleText);
        mTextUtils.setFont(mTitleView, TextUtils.FONT_ROBOTOBOLD);
        mProgressBar =
            (ProgressBar) mLayout.findViewById(R.id.progressIndicator);
        mProgressBar.setVisibility(View.VISIBLE);

        /* Nested fragments have to be added this way, not from xml */
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        final boolean retainInstance = false;
        final PhotoStreamGridFragment frag =
            PhotoStreamGridFragment.newInstance(mOAuth.getUser(),
                    retainInstance, ListView.CHOICE_MODE_MULTIPLE);
        ft.replace(R.id.photoStreamFragment, frag);
        ft.commit();

        /* When add button is clicked, get selected ids and add to queue */
        mLayout.findViewById(R.id.buttonAddToGroup).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        List<Photo> selectedPhotos = frag.getSelectedPhotos();
                        if (mRemaining < 0 || selectedPhotos.size() == 0) {
                            Log.e(TAG, "None or too many items selected");
                            return;
                        }
                        for (Photo photo : selectedPhotos) {
                            mQueue.add(new AddItemToGroupTask(mGroup.getId(),
                                    photo.getId(), mOAuth));
                        }
                        mActivity.startService(new Intent(mActivity,
                                    AddToGroupTaskQueueService.class));
                        dismiss();
                        Crouton.makeText(mActivity,
                            R.string.photos_will_be_added,
                            Style.CONFIRM).show();
                    }
                });

        return mLayout;
    }

    @Override
    public void onGroupInfoReady(Group group, Exception e) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onGroupInfoReady");
        mProgressBar.setVisibility(View.GONE);

        if (FlickrHelper.getInstance().handleFlickrUnavailable(mActivity, e)) {
            return;
        }

        /* If trouble getting group info we can't proceed */
        if (group == null) {
            dismiss();
            Crouton.makeText(mActivity, R.string.group_info_fetch_error,
                Style.ALERT).show();
            return;
        }

        /* Get group throttle info */
        Throttle throttle = group.getThrottle();
        if ("none".equals(throttle.getMode())) {
            mCount = ADD_AT_A_TIME;
            mRemaining = ADD_AT_A_TIME;
        } else {
            mRemaining = throttle.getRemaining();
            mCount = throttle.getCount();
        }

        if (mRemaining == 0) {
            dismiss();
            Crouton.makeText(mActivity, R.string.group_limit_reached,
                Style.INFO).show();
            return;
        }

        String title = String.format("%s/%s %s", mRemaining, mCount,
                getString(R.string.remaining));
        mTitleView.setText(title);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("Remaining: %d, Mode: %s, Count: %d",
                    throttle.getRemaining(), throttle.getMode(),
                    throttle.getCount()));
        }
    }
}
