package com.bourke.glimmr;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.graphics.Color;

import android.net.Uri;

import android.os.Bundle;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.people.User;

import com.viewpagerindicator.TabPageIndicator;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends SherlockFragmentActivity
    implements ViewPager.OnPageChangeListener {

    private static final String TAG = "Glimmr/MainActivity";
    private static final Logger logger = LoggerFactory.getLogger(
            MainActivity.class);

    public static final int PHOTOSTREAM_PAGE = 0;
    public static final int FRIENDS_PAGE = 1;
    public static final int GROUPS_PAGE = 2;

    private PhotoStreamFragment mPhotoStreamFragment =
        new PhotoStreamFragment();

    private FriendsFragment mFriendsFragment = new FriendsFragment();

    private GroupsFragment mGroupsFragment = new GroupsFragment();

    private int mCurrentPage = PHOTOSTREAM_PAGE;

    private Context mContext;

    private int mStackLevel = 0;

    //TODO: add to R.strings
    public static final String[] CONTENT =
        new String[] { "Photostream", "Friends", "Groups" };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");
        mContext = getApplicationContext();
        setContentView(R.layout.main);
        initViewPager();

        if (savedInstanceState != null) {
            mStackLevel = savedInstanceState.getInt("level");
        }

        OAuth oauth = getOAuthToken();
        if (oauth == null || oauth.getUser() == null) {
            OAuthTask task = new OAuthTask(this);
            task.execute();
        } else {
            //load(oauth);
        }
    }

    private OAuth getOAuthToken() {
        /* Restore preferences */
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_PRIVATE);
        String oauthTokenString = settings.getString(Constants.KEY_OAUTH_TOKEN,
                null);
        String tokenSecret = settings.getString(Constants.KEY_TOKEN_SECRET,
                null);
        if (oauthTokenString == null && tokenSecret == null) {
            logger.warn("No oauth token retrieved");
            return null;
        }

        OAuth oauth = new OAuth();
        String userName = settings.getString(Constants.KEY_USER_NAME, null);
        String userId = settings.getString(Constants.KEY_USER_ID, null);
        if (userId != null) {
            User user = new User();
            user.setUsername(userName);
            user.setId(userId);
            oauth.setUser(user);
        }

        OAuthToken oauthToken = new OAuthToken();
        oauth.setToken(oauthToken);
        oauthToken.setOauthToken(oauthTokenString);
        oauthToken.setOauthTokenSecret(tokenSecret);
        logger.debug("Retrieved token from preference store: " +
                "oauth token={}, and token secret={}", oauthTokenString,
                tokenSecret);

        return oauth;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /* This is very important, otherwise you would get a null Scheme in the
         * onResume later on. */
        setIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String scheme = intent.getScheme();
        OAuth savedToken = getOAuthToken();
        if (Constants.CALLBACK_SCHEME.equals(scheme) &&
                (savedToken == null || savedToken.getUser() == null)) {
            Uri uri = intent.getData();
            String query = uri.getQuery();
            logger.debug("Returned Query: {}", query);
            String[] data = query.split("&");
            if (data != null && data.length == 2) {
                String oauthToken = data[0].substring(data[0].indexOf("=")
                        + 1);
                String oauthVerifier = data[1].substring(data[1].indexOf("=")
                        + 1);
                logger.debug("OAuth Token: {}; OAuth Verifier: {}", oauthToken,
                        oauthVerifier);
                OAuth oauth = getOAuthToken();
                if (oauth != null && oauth.getToken() != null &&
                        oauth.getToken().getOauthTokenSecret() != null) {
                    GetOAuthTokenTask task = new GetOAuthTokenTask(this);
                    task.execute(oauthToken, oauth.getToken()
                            .getOauthTokenSecret(), oauthVerifier);
                }
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onPageScrolled(int pos, float posOffset, int posOffsetPx) {}

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected");
        mCurrentPage = position;
        switch (position) {
            case PHOTOSTREAM_PAGE:
                //mPhotoStreamFragment.refresh();
                break;
            case FRIENDS_PAGE:
                //mFriendsFragment.refresh();
                break;
            case GROUPS_PAGE:
                //mGroupsFragment.refresh();
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("level", mStackLevel);
    }

    public void onOAuthDone(OAuth result) {
        if (result == null) {
            Toast.makeText(this, "Authorization failed",
                    Toast.LENGTH_LONG).show();
        } else {
            User user = result.getUser();
            OAuthToken token = result.getToken();
            if (user == null || user.getId() == null || token == null
                    || token.getOauthToken() == null
                    || token.getOauthTokenSecret() == null) {
                Toast.makeText(this, "Authorization failed",
                        Toast.LENGTH_LONG).show();
                return;
                    }
            String message = String.format(Locale.US,
                    "Authorization Succeed: user=%s, userId=%s, " +
                    "oauthToken=%s, tokenSecret=%s",
                    user.getUsername(), user.getId(), token.getOauthToken(),
                    token.getOauthTokenSecret());
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            saveOAuthToken(user.getUsername(), user.getId(),
                    token.getOauthToken(), token.getOauthTokenSecret());
            //load(result);
        }
    }

    public void saveOAuthToken(String userName, String userId, String token,
            String tokenSecret) {
        logger.debug("Saving userName=%s, userId=%s, oauth token={}, and " +
                "token secret={}", new String[]{userName, userId, token,
                    tokenSecret});
        SharedPreferences sp = getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putString(Constants.KEY_OAUTH_TOKEN, token);
        editor.putString(Constants.KEY_TOKEN_SECRET, tokenSecret);
        editor.putString(Constants.KEY_USER_NAME, userName);
        editor.putString(Constants.KEY_USER_ID, userId);
        editor.commit();
    }

    private void initViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        GlimmrPagerAdapter adapter = new GlimmrPagerAdapter(
                getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        TabPageIndicator indicator =
            (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setOnPageChangeListener(this);
        indicator.setViewPager(viewPager);
    }

    class GlimmrPagerAdapter extends FragmentPagerAdapter {

        public GlimmrPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public SherlockFragment getItem(int position) {
            switch (position) {
                case PHOTOSTREAM_PAGE:
                    return mPhotoStreamFragment;
                case FRIENDS_PAGE:
                    return mFriendsFragment;
                case GROUPS_PAGE:
                    return mGroupsFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return MainActivity.CONTENT.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return MainActivity.CONTENT[position % MainActivity.CONTENT.length]
                .toUpperCase();
        }
    }
}
