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
        ImageView imageView = (ImageView) convertView;
        if(convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(250, 250));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0);
        }

        Photo photo = photos.get(position);
        ImageDownloadTask task = new ImageDownloadTask(imageView);
        Drawable drawable = new DownloadedDrawable(task);
        imageView.setImageDrawable(drawable);
        task.execute(photo.getSmallSquareUrl());

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

        return imageView;
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
