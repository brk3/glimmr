package com.bourke.glimmr;

import com.gmail.yuyang226.flickr.photos.PhotoList;

public interface IPhotoStreamReadyListener {

	void onPhotoStreamReady(PhotoList photos, boolean cancelled);
}
