package com.bourke.glimmrpro.tasks;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.FlickrHelper;
import com.bourke.glimmrpro.event.Events.IPhotoListReadyListener;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.SearchParameters;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchPhotosTask extends AsyncTask<OAuth, Void, List<Photo>> {

    private static final String TAG = "Glimmr/SearchPhotosTask";

    private IPhotoListReadyListener mListener;
    private Photoset mPhotoset;
    private int mPage;
    private String mSearchTerm;

    public SearchPhotosTask(IPhotoListReadyListener listener,
            String searchTerm, int page) {
        mListener = listener;
        mPage = page;
        mSearchTerm = searchTerm;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Photo> doInBackground(OAuth... params) {
        OAuth oauth = params[0];
        Set<String> extras = new HashSet<String>();
        extras.add("owner_name");
        extras.add("url_q");
        extras.add("url_l");
        extras.add("views");

        SearchParameters sp = new SearchParameters();
        sp.setExtras(extras);
        sp.setText(mSearchTerm);
        sp.setSort(SearchParameters.RELEVANCE);

        if (oauth != null) {
            OAuthToken token = oauth.getToken();
            Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                    token.getOauthToken(), token.getOauthTokenSecret());
            if (Constants.DEBUG) Log.d(TAG, "Fetching page " + mPage);
            try {
                return f.getPhotosInterface().search(
                        sp, Constants.FETCH_PER_PAGE, mPage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (Constants.DEBUG) Log.d(TAG, "Making unauthenticated call");
            if (Constants.DEBUG) Log.d(TAG, "Fetching page " + mPage);
            try {
                return FlickrHelper.getInstance().getPhotosInterface()
                    .search(sp, Constants.FETCH_PER_PAGE, mPage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Photo> result) {
        if (result == null) {
            Log.e(TAG, "Error fetching photolist, result is null");
            result = Collections.EMPTY_LIST;
        }
        mListener.onPhotosReady(result);
    }

    @Override
    protected void onCancelled(final List<Photo> result) {
        if (Constants.DEBUG) Log.d(TAG, "onCancelled");
    }
}
