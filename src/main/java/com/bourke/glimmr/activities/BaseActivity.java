package com.bourke.glimmr.activities;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.SearchView;
import android.widget.TextView;
import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.util.AQUtility;
import com.bourke.glimmr.R;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GlimmrAbCustomTitle;
import com.bourke.glimmr.common.OAuthUtils;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.tape.AddToGroupTaskQueueService;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.people.User;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public abstract class BaseActivity extends FragmentActivity {

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
        mActionBar = getActionBar();
        if (mActionBar != null) {
            if (!getResources().getBoolean(R.bool.sw600dp)) {
                new GlimmrAbCustomTitle(this).init(mActionBar);
            } else {
                mActionBar.setTitle("");
            }
        }
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

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
        if (Constants.DEBUG) Log.d(getLogTag(), "onDestroy");
        if (isTaskRoot()) {
            if (Constants.DEBUG) Log.d(getLogTag(), "Trimming file cache");
            AQUtility.cleanCacheAsync(this, Constants.CACHE_TRIM_TRIGGER_SIZE,
                   Constants.CACHE_TRIM_TARGET_SIZE);
        }
        Crouton.cancelAllCroutons();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
                        SettingsActivity.class);
                startActivity(preferencesActivity);
                return true;

            case R.id.menu_about:
                new AboutDialogFragment().show(getSupportFragmentManager(), "AboutDialogFragment");
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    class AboutDialogFragment extends eu.inmite.android.lib.dialogs.BaseDialogFragment {

        private PackageInfo mPackageInfo;

        public AboutDialogFragment() {
            try {
                mPackageInfo = getPackageManager().getPackageInfo(
                        getPackageName(), PackageManager.GET_META_DATA);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        private String getAppVersion() {
            String version = "None";
            if (mPackageInfo != null) {
               version = mPackageInfo.versionName;
            }
            return version;
        }

        @Override
        protected Builder build(Builder builder) {
            /* set dialog title */
            String title = String.format("About %s", getString(R.string.app_name));
            builder.setTitle(title);

            /* set dialog main message */
            final TextView message = new TextView(BaseActivity.this);
            SpannableString aboutText = new SpannableString(getString(R.string.about_text));
            String versionString = String.format("Version: %s", getAppVersion());
            message.setText(versionString + "\n\n" + aboutText);
            message.setTextSize(16);
            Linkify.addLinks(message, Linkify.ALL);
            builder.setView(message, 5, 5, 5, 5);

            /* set buttons (only add the "go pro" button to free version) */
            if (mPackageInfo != null && !mPackageInfo.packageName.contains("glimmrpro")) {
                builder.setNegativeButton(getString(R.string.pro_donate),
                        new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = Uri.parse(Constants.PRO_MARKET_LINK);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        dismiss();
                    }
                });
            }
            builder.setPositiveButton(getString(android.R.string.ok), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            return builder;
        }
    }
}
