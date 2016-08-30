package com.codepath.selfiespot.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.util.ViewUtils;
import com.codepath.selfiespot.views.DynamicHeightImageView;
import com.parse.ParseException;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class SelfieSpotViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = SelfieSpotViewHolder.class.getSimpleName();

    private static final int ROUND_TRANSFORMATION_RADIUS = 10;
    private static final int ROUND_TRANSFORMATION_MARGIN = 0;

    @BindView(R.id.iv_image)
    DynamicHeightImageView mImageView;

    @BindView(R.id.tv_name)
    TextView mNameTextView;

    @BindView(R.id.tv_likes)
    TextView mLikesTextView;

    @BindView(R.id.tv_author)
    TextView mAuthorTextView;

    public SelfieSpotViewHolder(final View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(final SelfieSpot selfieSpot) {
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

        Picasso.with(itemView.getContext())
                .load(selfieSpot.getMediaFile().getUrl())
                .fit()
                .centerInside()
                .transform(new RoundedCornersTransformation(ROUND_TRANSFORMATION_RADIUS, ROUND_TRANSFORMATION_MARGIN))
                .placeholder(R.drawable.ic_progress_indeterminate)
                .error(R.drawable.ic_error)
                .into(mImageView);
    }
}
