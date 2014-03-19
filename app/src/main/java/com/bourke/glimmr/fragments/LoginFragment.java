package com.bourke.glimmr.fragments;

import com.bourke.glimmr.BuildConfig;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bourke.glimmr.R;
import com.bourke.glimmr.activities.MainActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.event.Events.IAccessTokenReadyListener;
import com.bourke.glimmr.event.Events.IRequestTokenReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.fragments.dialogs.LoginErrorDialog;
import com.bourke.glimmr.tasks.GetRequestToken;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * Presents a welcome to user and a button to login.
 *
 * The login button then follows the OAuth flow described at
 * http://www.flickr.com/services/api/auth.oauth.html :-
 *
 * Get a Request Token
 * Get the User's Authorization
 * Exchange the Request Token for an Access Token
 *
 * The access token is then persisted to SharedPreferences.
 */
public final class LoginFragment extends BaseFragment
        implements IRequestTokenReadyListener, IAccessTokenReadyListener {

    private static final String TAG = "Glimmr/LoginFragment";
    private IOnNotNowClicked mNotNowListener;

    public static LoginFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "onCreateView");
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.login_fragment, container, false);

        setupTextViews();

        return mLayout;
    }

    @Override
    public void onRequestTokenReady(String authUri, Exception e) {
        if (FlickrHelper.getInstance().handleFlickrUnavailable(mActivity, e)) {
            return;
        }
        if (e != null) {
            /* Usually down to a bad clock / timezone on device */
            if (e.getMessage().equals("No authentication challenges found") ||
                    e.getMessage().equals("Received authentication " +
                            "challenge is null")) {
                SimpleDialogFragment.createBuilder(mActivity, mActivity.getSupportFragmentManager())
                        .setTitle(R.string.login_problem)
                        .setMessage(R.string.timezone_message)
                        .setPositiveButtonText(android.R.string.ok)
                        .setCancelable(true)
                        .show();
            }
        } else if (authUri != null && !authUri.startsWith("error")) {
            mActivity.startActivity(new Intent(
                        Intent.ACTION_VIEW, Uri.parse(authUri)));
        }
    }

    @Override
    public void onAccessTokenReady(OAuth accessToken) {
        if (accessToken == null) {
            LoginErrorDialog.show(mActivity);
            return;
        }
        persistAccessToken(accessToken);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Got token, saved to disk, good to start MainActivity");
        }
        Toast.makeText(mActivity, getString(R.string.logged_in),
                Toast.LENGTH_SHORT).show();

        /* Prevent the user pressing back to get to the unauthed activity */
        Intent intent = new Intent(mActivity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mActivity.finish();

        mActivity.startActivity(intent);
    }

    private void persistAccessToken(OAuth oauth) {
        SharedPreferences sp = mActivity.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        OAuthToken token = oauth.getToken();
        User user = oauth.getUser();

        editor.putString(Constants.KEY_OAUTH_TOKEN, token.getOauthToken());
        editor.putString(Constants.KEY_TOKEN_SECRET, token
                .getOauthTokenSecret());
        editor.putString(Constants.KEY_ACCOUNT_USER_NAME, user.getUsername());
        editor.putString(Constants.KEY_ACCOUNT_USER_ID, user.getId());
        editor.commit();
    }

    private void setupTextViews() {
        /* Set fonts */
        mTextUtils.setFont((TextView) mLayout.findViewById(R.id.textWelcome),
                TextUtils.FONT_ROBOTOTHIN);
        mTextUtils.setFont((TextView) mLayout.findViewById(R.id.textTo),
                TextUtils.FONT_ROBOTOTHIN);
        mTextUtils.setFont((TextView) mLayout.findViewById(R.id.textGlimmr),
                TextUtils.FONT_ROBOTOREGULAR);
        mTextUtils.setFont((TextView) mLayout.findViewById(R.id.textNotNow),
                TextUtils.FONT_ROBOTOTHIN);

        Button buttonLogin = (Button) mLayout.findViewById(R.id.btnLogin);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetRequestToken(LoginFragment.this, mActivity).execute();
            }
        });

        final TextView tvNotNow = (TextView) mLayout.findViewById(
                R.id.textNotNow);
        mTextUtils.colorTextViewSpan(tvNotNow, tvNotNow.getText().toString(),
                mActivity.getString(R.string.browse),
                mActivity.getResources().getColor(
                    R.color.abs__holo_blue_light));

        tvNotNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotNowListener.onNotNowClicked();
            }
        });

        tvNotNow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mTextUtils.colorTextViewSpan(tvNotNow,
                        tvNotNow.getText().toString(),
                        mActivity.getString(R.string.browse),
                        mActivity.getResources().getColor(
                            R.color.flickr_pink));
                } else {
                    mTextUtils.colorTextViewSpan(tvNotNow,
                        tvNotNow.getText().toString(),
                        mActivity.getString(R.string.browse),
                        mActivity.getResources().getColor(
                            R.color.abs__holo_blue_light));
                }
                return false;
            }
        });
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public void setNotNowListener(IOnNotNowClicked listener) {
        mNotNowListener = listener;
    }

    public interface IOnNotNowClicked {
        public void onNotNowClicked();
    }
}
