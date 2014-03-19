package com.bourke.glimmr.activities;

import com.bourke.glimmr.BuildConfig;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import com.bourke.glimmr.R;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.common.OAuthUtils;
import com.bourke.glimmr.fragments.search.AbstractPhotoSearchGridFragment;
import com.bourke.glimmr.fragments.search.PhotostreamSearchGridFragment;
import com.bourke.glimmr.fragments.search.PublicPhotoSearchGridFragment;

public class SearchActivity extends BottomOverlayActivity {

    private static final String TAG = "Glimmr/SearchActivity";

    private static final int RESULT_PUBLIC_PHOTOS_PAGE = 0;
    private static final int RESULT_PHOTOSTREAM_PAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (OAuthUtils.isLoggedIn(this)) {
            CONTENT = new String[] { getString(R.string.public_photos),
                getString(R.string.my_photos) };
        } else {
            CONTENT = new String[] { getString(R.string.public_photos) };
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, String.format("Got search query: '%s'",
                            searchQuery));
            }
            initViewPager(searchQuery);
        }
    }

    protected void initViewPager(final String searchQuery) {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mAdapter = new GlimmrPagerAdapter(getSupportFragmentManager(),
                mViewPager, mActionBar, CONTENT) {
            @Override
            public Fragment getItemImpl(int position) {
                switch (position) {
                    case RESULT_PUBLIC_PHOTOS_PAGE:
                        return PublicPhotoSearchGridFragment.newInstance(
                                searchQuery, AbstractPhotoSearchGridFragment
                                    .SORT_TYPE_RELAVANCE);
                    case RESULT_PHOTOSTREAM_PAGE:
                        return PhotostreamSearchGridFragment.newInstance(
                                searchQuery, AbstractPhotoSearchGridFragment
                                    .SORT_TYPE_RELAVANCE, mUser);
                }
                return null;
            }
        };
        super.initViewPager();
    }

    @Override
    protected void updateBottomOverlay() {
        // TODO
    }
}
