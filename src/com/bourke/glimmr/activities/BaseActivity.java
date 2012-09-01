package com.bourke.glimmr.activities;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.support.v4.view.ViewPager;

import android.text.SpannableString;
import android.text.util.Linkify;

import android.util.Log;
import android.util.Log;

import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GlimmrAbCustomTitle;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import android.net.Uri;

public abstract class BaseActivity extends SherlockFragmentActivity
        implements ViewPager.OnPageChangeListener {

    private static final String TAG = "Glimmr/BaseActivity";

    /**
     * Should contain current user and valid access token for that user.
     */
    protected OAuth mOAuth;

    protected AQuery mAq;

    protected ActionBar mActionBar;

    private MenuItem mMenuItemProgress;
    private MenuItem mMenuItemRefresh;

    private GlimmrAbCustomTitle mActionbarTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_PRIVATE);
        mOAuth = loadAccessToken(prefs);
        mActionBar = getSupportActionBar();

        /* Set custom title on action bar (it will be null for dialog
         * activities */
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionbarTitle = new GlimmrAbCustomTitle(getBaseContext());
            mActionbarTitle.init(mActionBar);
        }
    }

    /**
     * Clean the file cache when root activity exits.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Constants.DEBUG)
            Log.d(getLogTag(), "onDestroy");
        if (isTaskRoot()) {
            if (Constants.DEBUG)
                Log.d(getLogTag(), "Trimming file cache");
            AQUtility.cleanCacheAsync(this, Constants.CACHE_TRIM_TRIGGER_SIZE,
                   Constants.CACHE_TRIM_TARGET_SIZE);
        }
    }

    /**
     * Have to pass prefs as they can't be loaded from a static context
     */
    public static OAuth loadAccessToken(SharedPreferences prefs) {
        String oauthTokenString = prefs.getString(Constants.KEY_OAUTH_TOKEN,
                null);
        String tokenSecret = prefs.getString(Constants.KEY_TOKEN_SECRET, null);
        String userName = prefs.getString(Constants.KEY_USER_NAME, null);
        String userId = prefs.getString(Constants.KEY_USER_ID, null);

        OAuth oauth = null;
        if (oauthTokenString != null && tokenSecret != null && userName != null
                && userId != null) {
            oauth = new OAuth();
            OAuthToken oauthToken = new OAuthToken();
            oauth.setToken(oauthToken);
            oauthToken.setOauthToken(oauthTokenString);
            oauthToken.setOauthTokenSecret(tokenSecret);

            User user = new User();
            user.setUsername(userName);
            user.setId(userId);
            oauth.setUser(user);
        } else {
            if (Constants.DEBUG)
                Log.w(TAG, "No saved oauth token found");
            return null;
        }
        return oauth;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);
        mMenuItemProgress = menu.findItem(R.id.menu_progress);
        mMenuItemRefresh = menu.findItem(R.id.menu_refresh);
        showProgressIcon(false);
        return true;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case Constants.DIALOG_ABOUT:
                return showAboutDialog();
        }
        return null;
    }

    private Dialog showAboutDialog() {
        PackageInfo pInfo;
        String versionInfo = "Unknown";
        try {
            pInfo = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
            versionInfo = pInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        String aboutTitle = String.format("About %s",
                getString(R.string.app_name));
        String versionString = String.format("Version: %s", versionInfo);

        final TextView message = new TextView(this);
        message.setPadding(5, 5, 5, 5);
        SpannableString aboutText = new SpannableString(
                getString(R.string.about_text));
        message.setText(versionString + "\n\n" + aboutText);
        message.setTextSize(16);
        Linkify.addLinks(message, Linkify.ALL);

        return new AlertDialog.Builder(this).
            setTitle(aboutTitle).
            setCancelable(true).
            setIcon(R.drawable.ic_launcher).
            setNegativeButton(getString(R.string.pro_donate),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        Uri uri = Uri.parse(Constants.PRO_MARKET_LINK);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
            .setPositiveButton(getString(android.R.string.ok), null).
            setView(message).create();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                /* This is called when the Home (Up) button is pressed
                 * in the Action Bar. */
                Intent parentActivityIntent = new Intent(this,
                        MainActivity.class);
                parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);
                finish();
                return true;

            case R.id.menu_preferences:
                Intent preferencesActivity = new Intent(getBaseContext(),
                        PreferencesActivity.class);
                startActivity(preferencesActivity);
                return true;

            case R.id.menu_about:
                showDialog(Constants.DIALOG_ABOUT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showProgressIcon(boolean show) {
        if (mMenuItemProgress != null && mMenuItemRefresh != null) {
            if (show) {
                mMenuItemProgress.setVisible(true);
                mMenuItemRefresh.setVisible(false);
            } else {
                mMenuItemRefresh.setVisible(true);
                mMenuItemProgress.setVisible(false);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onPageScrolled(int pos, float posOffset, int posOffsetPx) {}

    @Override
    public void onPageSelected(int pos) {}

    protected String getLogTag() {
        return TAG;
    }
}
