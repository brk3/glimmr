package com.bourke.glimmr.fragments.upload;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.bourke.glimmr.R;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhotoUploadFragment extends BaseFragment {

    public static final String TAG = "Glimmr/PhotoUploadFragment";

    private static final String KEY_PHOTO = "com.bourke.glimmr.PhotoUploadFragment.KEY_PHOTO";

    private LocalPhotosGridFragment.LocalPhoto mPhoto;
    private EditText mEditTextTitle;
    private EditText mEditTextDescription;
    private Switch mSwitchIsPublic;
    private EditText mEditTextTags;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        new GsonHelper(mActivity).marshallObject(mPhoto, outState, KEY_PHOTO);
    }

    @Override
    public void onActivityCreated(Bundle inState) {
        super.onActivityCreated(inState);
        if (inState != null && mPhoto == null) {
            String json = inState.getString(KEY_PHOTO, "");
            mPhoto = new Gson().fromJson(json, LocalPhotosGridFragment.LocalPhoto.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (ScrollView) inflater.inflate(R.layout.photo_upload_fragment, container, false);

        TextView basicSectionTitle = (TextView) mLayout.findViewById(R.id.textViewBasicSection);
        mTextUtils.setFont(basicSectionTitle, TextUtils.FONT_ROBOTOTHIN);

        mEditTextTitle = (EditText) mLayout.findViewById(R.id.editTextTitle);
        mEditTextTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    mPhoto.getMetadata().setTitle(((TextView) view).getText().toString());
                }
            }
        });

        mEditTextDescription = (EditText) mLayout.findViewById(R.id.editTextDescription);
        mEditTextDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    mPhoto.getMetadata().setDescription(((TextView) view).getText().toString());
                }
            }
        });

        TextView advancedSectionTitle = (TextView)
                mLayout.findViewById(R.id.textViewAdvancedSection);
        mTextUtils.setFont(advancedSectionTitle, TextUtils.FONT_ROBOTOTHIN);

        mSwitchIsPublic = (Switch) mLayout.findViewById(R.id.switchIsPublic);
        mSwitchIsPublic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPhoto.getMetadata().setPublicFlag(isChecked);
            }
        });

        mEditTextTags = (EditText) mLayout.findViewById(R.id.editTextTags);
        mEditTextTags.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    String tags = ((TextView) view).getText().toString();
                    mPhoto.getMetadata().setTags(Arrays.asList(tags.split(",")));
                }
            }
        });

        return mLayout;
    }

    public void setPhoto(LocalPhotosGridFragment.LocalPhoto photo) {
        if (mPhoto != null) {
            updateMetadataFromUI();
        }
        mPhoto = photo;
        updateUIFromMetadata();
    }

    public void updateMetadataFromUI() {
        mPhoto.getMetadata().setTitle(mEditTextTitle.getText().toString());
        mPhoto.getMetadata().setDescription(mEditTextDescription.getText().toString());
        mPhoto.getMetadata().setPublicFlag(mSwitchIsPublic.isChecked());

        String strTags = mEditTextTags.getText().toString();
        mPhoto.getMetadata().setTags(Arrays.asList(strTags.split(",")));
    }

    public void updateUIFromMetadata() {
        mEditTextTitle.setText(mPhoto.getMetadata().getTitle());
        mEditTextDescription.setText(mPhoto.getMetadata().getDescription());
        mSwitchIsPublic.setChecked(mPhoto.getMetadata().isPublicFlag());

        if (mPhoto.getMetadata().getTags() != null) {
            List<String> tags = new ArrayList<String>(mPhoto.getMetadata().getTags());
            mEditTextTags.setText(tagsToString(tags));
        } else {
            mEditTextTags.setText("");
        }
    }

    private String tagsToString(List<String> tags) {
        StringBuilder tagDisplay = new StringBuilder();
        for (String tag : tags) {
            tagDisplay.append(tag);
            tagDisplay.append(",");
        }
        if (tagDisplay.length() > 0) {
            return tagDisplay.substring(0, tagDisplay.length() - 1);
        }
        return "";
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        /* Override so as not to display the usual PhotoGridFragment options */
    }
}

