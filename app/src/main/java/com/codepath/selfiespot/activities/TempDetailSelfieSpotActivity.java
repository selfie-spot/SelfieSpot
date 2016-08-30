package com.codepath.selfiespot.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.fragments.AlertLocationMapFragment;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.util.CollectionUtils;
import com.codepath.selfiespot.util.DateUtils;
import com.codepath.selfiespot.util.ParseUserUtil;
import com.codepath.selfiespot.util.ViewUtils;
import com.codepath.selfiespot.views.DynamicHeightImageView;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class TempDetailSelfieSpotActivity extends AppCompatActivity {
    private static final String TAG = TempDetailSelfieSpotActivity.class.getSimpleName();

    private static final String EXTRA_SELFIE_SPOT_ID = TempDetailSelfieSpotActivity.class.getSimpleName() + ":SELFIE_SPOT_ID";

    private static final int ROUND_TRANSFORMATION_RADIUS = 20;
    private static final int ROUND_TRANSFORMATION_MARGIN = 5;

    @BindView(R.id.iv_image)
    DynamicHeightImageView mImageView;

    @BindView(R.id.tv_name)
    TextView mNameTextView;

    @BindView(R.id.tv_author)
    TextView mAuthorTextView;

    @BindView(R.id.tv_time)
    TextView mTimeTextView;

    @BindView(R.id.tv_likes)
    TextView mLikesTextView;

    @BindView(R.id.fl_progress_holder)
    FrameLayout mProgressFrameLayout;

    @BindView(R.id.iv_bookmark_action)
    ImageView mBookmarkImageView;

    @BindView(R.id.iv_like_action)
    ImageView mLikeImageView;

    @BindView(R.id.iv_map_action)
    ImageView mMapImageView;

    @BindView(R.id.iv_share_action)
    ImageView mShareImageView;

    private SelfieSpot mSelfieSpot;

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

        final String selfieSpotId = getIntent().getStringExtra(EXTRA_SELFIE_SPOT_ID);
        showBusy();
        // retrieve SelfieSpot before initializing views
        final ParseQuery<SelfieSpot> query = SelfieSpot.getQuery();
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

    private void initViews() {
        mNameTextView.setText(mSelfieSpot.getName());

        try {
            mAuthorTextView.setText(String.format(getResources().getString(R.string.text_by), mSelfieSpot.getUser().fetchIfNeeded().getUsername()));
        } catch (final ParseException e) {
            Log.w(TAG, "Unable to retrieve username", e);
        }

        mImageView.setHeightRatio(
                (float) mSelfieSpot.getMediaHeight() / (float) mSelfieSpot.getMediaWidth());
        Picasso.with(this)
                .load(mSelfieSpot.getMediaFile().getUrl())
                .fit()
                .centerInside()
                .placeholder(R.drawable.ic_progress_indeterminate)
                .error(R.drawable.ic_error)
                .transform(new RoundedCornersTransformation(ROUND_TRANSFORMATION_RADIUS, ROUND_TRANSFORMATION_MARGIN))
                .into(mImageView);

        mTimeTextView.setText(DateUtils.getDetailPageTime(mSelfieSpot.getCreatedAt()));
        mLikesTextView.setText(ViewUtils.getSpannedText(this, getString(R.string.text_likes),
                mSelfieSpot.getLikesCount()));

        ParseUserUtil.isBookmarked(ParseUser.getCurrentUser(), mSelfieSpot, new FindCallback<SelfieSpot>() {
            @Override
            public void done(final List<SelfieSpot> objects, final ParseException e) {
                if (e != null) {
                    Toast.makeText(TempDetailSelfieSpotActivity.this, "Unable to determine if bookmarked", Toast.LENGTH_SHORT).show();
                    return;
                }

                setBookmarkIcon(! CollectionUtils.isEmpty(objects));

                // for the time being only enable the listener if not bookmarked
                // TODO - add ability to toggle bookmarks i.e., if bookmarked, user should have
                // the option to unbookmark
                if (CollectionUtils.isEmpty(objects)) {
                    mBookmarkImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            ParseUserUtil.bookmarkSelfieSpot(ParseUser.getCurrentUser(), mSelfieSpot, new SaveCallback() {
                                @Override
                                public void done(final ParseException e) {
                                    if (e != null) {
                                        Toast.makeText(TempDetailSelfieSpotActivity.this, "Unable to add bookmark", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Unable to bookmark: " + mSelfieSpot.getObjectId(), e);
                                        return;
                                    }
                                    setBookmarkIcon(true);
                                    mBookmarkImageView.setOnClickListener(null);
                                }
                            });
                        }
                    });
                }
            }
        });

        ParseUserUtil.isLiked(ParseUser.getCurrentUser(), mSelfieSpot, new FindCallback<SelfieSpot>() {
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
                            ParseUserUtil.likeSelfieSpot(ParseUser.getCurrentUser(), mSelfieSpot, new SaveCallback() {
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

        mMapImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final ParseGeoPoint location = mSelfieSpot.getLocation();
                final AlertLocationMapFragment mapFragment =
                        AlertLocationMapFragment.createInstance(new LatLng(location.getLatitude(), location.getLongitude()));
                mapFragment.show(getSupportFragmentManager(), "map");
            }
        });
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

    private void showBusy() {
        mProgressFrameLayout.setVisibility(View.VISIBLE);
    }

    private void hideBusy() {
        mProgressFrameLayout.setVisibility(View.GONE);
    }
}
