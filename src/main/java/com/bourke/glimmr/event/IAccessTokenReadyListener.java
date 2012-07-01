package com.bourke.glimmr.event;

import com.gmail.yuyang226.flickr.oauth.OAuth;

public interface IAccessTokenReadyListener {

    void onAccessTokenReady(OAuth oauth);
}
