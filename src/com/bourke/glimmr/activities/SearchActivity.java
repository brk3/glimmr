package com.bourke.glimmrpro.activities;

import android.app.SearchManager;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.activities.BottomOverlayActivity;
import com.bourke.glimmrpro.common.GlimmrPagerAdapter;
import com.bourke.glimmrpro.fragments.search.PhotoSearchGridFragment;
import com.bourke.glimmrpro.R;

public class SearchActivity extends BottomOverlayActivity {

    private static final String TAG = "Glimmr/SearchActivity";

    public static final int RESULT_PHOTO_PAGE = 0;
    public static final int RESULT_PEOPLE_PAGE = 1;

    private String mSearchQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        CONTENT = new String[] { getString(R.string.photos) };
            //getString(R.string.people) };
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mSearchQuery = intent.getStringExtra(SearchManager.QUERY);
            if (Constants.DEBUG) {
                Log.d(TAG, String.format("Got search query: '%s'",
                            mSearchQuery));
            }
            initViewPager();
        }
    }

    @Override
    protected void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mAdapter = new GlimmrPagerAdapter(getSupportFragmentManager(),
                mViewPager, mActionBar, CONTENT) {
            @Override
            public SherlockFragment getItemImpl(int position) {
                switch (position) {
                    case RESULT_PHOTO_PAGE:
                        return PhotoSearchGridFragment
                            .newInstance(mSearchQuery);
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
