package com.bourke.glimmr.activities;

import android.app.SearchManager;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.fragments.search.PhotoSearchGridFragment;
import com.bourke.glimmr.R;

public class SearchActivity extends BottomOverlayActivity {

    private static final String TAG = "Glimmr/SearchActivity";

    public static final int RESULT_PHOTO_PAGE = 0;
    public static final int RESULT_PEOPLE_PAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        CONTENT = new String[] { getString(R.string.photos) };
            //getString(R.string.people) };
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            if (Constants.DEBUG) {
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
            public SherlockFragment getItemImpl(int position) {
                switch (position) {
                    case RESULT_PHOTO_PAGE:
                        return PhotoSearchGridFragment.newInstance(searchQuery,
                                PhotoSearchGridFragment.SORT_TYPE_RELAVANCE);
                    case RESULT_PEOPLE_PAGE:
                        // TODO
                        //return PeopleSearchFragment.
                        //    .newInstance(mSearchQuery);
                        break;
                }
                return null;
            }
        };
        super.initViewPager();
    }

    @Override
    protected void updateBottomOverlay() {
    }
}
