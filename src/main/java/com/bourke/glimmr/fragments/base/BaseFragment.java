package com.bourke.glimmrpro.fragments.base;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.androidquery.AQuery;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.activities.BaseActivity;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.OAuthUtils;
import com.bourke.glimmrpro.common.TextUtils;
import com.googlecode.flickrjandroid.oauth.OAuth;

public abstract class BaseFragment extends SherlockFragment {

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
    protected AQuery mAq;
    protected ViewGroup mLayout;
    protected TextUtils mTextUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreate");

        mActivity = (BaseActivity) getSherlockActivity();
        mActionBar = mActivity.getSupportActionBar();
        mTextUtils = new TextUtils(mActivity.getAssets());

        setRetainInstance(shouldRetainInstance());
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Constants.DEBUG) Log.d(getLogTag(), "onResume");

        /* Update our reference to the activity as it may have changed */
        mActivity = (BaseActivity) getSherlockActivity();
        mActionBar = mActivity.getSupportActionBar();

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
        } else {
            menu.findItem(R.id.menu_login).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Constants.DEBUG) Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void startTask() {
        if (Constants.DEBUG) Log.d(getLogTag(), "startTask()");
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
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
