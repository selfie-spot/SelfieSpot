package com.codepath.selfiespot.util;

import com.codepath.selfiespot.models.SelfieSpot;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class ParseUserUtil {
    private static final String PROPERTY_RELATION_BOOKMARKS = "bookmarks";

    public static void bookmarkSelfieSpot(final ParseUser user, final SelfieSpot selfieSpot) {
        final ParseRelation<SelfieSpot> bookmarks = user.getRelation(PROPERTY_RELATION_BOOKMARKS);
        bookmarks.add(selfieSpot);
        user.saveInBackground();
    }
}
