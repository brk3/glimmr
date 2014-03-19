package com.bourke.glimmr.fragments.base;

import com.bourke.glimmr.BuildConfig;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ViewGroup;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.OAuthUtils;
import com.bourke.glimmr.common.TextUtils;
import com.googlecode.flickrjandroid.oauth.OAuth;

public abstract class BaseDialogFragment extends DialogFragment {

    private static final String TAG = "Glimmr/BaseDialogFragment";

    protected BaseActivity mActivity;

    /**
     * Should contain logged in user and valid access token for that user.
     */
    protected OAuth mOAuth;

    protected ViewGroup mLayout;
    protected TextUtils mTextUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "onCreate");

        mActivity = (BaseActivity) getActivity();
        mTextUtils = new TextUtils(mActivity.getAssets());
        mOAuth = OAuthUtils.loadAccessToken(mActivity);

        setRetainInstance(true);
        
        /* prevents parent from losing it's menu items */
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOAuth == null || mOAuth.getUser() == null) {
            mOAuth = OAuthUtils.loadAccessToken(mActivity);
        }
        /* Update our reference to the activity as it may have changed */
        mActivity = (BaseActivity) getActivity();
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
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "startTask()");
    }

    protected String getLogTag() {
        return TAG;
    }
}
