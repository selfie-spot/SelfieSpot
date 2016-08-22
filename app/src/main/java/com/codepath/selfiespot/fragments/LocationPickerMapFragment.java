package com.codepath.selfiespot.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationPickerMapFragment extends BaseMapFragment implements GoogleMap.OnMapClickListener {
    public static String ARG_INITIAL_POSITION = AlertLocationPickerMapFragment.class.getSimpleName() + ":INITIAL_POSITION";

    private LocationListener mLocationListener;
    private LatLng mInitialPosition;

    private Marker mCurrentMarker;

    public static LocationPickerMapFragment createInstance(final LatLng mInitialPosition) {
        final LocationPickerMapFragment fragment = new LocationPickerMapFragment();
        if (mInitialPosition != null) {
            final Bundle args = new Bundle();
            args.putParcelable(ARG_INITIAL_POSITION, mInitialPosition);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mInitialPosition = getArguments().getParcelable(ARG_INITIAL_POSITION);
        }
    }

    @Override
    protected void loadMap(final GoogleMap googleMap) {
        super.loadMap(googleMap);

        if (mInitialPosition != null) {
            onMapClick(mInitialPosition);
        }

        googleMap.setOnMapClickListener(this);
    }

    @Override
    protected void onLastLocationFound(final LatLng latLng) {
        // if a position has not been selected yet, mark the current location
        if (mCurrentMarker == null) {
            onMapClick(latLng);
        }
    }

    @Override
    public void onMapClick(final LatLng position) {
        if (mLocationListener != null) {
            mLocationListener.onLocationSelected(position);
        }
        drawMarker(position);
    }

    private void drawMarker(final LatLng position) {
        if (mCurrentMarker != null) {
            mCurrentMarker.remove();
            mCurrentMarker = null;
        }

        final BitmapDescriptor defaultMarker =
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);

        final MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title("Selfie Spot")
                .icon(defaultMarker);
        mCurrentMarker = mMap.addMarker(markerOptions);
    }

    public void setLocationListener(final LocationListener locationListener) {
        mLocationListener = locationListener;
    }

    public interface LocationListener {
        void onLocationSelected(final LatLng position);
    }
}
