package com.bourke.glimmr;

import android.app.ProgressDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;

import android.net.Uri;

import android.os.AsyncTask;

import android.widget.Toast;

import com.gmail.yuyang226.flickr.auth.Permission;
import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;

import java.net.URL;
import android.util.Log;

/**
 * This task generates the OAuth url and then opens it in a browser to request
 * access to the user's Flickr account.
 */
public class OAuthTask extends AsyncTask<Void, Integer, String> {

    private static final String TAG = "Glimmr/OAuthTask";

	private static final Uri OAUTH_CALLBACK_URI = Uri.parse(
            Constants.CALLBACK_SCHEME + "://oauth");

    private BaseFragment mFragment;
	private ProgressDialog mProgressDialog;
    private MainActivity mActivity;
    private Context mContext;

	public OAuthTask(BaseFragment fragment) {
		super();
        mFragment = fragment;
		mActivity = (MainActivity)fragment.getSherlockActivity();
        mContext = mActivity.getApplicationContext();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog = ProgressDialog.show(mActivity, "",
                "Generating the authorization request...");
		mProgressDialog.setCanceledOnTouchOutside(true);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dlg) {
				OAuthTask.this.cancel(true);
			}
		});
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			Flickr f = FlickrHelper.getInstance().getFlickr();
			OAuthToken oauthToken = f.getOAuthInterface().getRequestToken(
					OAUTH_CALLBACK_URI.toString());
			saveTokenSecret(oauthToken.getOauthTokenSecret());
			URL oauthUrl = f.getOAuthInterface().buildAuthenticationUrl(
					Permission.READ, oauthToken);
			return oauthUrl.toString();
		} catch (Exception e) {
            e.printStackTrace();
			return "error:" + e.getMessage();
		}
	}

	/**
	 * Saves the oauth token secret.
	 *
	 * @param tokenSecret
	 */
	private void saveTokenSecret(String tokenSecret) {
		Log.d(TAG, "request token: " + tokenSecret);
		mFragment.saveOAuthToken(null, null, null, tokenSecret);
		Log.d(TAG, "oauth token secret saved: " + tokenSecret);
	}

	@Override
	protected void onPostExecute(String result) {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		if (result != null && !result.startsWith("error") ) {
			mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri
					.parse(result)));
		} else {
			Toast.makeText(mActivity, result, Toast.LENGTH_LONG).show();
		}
	}
}
