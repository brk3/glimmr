package com.bourke.glimmr.event;

import com.gmail.yuyang226.flickr.groups.GroupList;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.people.User;
import com.gmail.yuyang226.flickr.photos.comments.Comment;
import com.gmail.yuyang226.flickr.photosets.Photosets;
import com.gmail.yuyang226.flickr.photos.Exif;
import com.gmail.yuyang226.flickr.photos.PhotoList;

import java.util.List;

public class Events {

    public interface IAccessTokenReadyListener {
        void onAccessTokenReady(OAuth oauth);
    }

    public interface ICommentsReadyListener {
        void onCommentsReady(List<Comment> exifItems, boolean cancelled);
    }

    public interface IContactsPhotosReadyListener {
        void onContactsPhotosReady(PhotoList contactsAndPhotos,
                boolean cancelled);
    }

    public interface IExifInfoReadyListener {
        void onExifInfoReady(List<Exif> exifItems, boolean cancelled);
    }

    public interface IGroupListReadyListener {
        void onGroupListReady(GroupList groups, boolean cancelled);
    }

    public interface IPhotoListReadyListener {
        void onPhotosReady(PhotoList photos, boolean cancelled);
    }

    public interface IPhotosetsReadyListener {
        void onPhotosetsReady(Photosets photosets, boolean cancelled);
    }

    public interface IRequestTokenReadyListener {
        void onRequestTokenReady(String authUri);
    }

    public interface IUserReadyListener {
        void onUserReady(User user, boolean cancelled);
    }

    public interface IFavoriteReadyListener {
        void onFavoriteComplete(boolean cancelled);
    }
}
