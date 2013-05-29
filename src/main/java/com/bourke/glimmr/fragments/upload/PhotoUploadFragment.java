package com.bourke.glimmr.fragments.upload;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.bourke.glimmr.R;
import com.bourke.glimmr.activities.LocationEditorActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.event.Events;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.tasks.LoadPhotosetsTask;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.photos.GeoData;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photosets.Photosets;

import java.io.IOException;
import java.util.*;

public class PhotoUploadFragment extends BaseFragment {

    public static final String TAG = "Glimmr/PhotoUploadFragment";

    public static final String KEY_PHOTO = "com.bourke.glimmr.PhotoUploadFragment.KEY_PHOTO";

    public static final int ACTIVITY_RESULT_ADD_LOCATION = 0;

    private LocalPhotosGridFragment.LocalPhoto mPhoto;
    private EditText mEditTextTitle;
    private EditText mEditTextDescription;
    private Switch mSwitchIsPublic;
    private EditText mEditTextTags;
    private Photosets mPhotosets;
    private GoogleMap mMap;

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

        TextView textViewSets = (TextView) mLayout.findViewById(R.id.textViewSets);
        textViewSets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPhotosets != null) {
                    new SetChooserDialog(photosetsToMap(mPhotosets)).show(
                            mActivity.getFragmentManager(), "SetChooserDialog");
                } else {
                    mActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    new LoadPhotosetsTask(new Events.IPhotosetsReadyListener() {
                        @Override
                        public void onPhotosetsReady(Photosets photosets) {
                            mActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            if (photosets != null) {
                                mPhotosets = photosets;
                                new SetChooserDialog(photosetsToMap(mPhotosets)).show(
                                        mActivity.getFragmentManager(), "SetChooserDialog");
                            } else {
                                // TODO: alert user
                            }
                        }
                    }, mOAuth.getUser()).execute(mOAuth);
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) mActivity.getSupportFragmentManager().
                findFragmentById(R.id.mapPreview);
        mMap = mapFragment.getMap();
        if (mMap != null) {
            /* start the location editor on click */
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    LocationEditorActivity.startForResult(mActivity, PhotoUploadFragment.this,
                            mPhoto, ACTIVITY_RESULT_ADD_LOCATION);
                }
            });
        } else {
            Log.w(TAG, "getMap() returned null, maps service unavailable");
        }

        return mLayout;
    }

    private final GeoData locationFromExif(LocalPhotosGridFragment.LocalPhoto photo) {
        try {
            ExifInterface exifInterface = new ExifInterface(photo.getUri());
            final String latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            final String longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            if (latitude != null && latitude != null) {
                return new GeoData(dmsToDegrees(longitude), dmsToDegrees(latitude),
                        Flickr.ACCURACY_STREET);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* http://android-er.blogspot.ie/2010/01/convert-exif-gps-info-to-degree-format.html */
    private Float dmsToDegrees(String dms) {
        String[] DMS = dms.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0/S1;

        return new Float(FloatD + (FloatM/60) + (FloatS/3600));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_RESULT_ADD_LOCATION) {
            Bundle extras = data.getExtras();
            GeoData geoData = new Gson().fromJson(extras.getString(
                    LocationEditorActivity.KEY_LOCATION, ""), GeoData.class);
            addMapMarkerAndZoom(geoData.getLatitude(), geoData.getLongitude());
        }
    }

    private void addMapMarkerAndZoom(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromFile(mPhoto.getUri()));
        mMap.addMarker(markerOptions);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(15)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private Map<String, Photoset> photosetsToMap(Photosets photosets) {
        HashMap<String, Photoset> ret = new HashMap();
        for (Photoset p : photosets.getPhotosets()) {
            ret.put(p.getTitle(), p);
        }
        return ret;
    }

    public void setPhoto(LocalPhotosGridFragment.LocalPhoto photo) {
        if (mPhoto != null) {
            updateMetadataFromUI();
        }
        mPhoto = photo;
        updateUIFromMetadata();
    }

    /** Store UI metadata updates to the current photo */
    public void updateMetadataFromUI() {
        mPhoto.getMetadata().setTitle(mEditTextTitle.getText().toString());
        mPhoto.getMetadata().setDescription(mEditTextDescription.getText().toString());
        mPhoto.getMetadata().setPublicFlag(mSwitchIsPublic.isChecked());

        String strTags = mEditTextTags.getText().toString();
        mPhoto.getMetadata().setTags(Arrays.asList(strTags.split(",")));
    }

    /** Update UI metadata display from the current photo */
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

        /* set location from local metadata if available */
        GeoData location = locationFromExif(mPhoto);
        if (location != null) {
            if (Constants.DEBUG) Log.d(TAG, "found location in local photo");
            mPhoto.setGeoData(location);
            addMapMarkerAndZoom(location.getLatitude(), location.getLongitude());
        } else if (Constants.DEBUG) {
            Log.d(TAG, "NO location found in local photo");
        }
    }

    private String tagsToString(List<String> tags) {
        StringBuilder tagDisplay = new StringBuilder();
        for (int i=0; i < tags.size(); i++) {
            tagDisplay.append(tags.get(i));
            if (i < tags.size()-1) {
                tagDisplay.append(",");
            }
        }
        return tagDisplay.toString();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        /* Override so as not to display the usual PhotoGridFragment options */
    }

    /* android-styled-dialogs doesn't support multichoice (yet) */
    public class SetChooserDialog extends DialogFragment {

        private List<Integer> mSelectedItems;
        private String[] mEntries;
        private HashMap<String, Photoset> mAllAvailablePhotosets;

        public SetChooserDialog(Map<String, Photoset> allAvailablePhotosets) {
            mAllAvailablePhotosets = (HashMap) allAvailablePhotosets;
            mEntries = mAllAvailablePhotosets.keySet().toArray(
                    new String[mAllAvailablePhotosets.size()]);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mSelectedItems = new ArrayList();
            boolean[] checkedItems = null;
            if (mPhoto.getPhotosets() != null) {
                checkedItems = new boolean[mEntries.length];
                Arrays.fill(checkedItems, false);
                HashMap<String, Photoset> current = (HashMap) photosetsToMap(mPhoto.getPhotosets());
                Set<String> currentEntries = current.keySet();
                List<String> entriesList = Arrays.asList(mEntries);
                for (int i=0; i<entriesList.size(); i++) {
                    if (currentEntries.contains(entriesList.get(i))) {
                        checkedItems[i] = true;
                        mSelectedItems.add(i);
                    }
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(mActivity.getString(R.string.add_to_sets))
                    .setMultiChoiceItems(mEntries, checkedItems,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                                    if (isChecked) {
                                        mSelectedItems.add(which);
                                    } else if (mSelectedItems.contains(which)) {
                                        mSelectedItems.remove(Integer.valueOf(which));
                                    }
                                }
                            })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            List<Photoset> selectedPhotosets = new ArrayList();
                            for (Integer i : mSelectedItems) {
                                if (Constants.DEBUG) Log.d(TAG, mEntries[i]);
                                selectedPhotosets.add(mAllAvailablePhotosets.get(mEntries[i]));
                            }
                            Photosets photosets = new Photosets();
                            photosets.setPhotosets(selectedPhotosets);
                            mPhoto.setPhotosets(photosets);
                            // TODO: update UI
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dismiss();
                                }
                            });
            return builder.create();
        }
    }
}

