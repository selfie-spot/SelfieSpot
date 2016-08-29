package com.codepath.selfiespot.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.models.SelfieSpot;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SelfieSpotAdapter extends RecyclerView.Adapter<SelfieSpotAdapter.ViewHolder> {
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

        @BindView(R.id.ivSelfieSpotImage)
        ImageView ivSelfieSpotImage;

        @BindView(R.id.tvSelfieSpotName)
        TextView tvSelfieSpotName;

        @BindView(R.id.tvSelfieSpotDesc)
        TextView tvSelfieSpotDesc;

        @BindView(R.id.rbSelfieSpotRatingBar)
        RatingBar rbSelfieSpotRatingBar;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final SelfieSpot selfieSpot) {
            tvSelfieSpotName.setText(selfieSpot.getName());
            tvSelfieSpotDesc.setText(selfieSpot.getDescription());
        }
    }
}
