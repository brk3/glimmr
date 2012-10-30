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

import android.graphics.Typeface;

import android.net.Uri;

import android.os.Bundle;

import android.text.SpannableString;
import android.text.util.Linkify;

import android.util.Log;

import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.util.AQUtility;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GlimmrAbCustomTitle;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

public abstract class BaseActivity extends SherlockFragmentActivity {

    private static final String TAG = "Glimmr/BaseActivity";

    /**
     * Account owner and valid access token for that user.
     */
    protected OAuth mOAuth;

    /**
     * User who's profile we're displaying, as distinct from the authorized
     * user.
     */
    protected User mUser;

    protected AQuery mAq;
    protected ActionBar mActionBar;

    private GlimmrAbCustomTitle mActionbarTitle;

    /** Fonts available for use */
    private Typeface mFontShadowsIntoLight;
    private Typeface mFontRobotoRegular;
    private Typeface mFontRobotoThin;
    private Typeface mFontRobotoLight;
    private Typeface mFontRobotoBold;

    public abstract User getUser();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_PRIVATE);
        mOAuth = loadAccessToken(prefs);
        if (mOAuth != null) {
            mUser = mOAuth.getUser();
        }
        if (Constants.DEBUG) {
            if (mUser == null || mOAuth == null) {
                Log.d(getLogTag(), "Proceeding unauthenticated");
            }
        }

        /* Set custom title on action bar (it will be null for dialog
         * activities */
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionbarTitle = new GlimmrAbCustomTitle(this);
            mActionbarTitle.init(mActionBar);
        }

        if (isTaskRoot()) {
            BitmapAjaxCallback.setCacheLimit(Constants.IMAGE_CACHE_LIMIT);
            BitmapAjaxCallback.setMaxPixelLimit(Constants.MEM_CACHE_PX_SIZE);
            if (Constants.DEBUG) {
                Log.d(getLogTag(), "IMAGE_CACHE_LIMIT: " +
                        Constants.IMAGE_CACHE_LIMIT);
                Log.d(getLogTag(), "MEM_CACHE_PX_SIZE: " +
                        Constants.MEM_CACHE_PX_SIZE);
            }
        }
    }

    public void setFont(TextView textView, final int font) {
        switch (font) {
            case Constants.FONT_SHADOWSINTOLIGHT:
                if (mFontShadowsIntoLight == null) {
                    mFontShadowsIntoLight = Typeface.createFromAsset(
                            getAssets(), Constants.FONT_PATH_SHADOWSINTOLIGHT);
                }
                textView.setTypeface(mFontShadowsIntoLight);
                break;

            case Constants.FONT_ROBOTOREGULAR:
                if (mFontRobotoRegular == null) {
                    mFontRobotoRegular = Typeface.createFromAsset(
                            getAssets(), Constants.FONT_PATH_ROBOTOREGULAR);
                }
                textView.setTypeface(mFontRobotoRegular);
                break;

            case Constants.FONT_ROBOTOTHIN:
                if (mFontRobotoThin == null) {
                    mFontRobotoThin = Typeface.createFromAsset(
                            getAssets(), Constants.FONT_PATH_ROBOTOTHIN);
                }
                textView.setTypeface(mFontRobotoThin);
                break;

            case Constants.FONT_ROBOTOLIGHT:
                if (mFontRobotoLight == null) {
                    mFontRobotoLight = Typeface.createFromAsset(
                            getAssets(), Constants.FONT_PATH_ROBOTOLIGHT);
                }
                textView.setTypeface(mFontRobotoLight);
                break;

            case Constants.FONT_ROBOTOBOLD:
                if (mFontRobotoBold == null) {
                    mFontRobotoBold = Typeface.createFromAsset(
                            getAssets(), Constants.FONT_PATH_ROBOTOBOLD);
                }
                textView.setTypeface(mFontRobotoBold);
                break;

            default:
                Log.e(getLogTag(), "Unknown font code: " + font);
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
        String userName = prefs.getString(Constants.KEY_ACCOUNT_USER_NAME,
                null);
        String userId = prefs.getString(Constants.KEY_ACCOUNT_USER_ID, null);

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
        return super.onCreateOptionsMenu(menu);
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

            case R.id.menu_search:
                onSearchRequested();
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
    public void onLowMemory() {
        if (Constants.DEBUG) {
            Log.d(getLogTag(), "onLowMemory: clearing mem cache");
        }
        BitmapAjaxCallback.clearCache();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    protected String getLogTag() {
        return TAG;
    }
}
