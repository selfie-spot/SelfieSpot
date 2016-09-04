package com.codepath.selfiespot.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.activities.TempDetailSelfieSpotActivity;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.util.CollectionUtils;
import com.codepath.selfiespot.views.adapters.SelfieSpotAdapter;
import com.codepath.selfiespot.views.adapters.SelfieSpotItemCallback;
import com.codepath.selfiespot.views.adapters.viewholders.SelfieSpotViewHolder;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

/**
 * Fragment to show the selfie spots created by logged-in user.
 */
public class MySelfiesFragment extends Fragment implements SelfieSpotItemCallback {
    private static final String TAG = MySelfiesFragment.class.getSimpleName();
    private static final String PIN_LABEL_MY_SELFIES = MySelfiesFragment.class.getSimpleName() + ":MY_SELFIES";

    @BindView(R.id.rv_my_selfies)
    RecyclerView mvSelfieSpotsRecyclerView;

    @BindView(R.id.srl_my_selfies)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.tv_no_selfies)
    TextView mNoSelfiesTextView;

    private SelfieSpotAdapter mSelfieSpotAdapter;
    private StaggeredGridLayoutManager mLayoutManager;

    // TODO - support for pagination

    public static MySelfiesFragment createInstance() {
        return new MySelfiesFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_selfies, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mSelfieSpotAdapter = new SelfieSpotAdapter(new ArrayList<SelfieSpot>(), this);
        final ScaleInAnimationAdapter adapter = new ScaleInAnimationAdapter(mSelfieSpotAdapter);
        adapter.setFirstOnly(false);
        mvSelfieSpotsRecyclerView.setAdapter(adapter);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mvSelfieSpotsRecyclerView.setLayoutManager(mLayoutManager);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                retrieveData();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        retrieveData();
    }

    private void retrieveData() {
        mSwipeRefreshLayout.setRefreshing(true);
        mSelfieSpotAdapter.clearAll();
        showBusy();

        final ParseQuery<SelfieSpot> query = SelfieSpot.getMySelfieSpots(ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<SelfieSpot>() {
            @Override
            public void done(final List<SelfieSpot> selfieSpotsobjects, final ParseException e) {
                if(e == null) {
                    Log.d(TAG, "Retrieved My SelfieSpots: " + selfieSpotsobjects.size());
                    if (CollectionUtils.isEmpty(selfieSpotsobjects)) {
                        showNoSelfies();
                    } else {
                        hideNoSelfies();
                        // cache according to the recommended pattern - https://parseplatform.github.io//docs/android/guide/#caching-query-results
                        ParseObject.unpinAllInBackground(PIN_LABEL_MY_SELFIES, new DeleteCallback() {
                            @Override
                            public void done(final ParseException e) {
                                mSelfieSpotAdapter.addSelfieSpots(selfieSpotsobjects);
                                ParseObject.pinAllInBackground(PIN_LABEL_MY_SELFIES, selfieSpotsobjects);
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "Unable to retrieve My SelfieSpots", e);
                }
                hideBusy();
            }
        });
    }

    @Override
    public void onSelfieSpotSelected(final SelfieSpot selfieSpot, final SelfieSpotViewHolder viewHolder) {
        final Intent intent = TempDetailSelfieSpotActivity.createIntent(getActivity(), selfieSpot.getObjectId());
        final ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(getActivity(), (View)viewHolder.mImageView, "image");
        startActivity(intent, options.toBundle());
    }

    private void showBusy() {
        mSwipeRefreshLayout.setRefreshing(true);
        mvSelfieSpotsRecyclerView.setVisibility(View.GONE);
        mNoSelfiesTextView.setVisibility(View.GONE);
    }

    private void hideBusy() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void showNoSelfies() {
        mvSelfieSpotsRecyclerView.setVisibility(View.GONE);
        mNoSelfiesTextView.setVisibility(View.VISIBLE);
    }

    private void hideNoSelfies() {
        mvSelfieSpotsRecyclerView.setVisibility(View.VISIBLE);
        mNoSelfiesTextView.setVisibility(View.GONE);
    }
}
