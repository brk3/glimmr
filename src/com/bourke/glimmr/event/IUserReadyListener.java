package com.bourke.glimmr;

import com.gmail.yuyang226.flickr.people.User;

public interface IUserReadyListener {

    void onUserReady(User user, boolean cancelled);
}
