package com.codepath.selfiespot.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.codepath.selfiespot.R;
import com.google.android.gms.maps.model.LatLng;

import butterknife.ButterKnife;

public class AlertLocationMapFragment extends DialogFragment {
    public static String ARG_POSITION = AlertLocationMapFragment.class.getSimpleName() + ":POSITION";

    private LatLng mPosition;

    public static AlertLocationMapFragment createInstance(final LatLng position) {
        final AlertLocationMapFragment fragment = new AlertLocationMapFragment();
        if (position != null) {
            final Bundle args = new Bundle();
            args.putParcelable(ARG_POSITION, position);
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
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_location, container, false);
        ButterKnife.bind(this, view);

        final LocationMapFragment fragment = LocationMapFragment.createInstance(mPosition);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.fl_container, fragment).commit();
        return view;
    }
}