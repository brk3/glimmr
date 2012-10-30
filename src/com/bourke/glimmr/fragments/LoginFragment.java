package com.bourke.glimmr.fragments;

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

import com.bourke.glimmr.activities.MainActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IAccessTokenReadyListener;
import com.bourke.glimmr.event.Events.IRequestTokenReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.GetRequestToken;

import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

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
        if (Constants.DEBUG) Log.d(TAG, "newInstance");
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreateView");
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.login_fragment, container, false);

        setupTextViews();

        return mLayout;
    }

    @Override
    public void onRequestTokenReady(String authUri) {
        if (authUri != null && !authUri.startsWith("error")) {
            mActivity.startActivity(new Intent(
                        Intent.ACTION_VIEW, Uri.parse(authUri)));
        } else {
            if (Constants.DEBUG) {
                Log.e(TAG, "Error getting request token in " +
                        "onRequestTokenReady");
            }
        }
    }

    @Override
    public void onAccessTokenReady(OAuth accessToken) {
        persistAccessToken(accessToken);
        if (Constants.DEBUG) {
            Log.d(TAG, "Got token, saved to disk, good to start MainActivity");
        }
        Toast.makeText(mActivity, getString(R.string.logged_in),
                Toast.LENGTH_SHORT).show();
        mActivity.startActivity(new Intent(mActivity, MainActivity.class));

        /* Prevent the user pressing back to get to the unauthed activity */
        mActivity.finish();
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
        mActivity.setFont((TextView) mLayout.findViewById(R.id.textWelcome),
                Constants.FONT_ROBOTOTHIN);
        mActivity.setFont((TextView) mLayout.findViewById(R.id.textTo),
                Constants.FONT_ROBOTOTHIN);
        mActivity.setFont((TextView) mLayout.findViewById(R.id.textGlimmr),
                Constants.FONT_ROBOTOREGULAR);
        mActivity.setFont((TextView) mLayout.findViewById(R.id.textNotNow),
                Constants.FONT_ROBOTOTHIN);

        Button buttonLogin = (Button) mLayout.findViewById(R.id.btnLogin);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetRequestToken(LoginFragment.this, mActivity).execute();
            }
        });

        final TextView tvNotNow = (TextView) mLayout.findViewById(
                R.id.textNotNow);
        colorTextViewSpan(tvNotNow, tvNotNow.getText().toString(),
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
                    colorTextViewSpan(tvNotNow, tvNotNow.getText().toString(),
                        mActivity.getString(R.string.browse),
                        mActivity.getResources().getColor(
                            R.color.flickr_pink));
                } else {
                    colorTextViewSpan(tvNotNow, tvNotNow.getText().toString(),
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
