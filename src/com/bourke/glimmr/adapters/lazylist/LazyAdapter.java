package com.fedorvlasov.lazylist;

import android.app.Activity;

import android.content.Context;

import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class LazyAdapter extends BaseAdapter {

    private Context mContext;

    public ImageLoader imageLoader;

    public List<String> mItems = new ArrayList<String>();

    public LazyAdapter(Activity a) {
        mContext = a.getApplicationContext();
        imageLoader = new ImageLoader(mContext);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        // TODO
        //imageLoader.DisplayImage(data[position], imageView);

        return imageView;
    }

    public int getCount() {
        return mItems.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

}
