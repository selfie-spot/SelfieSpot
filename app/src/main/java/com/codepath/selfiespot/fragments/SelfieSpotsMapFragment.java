package com.codepath.selfiespot.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.activities.TempDetailSelfieSpotActivity;
import com.codepath.selfiespot.models.SearchFilter;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.util.CollectionUtils;
import com.codepath.selfiespot.views.SelfieSpotItemRenderer;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelfieSpotsMapFragment extends BaseMapFragment implements
        ClusterManager.OnClusterItemClickListener<SelfieSpot>, FiltersDialogFragment.FiltersCallback {
    private static final String TAG = SelfieSpotsMapFragment.class.getSimpleName();
    private static final String PIN_LABEL_SELFIES = MySelfiesFragment.class.getSimpleName() + ":SELFIES";

    private static final int MIN_ZOOM_LEVEL = 12;
    private static final int COUNTDOWN_TIME = 1000;

    // TODO - check if there is a better way to keep reference to added markers
    private Set<String> mMarkersReference = new HashSet<>();

    private ClusterManager<SelfieSpot> mClusterManager;
    private ParseQuery<SelfieSpot> mCurrentQuery;

    private CountDownTimer mCountDownTimer;

    private MenuItem mActionProgressBarMenuItem;
    private MenuItem mFilterMenuItem;
    private SearchFilter mSearchFilter;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(final Bundle bundle) {
        super.onActivityCreated(bundle);

        // unpin all the previously pinned objects
        ParseObject.unpinAllInBackground(PIN_LABEL_SELFIES, new DeleteCallback() {
            @Override
            public void done(final ParseException e) {
                if (e != null) {
                    Log.w(TAG, "Error unpinning: " + PIN_LABEL_SELFIES);
                } else {
                    Log.d(TAG, "Unpinned: " + PIN_LABEL_SELFIES);
                }
            }
        });
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void loadMap(final GoogleMap googleMap) {
        super.loadMap(googleMap);

        mClusterManager = new ClusterManager<>(getActivity(), googleMap);
        mClusterManager.setRenderer(new SelfieSpotItemRenderer(getActivity(), mMap, mClusterManager));

        // googleMap.setOnCameraChangeListener(mClusterManager);
        googleMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterItemClickListener(this);

        mCountDownTimer = new CountDownTimer(COUNTDOWN_TIME, COUNTDOWN_TIME) {
            @Override
            public void onTick(final long millisUntilFinished) {
                // no-op
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "Count down timer triggered, querying parse");
                doQuery();
            }
        };

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (! retrieveSelfieSpots()) {
                    clearAll();
                    return;
                }

                // cancel the ongoing timer, this would prevent sending a request every time the
                // map is panned
                mCountDownTimer.cancel();
                Log.d(TAG, "Cancelling count down timer");

                // start the counter
                mCountDownTimer.start();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndQuery();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_frag_selfie_map, menu);
        mActionProgressBarMenuItem = menu.findItem(R.id.action_view_progress);
        mFilterMenuItem = menu.findItem(R.id.action_nav_filter);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_nav_filter: {
                openFilters();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void openFilters() {
        final FiltersDialogFragment fragment = FiltersDialogFragment.createInstance(mSearchFilter);
        fragment.show(getChildFragmentManager(), "dialog");
        fragment.setFiltersCallback(this);
    }

    private void checkAndQuery() {
        if (mMap != null && retrieveSelfieSpots()) {
            clearAll();
            doQuery();
        }
    }

    private void doQuery() {
        // cancel the current query if running (no way to check if current query is running)
        if (mCurrentQuery != null) {
            mCurrentQuery.cancel();
        }

        showBusy();

        final LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        final ParseGeoPoint sw = new ParseGeoPoint(latLngBounds.southwest.latitude, latLngBounds.southwest.longitude);
        final ParseGeoPoint ne = new ParseGeoPoint(latLngBounds.northeast.latitude, latLngBounds.northeast.longitude);

        // TODO - add parsequery cache, set a TTL
        mCurrentQuery = SelfieSpot.getWhereWithinGeoBoxQuery(sw, ne, mSearchFilter);

        mCurrentQuery.findInBackground(new FindCallback<SelfieSpot>() {
            @Override
            public void done(final List<SelfieSpot> selfieSpots, final ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Retrieved selfie-spots: " + selfieSpots.size());
                    ParseObject.pinAllInBackground(PIN_LABEL_SELFIES, selfieSpots);
                    addMarkers(selfieSpots);
                } else {
                    Log.e(TAG, "Unable to retrieve selfie-spots", e);
                    Toast.makeText(getActivity(), "Unable to retrieve selfie-spots", Toast.LENGTH_SHORT).show();
                }
                hideBusy();
            }
        });
    }

    protected boolean animateCameraToCurrentLocation() {
        return true;
    }

    private void addMarkers(final List<SelfieSpot> selfieSpots) {
        // the user might have zoomed-out beyond the min zoom level, so, no point adding markers,
        // also clear the map
        if (mMap.getCameraPosition().zoom < MIN_ZOOM_LEVEL) {
            clearAll();
        }

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

    private void showBusy() {
        mActionProgressBarMenuItem.setVisible(true);
    }

    private void hideBusy() {
        mActionProgressBarMenuItem.setVisible(false);
    }

    private void clearAll() {
        mClusterManager.clearItems();
        mClusterManager.cluster();
        mMarkersReference.clear();
        mMap.clear();
    }

    private boolean retrieveSelfieSpots() {
        final float zoomLevel = mMap.getCameraPosition().zoom;
        final boolean retrieve = zoomLevel >= MIN_ZOOM_LEVEL;
        if (! retrieve) {
            Log.d(TAG, "Zoom level too high: " + zoomLevel);
        }
        return retrieve;
    }

    @Override
    public boolean onClusterItemClick(final SelfieSpot selfieSpot) {
        final Intent intent = TempDetailSelfieSpotActivity.createIntent(getActivity(), selfieSpot.getObjectId());
        startActivity(intent);
        return true;
    }

    @Override
    public void setFilters(final SearchFilter searchFilter) {
        Log.d(TAG, "Filter: " + searchFilter);
        mSearchFilter = searchFilter;

        if (CollectionUtils.isEmpty(searchFilter.getTags()) && ! searchFilter.isHideZeroLikes()) {
            // no filtering
            mFilterMenuItem.setIcon(R.drawable.ic_filter);
        } else {
            // some filtering criteria exists
            mFilterMenuItem.setIcon(R.drawable.ic_filter_populated);
        }

        checkAndQuery();
    }
}