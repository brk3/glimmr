package com.bourke.glimmrpro.fragments.base;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.androidquery.AQuery;
import com.bourke.glimmrpro.activities.BaseActivity;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.OAuthUtils;
import com.bourke.glimmrpro.common.TextUtils;
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
    protected TextUtils mTextUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreate");

        mActivity = (BaseActivity) getSherlockActivity();
        mTextUtils = new TextUtils(mActivity.getAssets());
        mOAuth = OAuthUtils.loadAccessToken(mActivity);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOAuth == null || mOAuth.getUser() == null) {
            mOAuth = OAuthUtils.loadAccessToken(mActivity);
        }
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
    }

    protected String getLogTag() {
        return TAG;
    }
}
