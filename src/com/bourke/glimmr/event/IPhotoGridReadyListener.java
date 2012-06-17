package com.bourke.glimmr;

import com.gmail.yuyang226.flickr.photos.PhotoList;

public interface IPhotoGridReadyListener {

	void onPhotosReady(PhotoList photos, boolean cancelled);
}
