package com.bourke.glimmrpro.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

public class OAuthUtils {

    private static final String TAG = "Glimmr/OAuthUtils";

    public static boolean isLoggedIn(Context context) {
        OAuth oauth = loadAccessToken(context);
        boolean isLoggedIn = (oauth != null && oauth.getUser() != null);
        if (Constants.DEBUG) Log.d(TAG, "isLoggedIn: " + isLoggedIn);
        return isLoggedIn;
    }

    public static OAuth loadAccessToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
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
            if (Constants.DEBUG) {
                Log.w(TAG, "No saved oauth token found");
            }
            return null;
        }
        return oauth;
    }


}
