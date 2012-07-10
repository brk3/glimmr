package com.bourke.glimmr.event;

import com.gmail.yuyang226.flickr.photos.comments.Comment;

import java.util.List;

public interface ICommentsReadyListener {

    void onCommentsReady(List<Comment> exifItems, boolean cancelled);
}
