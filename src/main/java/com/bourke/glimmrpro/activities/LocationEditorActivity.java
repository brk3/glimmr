package com.bourke.glimmrpro.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.common.BitmapUtils;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.GsonHelper;
import com.bourke.glimmrpro.common.UsageTips;
import com.bourke.glimmrpro.fragments.upload.LocalPhotosGridFragment;
import com.bourke.glimmrpro.fragments.upload.PhotoUploadFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.photos.GeoData;

public class LocationEditorActivity extends BaseActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener {

    private static final String TAG = "Glimmr/LocationEditorActivity";

    /**
     * Request code to send to Google Play services. Code is returned in Activity.onActivityResult.
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static final String KEY_PHOTO = "com.bourke.glimmrpro.LocationEditorActivity.KEY_PHOTO";
    public static final String KEY_LOCATION =
            "com.bourke.glimmrpro.LocationEditorActivity.KEY_LOCATION";

    private GoogleMap mMap;
    private LocationClient mLocationClient;
    private LocalPhotosGridFragment.LocalPhoto mLocalPhoto;
    private GeoData mLocation;
    private MarkerOptions mMapMarker;

    public static void start(Context context, LocalPhotosGridFragment.LocalPhoto photo) {
        Intent intent = new Intent(context, LocationEditorActivity.class);
        Bundle extras = new Bundle();
        new GsonHelper(context).marshallObject(photo, extras, KEY_PHOTO);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    public static void startForResult(Context context, Fragment fragment,
            LocalPhotosGridFragment.LocalPhoto photo, int requestCode) {
        Intent intent = new Intent(context, LocationEditorActivity.class);
        Bundle extras = new Bundle();
        new GsonHelper(context).marshallObject(photo, extras, KEY_PHOTO);
        intent.putExtras(extras);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(TAG, "onCreate");

        setContentView(R.layout.location_editor_activity);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        mLocationClient = new LocationClient(this, this, this);
        initMap();

        UsageTips.getInstance().show(this, getString(R.string.set_location_tip), false);
    }

    private void handleIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.e(TAG, "No extras found in intent");
        }
        String json = extras.getString(KEY_PHOTO, "");
        mLocalPhoto = new Gson().fromJson(json, LocalPhotosGridFragment.LocalPhoto.class);
    }

    /**
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        /* Connect the client. */
        mLocationClient.connect();
    }

    /**
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        /* Disconnecting the client invalidates it. */
        mLocationClient.disconnect();
        super.onStop();
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMap = mapFragment.getMap();
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMarkerDragListener(this);
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    mMap.clear();
                    addMapMarkerAndZoom(latLng.latitude, latLng.longitude);
                }
            });
        } else {
            Log.w(TAG, "getMap() returned null, maps not available");
        }
    }

    /**
     * Handle results returned to the FragmentActivity by Google Play services
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
              switch (resultCode) {
                  case Activity.RESULT_OK :
                     // Try the request again
                     break;
              }
         }
    }

    /**
     * Check that Google Play services is available
     */
    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Constants.DEBUG)  Log.d("TAG", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }
        }
        return false;
    }

    /**
     * Called by Location Services when the request to connect the client finishes successfully. At
     * this point, you can request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        /* handle intent here so we know we have mLocalPhoto before accessing it for location */
        handleIntent();

        if (mMapMarker == null) {
            /* check if the photo we've been passed has a location we can use, otherwise use current
            * location */
            GeoData geoData = mLocalPhoto.getGeoData();
            if (geoData != null) {
                addMapMarkerAndZoom(geoData.getLatitude(), geoData.getLongitude());
            } else {
                Location lastLocation = mLocationClient.getLastLocation();
                addMapMarkerAndZoom(lastLocation.getLatitude(), lastLocation.getLongitude());
            }
        }
    }

    private void addMapMarkerAndZoom(double latitude, double longitude) {
        mLocation = new GeoData((float)longitude, (float)latitude, Flickr.ACCURACY_STREET);
        LatLng latLng = new LatLng(latitude, longitude);
        String title = mLocalPhoto.getMetadata().getTitle();
        final int THUMBNAIL_SIZE = 100;
        Bitmap icon = ThumbnailUtils.extractThumbnail(
                BitmapUtils.decodeSampledBitmap(
                        mLocalPhoto.getUri(), THUMBNAIL_SIZE, THUMBNAIL_SIZE),
                THUMBNAIL_SIZE, THUMBNAIL_SIZE);

        mMapMarker = new MarkerOptions();
        mMapMarker
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .draggable(true);
        mMap.addMarker(mMapMarker);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(15)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.locationeditoractivity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_set_location:
                setActivityResult();
                finish();
                return true;  /* important */
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setActivityResult();
        super.onBackPressed();
    }

    private void setActivityResult() {
        Intent result = new Intent();
        Bundle resultData = new Bundle();
        new GsonHelper(this).marshallObject(mLocation, resultData, KEY_LOCATION);
        if (Constants.DEBUG) {
            Log.d(TAG, String.format("setting result location of %s,%s",
                    mLocation.getLatitude(), mLocation.getLongitude()));
        }
        result.putExtras(resultData);
        setResult(PhotoUploadFragment.ACTIVITY_RESULT_ADD_LOCATION, result);
    }

    /**
     * Called by Location Services if the connection to the location client drops because of an
     * error.
     */
     @Override
     public void onDisconnected() {
         // Display the connection status
     }

    /**
     * Called by Location Services if the attempt to Location Services fails.
     */
     @Override
     public void onConnectionFailed(ConnectionResult connectionResult) {
         /*
          * Google Play services can resolve some errors it detects.
          * If the error has a resolution, try sending an Intent to
          * start a Google Play services activity that can resolve
          * error.
          */
         if (connectionResult.hasResolution()) {
             try {
                 // Start an Activity that tries to resolve the error
                 connectionResult.startResolutionForResult(this,
                         CONNECTION_FAILURE_RESOLUTION_REQUEST);
             } catch (IntentSender.SendIntentException e) {
                 /* Thrown if Google Play services canceled the original PendingIntent */
                 e.printStackTrace();
             }
         } else {
             new ErrorDialogFragment().show(getSupportFragmentManager(), "ErrorDialogFragment");
         }
     }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng latLng = marker.getPosition();
        mLocation = new GeoData((float)latLng.longitude, (float)latLng.latitude,
                Flickr.ACCURACY_STREET);
        if (Constants.DEBUG) {
            Log.d(TAG, "Updated mLocation to " + latLng.latitude + "," + latLng.longitude);
        }
    }

    static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
