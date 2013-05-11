package com.bourke.glimmrpro.tasks;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmrpro.activities.SearchActivity;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.FlickrHelper;
import com.bourke.glimmrpro.event.Events.IPhotoListReadyListener;
import com.bourke.glimmrpro.fragments.search.AbstractPhotoSearchGridFragment;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.SearchParameters;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("EmptyMethod")
public class SearchPhotosTask extends AsyncTask<OAuth, Void, List<Photo>> {

    private static final String TAG = "Glimmr/SearchPhotosTask";

    private final IPhotoListReadyListener mListener;
    private Photoset mPhotoset;
    private final int mPage;
    private final String mSearchTerm;
    private final int mSortType;
    private String mUserId;

    public SearchPhotosTask(IPhotoListReadyListener listener,
            String searchTerm, int sortType, int page) {
        mListener = listener;
        mPage = page;
        mSearchTerm = searchTerm;
        mSortType = sortType;
    }

    public SearchPhotosTask(IPhotoListReadyListener listener,
            String searchTerm, int sortType, int page, String userId) {
        this(listener, searchTerm, sortType, page);
        mUserId = userId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Photo> doInBackground(OAuth... params) {
        OAuth oauth = params[0];

        SearchParameters sp = new SearchParameters();
        sp.setExtras(Constants.EXTRAS);
        sp.setText(mSearchTerm);
        if (mUserId != null) {
            sp.setUserId(mUserId);
        }
        switch (mSortType) {
            case AbstractPhotoSearchGridFragment.SORT_TYPE_RECENT:
                if (Constants.DEBUG) Log.d(TAG, "Search type:RECENT");
                sp.setSort(SearchParameters.DATE_POSTED_DESC);
                break;
            case AbstractPhotoSearchGridFragment.SORT_TYPE_INTERESTING:
                if (Constants.DEBUG) Log.d(TAG, "Search type:INTERESTINGNESS");
                sp.setSort(SearchParameters.INTERESTINGNESS_DESC);
                break;
            case AbstractPhotoSearchGridFragment.SORT_TYPE_RELAVANCE:
                if (Constants.DEBUG) Log.d(TAG, "Search type:RELAVANCE");
                sp.setSort(SearchParameters.RELEVANCE);
                break;
            default:
                Log.e(TAG, "Unknown sort type, defaulting to RELAVANCE");
                sp.setSort(SearchParameters.RELEVANCE);
        }

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
