package com.codepath.selfiespot.views.adapters;

import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.views.adapters.viewholders.SelfieSpotViewHolder;

/**
 * Interface to be implemented to be notified about actions on selfieSpot items.
 *
 */

public interface SelfieSpotItemCallback {
    /**
     *  * @param selfieSpot
     */

    void onSelfieSpotSelected(SelfieSpot selfieSpot, SelfieSpotViewHolder viewHolder);
}
