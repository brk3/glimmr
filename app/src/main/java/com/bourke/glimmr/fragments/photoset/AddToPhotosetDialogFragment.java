package com.bourke.glimmr.fragments.photoset;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bourke.glimmr.R;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.common.TaskQueueDelegateFactory;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.fragments.base.BaseDialogFragment;
import com.bourke.glimmr.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmr.tape.AddToPhotosetTaskQueueService;
import com.bourke.glimmr.tasks.AddItemToPhotosetTask;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.squareup.tape.TaskQueue;

import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

// TODO
// * Persist selections on rotate

/**
 * Present a dialog to that allows the user to select photos from their
 * photo stream to add to the current photoset.
 *
 * This is really just a host for the main fragments, we could make it an
 * activity but ABS doesn't support Activity dialog themes.
 */
public class AddToPhotosetDialogFragment extends BaseDialogFragment {

    public static final String TAG = "Glimmr/AddToPhotosetDialogFragment";

    private static final String KEY_PHOTOSET =
            "com.bourke.glimmr.AddToPhotosetDialogFragment.KEY_PHOTOSET";

    private Photoset mPhotoset;
    private TaskQueue mQueue;

    public static AddToPhotosetDialogFragment newInstance(Photoset photoset) {
        AddToPhotosetDialogFragment newFragment =
            new AddToPhotosetDialogFragment();
        newFragment.mPhotoset = photoset;
        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TaskQueueDelegateFactory<AddItemToPhotosetTask> factory =
            new TaskQueueDelegateFactory<AddItemToPhotosetTask>(mActivity);
        mQueue = new TaskQueue(factory.get(Constants.PHOTOSET_QUEUE, AddItemToPhotosetTask.class));
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        new GsonHelper(mActivity).marshallObject(
                mPhotoset, outState, KEY_PHOTOSET);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && mPhotoset == null) {
            String json = savedInstanceState.getString(KEY_PHOTOSET);
            if (json != null) {
                mPhotoset = new Gson().fromJson(json, Photoset.class);
            } else {
                Log.e(TAG, "No stored photoset found in savedInstanceState");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (LinearLayout) inflater.inflate(
                R.layout.add_to_photoset_fragment, container, false);

        TextView titleTextView = (TextView)
                mLayout.findViewById(R.id.titleText);
        titleTextView.setText(R.string.add_photos);
        mTextUtils.setFont(titleTextView, TextUtils.FONT_ROBOTOBOLD);
        //ProgressBar progressBar = (ProgressBar)
        //        mLayout.findViewById(R.id.progressIndicator);

        /* Nested fragments have to be added this way, not from xml */
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        final boolean retainInstance = false;
        final PhotoStreamGridFragment frag =
            PhotoStreamGridFragment.newInstance(mOAuth.getUser(),
                    retainInstance, ListView.CHOICE_MODE_MULTIPLE);
        ft.replace(R.id.photoStreamFragment, frag);
        ft.commit();

        /* When add button is clicked, get selected ids and add to queue */
        mLayout.findViewById(R.id.buttonAddToPhotoset).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        List<Photo> selectedPhotos = frag.getSelectedPhotos();
                        for (Photo photo : selectedPhotos) {
                            mQueue.add(new AddItemToPhotosetTask(
                                    mPhotoset.getId(), photo.getId(), mOAuth));
                        }
                        mActivity.startService(new Intent(mActivity,
                                    AddToPhotosetTaskQueueService.class));
                        dismiss();
                        Crouton.makeText(mActivity,
                            R.string.photos_will_be_added,
                            Style.CONFIRM).show();
                    }
                });

        return mLayout;
    }
}
