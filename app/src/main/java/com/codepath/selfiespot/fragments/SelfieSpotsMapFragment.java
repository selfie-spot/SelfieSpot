package com.codepath.selfiespot.fragments;

import android.util.Log;
import android.widget.Toast;

import com.codepath.selfiespot.models.SelfieSpot;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.List;

public class SelfieSpotsMapFragment extends BaseMapFragment {
    private static final String TAG = SelfieSpotsMapFragment.class.getSimpleName();

    @Override
    protected void loadMap(final GoogleMap googleMap) {
        super.loadMap(googleMap);

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                // FIXME - instead of this brute mechanism, maintain list of markers and remove/add
                // them if not in the map already
                googleMap.clear();
                final LatLngBounds latLngBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
                ParseGeoPoint sw = new ParseGeoPoint(latLngBounds.southwest.latitude, latLngBounds.southwest.longitude);
                ParseGeoPoint ne = new ParseGeoPoint(latLngBounds.northeast.latitude, latLngBounds.northeast.longitude);
                ParseQuery<SelfieSpot> query = SelfieSpot.getWhereWithinGeoBoxQuery(sw, ne);
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
            final MarkerOptions markerOpts =
                    new MarkerOptions().position(new LatLng(selfieSpot.getLocation().getLatitude(),
                            selfieSpot.getLocation().getLongitude()));
            mMap.addMarker(markerOpts);
        }
    }
}