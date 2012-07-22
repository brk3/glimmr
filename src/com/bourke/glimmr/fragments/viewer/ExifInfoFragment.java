package com.bourke.glimmr.fragments.viewer;

import android.widget.TableRow.LayoutParams;
import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ScrollView;

import com.androidquery.AQuery;

import com.bourke.glimmr.event.Events.IExifInfoReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadExifInfoTask;

import com.gmail.yuyang226.flickr.photos.Exif;
import com.gmail.yuyang226.flickr.photos.Photo;

import java.util.List;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public final class ExifInfoFragment extends BaseFragment
        implements IExifInfoReadyListener {

    protected String TAG = "Glimmr/ExifInfoFragment";

    private Photo mPhoto = new Photo();
    private AQuery mAq;

    public static ExifInfoFragment newInstance(Photo photo) {
        ExifInfoFragment photoFragment = new ExifInfoFragment();
        photoFragment.mPhoto = photo;
        return photoFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (ScrollView) inflater.inflate(
                R.layout.exif_info_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);
        return mLayout;
    }

    @Override
    protected void startTask() {
        super.startTask();
        Log.d(getLogTag(), "startTask()");
        new LoadExifInfoTask(mActivity, this, mPhoto).execute(mOAuth);
    }

    /**
     * Creates a TableRow with two columns containing TextViews, and adds it to
     * the main TableView.
     */
    private void addKeyValueRow(String key, String value) {
        /* Create the TableRow */
        TableLayout tl = (TableLayout) mLayout.findViewById(R.id.tableLayout1);
        TableRow tr = new TableRow(mActivity);
        TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams(
                    TableLayout.LayoutParams.FILL_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
        /* left, top, right, bottom */
        tableRowParams.setMargins(5, 5, 5, 0);
        tr.setLayoutParams(tableRowParams);

        TextView textViewKey =  new TextView(mActivity);
        LayoutParams textViewKeyParams = new LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
        textViewKey.setLayoutParams(textViewKeyParams);
        textViewKey.setTextColor(R.color.text_light);
        textViewKey.setText(key);
        tr.addView(textViewKey);

        TextView textViewValue =  new TextView(mActivity);
        LayoutParams textViewValueParams = new LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
        textViewValueParams.span = 2;
        textViewValue.setLayoutParams(textViewValueParams);
        textViewValue.setText(value);
        tr.addView(textViewValue);

        /* Add the row to the table */
        tl.addView(tr);
    }

    public void onExifInfoReady(List<Exif> exifInfo, boolean cancelled) {
        Log.d(getLogTag(), "onExifInfoReady, exifInfo.size(): "
                + exifInfo.size());
        for (Exif e : exifInfo) {
            if (e.getTag().equals("ISO")) {
                mAq.id(R.id.textViewISOValue).text(e.getRaw());
            } else if (e.getTag().equals("ExposureTime")) {
                mAq.id(R.id.textViewShutterValue).text(e.getRaw());
            } else if (e.getTag().equals("FNumber")) {
                mAq.id(R.id.textViewApertureValue).text(e.getRaw());
            } else if (e.getTag().equals("Model")) {
                addKeyValueRow("Camera", e.getRaw());
            } else if (e.getTag().equals("FocalLength")) {
                addKeyValueRow(e.getLabel(), e.getRaw());
            } else if (e.getTag().equals("ExposureProgram")) {
                addKeyValueRow(e.getLabel(), e.getRaw());
            } else if (e.getTag().equals("DateTimeOriginal")) {
                addKeyValueRow(e.getLabel(), e.getRaw());
            } else if (e.getTag().equals("Quality")) {
                addKeyValueRow(e.getLabel(), e.getRaw());
            } else if (e.getTag().equals("LensType")) {
                addKeyValueRow(e.getLabel(), e.getRaw());
            } else if (e.getTag().equals("Software")) {
                addKeyValueRow(e.getLabel(), e.getRaw());
            }
        }
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}
