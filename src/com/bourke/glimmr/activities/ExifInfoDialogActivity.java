package com.bourke.glimmr.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.fragments.viewer.ExifInfoFragment;
import com.bourke.glimmr.R;

import com.gmail.yuyang226.flickr.photos.Photo;

/**
 * Simple dialog themed Activity that floats over another.  Contains a fragment
 * to show exif info about a photo.
 */
public class ExifInfoDialogActivity extends BaseActivity {

    public static final String TAG = "Glimmr/ExifInfoDialogActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exif_info_dialog_activity);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            Log.e(TAG, "null bundle, ExifInfoDialogActivity requires a Photo");
            return;
        }

        Photo photo = (Photo) bundle.getSerializable(Constants
                .KEY_EXIF_INFO_DIALOG_ACTIVITY_PHOTO);
        if (photo != null) {
            Log.d(TAG, "Got photo to fetch exif for: " + photo.getId());

            /* Create and add the fragment now we have the photo to fetch exif
             * info for */
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
            ExifInfoFragment newFragment = ExifInfoFragment.newInstance(photo);
            fragmentTransaction.add(R.id.layout, newFragment);
            fragmentTransaction.commit();
        } else {
            Log.e(TAG, "photo from intent is null");
            // TODO: show error / recovery
        }
    }
}
