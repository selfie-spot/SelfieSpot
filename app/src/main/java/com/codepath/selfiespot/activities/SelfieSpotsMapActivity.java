package com.codepath.selfiespot.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.selfiespot.R;
import com.codepath.selfiespot.repositories.PreferencesDAO;
import com.codepath.selfiespot.util.ParseUserUtil;
import com.parse.ParseUser;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class SelfieSpotsMapActivity extends AppCompatActivity {
    private static final String TAG = SelfieSpotsMapActivity.class.getSimpleName();
    private static final int PROFILE_TRANSFORM_MARGIN = 0;
    private static final int PROFILE_TRANSFORM_RADIUS = 10;

    private static final int INDEX_HOME = 0;
    private static final int DURATION_FAB_DELAY = 600; // millis

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.nv_drawer)
    NavigationView mDrawer;

    @BindView(R.id.fab_selfie)
    FloatingActionButton mSelfieFab;

    @BindView(R.id.fl_fab_container)
    FrameLayout mSelfieFabContainer;

    private ActionBarDrawerToggle mDrawerToggle;

    @Inject
    PreferencesDAO mPreferencesDAO;

    public static Intent createIntent(final Context context) {
        final Intent intent = new Intent(context, SelfieSpotsMapActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie_spots_map);
        ButterKnife.bind(this);

        mDrawerToggle = setupDrawerToggle();
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        setSupportActionBar(mToolbar);
        setupDrawerContent();

        setHomeInfo();

        mSelfieFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                openCreateSelfieSpot();
            }
        });

        populateLoggedInUserDetails();
    }

    private void openCreateSelfieSpot() {
        mSelfieFabContainer.setBackgroundResource(R.drawable.circular_button);
        final Intent intent = EditSelfieSpotActivity.createIntent(SelfieSpotsMapActivity.this, null);

        final int cx = (mSelfieFab.getWidth() / 2);
        final int cy = (mSelfieFab.getHeight() / 2);

        final ActivityOptionsCompat compat = ActivityOptionsCompat.makeClipRevealAnimation(mSelfieFab, cx, cy, mSelfieFab.getWidth(), mSelfieFab.getHeight());
        startActivity(intent, compat.toBundle());
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSelfieFab.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSelfieFab.show();
            }
        }, DURATION_FAB_DELAY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSelfieFabContainer.setBackground(null);
        mSelfieFab.hide();
    }

    private void setHomeInfo() {
        final MenuItem homeMenuItem = mDrawer.getMenu().getItem(INDEX_HOME);
        homeMenuItem.setChecked(true);
        setTitle(homeMenuItem.getTitle());
    }

    private void populateLoggedInUserDetails() {
        final View headerLayout = mDrawer.getHeaderView(0);
        final TextView profileNameTextView = (TextView) headerLayout.findViewById(R.id.tv_profile_name);
        final ImageView profileImageView = (ImageView) headerLayout.findViewById(R.id.iv_profile);

        final ParseUser parseUser = ParseUser.getCurrentUser();
        profileNameTextView.setText(parseUser.getUsername());

        Glide.with(this)
                .load(ParseUserUtil.getProfilePictureUrl(parseUser))
                .bitmapTransform(new RoundedCornersTransformation(this, PROFILE_TRANSFORM_RADIUS, PROFILE_TRANSFORM_MARGIN))
                .placeholder(R.drawable.ic_progress_indeterminate)
                .error(R.drawable.ic_error)
                .into(profileImageView);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
    }

    private void setupDrawerContent() {
        mDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
                selectDrawerItem(item);
                return true;
            }
        });
    }

    public void selectDrawerItem(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_home: {
                // already on home, do nothing
                break;
            }
            case R.id.nav_profile: {
               final Intent intent = ProfileActivity.createIntent(this);
                startActivity(intent);
                break;
            }
            case R.id.nav_settings: {
                // no-op
                break;
            }
            default: {
                Log.w(TAG, "Unknown menu item: " + menuItem.getTitle());
                // no-op
                break;
            }
        }
        mDrawerLayout.closeDrawers();
    }

}
