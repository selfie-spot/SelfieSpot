package com.codepath.selfiespot.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.activities.TempDetailSelfieSpotActivity;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.util.ParseUserUtil;
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

/**
 * Fragment to show bookmarked selfie spots.
 */
public class BookmarkedSelfiesFragment extends Fragment implements SelfieSpotItemCallback {
    private static final String TAG = MySelfiesFragment.class.getSimpleName();

    @BindView(R.id.rvMySelfieSpot)
    RecyclerView rvSelfieSpot;

    private SelfieSpotAdapter mSelfieSpotAdapter;

    // TODO - support for pagination

    public static BookmarkedSelfiesFragment createInstance() {
        return new BookmarkedSelfiesFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmarked_selfies, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mSelfieSpotAdapter = new SelfieSpotAdapter(new ArrayList<SelfieSpot>());
        rvSelfieSpot.setAdapter(mSelfieSpotAdapter);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvSelfieSpot.setLayoutManager(linearLayoutManager);

    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        retrieveData();
    }

    private void retrieveData() {
        final ParseQuery<SelfieSpot> query = ParseUserUtil.getBookmarksQuery(ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<SelfieSpot>() {
            @Override
            public void done(final List<SelfieSpot> selfieSpotsobjects, final ParseException e) {
                if(e == null) {
                    Log.d(TAG, "Retrieved Bookmarks SelfieSpots: " + selfieSpotsobjects.size());
                    mSelfieSpotAdapter.addSelfieSpots(selfieSpotsobjects);
                }
                else{
                    Log.e(TAG, "Unable to retrieve Bookmarked SelfieSpots", e);
                }
            }
        });
    }

    @Override
    public void onSelfieSpotSelected(final SelfieSpot selfieSpot) {
        final Intent intent = TempDetailSelfieSpotActivity.createIntent(getActivity(), selfieSpot.getObjectId());
        startActivity(intent);
    }
}
