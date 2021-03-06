package com.codepath.selfiespot.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.codepath.selfiespot.R;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.util.ViewUtils;
import com.codepath.selfiespot.views.DynamicHeightImageView;
import com.codepath.selfiespot.views.adapters.SelfieSpotItemCallback;
import com.parse.ParseException;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class SelfieSpotViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = SelfieSpotViewHolder.class.getSimpleName();

    private static final int ROUND_TRANSFORMATION_RADIUS = 10;
    private static final int ROUND_TRANSFORMATION_MARGIN = 0;

    final SelfieSpotItemCallback mCallback;
    private SelfieSpot mSelfieSpot;

    @BindView(R.id.iv_image)
    public DynamicHeightImageView mImageView;

    @BindView(R.id.tv_name)
    public TextView mNameTextView;

    @BindView(R.id.tv_likes)
    public TextView mLikesTextView;

    @BindView(R.id.tv_author)
    public TextView mAuthorTextView;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            mCallback.onSelfieSpotSelected(mSelfieSpot, SelfieSpotViewHolder.this);
        }
    };

    public SelfieSpotViewHolder(final View itemView,
                                final SelfieSpotItemCallback callback) {
        super(itemView);
        itemView.setOnClickListener(mOnClickListener);
        mCallback = callback;
        ButterKnife.bind(this, itemView);
    }

    public void bind(final SelfieSpot selfieSpot) {
        mSelfieSpot = selfieSpot;

        mNameTextView.setText(selfieSpot.getName());
        mLikesTextView.setText(ViewUtils.getSpannedText(itemView.getContext(),
                itemView.getContext().getResources().getString(R.string.text_likes), selfieSpot.getLikesCount()));
        try {
            mAuthorTextView.setText(String.format(itemView.getContext().getResources().getString(R.string.text_by),
                    selfieSpot.getUser().fetchIfNeeded().getUsername()));
        } catch (final ParseException e) {
            Log.e(TAG, "Unable to retrieve username", e);
        }

        mImageView.setHeightRatio((float) selfieSpot.getMediaHeight() / (float) selfieSpot.getMediaWidth());

        Glide.clear(mImageView);
        Glide.with(itemView.getContext())
                .load(selfieSpot.getMediaFile().getUrl())
                .bitmapTransform(new RoundedCornersTransformation(itemView.getContext(), ROUND_TRANSFORMATION_RADIUS, ROUND_TRANSFORMATION_MARGIN))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.ic_progress_indeterminate)
                .error(R.drawable.ic_error)
                .into(mImageView);
    }
}
