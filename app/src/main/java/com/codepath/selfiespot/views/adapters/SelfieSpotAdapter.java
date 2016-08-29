package com.codepath.selfiespot.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.util.ViewUtils;
import com.parse.ParseException;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SelfieSpotAdapter extends RecyclerView.Adapter<SelfieSpotAdapter.ViewHolder> {
    private static final String TAG = SelfieSpotAdapter.class.getSimpleName();

    private List<SelfieSpot> mSelfieSpots;

    public SelfieSpotAdapter(final List<SelfieSpot> mSelfieSpots) {
        this.mSelfieSpots = mSelfieSpots;
    }

    @Override
    public SelfieSpotAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);

        final View selfiespotview = inflater.inflate(R.layout.item_selfiespot, parent, false);
        final ViewHolder viewHolder = new ViewHolder(selfiespotview);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final SelfieSpot selfieSpot = mSelfieSpots.get(position);
        holder.bind(selfieSpot);
    }

    @Override
    public int getItemCount() {
        return mSelfieSpots.size();
    }

    public void addSelfieSpots(final List<SelfieSpot> selfieSpots){
        mSelfieSpots.addAll(selfieSpots);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivImage)
        ImageView mImageView;

        @BindView(R.id.tvName)
        TextView mNameTextView;

        @BindView(R.id.tvLikes)
        TextView mLikesTextView;

        @BindView(R.id.tvAuthor)
        TextView mAuthorTextView;

        public ViewHolder(final View itemView) {
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

            Picasso.with(itemView.getContext())
                    .load(selfieSpot.getMediaFile().getUrl())
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.ic_progress_indeterminate)
                    .error(R.drawable.ic_error)
                    .into(mImageView);
        }
    }
}
