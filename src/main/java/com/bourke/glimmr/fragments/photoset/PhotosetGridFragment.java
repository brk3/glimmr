package com.bourke.glimmrpro.fragments.photoset;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.GsonHelper;
import com.bourke.glimmrpro.event.Events.IPhotoListReadyListener;
import com.bourke.glimmrpro.fragments.base.PhotoGridFragment;
import com.bourke.glimmrpro.tasks.LoadPhotosetPhotosTask;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photosets.Photoset;

import java.util.List;

public class PhotosetGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/PhotosetGridFragment";

    private static final String KEY_NEWEST_PHOTOSET_PHOTO_ID =
        "glimmr_newest_photoset_photo_id";
    public static final String KEY_PHOTOSET =
            "com.bourke.glimmr.PhotosetGridFragment.KEY_PHOTOSET";

    private Photoset mPhotoset;

    public static PhotosetGridFragment newInstance(Photoset photoset) {
        PhotosetGridFragment newFragment = new PhotosetGridFragment();
        newFragment.mPhotoset = photoset;
        return newFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.photosetviewer_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_photos:
                FragmentTransaction ft =
                    mActivity.getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                Fragment prev = mActivity.getSupportFragmentManager()
                    .findFragmentByTag(com.bourke.glimmrpro.fragments.photoset.AddToPhotosetDialogFragment.TAG);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                SherlockDialogFragment newFragment =
                    com.bourke.glimmrpro.fragments.photoset.AddToPhotosetDialogFragment.newInstance(mPhotoset);
                newFragment.show(ft, com.bourke.glimmrpro.fragments.photoset.AddToPhotosetDialogFragment.TAG);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowDetailsOverlay = false;
    }

    /**
     * Once the parent binds the adapter it will trigger cacheInBackground
     * for us as it will be empty when first bound.  So we don't need to
     * override startTask().
     */
    @Override
    protected boolean cacheInBackground() {
        startTask(mPage++);
        return mMorePages;
    }

    private void startTask(int page) {
        super.startTask();
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        new LoadPhotosetPhotosTask(this, mPhotoset, page).execute(mOAuth);
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
    public void onPhotosReady(List<Photo> photos, Exception e) {
        super.onPhotosReady(photos, e);
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
        if (mPhotoset != null) {
            User owner = mPhotoset.getOwner();
            if (owner != null) {
                for (Photo p : photos) {
                    p.setOwner(owner);
                    p.setUrl(String.format("%s%s%s%s",
                                "http://flickr.com/photos/",
                                owner.getId(), "/", p.getId()));
                }
            }
            if (photos != null && photos.isEmpty()) {
                mMorePages = false;
            }
        }
    }

    @Override
    public String getNewestPhotoId() {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_NEWEST_PHOTOSET_PHOTO_ID, null);
    }

    @Override
    public void storeNewestPhotoId(Photo photo) {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NEWEST_PHOTOSET_PHOTO_ID, photo.getId());
        editor.commit();
        if (Constants.DEBUG)
            Log.d(getLogTag(), "Updated most recent photoset photo id to " +
                photo.getId());
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}
