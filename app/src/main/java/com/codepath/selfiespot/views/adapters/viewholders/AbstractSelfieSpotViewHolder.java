package com.codepath.selfiespot.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.views.adapters.SelfieSpotItemCallback;


public abstract class AbstractSelfieSpotViewHolder extends RecyclerView.ViewHolder {
    protected SelfieSpot mSelfieSpot;
    protected final SelfieSpotItemCallback mSelfieSpotCallback;


    public AbstractSelfieSpotViewHolder(View itemView, SelfieSpotItemCallback mSelfieSpotCallback) {
        super(itemView);
        this.mSelfieSpotCallback = mSelfieSpotCallback;
    }
}
