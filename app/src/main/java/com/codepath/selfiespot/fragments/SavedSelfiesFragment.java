package com.codepath.selfiespot.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.views.adapters.SelfieSpotItemCallback;

/**
 * A simple {@link Fragment} subclass.
 */
public class SavedSelfiesFragment extends Fragment implements SelfieSpotItemCallback {


    public SavedSelfiesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved_selfies, container, false);
    }

    @Override
    public void onSelfieSpotSelected(SelfieSpot selfieSpot) {

    }
}
