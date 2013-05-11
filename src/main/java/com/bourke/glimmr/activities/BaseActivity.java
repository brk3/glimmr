package com.bourke.glimmrpro.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;

import android.net.Uri;

import android.os.Bundle;

import android.preference.PreferenceManager;

import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

import android.text.SpannableString;
import android.text.util.Linkify;

import android.util.Log;

import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.widget.SearchView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.util.AQUtility;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.GlimmrAbCustomTitle;
import com.bourke.glimmrpro.common.OAuthUtils;
import com.bourke.glimmrpro.common.TextUtils;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.tape.AddToGroupTaskQueueService;

import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.people.User;

import de.keyboardsurfer.android.widget.crouton.Crouton;

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

    protected TextUtils mTextUtils;

    public abstract User getUser();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        /* Load the users oauth token */
        mOAuth = OAuthUtils.loadAccessToken(this);
        if (mOAuth != null) {
            mUser = mOAuth.getUser();
        }

        mTextUtils = new TextUtils(getAssets());

        /* Set custom title on action bar (it will be null for dialog
         * activities */
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            if (!getResources().getBoolean(R.bool.sw600dp)) {
                new GlimmrAbCustomTitle(this).init(mActionBar);
            } else {
                mActionBar.setTitle("");
            }
        }
        setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);

        /* Load default preferences */
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (isTaskRoot()) {
            /* Tune the aquery cache */
            BitmapAjaxCallback.setCacheLimit(Constants.IMAGE_CACHE_LIMIT);
            BitmapAjaxCallback.setMaxPixelLimit(Constants.MEM_CACHE_PX_SIZE);
            if (Constants.DEBUG) {
                Log.d(getLogTag(), "IMAGE_CACHE_LIMIT: " +
                        Constants.IMAGE_CACHE_LIMIT);
                Log.d(getLogTag(), "MEM_CACHE_PX_SIZE: " +
                        Constants.MEM_CACHE_PX_SIZE);
            }

            /* Start the Group service for any pending tasks */
            if ( ! AddToGroupTaskQueueService.IS_RUNNING &&
                    OAuthUtils.isLoggedIn(this)) {
                if (Constants.DEBUG) {
                    Log.d(TAG, "Starting AddToGroupTaskQueueService");
                }
                startService(new Intent(this,
                            AddToGroupTaskQueueService.class));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        final SharedPreferences prefs = getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        final boolean isFirstRun = prefs.getBoolean(
                Constants.KEY_IS_FIRST_RUN, true);
        if (isFirstRun) {
            if (Constants.DEBUG) Log.d(TAG, "onPause: set isFirstRun=false");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Constants.KEY_IS_FIRST_RUN, false);
            editor.commit();
        }
    }

    /**
     * Clean the file cache when root activity exits.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Constants.DEBUG) {
            Log.d(getLogTag(), "onDestroy");
        }
        if (isTaskRoot()) {
            if (Constants.DEBUG)
                Log.d(getLogTag(), "Trimming file cache");
            AQUtility.cleanCacheAsync(this, Constants.CACHE_TRIM_TRIGGER_SIZE,
                   Constants.CACHE_TRIM_TARGET_SIZE);
        }
        Crouton.cancelAllCroutons();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);
        SearchManager searchManager =
            (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
            (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                /* This is called when the Home (Up) button is pressed
                 * in the Action Bar. http://goo.gl/lJxjA */
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.from(this)
                        .addNextIntent(upIntent)
                        .startActivities();
                    finish();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
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
