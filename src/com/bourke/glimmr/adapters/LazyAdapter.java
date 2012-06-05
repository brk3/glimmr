package com.bourke.glimmr;

import android.app.Activity;

import android.content.Context;

import android.graphics.drawable.Drawable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bourke.glimmr.ImageUtils.DownloadedDrawable;

import com.gmail.yuyang226.flickr.photos.Photo;
import com.gmail.yuyang226.flickr.photos.PhotoList;
import android.widget.LinearLayout;
import android.widget.AbsListView;

public class LazyAdapter extends BaseAdapter {

    private Activity activity;
    private PhotoList photos;
    private Context mContext;
    private static LayoutInflater inflater = null;

    public LazyAdapter(Activity a, PhotoList d) {
        activity = a;
        mContext = a.getApplicationContext();
        photos = d;
        inflater = (LayoutInflater)activity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if(convertView == null) {
            vi = inflater.inflate(R.layout.gridview_item, null);
        }

        ImageView imageView = (ImageView) vi.findViewById(R.id.image_item);

        Photo photo = photos.get(position);
        ImageDownloadTask task = new ImageDownloadTask(imageView);
        Drawable drawable = new DownloadedDrawable(task);
        imageView.setImageDrawable(drawable);
        task.execute(photo.getLargeSquareUrl());

        /*
        ImageView viewIcon = (ImageView)vi.findViewById(R.id.viewIcon);
        if (photo.getViews() >= 0) {
        	viewIcon.setImageResource(R.drawable.views);
        	TextView viewsText = (TextView)vi.findViewById(R.id.viewsText);
        	viewsText.setText(String.valueOf(photo.getViews()));
        } else {
        	viewIcon.setImageBitmap(null);
        }
        */

        return vi;
    }

    public int getCount() {
        return photos.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

}
