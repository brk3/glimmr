package com.bourke.glimmr.activities;

import com.bourke.glimmr.BuildConfig;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.bourke.glimmr.common.Constants;

import java.util.List;

public class LinkInterceptorActivity extends Activity {

    private static final String TAG = "Glimmr/LinkInterceptorActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        Uri uri = intent.getData();
        List<String> params = uri.getPathSegments();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("Received URI: '%s', params: %d",
                    uri.toString(), params.size()));
        }

        if (params.get(0).equals("photos")) {
            if (params.size() > 2) {
                if (params.get(2).equals("sets")) {
                    parsePhotoset(params);
                } else {
                    parseSinglePhoto(params);
                }
            }
        } else if (params.get(0).equals("people")) {
            parseProfile(uri, params);
        } else if (params.get(0).equals("groups")) {
            parseGroup(uri, params);
        } else {
            Log.w(TAG, "Don't know how to parse this URI");
        }
    }

    /* Group: http://www.flickr.com/groups/{group-id}|{group-name}/ */
    private void parseGroup(Uri uri, List<String> params) {
        if (params.size() > 1) {
            String s = params.get(1);
            Intent groupViewer = new Intent(this, GroupViewerActivity.class);
            if (isFlickrId(s)) {
                groupViewer.putExtra(GroupViewerActivity.KEY_GROUP_ID, s);
                groupViewer.setAction(
                        GroupViewerActivity.ACTION_VIEW_GROUP_BY_ID);
            } else {
                groupViewer.putExtra(GroupViewerActivity.KEY_GROUP_URL,
                        uri.toString());
                groupViewer.setAction(
                        GroupViewerActivity.ACTION_VIEW_GROUP_BY_NAME);
            }
            startActivity(groupViewer);
        } else {
            Log.e(TAG, "Not enough params to parse group");
        }
    }

    /* Profile: http://www.flickr.com/people/{user-id}|{user-name}/ */
    private void parseProfile(Uri uri, List<String> params) {
        if (params.size() > 1) {
            Intent profileViewer = new Intent(this, ProfileViewerActivity.class);
            String s = params.get(1);
            if (isFlickrId(s)) {
                profileViewer.putExtra(
                        ProfileViewerActivity.KEY_PROFILE_ID, s);
                profileViewer.setAction(ProfileViewerActivity
                        .ACTION_VIEW_USER_BY_ID);
            } else {
                profileViewer.putExtra(
                        ProfileViewerActivity.KEY_PROFILE_URL, uri.toString());
                profileViewer.setAction(ProfileViewerActivity
                        .ACTION_VIEW_USER_BY_URL);
            }
            startActivity(profileViewer);
        } else {
            Log.e(TAG, "Not enough params to parse profile");
        }
    }

    /* Photo: http://www.flickr.com/photos/{user-id}/{photo-id} */
    private void parseSinglePhoto(List<String> params) {
        if (params.size() > 2) {
            String photoId = params.get(2);
            PhotoViewerActivity.startPhotoViewer(this, photoId);
        } else {
            Log.e(TAG, "Not enough params to parse photo");
        }
    }

    /* Set: http://www.flickr.com/photos/{user-id}/sets/{set-id} */
    private void parsePhotoset(List<String> params) {
       if (params.size() > 3) {
           String setId = params.get(3);
           PhotosetViewerActivity.startPhotosetViewer(this, setId);
       } else {
           Log.e(TAG, "Not enough params to parse set");
       }
    }

    private boolean isFlickrId(String s) {
        return (s != null) && s.charAt(s.length() - 4) == '@';
    }
}
