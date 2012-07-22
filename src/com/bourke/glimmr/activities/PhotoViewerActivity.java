package com.bourke.glimmr.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.androidquery.AQuery;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.fragments.viewer.PhotoViewerFragment;
import com.bourke.glimmr.R;

import com.gmail.yuyang226.flickr.photos.Photo;

import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.PageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for viewing photos.
 *
 * Receives a list of photos via an intent and shows the first one specified by
 * a startIndex in a zoomable ImageView.
 */
public class PhotoViewerActivity extends BaseActivity
        implements ViewPager.OnPageChangeListener {

    private static final String TAG = "Glimmr/PhotoViewerActivity";

    private MenuItem mFavoriteButton;

    private List<Photo> mPhotos = new ArrayList<Photo>();
    private int mSelectedIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.photoviewer);

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mAq = new AQuery(this);
        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.photoviewer_menu, menu);
        mFavoriteButton = menu.findItem(R.id.menu_favorite);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_view_comments:
                onCommentsButtonClick();
                return true;
            case R.id.menu_favorite:
                onFavoriteButtonClick();
                return true;
            case R.id.menu_view_exif:
                onExifButtonClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCommentsButtonClick() {
        Intent activity = new Intent(this, CommentsDialogActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.COMMENTS_DIALOG_ACTIVITY_PHOTO,
                mPhotos.get(mSelectedIndex));
        activity.putExtras(bundle);
        startActivity(activity);
    }

    public void onFavoriteButtonClick() {
        if (mPhotos.get(mSelectedIndex).isFavorite()) {
            mFavoriteButton.setIcon(R.drawable.ic_rating_important_dark);
        } else {
            mFavoriteButton.setIcon(R.drawable.ic_rating_not_important_dark);
        }
    }

    public void onExifButtonClick() {
        Intent exifActivity = new Intent(this, ExifInfoDialogActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_EXIF_INFO_DIALOG_ACTIVITY_PHOTO,
                mPhotos.get(mSelectedIndex));
        exifActivity.putExtras(bundle);
        startActivity(exifActivity);
    }

    /**
     * Toggle whether the actionbar and button panel are showing or not.
     */
    public void toggleOverlayVisibility(View view) {
        // TODO: there is a visible flicker when the actionbar hides, possibly
        // due to the layout been redrawn.  An overlay actionbar may fix this.
        if (mActionBar.isShowing()) {
            mActionBar.hide();
        } else {
            mActionBar.show();
        }
    }


    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        mPhotos = (ArrayList<Photo>) bundle.getSerializable(Constants
                .KEY_PHOTOVIEWER_LIST);
        mSelectedIndex = bundle.getInt(Constants.KEY_PHOTOVIEWER_START_INDEX);

        if (mPhotos != null) {
            Log.d(TAG, "Got list of photo urls, size: " + mPhotos.size());
            PhotoViewerPagerAdapter adapter = new PhotoViewerPagerAdapter(
                    getSupportFragmentManager());
            ViewPager pager = (ViewPager) findViewById(R.id.pager);
            pager.setAdapter(adapter);
            PageIndicator indicator = (LinePageIndicator) findViewById(
                    R.id.indicator);
            indicator.setOnPageChangeListener(this);
            indicator.setViewPager(pager);
            indicator.setCurrentItem(mSelectedIndex);
        } else {
            Log.e(TAG, "Photos from intent are null");
            // TODO: show error / recovery
        }
    }

    @Override
    public void onPageSelected(int position) {
        mSelectedIndex = position;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
            int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    class PhotoViewerPagerAdapter extends FragmentPagerAdapter {
        public PhotoViewerPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PhotoViewerFragment.newInstance(mPhotos.get(position));
        }

        @Override
        public int getCount() {
            return mPhotos.size();
        }
    }
}
