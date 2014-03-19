package com.bourke.glimmr.fragments.base;

import com.bourke.glimmr.BuildConfig;
import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.bourke.glimmr.R;
import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.OAuthUtils;
import com.bourke.glimmr.common.TextUtils;
import com.googlecode.flickrjandroid.oauth.OAuth;

import eu.inmite.android.lib.dialogs.ISimpleDialogListener;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

public abstract class BaseFragment extends Fragment implements ISimpleDialogListener {

    private static final String TAG = "Glimmr/BaseFragment";

    /**
     * It's useful to keep a reference to the parent activity in our fragments.
     */
    protected BaseActivity mActivity;

    /**
     * Should contain logged in user and valid access token for that user.
     */
    protected OAuth mOAuth;

    protected ActionBar mActionBar;
    protected ViewGroup mLayout;
    protected TextUtils mTextUtils;
    protected SharedPreferences mDefaultSharedPrefs;

    private static final int DIALOG_LOGOUT_CONFIRMATION = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "onCreate");

        mActivity = (BaseActivity) getActivity();
        mActionBar = mActivity.getActionBar();
        mTextUtils = new TextUtils(mActivity.getAssets());
        mDefaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);

        setRetainInstance(shouldRetainInstance());
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "onResume");

        /* Update our reference to the activity as it may have changed */
        mActivity = (BaseActivity) getActivity();
        mActionBar = mActivity.getActionBar();

        startTask();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.fragment_main_menu, menu);
        SearchManager searchManager =
            (SearchManager) mActivity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
            (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(mActivity.getComponentName()));
        if (mOAuth == null || mOAuth.getUser() == null) {
            menu.findItem(R.id.menu_login).setVisible(true);
            menu.findItem(R.id.menu_logout).setVisible(false);
        } else {
            menu.findItem(R.id.menu_login).setVisible(false);
            menu.findItem(R.id.menu_logout).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                return true;
            case R.id.menu_logout:
                SimpleDialogFragment.createBuilder(mActivity, mActivity.getSupportFragmentManager())
                        .setTitle("Logout")
                        .setMessage("Are you sure?")
                        .setPositiveButtonText(android.R.string.yes)
                        .setNegativeButtonText(android.R.string.cancel)
                        .setCancelable(true)
                        .setTargetFragment(this, DIALOG_LOGOUT_CONFIRMATION)
                        .setRequestCode(DIALOG_LOGOUT_CONFIRMATION)
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {
        if (requestCode == DIALOG_LOGOUT_CONFIRMATION) {
            OAuthUtils.logout(mActivity);
        }
    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {
    }

    protected void startTask() {
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "startTask()");
        if (mOAuth == null || mOAuth.getUser() == null) {
            mOAuth = OAuthUtils.loadAccessToken(mActivity);
        }
    }

    protected void refresh() {
        Log.d(getLogTag(), "refresh");
    }

    protected boolean shouldRetainInstance() {
        return true;
    }

    protected String getLogTag() {
        return TAG;
    }
}
