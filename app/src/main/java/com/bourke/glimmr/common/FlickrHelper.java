package com.bourke.glimmr.common;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.bourke.glimmr.R;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.REST;
import com.googlecode.flickrjandroid.RequestContext;
import com.googlecode.flickrjandroid.contacts.ContactsInterface;
import com.googlecode.flickrjandroid.favorites.FavoritesInterface;
import com.googlecode.flickrjandroid.groups.GroupsInterface;
import com.googlecode.flickrjandroid.groups.pools.PoolsInterface;
import com.googlecode.flickrjandroid.interestingness.InterestingnessInterface;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.PeopleInterface;
import com.googlecode.flickrjandroid.photos.PhotosInterface;
import com.googlecode.flickrjandroid.photos.comments.CommentsInterface;
import com.googlecode.flickrjandroid.photosets.PhotosetsInterface;
import com.googlecode.flickrjandroid.urls.UrlsInterface;

import javax.xml.parsers.ParserConfigurationException;

public final class FlickrHelper {

    private static final String TAG = "FlickrHelper";

    private static FlickrHelper instance = null;

    private FlickrHelper() {
    }

    public static FlickrHelper getInstance() {
        if (instance == null) {
            instance = new FlickrHelper();
        }

        return instance;
    }

    public Flickr getFlickr() {
        try {
            return new Flickr(Keys.API_KEY, Keys.API_SECRET, new REST());
        } catch (ParserConfigurationException e) {
            return null;
        }
    }

    public Flickr getFlickrAuthed(String token, String secret) {
        Flickr f = getFlickr();
        RequestContext requestContext = RequestContext.getRequestContext();
        OAuth auth = new OAuth();
        auth.setToken(new OAuthToken(token, secret));
        requestContext.setOAuth(auth);
        return f;
    }

    public InterestingnessInterface getInterestingInterface() {
        Flickr f = getFlickr();
        if (f != null) {
            return f.getInterestingnessInterface();
        } else {
            return null;
        }
    }

    public PhotosInterface getPhotosInterface() {
        Flickr f = getFlickr();
        if (f != null) {
            return f.getPhotosInterface();
        } else {
            return null;
        }
    }

    public ContactsInterface getContactsInterface() {
        Flickr f = getFlickr();
        if (f != null) {
            return f.getContactsInterface();
        } else {
            return null;
        }
    }

    public FavoritesInterface getFavoritesInterface() {
        Flickr f = getFlickr();
        if (f != null) {
            return f.getFavoritesInterface();
        } else {
            return null;
        }
    }

    public PoolsInterface getPoolsInterface() {
        Flickr f = getFlickr();
        if (f != null) {
            return f.getPoolsInterface();
        } else {
            return null;
        }
    }

    public PhotosetsInterface getPhotosetsInterface() {
        Flickr f = getFlickr();
        if (f != null) {
            return f.getPhotosetsInterface();
        } else {
            return null;
        }
    }

    public CommentsInterface getCommentsInterface() {
        Flickr f = getFlickr();
        if (f != null) {
            return f.getCommentsInterface();
        } else {
            return null;
        }
    }

    public PeopleInterface getPeopleInterface() {
        Flickr f = getFlickr();
        if (f != null) {
            return f.getPeopleInterface();
        } else {
            return null;
        }
    }

    public GroupsInterface getGroupsInterface() {
        Flickr f = getFlickr();
        if (f != null) {
            return f.getGroupsInterface();
        } else {
            return null;
        }
    }

    public UrlsInterface getUrlsInterface() {
        Flickr f = getFlickr();
        if (f != null) {
            return f.getUrlsInterface();
        } else {
            return null;
        }
    }

    /**
     * Check if exception e is the cause of Flickr being down and show some toast.
     * @param context
     * @return true if flickr is down
     */
    public boolean handleFlickrUnavailable(Context context, Exception e) {
        if (e != null && e instanceof FlickrException) {
            if (((FlickrException) e).getErrorCode().equals(
                    Constants.ERR_CODE_FLICKR_UNAVAILABLE)) {
                e.printStackTrace();
                Log.w(TAG, "Flickr seems down at the moment");
                Toast.makeText(context, context.getString(R.string.flickr_unavailable),
                        Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return false;
    }
}
