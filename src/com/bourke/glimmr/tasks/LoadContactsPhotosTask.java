package com.bourke.glimmr;

import android.os.AsyncTask;

import android.util.Log;

import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.FlickrException;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.people.User;
import com.gmail.yuyang226.flickr.photos.PhotoList;

import java.io.IOException;

import java.util.HashSet;
import java.util.Set;

public class LoadContactsPhotosTask extends AsyncTask<OAuth, Void, PhotoList> {

    private static final String TAG = "Glimmr/LoadContactsPhotosTask";

    private IPhotoListReadyListener mListener;

	public LoadContactsPhotosTask(IPhotoListReadyListener listener) {
        mListener = listener;
	}

	@Override
	protected PhotoList doInBackground(OAuth... arg0) {
		OAuthToken token = arg0[0].getToken();
		Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                token.getOauthToken(), token.getOauthTokenSecret());
		User user = arg0[0].getUser();
		try {
            int amountToFetch = 50;  // Max is 50
            Set<String> extras = new HashSet<String>();
            extras.add("owner_name");
            extras.add("url_q");
            extras.add("url_l");
            extras.add("views");
            boolean justFriends = false;
            boolean singlePhoto = false;
            boolean includeSelf = false;
			return f.getPhotosInterface().getContactsPhotos(amountToFetch,
                    extras, justFriends, singlePhoto, includeSelf);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FlickrException e) {
            e.printStackTrace();
        } catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(final PhotoList result) {
		if (result != null) {
            boolean cancelled = false;
            mListener.onPhotosReady(result, cancelled);
		} else {
            Log.e(TAG, "error fetching contacts/photos, result is null");
            // TODO: alert user / recover
        }
	}
}
