package com.codepath.selfiespot.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.util.CollectionUtils;
import com.codepath.selfiespot.util.ParseUserUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class GoogleApiClientBootstrapService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = GoogleApiClientBootstrapService.class.getSimpleName();
    private static final int RADIUS_IN_METERS = 200;


    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.d(TAG, "Starting service..");

        // keep alive until we call stopSelf()
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        super.onDestroy();
    }

    @Override
    public void onConnected(final Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
        retrieveBoomarksAndUpdateGeofences();
    }

    @Override
    public void onConnectionSuspended(final int i) {
        Log.i(TAG, "GoogleApiClient Connection Suspended");
    }

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    private void retrieveBoomarksAndUpdateGeofences() {
        final ParseQuery<SelfieSpot> query = ParseUserUtil.getBookmarksQuery(ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<SelfieSpot>() {
            @Override
            public void done(final List<SelfieSpot> selfieSpotsobjects, final ParseException e) {
                if(e == null) {
                    // remove existing geofences
                    LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeofencePendingIntent());
                    Log.d(TAG, "Retrieved Bookmarks SelfieSpots: " + selfieSpotsobjects.size());
                    if (! CollectionUtils.isEmpty(selfieSpotsobjects)) {
                        addGeofences(selfieSpotsobjects);
                    }
                } else {
                    Log.e(TAG, "Unable to retrieve Bookmarked SelfieSpots", e);
                }
            }
        });
    }



    @SuppressWarnings({"MissingPermission"})
    private void addGeofences(final List<SelfieSpot> selfieSpotsobjects) {
        // geofences request
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeoFencingRequest(selfieSpotsobjects),
                getGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(final Status status) {
                if (status.isSuccess()) {
                    Log.e(TAG, "Successfully added geofences");
                } else {
                    Log.e(TAG, "Error adding Geofencing" + status.getStatusMessage());
                }
                // kill the service, we no longer need it..
                stopSelf();
            }
        });
    }

    private GeofencingRequest getGeoFencingRequest(final List<SelfieSpot> selfieSpots) {
        final List<Geofence> geofenceList = new ArrayList<>();

        for (final SelfieSpot selfieSpot: selfieSpots) {
            final LatLng location = selfieSpot.getPosition();
            geofenceList.add(createGeofence(selfieSpot.getObjectId(), location.latitude, location.longitude, RADIUS_IN_METERS));
        }

        final GeofencingRequest.Builder builder = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofences(geofenceList);

        return builder.build();
    }

    private Geofence createGeofence(final String id, double lat, final double lon, final float radiusInMeters) {
        return new Geofence.Builder().setRequestId(id)
                .setCircularRegion(lat, lon, radiusInMeters)
                .setExpirationDuration(-1)
                .setLoiteringDelay(5000)
                .setNotificationResponsiveness(0)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        final Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
