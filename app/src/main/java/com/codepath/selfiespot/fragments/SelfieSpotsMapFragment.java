package com.codepath.selfiespot.fragments;

import android.content.Intent;
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
    // TODO - check if there is a better way to keep reference to added markers
    private Set<String> mMarkersReference = new HashSet<>();

    private ClusterManager<SelfieSpot> mClusterManager;

    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void loadMap(final GoogleMap googleMap) {
        super.loadMap(googleMap);

        googleMap.setMyLocationEnabled(false);

        mClusterManager = new ClusterManager<>(getActivity(), googleMap);
        mClusterManager.setRenderer(new SelfieSpotItemRenderer(getActivity(), mMap, mClusterManager));

        // googleMap.setOnCameraChangeListener(mClusterManager);
        googleMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterItemClickListener(this);

        // TODO - add parsequery cache, set a TTL

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                final LatLngBounds latLngBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
                final ParseGeoPoint sw = new ParseGeoPoint(latLngBounds.southwest.latitude, latLngBounds.southwest.longitude);
                final ParseGeoPoint ne = new ParseGeoPoint(latLngBounds.northeast.latitude, latLngBounds.northeast.longitude);
                final ParseQuery<SelfieSpot> query = SelfieSpot.getWhereWithinGeoBoxQuery(sw, ne);
                query.findInBackground(new FindCallback<SelfieSpot>() {
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
        });
    }

    private void addMarkers(final List<SelfieSpot> selfieSpots) {
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

    @Override
    public boolean onClusterItemClick(final SelfieSpot selfieSpot) {
        final Intent intent = EditSelfieSpotActivity.createIntent(getActivity(), selfieSpot.getObjectId());
        startActivity(intent);
        return true;
    }
}