package com.bourke.glimmrpro.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.androidquery.AQuery;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.GsonHelper;
import com.bourke.glimmrpro.common.TaskQueueDelegateFactory;
import com.bourke.glimmrpro.common.UsageTips;
import com.bourke.glimmrpro.fragments.upload.LocalPhotosGridFragment;
import com.bourke.glimmrpro.fragments.upload.PhotoUploadFragment;
import com.bourke.glimmrpro.fragments.base.BaseFragment;
import com.bourke.glimmrpro.tape.UploadPhotoTaskQueueService;
import com.bourke.glimmrpro.tasks.UploadPhotoTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.tape.TaskQueue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Contains a ViewPager to display each image to be uploaded.
 *
 * Below the ViewPager is a PhotoUploadFragment which contains fields to set metadata info on the
 * image currently shown in the ViewPager.
 */
public class PhotoUploadActivity extends BaseActivity {

    private static final String TAG = "Glimmr/PhotoUploadActivity";

    private static final String KEY_PHOTO_LIST =
            "com.bourke.glimmrpro.PhotoUploadActivity.KEY_PHOTO_LIST";

    public static final String QUEUE_FILE = "upload_task_queue.json";

    private List<LocalPhotosGridFragment.LocalPhoto> mUploadImages;
    private ImagePagerAdapter mAdapter;
    private ViewPager mPager;
    private TaskQueue mQueue;
    private PhotoUploadFragment mPhotoUploadFragment;

    /**
     * Start the PhotoUploadActivity with a list of image uris to process.
     */
    public static void startPhotoUploadActivity(Context context,
            ArrayList<LocalPhotosGridFragment.LocalPhoto> uris) {
        Intent photoUploadActivity = new Intent(context, PhotoUploadActivity.class);
        Bundle extras = new Bundle();
        new GsonHelper(context).marshallObject(uris, extras, KEY_PHOTO_LIST);
        photoUploadActivity.putExtras(extras);
        context.startActivity(photoUploadActivity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreate");

        setContentView(R.layout.photo_upload_activity);
        mPhotoUploadFragment = (PhotoUploadFragment)
                getSupportFragmentManager().findFragmentById(R.id.photoUploadFragment);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mAq = new AQuery(this);

        TaskQueueDelegateFactory<UploadPhotoTask> factory =
                new TaskQueueDelegateFactory<UploadPhotoTask>(this);
        mQueue = new TaskQueue(factory.get(QUEUE_FILE, UploadPhotoTask.class));

        /* hide the keyboard */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        String json = extras.getString(KEY_PHOTO_LIST);
        Type collectionType =
                new TypeToken<Collection<LocalPhotosGridFragment.LocalPhoto>>(){}.getType();
        mUploadImages = new Gson().fromJson(json, collectionType);
        if (mUploadImages.size() > 1) {
            UsageTips.getInstance().show(this, getString(R.string.upload_tip), false);
        }
        initViewPager();
    }

    private void initViewPager() {
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), mUploadImages);
        mPager = (ViewPager) findViewById(R.id.viewPager);
        mPager.setOnPageChangeListener(mAdapter);
        mPager.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photouploadactivity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_upload_photos:
                /* make sure to grab UI state for current photo before finishing */
                mPhotoUploadFragment.updateMetadataFromUI();

                /* now add each photo to upload queue, and start the tape service */
                for (LocalPhotosGridFragment.LocalPhoto photo : mUploadImages) {
                    mQueue.add(new UploadPhotoTask(mOAuth, photo));
                    // TODO: if sets also start set queue
                }

                startService(new Intent(this, UploadPhotoTaskQueueService.class));

                final Intent mainActivity = new Intent(this, MainActivity.class);
                mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainActivity);
        }
        return super.onOptionsItemSelected(item);
    }

    public class ImagePagerAdapter extends FragmentPagerAdapter
            implements ViewPager.OnPageChangeListener {

        private List<LocalPhotosGridFragment.LocalPhoto> mPhotos;

        public ImagePagerAdapter(FragmentManager fm,
                List<LocalPhotosGridFragment.LocalPhoto> imageUris) {
            super(fm);
            mPhotos = imageUris;
            mPhotoUploadFragment.setPhoto(mPhotos.get(0));
        }

        @Override
        public Fragment getItem(int position) {
            return ImageFragment.newInstance(mPhotos.get(position).getUri());
        }

        @Override
        public int getCount() {
            return mPhotos.size();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mPhotoUploadFragment.setPhoto(mPhotos.get(position));
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }

    public static class ImageFragment extends BaseFragment {

        private String mImageUri;

        public static ImageFragment newInstance(String imageUri) {
            ImageFragment f = new ImageFragment();
            f.mImageUri = imageUri;
            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            ImageView image = new ImageView(getActivity());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
            image.setLayoutParams(layoutParams);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mAq.id(image).image(mImageUri, Constants.USE_MEMORY_CACHE, Constants.USE_FILE_CACHE,
                    0, 0, null, AQuery.FADE_IN_NETWORK);

            LinearLayout layout = new LinearLayout(getActivity());
            layout.addView(image);

            return layout;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            /* Override so as not to display the usual PhotoGridFragment options */
        }
    }
}
