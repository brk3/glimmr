package com.bourke.glimmr.services;

import com.googlecode.flickrjandroid.oauth.OAuth;

public interface GlimmrNotificationHandler<T> {

    public void startTask(OAuth oauth);

    /**
     * If this returns false the handler will not be executed by the service
     */
    public boolean enabledInPreferences();

    public String getLatestIdNotifiedAbout();

    public void storeLatestIdNotifiedAbout(T t);
}
