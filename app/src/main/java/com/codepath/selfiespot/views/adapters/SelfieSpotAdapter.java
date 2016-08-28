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
    private Context mContext;


    public SelfieSpotAdapter(Context mContext, List<SelfieSpot> mSelfieSpots) {
        this.mContext = mContext;
        this.mSelfieSpots = mSelfieSpots;
    }

    private Context getContext(){
        return mContext;
    }

    @Override
    public SelfieSpotAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View  selfiespotview = inflater.inflate(R.layout.item_selfiespot, parent, false);
        ViewHolder viewHolder = new ViewHolder(selfiespotview);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SelfieSpot  selfieSpot = mSelfieSpots.get(position);

        TextView tvSelfieSpotName = holder.tvSelfieSpotName;
        tvSelfieSpotName.setText(selfieSpot.getName());

        TextView tvSelfieSpotDesc = holder.tvSelfieSpotDesc;
        tvSelfieSpotDesc.setText(selfieSpot.getDescription());
    }

    @Override
    public int getItemCount() {
        return mSelfieSpots.size();
    }


    public void addSelfieSpots(List<SelfieSpot> selfieSpots){
        mSelfieSpots.addAll(selfieSpots);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivSelfieSpotImage)
        public ImageView ivSelfieSpotImage;

        @BindView(R.id.tvSelfieSpotName)
        public TextView tvSelfieSpotName;

        @BindView(R.id.tvSelfieSpotDesc)
        public TextView tvSelfieSpotDesc;

        @BindView(R.id.rbSelfieSpotRatingBar)
        public RatingBar rbSelfieSpotRatingBar;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


    }
}
