package com.bourke.glimmr.event;

import com.gmail.yuyang226.flickr.groups.GroupList;

public interface IGroupListReadyListener {

    void onGroupListReady(GroupList groups, boolean cancelled);
}
