package com.bourke.glimmr.common;

import com.bourke.glimmr.BuildConfig;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class UsageTips {

    private static final String TAG = "Glimmr/UsageTips";

    private static final UsageTips SINGLETON = new UsageTips();
    private UsageTips() {
    }

    private static final Set<String> mShownTips = new HashSet<String>();

    public static UsageTips getInstance() {
        return SINGLETON;
    }

    /**
     * Show a usage tip via an INFO style crouton.  Tips are shown once per session unless the
     * force param is true.
     * @param activity
     * @param tip
     * @param force
     */
    public void show(Activity activity, String tip, boolean force) {
        SharedPreferences defaultSharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(activity);
        boolean enable = defaultSharedPrefs.getBoolean(Constants.KEY_ENABLE_USAGE_TIPS, false);
        if (!enable) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Usage tips disabled in preferences");
            return;
        }
        if (!force && !mShownTips.contains(tip)) {
            Crouton.cancelAllCroutons();
            Crouton.makeText(activity, tip, Style.INFO).show();
            mShownTips.add(tip);
        }
    }
}
