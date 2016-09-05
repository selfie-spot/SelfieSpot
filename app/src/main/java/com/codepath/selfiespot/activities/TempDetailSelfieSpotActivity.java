package com.codepath.selfiespot.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.codepath.selfiespot.R;
import com.codepath.selfiespot.fragments.AlertLocationMapFragment;
import com.codepath.selfiespot.fragments.LocationMapFragment;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.services.GoogleApiClientBootstrapService;
import com.codepath.selfiespot.util.CollectionUtils;
import com.codepath.selfiespot.util.DateUtils;
import com.codepath.selfiespot.util.ParseUserUtil;
import com.codepath.selfiespot.util.ViewUtils;
import com.codepath.selfiespot.views.DynamicHeightImageView;
import com.google.android.gms.maps.model.LatLng;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TempDetailSelfieSpotActivity extends AppCompatActivity {
    private static final String TAG = TempDetailSelfieSpotActivity.class.getSimpleName();

    private static final String EXTRA_SELFIE_SPOT_ID = TempDetailSelfieSpotActivity.class.getSimpleName() + ":SELFIE_SPOT_ID";

    private static final int ROUND_TRANSFORMATION_RADIUS = 20;
    private static final int ROUND_TRANSFORMATION_MARGIN = 5;

    @BindView(R.id.iv_image)
    DynamicHeightImageView mImageView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tv_name)
    TextView mNameTextView;

    @BindView(R.id.tv_author)
    TextView mAuthorTextView;

    @BindView(R.id.tv_time)
    TextView mTimeTextView;

    @BindView(R.id.tv_likes)
    TextView mLikesTextView;

    @BindView(R.id.tv_tags)
    TextView mTagsTextView;

    @BindView(R.id.divider_likes)
    View mLikesDivider;

    @BindView(R.id.iv_bookmark_action)
    ImageView mBookmarkImageView;

    @BindView(R.id.iv_like_action)
    ImageView mLikeImageView;

    @BindView(R.id.iv_share_action)
    ImageView mShareImageView;

    @BindView(R.id.fl_progress_holder)
    FrameLayout mProgressFrameLayout;

    @BindView(R.id.pb_loading)
    ProgressBar mLoadingProgressBar;

    private SelfieSpot mSelfieSpot;

    private MenuItem mDeleteItem;
    private boolean mCurrentlyBookmarked;

    public static Intent createIntent(final Context context, final String selfieSpotId) {
        final Intent intent = new Intent(context, TempDetailSelfieSpotActivity.class);
        if (!TextUtils.isEmpty(selfieSpotId)) {
            intent.putExtra(EXTRA_SELFIE_SPOT_ID, selfieSpotId);
        }
        return intent;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_detail_selfie_spot);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final String selfieSpotId = getIntent().getStringExtra(EXTRA_SELFIE_SPOT_ID);
        showBusy();
        // retrieve SelfieSpot before initializing views
        final ParseQuery<SelfieSpot> query = SelfieSpot.getQuery();
        query.fromLocalDatastore();
        query.getInBackground(selfieSpotId, new GetCallback<SelfieSpot>() {
            @Override
            public void done(final SelfieSpot selfieSpot, final ParseException e) {
                if (e != null) {
                    Toast.makeText(TempDetailSelfieSpotActivity.this,
                            "Unable to retrieve SelfieSpot", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Unable to retrieve SelfieSpot: " + selfieSpotId, e);
                    finish();
                } else {
                    hideBusy();
                    mSelfieSpot = selfieSpot;
                    initViews();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_detail, menu);
        mDeleteItem = menu.findItem(R.id.action_delete);
        determineDeleteMenuVisiblity();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home: {
                supportFinishAfterTransition();
                return true;
            }
            case R.id.action_delete: {
                delete();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void delete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(R.string.delete_warning)
                .setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int i) {
                        // no-op
                    }
                })
                .setPositiveButton(R.string.delete_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int i) {
                        showBusy();

                        mSelfieSpot.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                // TODO - determine if activity is visible before doing anything view related
                                if (e == null) {
                                    finish();
                                } else {
                                    hideBusy();
                                    Toast.makeText(TempDetailSelfieSpotActivity.this, "Error deleting SelfieSpot", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                })
                .show();
    }

    private void initViews() {
        mNameTextView.setText(mSelfieSpot.getName());

        final ParseUser loggedInUser = ParseUser.getCurrentUser();

        try {
            mAuthorTextView.setText(String.format(getResources().getString(R.string.text_by), mSelfieSpot.getUser().fetchIfNeeded().getUsername()));
        } catch (final ParseException e) {
            Log.w(TAG, "Unable to retrieve username", e);
        }

        mImageView.setHeightRatio(
                (float) mSelfieSpot.getMediaHeight() / (float) mSelfieSpot.getMediaWidth());

        Glide.with(this)
                .load(mSelfieSpot.getMediaFile().getUrl())
                .fitCenter()
                .bitmapTransform(new RoundedCornersTransformation(this, ROUND_TRANSFORMATION_RADIUS, ROUND_TRANSFORMATION_MARGIN))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.ic_progress_indeterminate)
                .error(R.drawable.ic_error)
                .into(mImageView);

        mTimeTextView.setText(DateUtils.getDetailPageTime(mSelfieSpot.getCreatedAt()));
        mLikesTextView.setText(ViewUtils.getSpannedText(this, getString(R.string.text_likes),
                mSelfieSpot.getLikesCount()));

        ParseUserUtil.isBookmarked(loggedInUser, mSelfieSpot, new FindCallback<SelfieSpot>() {
            @Override
            public void done(final List<SelfieSpot> objects, final ParseException e) {
                if (e != null) {
                    Toast.makeText(TempDetailSelfieSpotActivity.this, "Unable to determine if bookmarked", Toast.LENGTH_SHORT).show();
                    return;
                }

                mCurrentlyBookmarked = ! CollectionUtils.isEmpty(objects);
                setBookmarkIcon(mCurrentlyBookmarked);

                mBookmarkImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        if (! mCurrentlyBookmarked) {
                            ParseUserUtil.bookmarkSelfieSpot(loggedInUser, mSelfieSpot, new SaveCallback() {
                                @Override
                                public void done(final ParseException e) {
                                    if (e != null) {
                                        Toast.makeText(TempDetailSelfieSpotActivity.this, "Unable to add bookmark", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Unable to add bookmark: " + mSelfieSpot.getObjectId(), e);
                                        return;
                                    }
                                    mCurrentlyBookmarked = true;
                                    setBookmarkIcon(mCurrentlyBookmarked);

                                    // fire service to update bookmarks geofences
                                    TempDetailSelfieSpotActivity.this.startService(GoogleApiClientBootstrapService.createIntent(TempDetailSelfieSpotActivity.this));
                                }
                            });
                        } else {
                            ParseUserUtil.unbookmarkSelfieSpot(loggedInUser, mSelfieSpot, new SaveCallback() {
                                @Override
                                public void done(final ParseException e) {
                                    if (e != null) {
                                        Toast.makeText(TempDetailSelfieSpotActivity.this, "Unable to remove bookmark", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Unable to remove bookmark: " + mSelfieSpot.getObjectId(), e);
                                        return;
                                    }
                                    mCurrentlyBookmarked = false;
                                    setBookmarkIcon(mCurrentlyBookmarked);

                                    // fire service to update bookmarks geofences
                                    TempDetailSelfieSpotActivity.this.startService(GoogleApiClientBootstrapService.createIntent(TempDetailSelfieSpotActivity.this));
                                }
                            });
                        }
                    }
                });

                // for the time being only enable the listener if not bookmarked
                // TODO - add ability to toggle bookmarks i.e., if bookmarked, user should have
                // the option to unbookmark
                if (CollectionUtils.isEmpty(objects)) {
                    mBookmarkImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            ParseUserUtil.bookmarkSelfieSpot(loggedInUser, mSelfieSpot, new SaveCallback() {
                                @Override
                                public void done(final ParseException e) {
                                    if (e != null) {
                                        Toast.makeText(TempDetailSelfieSpotActivity.this, "Unable to add bookmark", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Unable to bookmark: " + mSelfieSpot.getObjectId(), e);
                                        return;
                                    }
                                    setBookmarkIcon(true);
                                    mBookmarkImageView.setOnClickListener(null);

                                    // fire service to update bookmarks geofences
                                    TempDetailSelfieSpotActivity.this.startService(GoogleApiClientBootstrapService.createIntent(TempDetailSelfieSpotActivity.this));
                                }
                            });
                        }
                    });
                }
            }
        });

        ParseUserUtil.isLiked(loggedInUser, mSelfieSpot, new FindCallback<SelfieSpot>() {
            @Override
            public void done(final List<SelfieSpot> objects, final ParseException e) {
                if (e != null) {
                    Toast.makeText(TempDetailSelfieSpotActivity.this, "Unable to determine if liked", Toast.LENGTH_SHORT).show();
                    return;
                }

                setLikeIcon(! CollectionUtils.isEmpty(objects), mSelfieSpot.getLikesCount());

                // for the time being only enable the listener if not bookmarked
                // TODO - add ability to toggle likes i.e., if likes, user should have
                // the option to unlikes
                if (CollectionUtils.isEmpty(objects)) {
                    mLikeImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            ParseUserUtil.likeSelfieSpot(loggedInUser, mSelfieSpot, new SaveCallback() {
                                @Override
                                public void done(final ParseException e) {
                                    if (e != null) {
                                        Toast.makeText(TempDetailSelfieSpotActivity.this, "Unable to like", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Unable to like: " + mSelfieSpot.getObjectId(), e);
                                        return;
                                    }
                                    setLikeIcon(true, mSelfieSpot.getLikesCount());
                                    mLikeImageView.setOnClickListener(null);
                                }
                            });
                        }
                    });
                }
            }
        });

        final LocationMapFragment mLocationMapFragment = LocationMapFragment.createInstance(mSelfieSpot.getPosition());
        mLocationMapFragment.setEnableUiGestureSettings(false);
        mLocationMapFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final LatLng location = mSelfieSpot.getPosition();
                final AlertLocationMapFragment mapFragment =
                        AlertLocationMapFragment.createInstance(location);
                mapFragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_FullScreen);
                mapFragment.show(getSupportFragmentManager(), "map");
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_map_container, mLocationMapFragment).commit();

        if (! CollectionUtils.isEmpty(mSelfieSpot.getTags())) {
            mTagsTextView.setText(getTagsText(mSelfieSpot.getTags()));
            mTagsTextView.setVisibility(View.VISIBLE);
            mLikesDivider.setVisibility(View.VISIBLE);
        }

        determineDeleteMenuVisiblity();
    }

    private void determineDeleteMenuVisiblity() {
        final ParseUser loggedInUser = ParseUser.getCurrentUser();

        if (mDeleteItem == null || mSelfieSpot == null) {
            return;
        }

        if (loggedInUser.getObjectId().equals(mSelfieSpot.getUser().getObjectId())) {
            mDeleteItem.setVisible(true);
        }
    }

    private void setBookmarkIcon(final boolean bookmarked) {
        if (bookmarked) {
            mBookmarkImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_active));
        } else {
            mBookmarkImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark));
        }
    }

    private void setLikeIcon(final boolean liked, final int count) {
        if (liked) {
            mLikeImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up_active));
        } else {
            mLikeImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up));
        }
        mLikesTextView.setText(ViewUtils.getSpannedText(this, getString(R.string.text_likes), count));
    }

    private String getTagsText(final Collection<String> tags) {
        final List<String> tagsWithHash = new ArrayList<>();
        for (final String tag : tags) {
            tagsWithHash.add("#" + tag);
        }

        return TextUtils.join(" ", tagsWithHash);
    }

    private void showBusy() {
        mProgressFrameLayout.setVisibility(View.VISIBLE);
    }

    private void hideBusy() {
        mProgressFrameLayout.setVisibility(View.GONE);
    }
}
