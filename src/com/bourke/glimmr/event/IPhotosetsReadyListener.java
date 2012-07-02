package com.bourke.glimmr.event;

import com.gmail.yuyang226.flickr.photosets.Photosets;

public interface IPhotosetsReadyListener {

    void onPhotosetsReady(Photosets photosets, boolean cancelled);
}
