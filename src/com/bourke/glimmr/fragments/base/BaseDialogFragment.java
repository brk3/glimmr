package com.bourke.glimmr.fragments.base;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.util.Log;

import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockDialogFragment;

import com.androidquery.AQuery;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.Constants;

import com.googlecode.flickrjandroid.oauth.OAuth;

public abstract class BaseDialogFragment extends SherlockDialogFragment {

    private static final String TAG = "Glimmr/BaseDialogFragment";

    protected BaseActivity mActivity;

    /**
     * Should contain logged in user and valid access token for that user.
     */
    protected OAuth mOAuth;

    protected AQuery mAq;
    protected ViewGroup mLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreate");

        mActivity = (BaseActivity) getSherlockActivity();
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Constants.DEBUG) Log.d(getLogTag(), "onResume");

        /* Update our reference to the activity as it may have changed */
        mActivity = (BaseActivity) getSherlockActivity();
        startTask();
    }

    /**
     * Avoid being dismissed on rotate -
     * http://code.google.com/p/android/issues/detail?id=17423
     */
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    protected void startTask() {
        if (Constants.DEBUG) Log.d(getLogTag(), "startTask()");
        if (mOAuth == null || mOAuth.getUser() == null) {
            SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                    .PREFS_NAME, Context.MODE_PRIVATE);
            mOAuth = BaseActivity.loadAccessToken(prefs);
        }
    }

    protected void refresh() {
        Log.e(getLogTag(), "refresh");
    }

    protected String getLogTag() {
        return TAG;
    }
}
