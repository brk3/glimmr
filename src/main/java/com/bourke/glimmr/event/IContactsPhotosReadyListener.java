package com.bourke.glimmr.event;

import com.gmail.yuyang226.flickr.photos.PhotoList;

public interface IContactsPhotosReadyListener {

    void onContactsPhotosReady(PhotoList contactsAndPhotos, boolean cancelled);
}
