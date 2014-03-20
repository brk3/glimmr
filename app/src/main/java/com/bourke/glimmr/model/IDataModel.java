package com.bourke.glimmr.model;

import com.bourke.glimmr.event.Events;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public interface IDataModel {

    public static final int TYPE_PHOTOSTREAM = 0;
    public static final int TYPE_CONTACTS = 1;
    public static final int TYPE_FAVORITES = 2;

    public abstract void fetchNextPage(final Events.IPhotoListReadyListener listener);

    public abstract void save();

    public abstract void load();

    public abstract List<Photo> getPhotos();

    public abstract void clear();

    public abstract boolean isEmpty();
}
