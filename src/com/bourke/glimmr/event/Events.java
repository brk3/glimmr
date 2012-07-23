package com.bourke.glimmr.event;

import com.gmail.yuyang226.flickr.groups.GroupList;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.people.User;
import com.gmail.yuyang226.flickr.photos.comments.Comment;
import com.gmail.yuyang226.flickr.photosets.Photosets;
import com.gmail.yuyang226.flickr.photos.Exif;
import com.gmail.yuyang226.flickr.photos.PhotoList;
import com.gmail.yuyang226.flickr.photos.Photo;

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
}
