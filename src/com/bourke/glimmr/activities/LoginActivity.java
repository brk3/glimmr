package com.bourke.glimmr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;

import android.os.Bundle;

import android.util.Log;

import android.view.View;

import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.people.User;

public class LoginActivity extends SherlockFragmentActivity
        implements IRequestTokenReadyListener, IAccessTokenReadyListener {

    private static final String TAG = "Glimmr/LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);
        getSupportActionBar().hide();
    }

    public void loginUser(View view) {
        new GetRequestToken(this, this).execute();
    }

    @Override
    public void onRequestTokenReady(String authUri) {
		if (authUri != null && !authUri.startsWith("error")) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUri)));
		} else {
            Log.e(TAG, "Error getting request token in onRequestTokenReady");
		}
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

        if (intent != null) {
            String scheme = intent.getScheme();
            if (Constants.CALLBACK_SCHEME.equals(scheme)) {
                Uri uri = intent.getData();
                String query = uri.getQuery();
                Log.d(TAG, "Returned Query: " + query);

                String[] data = query.split("&");
                if (data != null && data.length == 2) {
                    String oauthToken = data[0].substring(data[0]
                            .indexOf("=")+1);
                    String oAuthSecret = getSavedOAuthSecret();
                    String oauthVerifier = data[1].substring(data[1]
                            .indexOf("=")+1);
                    new GetAccessTokenTask(this).execute(oauthToken,
                            oAuthSecret, oauthVerifier);
                }
            }
        } else {
            Log.d(TAG, "Received null intent");
        }
	}

    @Override
    public void onAccessTokenReady(OAuth accessToken) {
        persistAccessToken(accessToken);
        Log.d(TAG, "Got token, saved to disk, good to start MainActivity");
        Toast.makeText(this, "Logged In ^_^", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
    }

    private String getSavedOAuthSecret() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_PRIVATE);
        String tokenSecret = prefs.getString(Constants.KEY_TOKEN_SECRET, null);
        return tokenSecret;
    }

    private void persistAccessToken(OAuth oauth) {
        SharedPreferences sp = getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        OAuthToken token = oauth.getToken();
        User user = oauth.getUser();

        editor.putString(Constants.KEY_OAUTH_TOKEN, token.getOauthToken());
        editor.putString(Constants.KEY_TOKEN_SECRET, token
                .getOauthTokenSecret());
		editor.putString(Constants.KEY_USER_NAME, user.getUsername());
		editor.putString(Constants.KEY_USER_ID, user.getId());
        editor.commit();
    }
}
