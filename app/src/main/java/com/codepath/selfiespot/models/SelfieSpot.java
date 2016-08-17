package com.codepath.selfiespot.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("SelfieSpot")
public class SelfieSpot extends ParseObject {
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_DESC = "desc";

    // empty constructor required
    public SelfieSpot() {
        // no-op
    }

    public String getName() {
        return getString(PROPERTY_NAME);
    }

    public void setName(final String value) {
        put(PROPERTY_NAME, value);
    }

    public String getDescription() {
        return getString(PROPERTY_DESC);
    }

    public void setDescription(final String description) {
        put(PROPERTY_DESC, description);
    }
}
