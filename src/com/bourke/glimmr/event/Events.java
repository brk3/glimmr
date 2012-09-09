package com.bourke.glimmr.event;

import com.googlecode.flickrjandroid.groups.GroupList;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.comments.Comment;
import com.googlecode.flickrjandroid.photosets.Photosets;
import com.googlecode.flickrjandroid.photos.Exif;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

import java.util.List;

public class Events {

    public interface IAccessTokenReadyListener {
        void onAccessTokenReady(OAuth oauth);
    }

    public interface ICommentsReadyListener {
        void onCommentsReady(List<Comment> exifItems);
    }

    public interface IContactsPhotosReadyListener {
        void onContactsPhotosReady(PhotoList contactsAndPhotos);
    }

    public interface IExifInfoReadyListener {
        void onExifInfoReady(List<Exif> exifItems);
    }

    public interface IGroupListReadyListener {
        void onGroupListReady(GroupList groups);
    }

    public interface IPhotoListReadyListener {
        void onPhotosReady(PhotoList photos);
    }

    public interface IPhotosetsReadyListener {
        void onPhotosetsReady(Photosets photosets);
    }

    public interface IRequestTokenReadyListener {
        void onRequestTokenReady(String authUri);
    }

    public interface IUserReadyListener {
        void onUserReady(User user);
    }

    public interface IFavoriteReadyListener {
        void onFavoriteComplete(Exception e);
    }

    public interface IPhotoInfoReadyListener {
        void onPhotoInfoReady(Photo photo);
    }

    public interface ICommentAddedListener {
        void onCommentAdded(String commentId);
    }

    public interface IOverlayVisbilityListener {
        void onVisibilityChanged();
    }
}
