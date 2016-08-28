package com.codepath.selfiespot.views.adapters;

import com.codepath.selfiespot.models.SelfieSpot;

/**
 * Interface to be implemented to be notified about actions on selfieSpot items.    *

 */

public interface SelfieSpotItemCallback {
    /**
     *  * @param selfieSpot
     */

    void onSelfieSpotSelected(SelfieSpot selfieSpot);
}
