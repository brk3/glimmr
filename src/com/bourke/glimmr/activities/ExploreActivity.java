package com.bourke.glimmr.activities;

import com.actionbarsherlock.view.Menu;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.fragments.explore.RecentPublicPhotosFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.GetAccessTokenTask;
import com.bourke.glimmr.fragments.LoginFragment;

import com.googlecode.flickrjandroid.people.User;
import android.widget.Toast;

/**
 * Hosts fragments that don't require log in.
 */
public class ExploreActivity extends BaseActivity
        implements LoginFragment.IOnNotNowClicked {

    private static final String TAG = "Glimmr/ExploreActivity";

    public static final int INTERESTING_PAGE = 0;
    //public static final int TAGS_PAGE = 1;

    public static String[] CONTENT;

    private LoginFragment mLoginFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreate");

        CONTENT = new String[] { "Last 7 Days" };

        setContentView(R.layout.explore_activity);
        mAq = new AQuery(this);
        mLoginFragment = (LoginFragment) getSupportFragmentManager()
            .findFragmentById(R.id.loginFragment);
        mLoginFragment.setNotNowListener(this);
        initViewPager();

        handleIntent(getIntent());
        //Appirater.appLaunched(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        ExplorePagerAdapter adapter = new ExplorePagerAdapter(
                getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        //TitlePageIndicator indicator = (TitlePageIndicator) findViewById(
        //        R.id.indicator);
        //indicator.setOnPageChangeListener(this);
        //indicator.setViewPager(viewPager);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Constants.DEBUG) Log.d(getLogTag(), "onNewIntent");

        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String scheme = intent.getScheme();
            if (Constants.CALLBACK_SCHEME.equals(scheme)) {
                Uri uri = intent.getData();
                String[] data = uri.getQuery().split("&");
                SharedPreferences prefs = getSharedPreferences(Constants
                        .PREFS_NAME, Context.MODE_PRIVATE);
                String oAuthSecret = prefs.getString(
                        Constants.KEY_TOKEN_SECRET, null);
                String oauthToken = data[0].substring(data[0]
                        .indexOf("=")+1);
                String oauthVerifier = data[1].substring(data[1]
                        .indexOf("=")+1);
                new GetAccessTokenTask(mLoginFragment).execute(oauthToken,
                        oAuthSecret, oauthVerifier);
            } else {
                if (Constants.DEBUG) {
                    Log.d(TAG, "Received intent but unknown scheme: " +
                            scheme);
                }
            }
        } else {
            if (Constants.DEBUG)
                Log.d(TAG, "Started with null intent");
        }
    }

    @Override
    public void onNotNowClicked() {
        mAq.id(R.id.loginFragment).invisible();
        Toast.makeText(this, getString(R.string.login_later),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public User getUser() {
        return mUser;
    }

    class ExplorePagerAdapter extends FragmentPagerAdapter {
        public ExplorePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public SherlockFragment getItem(int position) {
            switch (position) {
                case INTERESTING_PAGE:
                    return RecentPublicPhotosFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return ExploreActivity.CONTENT.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return ExploreActivity.CONTENT[
                position % ExploreActivity.CONTENT.length].toUpperCase();
        }
    }
}
