package com.bourke.glimmrpro.services;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

import java.util.List;

public interface GlimmrPhotoNotificationHandler {

    public void showNewPhotosNotification(List<Photo> newPhotos);

    public List<Photo> checkForNewPhotos(PhotoList photos);

    public String getNewestViewedPhotoId();

    public String getNewestNotificationPhotoId();

    public void storeNewestNotificationPhotoId(Photo photo);
}
