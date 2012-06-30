package com.bourke.glimmr;

import com.gmail.yuyang226.flickr.photosets.Photosets;

public interface IPhotosetListReadyListener {

	void onPhotosetListReady(Photosets photosets, boolean cancelled);
}
