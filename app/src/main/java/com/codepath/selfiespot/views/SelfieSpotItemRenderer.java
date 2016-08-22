package com.codepath.selfiespot.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.models.SelfieSpot;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelfieSpotItemRenderer extends DefaultClusterRenderer<SelfieSpot> {
    private final IconGenerator mIconGenerator;

    @BindView(R.id.tv_rating)
    TextView mRatingTextView;

    public SelfieSpotItemRenderer(final Context context, final GoogleMap map,
                                  final ClusterManager<SelfieSpot> clusterManager) {
        super(context, map, clusterManager);

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View selfieView = inflater.inflate(R.layout.item_render_selfie, null);

        ButterKnife.bind(this, selfieView);

        mIconGenerator = new IconGenerator(context);
        mIconGenerator.setContentView(selfieView);
    }

    @Override
    protected void onBeforeClusterItemRendered(final SelfieSpot selfieSpot, final MarkerOptions markerOptions) {
        float rating = 0;

        if (selfieSpot.getPropertyReviewsCount() > 0) {
            rating = (float) selfieSpot.getPropertyReviewStarsCount() / (float) selfieSpot.getPropertyReviewsCount();
        }

        mRatingTextView.setText(Float.toString(rating));
        final Bitmap icon = mIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(selfieSpot.getName());
    }

    @Override
    protected boolean shouldRenderAsCluster(final Cluster cluster) {
        return cluster.getSize() > 1;
    }
}
