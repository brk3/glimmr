package com.bourke.glimmr.activities;

import com.bourke.glimmr.BuildConfig;
import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

import com.bourke.glimmr.R;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GlimmrAbCustomTitle;
import com.bourke.glimmr.common.OAuthUtils;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.fragments.dialogs.BuyProDialog;
import com.bourke.glimmr.tape.AddToGroupTaskQueueService;
import com.bourke.glimmr.tape.AddToPhotosetTaskQueueService;
import com.bourke.glimmr.tape.UploadPhotoTaskQueueService;
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
            startTapeQueues();
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
            if (BuildConfig.DEBUG) Log.d(TAG, "onPause: set isFirstRun=false");
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
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "onDestroy");
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

            case R.id.menu_upload:
                if (Constants.PRO_VERSION) {
                    Intent localPhotosActivity = new Intent(getBaseContext(),
                            LocalPhotosActivity.class);
                    startActivity(localPhotosActivity);
                } else {
                    BuyProDialog.show(this);
                }
                return true;

            case R.id.menu_about:
                new AboutDialogFragment().show(getSupportFragmentManager(), "AboutDialogFragment");
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    /** Start each service for any pending tasks */
    private void startTapeQueues() {
        if (OAuthUtils.isLoggedIn(this)) {
            if (!AddToGroupTaskQueueService.IS_RUNNING) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Starting AddToGroupTaskQueueService");
                startService(new Intent(this, AddToGroupTaskQueueService.class));
            }
            if (!AddToPhotosetTaskQueueService.IS_RUNNING) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Starting AddToPhotosetTaskQueueService");
                startService(new Intent(this, AddToPhotosetTaskQueueService.class));
            }
            if (!UploadPhotoTaskQueueService.IS_RUNNING) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Starting UploadPhotoTaskQueueService");
                startService(new Intent(this, UploadPhotoTaskQueueService.class));
            }
        }
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
            } catch (PackageManager.NameNotFoundException e) {
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
            final TextView message = new TextView(getActivity());
            SpannableString aboutText = new SpannableString(getString(R.string.about_text));
            String versionString = String.format("Version: %s", getAppVersion());
            message.setText(versionString + "\n\n" + aboutText);
            message.setTextSize(16);
            Linkify.addLinks(message, Linkify.ALL);
            int padding = (int) getActivity().getResources()
                    .getDimension(R.dimen.dialog_message_padding);
            builder.setView(message, padding, padding, padding, padding);

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
