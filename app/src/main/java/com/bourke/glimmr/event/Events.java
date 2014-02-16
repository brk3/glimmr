package com.bourke.glimmr.event;

import android.graphics.Bitmap;

import com.googlecode.flickrjandroid.activity.Item;
import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Exif;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.Size;
import com.googlecode.flickrjandroid.photos.comments.Comment;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photosets.Photosets;
import com.googlecode.flickrjandroid.tags.Tag;

import java.util.List;

public class Events {

    public interface IAccessTokenReadyListener {
        void onAccessTokenReady(OAuth oauth);
    }

    public interface ICommentsReadyListener {
        void onCommentsReady(List<Comment> exifItems, Exception e);
    }

    public interface IExifInfoReadyListener {
        void onExifInfoReady(List<Exif> exifItems, Exception e);
    }

    public interface IGroupListReadyListener {
        void onGroupListReady(List<Group> groups, Exception e);
    }

    public interface IPhotoListReadyListener {
        void onPhotosReady(List<Photo> photos, Exception e);
    }

    public interface IPhotosetsReadyListener {
        void onPhotosetsReady(Photosets photosets, Exception e);
    }

    public interface IRequestTokenReadyListener {
        void onRequestTokenReady(String authUri, Exception e);
    }

    public interface IUserReadyListener {
        void onUserReady(User user, Exception e);
    }

    public interface IFavoriteReadyListener {
        void onFavoriteComplete(Exception e);
    }

    public interface IPhotoInfoReadyListener {
        void onPhotoInfoReady(Photo photo, Exception e);
    }

    public interface IGroupInfoReadyListener {
        public void onGroupInfoReady(Group group, Exception e);
    }

    public interface ICommentAddedListener {
        void onCommentAdded(String commentId, Exception e);
    }

    public interface PhotoItemLongClickDialogListener {
        public void onLongClickDialogSelection(Photo photo, int which);
    }

    public interface IActivityItemsReadyListener {
        public void onItemListReady(List<Item> items, Exception e);
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
        public void onPhotoSizesReady(List<Size> sizes, Exception e);
    }

    public interface IPhotosetReadyListener {
        public void onPhotosetReady(Photoset photoset, Exception e);
    }

    public interface IGroupIdReadyListener {
        public void onGroupIdReady(String groupId, Exception e);
    }

    public interface IProfileIdReadyListener {
        public void onProfileIdReady(String profileId, Exception e);
    }

    public interface IPhotoDownloadedListener {
        public void onPhotoDownloaded(Bitmap bitmap, Exception e);
    }
}
