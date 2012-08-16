package com.bourke.glimmr.activities;

import android.content.Context;
import android.content.Intent;

import android.graphics.Typeface;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.fragments.photoset.PhotosetGridFragment;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photosets.Photoset;

import com.viewpagerindicator.TitlePageIndicator;

public class PhotosetViewerActivity extends BaseActivity {

    private static final String TAG = "Glimmr/PhotosetViewerActivity";

    public static final int PHOTOSET_PAGE = 0;

    public static final String[] CONTENT = new String[] { "Set" };

    private TextView mAbSubtitle;

    /**
     * The Photoset this activity is concerned with
     */
    private Photoset mPhotoset = new Photoset();

    /**
     * User who's profile we're displaying, as distinct from the authorized
     * user.
     */
    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mOAuth == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            setContentView(R.layout.main);

            mActionBar.setDisplayHomeAsUpEnabled(true);
            mAq = new AQuery(this);

            handleIntent(getIntent());
        }
    }

    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mPhotoset = (Photoset) bundle.getSerializable(Constants.
                    KEY_PHOTOSETVIEWER_PHOTOSET);
            mUser = (User) bundle.getSerializable(
                    Constants.KEY_PHOTOSETVIEWER_USER);
            if (mPhotoset != null && mUser != null) {
                Log.d(TAG, "Got photoset to view: " + mPhotoset.getTitle());
                ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
                GroupPagerAdapter adapter = new GroupPagerAdapter(
                        getSupportFragmentManager());
                viewPager.setAdapter(adapter);
                TitlePageIndicator indicator = (TitlePageIndicator)
                    findViewById(R.id.indicator);
                indicator.setOnPageChangeListener(this);
                indicator.setViewPager(viewPager);

                setActionBarSubtitle(mPhotoset.getTitle());
            } else {
                Log.e(TAG, "Photoset/User from intent is null");
                // TODO: show error / recovery
            }
        } else {
            Log.e(TAG, "Bundle is null, PhotosetViewerActivity requires an " +
                    "intent containing a Photoset and a User");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void initActionBar() {
        LayoutInflater inflator = (LayoutInflater)
            getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.action_bar_subtitle_item, null);
        mAbTitle = (TextView) v.findViewById(R.id.abTitle);
        mAbSubtitle = (TextView) v.findViewById(R.id.abSubtitle);
        Typeface typeface = Typeface.createFromAsset(getAssets(),
                Constants.FONT_SHADOWSINTOLIGHT);
        mAbTitle.setTypeface(typeface);
        mAbTitle.setText(getTitle().toString());
        mActionBar.setCustomView(v);
    }

    private void setActionBarSubtitle(String subTitle) {
        mAbSubtitle.setText(subTitle);
    }

    class GroupPagerAdapter extends FragmentPagerAdapter {
        public GroupPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public SherlockFragment getItem(int position) {
            switch (position) {
                case PHOTOSET_PAGE:
                    return PhotosetGridFragment.newInstance(mPhotoset, mUser);
            }
            return null;
        }

        @Override
        public int getCount() {
            return PhotosetViewerActivity.CONTENT.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return PhotosetViewerActivity.CONTENT[position %
                PhotosetViewerActivity.CONTENT.length].toUpperCase();
        }
    }
}
