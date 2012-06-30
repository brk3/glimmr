package com.bourke.glimmr;

import com.gmail.yuyang226.flickr.photos.PhotoList;

public interface IPhotoListReadyListener {

    void onPhotosReady(PhotoList photos, boolean cancelled);
}
