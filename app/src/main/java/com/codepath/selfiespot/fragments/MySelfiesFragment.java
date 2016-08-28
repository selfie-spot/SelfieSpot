package com.codepath.selfiespot.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.views.EndlessRecyclerViewScrollListener;
import com.codepath.selfiespot.views.adapters.SelfieSpotAdapter;
import com.codepath.selfiespot.views.adapters.SelfieSpotItemCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class MySelfiesFragment extends Fragment implements SelfieSpotItemCallback {
    private static final String TAG = MySelfiesFragment.class.getSimpleName();
    private static final String EXTRA_SELFIE_SPOT_ID = MySelfiesFragment.class.getSimpleName() + ":SELFIE_SPOT_ID";


    SelfieSpotAdapter mSelfieSpotAdapter;
    ArrayList<SelfieSpot> selfieSpots;
    @BindView(R.id.rvMySelfieSpot)
    RecyclerView rvSelfieSpot;
    private Unbinder unbinder;
    int limit = 20;

    private EndlessRecyclerViewScrollListener mEndlessRecyclerViewScrollListener;

    public static Intent createIntent(final Context context, final String selfieSpotId) {
        final Intent intent = new Intent(context, MySelfiesFragment.class);
        if (! TextUtils.isEmpty(selfieSpotId)) {
            intent.putExtra(EXTRA_SELFIE_SPOT_ID, selfieSpotId);
        }
        return intent;
    }

    public MySelfiesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_my_selfies, container, false);
        unbinder = ButterKnife.bind(this, v);

        mSelfieSpotAdapter = new SelfieSpotAdapter(getContext(), selfieSpots);
        rvSelfieSpot.setAdapter(mSelfieSpotAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvSelfieSpot.setLayoutManager(linearLayoutManager);
        rvSelfieSpot.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                populateData(limit += 20);
            }
        });
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfieSpots = new ArrayList<>();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populateData(limit);

    }


    private void populateData(int limit){

        if (ParseUser.getCurrentUser() != null) {
            ParseQuery<SelfieSpot> query = SelfieSpot.getMySelfieSpot(ParseUser.getCurrentUser(), limit);
            query.findInBackground(new FindCallback<SelfieSpot>() {
                @Override
                public void done(List<SelfieSpot> selfieSpotsobjects, ParseException e) {
                    if(e == null) {
                        Log.d("selfiespot", "SUCCESS");
                        mSelfieSpotAdapter.addSelfieSpots(selfieSpotsobjects);
                    }
                    else{
                        Log.d("selfiespot", "Failure");
                    }
                }
            });
        }
    }


    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        rvSelfieSpot.setLayoutManager(null);
    }


    @Override
    public void onSelfieSpotSelected(SelfieSpot selfieSpot) {

    }
}
