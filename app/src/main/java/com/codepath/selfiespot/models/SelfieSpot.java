package com.codepath.selfiespot.models;

import com.codepath.selfiespot.util.CollectionUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.Collection;

/*
{
    "results": [
        {
            "objectId": "bTaejW3RKS",
            "name": "Name17",
            "loc": {
                "__type": "GeoPoint",
                "latitude": 28.3481967,
                "longitude": -81.5140857
            },
            "user": {
                "__type": "Pointer",
                "className": "_User",
                "objectId": "dGM5nCf9aB"
            },
            "desc": "",
            "createdAt": "2016-08-21T19:36:47.718Z",
            "updatedAt": "2016-08-22T22:11:57.955Z",
            "ACL": {
                "dGM5nCf9aB": {
                    "read": true,
                    "write": true
                }
            }
        },
      }

  */

@ParseClassName("SelfieSpot")
public class SelfieSpot extends ParseObject implements ClusterItem {
    public static final int DEFAULT_LIMIT = 100;

    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_DESC = "desc";
    private static final String PROPERTY_USER = "user";
    private static final String PROPERTY_LOCATION = "loc";
    private static final String PROPERTY_PICTURE = "pic";
    private static final String PROPERTY_WIDTH = "w";
    private static final String PROPERTY_HEIGHT = "h";
    private static final String PROPERTY_REVIEWS_COUNT = "reviews_count";
    private static final String PROPERTY_REVIEW_STARS_COUNT = "review_stars_count";
    private static final String PROPERTY_TAGS = "tags";
    private static final String PROPERTY_LIKES = "likes";

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

    public int getPropertyReviewsCount() {
        return getInt(PROPERTY_REVIEWS_COUNT);
    }

    public void setPropertyReviewsCount(final int propertyReviewsCount) {
        put(PROPERTY_REVIEWS_COUNT, propertyReviewsCount);
    }

    public int getPropertyReviewStarsCount() {
        return getInt(PROPERTY_REVIEW_STARS_COUNT);
    }

    public void setPropertyReviewStarsCount(final int propertyReviewStarsCount) {
        put(PROPERTY_REVIEW_STARS_COUNT, propertyReviewStarsCount);
    }

    public static ParseQuery<SelfieSpot> getQuery() {
        return ParseQuery.getQuery(SelfieSpot.class);
    }

    public ParseFile getMediaFile() {
        return getParseFile(PROPERTY_PICTURE);
    }

    public void setMediaFile(final ParseFile mediaFile) {
        put(PROPERTY_PICTURE, mediaFile);
    }

    public int getMediaWidth() {
        return getInt(PROPERTY_WIDTH);
    }

    public void setMediaWidth(final int width) {
        put(PROPERTY_WIDTH, width);
    }

    public int getMediaHeight() {
        return getInt(PROPERTY_HEIGHT);
    }

    public void setMediaHeight(final int height) {
        put(PROPERTY_HEIGHT, height);
    }

    public Collection<String> getTags() {
        final Collection<String> tags = getList(PROPERTY_TAGS);
        return tags;
    }

    public void setTags(final Collection<String> tags) {
        remove(PROPERTY_TAGS);
        addAllUnique(PROPERTY_TAGS, tags);
    }

    public void removeTag(final String tag) {
        removeAll(PROPERTY_TAGS, Arrays.asList(tag));
    }

    public int getLikesCount() {
        return getInt(PROPERTY_LIKES);
    }

    public void like() {
        increment(PROPERTY_LIKES);
    }

    @Override
    public LatLng getPosition() {
        final ParseGeoPoint location = getLocation();
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static ParseQuery<SelfieSpot> getWhereWithinGeoBoxQuery(final ParseGeoPoint sw,
                                                                   final ParseGeoPoint ne,
                                                                   final SearchFilter searchFilter) {
        final ParseQuery<SelfieSpot> query = getQuery();
        query.whereWithinGeoBox(PROPERTY_LOCATION, sw, ne);

        if (searchFilter != null) {
            if (searchFilter.isHideZeroLikes()) {
                query.whereGreaterThan(PROPERTY_LIKES, 0);
            }

            if (! CollectionUtils.isEmpty(searchFilter.getTags())) {
                query.whereContainedIn(PROPERTY_TAGS, searchFilter.getTags());
            }
        }
        query.setLimit(DEFAULT_LIMIT);
        return query;
    }

    public static ParseQuery<SelfieSpot> getMySelfieSpots(final ParseUser parseUser) {
        final ParseQuery<SelfieSpot> query = SelfieSpot.getQuery();
        query.whereEqualTo(PROPERTY_USER, parseUser);
        return query;
    }
}
