package com.codepath.selfiespot.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.codepath.selfiespot.R;
import com.google.android.gms.maps.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlertLocationPickerMapFragment extends DialogFragment implements LocationPickerMapFragment.LocationListener {
    public static String ARG_INITIAL_POSITION = AlertLocationPickerMapFragment.class.getSimpleName() + ":INITIAL_POSITION";

    private LatLng mInitialPosition;
    private LatLng mCurrentPosition;

    private LocationPickedListener mLocationPickedListener;

    @BindView(R.id.fab_select_location)
    FloatingActionButton mSelectionButton;

    public static AlertLocationPickerMapFragment createInstance(final LatLng mInitialPosition) {
        final AlertLocationPickerMapFragment fragment = new AlertLocationPickerMapFragment();
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
    public void onResume() {
        // Get existing layout params for the window
        final ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_alert_location_picker, container, false);
        ButterKnife.bind(this, view);

        final LocationPickerMapFragment fragment = LocationPickerMapFragment.createInstance(mInitialPosition);
        fragment.setLocationListener(this);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.fl_container, fragment).commit();

        mSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (mLocationPickedListener != null) {
                    mLocationPickedListener.onLocationPicked(mCurrentPosition);
                }
            }
        });

        return view;
    }

    @Override
    public void onLocationSelected(final LatLng position) {
        mCurrentPosition = position;
    }

    public void setLocationPickedListener(final LocationPickedListener locationPickedListener) {
        mLocationPickedListener = locationPickedListener;
    }

    public interface LocationPickedListener {
        void onLocationPicked(LatLng location);
    }
}
