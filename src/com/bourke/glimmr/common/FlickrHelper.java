package com.bourke.glimmr;

import com.gmail.yuyang226.flickr.contacts.ContactsInterface;
import com.gmail.yuyang226.flickr.favorites.FavoritesInterface;
import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.groups.pools.PoolsInterface;
import com.gmail.yuyang226.flickr.interestingness.InterestingnessInterface;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.photos.PhotosInterface;
import com.gmail.yuyang226.flickr.RequestContext;
import com.gmail.yuyang226.flickr.REST;

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
}
