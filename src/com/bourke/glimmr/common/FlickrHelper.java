package com.bourke.glimmrpro.common;

import com.googlecode.flickrjandroid.activity.ActivityInterface;
import com.googlecode.flickrjandroid.contacts.ContactsInterface;
import com.googlecode.flickrjandroid.favorites.FavoritesInterface;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.groups.pools.PoolsInterface;
import com.googlecode.flickrjandroid.interestingness.InterestingnessInterface;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.PeopleInterface;
import com.googlecode.flickrjandroid.photos.comments.CommentsInterface;
import com.googlecode.flickrjandroid.photosets.PhotosetsInterface;
import com.googlecode.flickrjandroid.photos.PhotosInterface;
import com.googlecode.flickrjandroid.RequestContext;
import com.googlecode.flickrjandroid.REST;

import javax.xml.parsers.ParserConfigurationException;

public final class FlickrHelper {

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
            Flickr f = new Flickr(Keys.API_KEY, Keys.API_SECRET, new REST());
            return f;
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

    public ActivityInterface getActivityInterface() {
        Flickr f = getFlickr();
        if (f != null) {
            return f.getActivityInterface();
        } else {
            return null;
        }
    }
}
