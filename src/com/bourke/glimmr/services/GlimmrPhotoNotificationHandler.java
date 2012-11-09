package com.bourke.glimmrpro.services;

import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public interface GlimmrPhotoNotificationHandler {

    public void showNewPhotosNotification(List<Photo> newPhotos);

    public String getNewestViewedPhotoId();

    public String getNewestNotificationPhotoId();

    public void storeNewestNotificationPhotoId(Photo photo);
}
