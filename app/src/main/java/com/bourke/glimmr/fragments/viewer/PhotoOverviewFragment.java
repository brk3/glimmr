package com.bourke.glimmr.fragments.viewer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import com.bourke.glimmr.R;
import com.bourke.glimmr.activities.SearchActivity;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.event.Events.IExifInfoReadyListener;
import com.bourke.glimmr.event.Events.TagClickDialogListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.tasks.LoadExifInfoTask;
import com.googlecode.flickrjandroid.photos.Exif;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.tags.Tag;

import java.util.Collection;
import java.util.List;

public final class PhotoOverviewFragment extends BaseFragment
        implements IExifInfoReadyListener, TagClickDialogListener {

    private static final String TAG = "Glimmr/PhotoOverviewFragment";

    private Photo mPhoto = new Photo();
    private LoadExifInfoTask mTask;

    private TextView mTextViewISO;
    private TextView mTextViewShutter;
    private TextView mTextViewAperture;
    private TextView mTextViewFocalLength;

    /* http://www.flickr.com/services/api/flickr.photos.getExif.html */
    private static final String ERR_PERMISSION_DENIED = "2";

    public static PhotoOverviewFragment newInstance(Photo photo) {
        PhotoOverviewFragment photoFragment = new PhotoOverviewFragment();
        photoFragment.mPhoto = photo;
        return photoFragment;
    }

    /** Can't retain fragments that are nested in other fragments */
    @Override
    protected boolean shouldRetainInstance() {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (ScrollView) inflater.inflate(
                R.layout.photo_overview_fragment, container, false);

        mTextViewISO = (TextView)
            mLayout.findViewById(R.id.textViewISO);

        mTextViewShutter = (TextView)
            mLayout.findViewById(R.id.textViewShutter);

        mTextViewAperture = (TextView)
            mLayout.findViewById(R.id.textViewAperture);

        mTextViewFocalLength = (TextView)
            mLayout.findViewById(R.id.textViewFocalLength);

        /* build the title textview */
        TextView titleTextView = (TextView)
                mLayout.findViewById(R.id.textViewTitle);
        String title = mPhoto.getTitle();
        if ("".equals(title)) {
            titleTextView.setText("‘" + getString(R.string.none) + "’");
        } else {
            titleTextView.setText("‘" + title + "’");
        }

        /* build the tags textview */
        TextView mTextTags = (TextView) mLayout.findViewById(R.id.textViewTags);
        StringBuilder tags = new StringBuilder();
        final Collection<Tag> allTags = mPhoto.getTags();
        int count = 0;
        for (Tag t : allTags) {
            tags.append(t.getValue());
            if (count < allTags.size()-1) {
                tags.append(" · ");
            }
            count++;
        }
        if ("".equals(tags.toString())) {
            mTextTags.setText("‘" + getString(R.string.none) + "’");
        } else {
            mTextTags.setText(tags.toString());
            mTextTags.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TagClickDialog d = new TagClickDialog(mActivity,
                            PhotoOverviewFragment.this,
                            allTags.toArray(new Tag[allTags.size()]));
                    d.show(mActivity.getSupportFragmentManager(),
                            "tags_click_dialog");
                }
            });
        }

        /* build the description textview */
        TextView textviewDescription = (TextView)
                mLayout.findViewById(R.id.textViewDescription);
        String description = mPhoto.getDescription();
        if ("".equals(description)) {
            textviewDescription.setText("‘" + getString(R.string.none) + "’");
        } else {
            textviewDescription.setText(Html.fromHtml(description));
        }

        setHasOptionsMenu(false);

        return mLayout;
    }

    @Override
    public void onTagClick(Tag tag) {
        Intent searchActivity = new Intent(mActivity, SearchActivity.class);
        searchActivity.setAction(Intent.ACTION_SEARCH);
        searchActivity.putExtra(SearchManager.QUERY, tag.getValue());
        mActivity.startActivity(searchActivity);
    }

    @Override
    protected void startTask() {
        super.startTask();
        mTask = new LoadExifInfoTask(this, mPhoto);
        mTask.execute(mOAuth);
    }

    public void onExifInfoReady(List<Exif> exifInfo, Exception exc) {
        mActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);

        if (FlickrHelper.getInstance().handleFlickrUnavailable(mActivity, exc)) {
            return;
        }

        for (Exif e : exifInfo) {
            String value = e.getRaw();
            if ("ISO".equals(e.getTag())) {
                styleTextView(mTextViewISO,
                        mActivity.getString(R.string.iso), value);
            } else if ("ExposureTime".equals(e.getTag())) {
                styleTextView(mTextViewShutter,
                        mActivity.getString(R.string.shutter), value);
            } else if ("FNumber".equals(e.getTag())) {
                value = "ƒ/" + value;
                styleTextView(mTextViewAperture,
                        mActivity.getString(R.string.aperture), value);
            } else if ("FocalLength".equals(e.getTag())) {
                styleTextView(mTextViewFocalLength,
                        mActivity.getString(R.string.focal_length), value);
            }
        }

        if (mActivity.getString(R.string.iso).equals(
                    mTextViewISO.getText())) {
            styleTextView(mTextViewISO,
                mActivity.getString(R.string.iso), "?");
        }
        if (mActivity.getString(R.string.aperture).equals(
                mTextViewAperture.getText())) {
            styleTextView(mTextViewAperture,
                mActivity.getString(R.string.aperture), "?");
        }
        if (mActivity.getString(R.string.shutter).equals(
                mTextViewShutter.getText())) {
            styleTextView(mTextViewShutter,
                mActivity.getString(R.string.shutter), "?");
        }
        if (mActivity.getString(R.string.focal_length).equals(
                mTextViewFocalLength.getText())) {
            styleTextView(mTextViewFocalLength,
                mActivity.getString(R.string.focal_length), "?");
        }
    }

    private void styleTextView(TextView textView, String key, String value) {
        String fullString = String.format("%s: %s", key, value);
        textView.setText(fullString);
        mTextUtils.fontTextViewSpan(textView, fullString, value,
                TextUtils.FONT_ROBOTOLIGHT);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    class TagClickDialog extends DialogFragment {
        private final TagClickDialogListener mListener;
        private final Context mContext;
        private final Tag[] mTags;

        public TagClickDialog(Context context,
                TagClickDialogListener listener, Tag[] tags) {
            mListener = listener;
            mContext = context;
            mTags = tags;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String[] dialogItems = new String[mTags.length];
            for (int i=0; i<mTags.length; i++) {
                dialogItems[i] = mTags[i].getValue();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(mActivity.getString(R.string.tags))
                .setItems(dialogItems, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                            int which) {
                        mListener.onTagClick(mTags[which]);
                    }
                });
            return builder.create();
        }
    }
}
