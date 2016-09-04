package com.codepath.selfiespot.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.views.adapters.viewholders.SelfieSpotViewHolder;

import java.util.List;


public class SelfieSpotAdapter extends RecyclerView.Adapter<SelfieSpotViewHolder> {
    private List<SelfieSpot> mSelfieSpots;
    private final SelfieSpotItemCallback mItemCallback;

    public SelfieSpotAdapter(final List<SelfieSpot> mSelfieSpots, final SelfieSpotItemCallback itemCallback) {
        this.mSelfieSpots = mSelfieSpots;
        this.mItemCallback = itemCallback;
    }

    @Override
    public SelfieSpotViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);

        final View selfiespotview = inflater.inflate(R.layout.item_selfiespot, parent, false);
        final SelfieSpotViewHolder viewHolder = new SelfieSpotViewHolder(selfiespotview, mItemCallback);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final SelfieSpotViewHolder holder, final int position) {
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

    public void clearAll() {
        mSelfieSpots.clear();
        notifyDataSetChanged();
    }
}