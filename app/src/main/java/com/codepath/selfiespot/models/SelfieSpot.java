package com.codepath.selfiespot.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("SelfieSpot")
public class SelfieSpot extends ParseObject {
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_DESC = "desc";
    private static final String PROPERTY_USER = "user";
    private static final String PROPERTY_LOCATION = "loc";


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

    public ParseUser getUser() {
        return getParseUser(PROPERTY_USER);
    }

    public void setUser(final ParseUser value) {
        put(PROPERTY_USER, value);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(PROPERTY_LOCATION);
    }

    public void setLocation(final ParseGeoPoint value) {
        put(PROPERTY_LOCATION, value);
    }

    public static ParseQuery<SelfieSpot> getQuery() {
        return ParseQuery.getQuery(SelfieSpot.class);
    }
}
