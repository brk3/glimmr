package com.bourke.glimmr.event;

import com.gmail.yuyang226.flickr.photos.Exif;

import java.util.List;

public interface IExifInfoReadyListener {

    void onExifInfoReady(List<Exif> exifItems, boolean cancelled);
}
