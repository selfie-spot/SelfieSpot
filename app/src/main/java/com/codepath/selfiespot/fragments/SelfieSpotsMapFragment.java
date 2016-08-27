package com.codepath.selfiespot.fragments;

import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.codepath.selfiespot.activities.EditSelfieSpotActivity;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.views.SelfieSpotItemRenderer;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelfieSpotsMapFragment extends BaseMapFragment implements ClusterManager.OnClusterItemClickListener<SelfieSpot> {
    private static final String TAG = SelfieSpotsMapFragment.class.getSimpleName();
    private static final int MIN_ZOOM_LEVEL = 12;
    private static final int COUNTDOWN_TIME = 1000;

    // TODO - check if there is a better way to keep reference to added markers
    private Set<String> mMarkersReference = new HashSet<>();

    private ClusterManager<SelfieSpot> mClusterManager;
    private ParseQuery<SelfieSpot> mCurrentQuery;

    private CountDownTimer mCountDownTimer;

    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void loadMap(final GoogleMap googleMap) {
        super.loadMap(googleMap);

        mClusterManager = new ClusterManager<>(getActivity(), googleMap);
        mClusterManager.setRenderer(new SelfieSpotItemRenderer(getActivity(), mMap, mClusterManager));

        // googleMap.setOnCameraChangeListener(mClusterManager);
        googleMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterItemClickListener(this);

        mCountDownTimer = new CountDownTimer(COUNTDOWN_TIME, COUNTDOWN_TIME) {
            @Override
            public void onTick(final long millisUntilFinished) {
                // no-op
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "Count down timer triggered, querying parse");
                doQuery();
            }
        };

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                final float zoomLevel = googleMap.getCameraPosition().zoom;
                if (zoomLevel < MIN_ZOOM_LEVEL) {
                    Log.d(TAG, "Zoom level too high: " + zoomLevel);
                    clearAll();
                    return;
                }

                // cancel the ongoing timer, this would prevent sending a request every time the
                // map is panned
                mCountDownTimer.cancel();
                Log.d(TAG, "Cancelling count down timer");

                // start the counter
                mCountDownTimer.start();
            }
        });
    }

    private void doQuery() {
        // cancel the current query if running (no way to check if current query is running)
        if (mCurrentQuery != null) {
            mCurrentQuery.cancel();
        }

        final LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        final ParseGeoPoint sw = new ParseGeoPoint(latLngBounds.southwest.latitude, latLngBounds.southwest.longitude);
        final ParseGeoPoint ne = new ParseGeoPoint(latLngBounds.northeast.latitude, latLngBounds.northeast.longitude);

        // TODO - add parsequery cache, set a TTL

        mCurrentQuery = SelfieSpot.getWhereWithinGeoBoxQuery(sw, ne);
        mCurrentQuery.findInBackground(new FindCallback<SelfieSpot>() {
            @Override
            public void done(final List<SelfieSpot> selfieSpots, final ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Retrieved selfie-spots: " + selfieSpots.size());
                    addMarkers(selfieSpots);
                } else {
                    Log.e(TAG, "Unable to retrieve selfie-spots", e);
                    Toast.makeText(getActivity(), "Unable to retrieve selfie-spots", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addMarkers(final List<SelfieSpot> selfieSpots) {
        // the user might have zoomed-out beyond the min zoom level, so, no point adding markers,
        // also clear the map
        if (mMap.getCameraPosition().zoom < MIN_ZOOM_LEVEL) {
            clearAll();
        }

        for(final SelfieSpot selfieSpot: selfieSpots) {
            // already added to the map, don't do anything
            if(mMarkersReference.contains(selfieSpot.getObjectId())) {
                continue;
            }

            mClusterManager.addItem(selfieSpot);
            mMarkersReference.add(selfieSpot.getObjectId());
        }
        // without this, pins are not displayed the first time
        mClusterManager.cluster();
    }

    private void clearAll() {
        mClusterManager.clearItems();
        mMarkersReference.clear();
        mMap.clear();
    }

    @Override
    public boolean onClusterItemClick(final SelfieSpot selfieSpot) {
        final Intent intent = EditSelfieSpotActivity.createIntent(getActivity(), selfieSpot.getObjectId());
        startActivity(intent);
        return true;
    }
}