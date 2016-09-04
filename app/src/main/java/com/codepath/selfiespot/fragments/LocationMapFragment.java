package com.codepath.selfiespot.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationMapFragment extends BaseMapFragment {
    public static String ARG_POSITION = AlertLocationPickerMapFragment.class.getSimpleName() + "POSITION";

    private LatLng mPosition;
    private View.OnClickListener mOnClickListener;
    private boolean mEnableUiGestureSettings = true;

    public static LocationMapFragment createInstance(final LatLng mPosition) {
        final LocationMapFragment fragment = new LocationMapFragment();
        if (mPosition != null) {
            final Bundle args = new Bundle();
            args.putParcelable(ARG_POSITION, mPosition);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPosition = getArguments().getParcelable(ARG_POSITION);
        }
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void loadMap(final GoogleMap googleMap) {
        super.loadMap(googleMap);
        googleMap.getUiSettings().setAllGesturesEnabled(mEnableUiGestureSettings);
        mMap.setMyLocationEnabled(false);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                if (mOnClickListener != null) {
                    // google map doesn't expose a view
                    mOnClickListener.onClick(null);
                }
            }
        });

        drawMarker(mPosition);

        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mPosition, 17);
        mMap.animateCamera(cameraUpdate);
    }

    private void drawMarker(final LatLng position) {
        final BitmapDescriptor defaultMarker =
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);

        final MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title("Selfie Spot")
                .icon(defaultMarker);

        mMap.addMarker(markerOptions);
    }

    public void setOnClickListener(final View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void setEnableUiGestureSettings(final boolean enableUiGestureSettings) {
        mEnableUiGestureSettings = enableUiGestureSettings;
    }
}
