package com.bourke.glimmr.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IFavoriteReadyListener;
import com.bourke.glimmr.fragments.viewer.PhotoViewerFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.SetFavoriteTask;

import com.gmail.yuyang226.flickr.photos.Photo;

import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.PageIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for viewing photos.
 *
 * Receives a list of photos via an intent and shows the first one specified by
 * a startIndex in a zoomable ImageView.
 */
public class PhotoViewerActivity extends BaseActivity
        implements ViewPager.OnPageChangeListener, IFavoriteReadyListener {

    private static final String TAG = "Glimmr/PhotoViewerActivity";

    private List<Photo> mPhotos = new ArrayList<Photo>();
    private int mSelectedIndex = 0;
    private MenuItem mFavoriteButton;

    private Map<Integer, PhotoViewerFragment> mPageReferenceMap =
        new HashMap<Integer, PhotoViewerFragment>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.photoviewer);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getLogTag(), "onCreateOptionsMenu");
        getSupportMenuInflater().inflate(R.menu.photoviewer_menu, menu);
        mFavoriteButton = menu.findItem(R.id.menu_favorite);
        return true;
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

    // TODO: add lock around this
    public void onFavoriteButtonClick() {
        PhotoViewerFragment currentFragment = getFragment(mSelectedIndex);
        if (currentFragment != null) {
            Photo currentPhoto = currentFragment.getPhoto();
            if (currentPhoto != null) {
                updateFavoriteButtonIcon(!currentPhoto.isFavorite());
                Log.d(getLogTag(), String.format(
                            "Starting SetFavoriteTask, id/index:"
                            + "%s/%s", currentPhoto.getId(), mSelectedIndex));
                new SetFavoriteTask(this, this, currentPhoto).execute(mOAuth);
            } else {
                Log.e(TAG, "onFavoriteButtonClick: currentPhoto is null");
            }
        } else {
            Log.e(TAG, "onFavoriteButtonClick: currentFragment is null");
        }
    }

    /**
     * Update the icon the favorites button based on the state of the current
     * photo.
     */
    public void updateFavoriteButtonIcon(boolean favorite) {
        Log.d(getLogTag(), "updateFavoriteButtonIcon: " + favorite);
        if (favorite) {
            mFavoriteButton.setIcon(R.drawable.ic_rating_important_dark);
        } else {
            mFavoriteButton.setIcon(R.drawable.ic_rating_not_important_dark);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        mPhotos = (ArrayList<Photo>) bundle.getSerializable(Constants
                .KEY_PHOTOVIEWER_LIST);
        mSelectedIndex = bundle.getInt(Constants.KEY_PHOTOVIEWER_START_INDEX);

        if (mPhotos != null) {
            Log.d(getLogTag(), "Got list of photo urls, size: "
                    + mPhotos.size());
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
            Log.e(getLogTag(), "Photos from intent are null");
            // TODO: show error / recovery
        }
    }

    public int getSelectedFragmentId() {
        return mSelectedIndex;
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(getLogTag(), "onPageSelected: " + position);
        mSelectedIndex = position;
        PhotoViewerFragment current = getFragment(position);
        if (current != null) {
            current.refreshFavoriteIcon();
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

    @Override
    public void onPageScrolled(int position, float positionOffset,
            int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public PhotoViewerFragment getFragment(int key) {
        Log.d(TAG, "getFragment: " + key);
        return mPageReferenceMap.get(key);
    }

    @Override
    public void onFavoriteComplete(Exception e) {
        if (e != null) {
            Log.d(getLogTag(), "Error setting favorite on photo");
            return;
        } else {
            Log.d(getLogTag(), "Successfully favorited/unfavorited photo");
        }
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    class PhotoViewerPagerAdapter extends FragmentStatePagerAdapter {
        public PhotoViewerPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "getItem: " + position);
            PhotoViewerFragment myFragment = PhotoViewerFragment.newInstance(
                    mPhotos.get(position), position);
            mPageReferenceMap.put(position, myFragment);
            return myFragment;
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            Log.d(TAG, "destroyItem: " + position);
            super.destroyItem(container, position, object);
            mPageReferenceMap.remove(position);
        }

        @Override
        public int getCount() {
            return mPhotos.size();
        }
    }
}
