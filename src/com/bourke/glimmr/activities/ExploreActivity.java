package com.bourke.glimmrpro.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import android.util.Log;

import android.widget.Toast;
import android.widget.Toast;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem;

import com.androidquery.AQuery;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.fragments.explore.RecentPublicPhotosFragment;
import com.bourke.glimmrpro.fragments.LoginFragment;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.tasks.GetAccessTokenTask;

import com.googlecode.flickrjandroid.people.User;

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
        refreshLoginFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login:
                setLoginFragmentVisibility(true);
                SharedPreferences sp = getSharedPreferences(
                        Constants.PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(Constants.LOGIN_LATER_SELECTED, false);
                editor.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        setLoginFragmentVisibility(false);
        SharedPreferences sp =
            getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(Constants.LOGIN_LATER_SELECTED, true);
        editor.commit();
        Toast.makeText(this, getString(R.string.login_later),
                Toast.LENGTH_LONG).show();
    }

    private void setLoginFragmentVisibility(boolean show) {
        FragmentTransaction ft =
            getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        if (show) {
            ft.show(mLoginFragment);
        } else {
            ft.hide(mLoginFragment);
        }
        ft.commit();
    }

    private void refreshLoginFragment() {
        SharedPreferences sp =
            getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        boolean loginLater =
            sp.getBoolean(Constants.LOGIN_LATER_SELECTED, false);
        if (loginLater) {
            setLoginFragmentVisibility(false);
        } else {
            setLoginFragmentVisibility(true);
        }
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
