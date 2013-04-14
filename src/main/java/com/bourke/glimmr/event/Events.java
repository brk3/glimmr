package com.bourke.glimmrpro.event;

import com.googlecode.flickrjandroid.tags.Tag;
import com.googlecode.flickrjandroid.activity.Item;
import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.comments.Comment;
import com.googlecode.flickrjandroid.photosets.Photosets;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photos.Exif;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;
import com.googlecode.flickrjandroid.photos.Size;

public class Events {

    public interface IAccessTokenReadyListener {
        void onAccessTokenReady(OAuth oauth);
    }

    public interface ICommentsReadyListener {
        void onCommentsReady(List<Comment> exifItems);
    }

    public interface IContactsPhotosReadyListener {
        void onContactsPhotosReady(List<Photo> contactsAndPhotos);
    }

    public interface IExifInfoReadyListener {
        void onExifInfoReady(List<Exif> exifItems, Exception e);
    }

    public interface IGroupListReadyListener {
        void onGroupListReady(List<Group> groups);
    }

    public interface IPhotoListReadyListener {
        void onPhotosReady(List<Photo> photos);
    }

    public interface IPhotosetsReadyListener {
        void onPhotosetsReady(Photosets photosets);
    }

    public interface IRequestTokenReadyListener {
        void onRequestTokenReady(String authUri, Exception e);
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

    public interface IGroupInfoReadyListener {
        public void onGroupInfoReady(Group group);
    }

    public interface ICommentAddedListener {
        void onCommentAdded(String commentId);
    }

    public interface PhotoItemLongClickDialogListener {
        public void onLongClickDialogSelection(Photo photo, int which);
    }

    public interface IActivityItemsReadyListener {
        public void onItemListReady(List<Item> items);
    }

    public interface TagClickDialogListener {
        public void onTagClick(Tag tag);
    }

    public interface GroupItemLongClickDialogListener {
        public void onLongClickDialogSelection(Group group, int which);
    }

    public interface PhotosetItemLongClickDialogListener {
        public void onLongClickDialogSelection(Photoset photoset, int which);
    }

    /**
     * Called from the task in the event of a failure
     *
     * @param itemId        Id of the item associated with the failed task
     * @param retry         If False, task is considered bad and should not be
     *                      queued for retry.
     */
    public interface ITaskQueueServiceListener {
        public void onSuccess(final String itemId);
        public void onFailure(final String itemId, final boolean retry);
    }

    public interface IPhotoSizesReadyListener {
        public void onPhotoSizesReady(List<Size> sizes);
    }
}
