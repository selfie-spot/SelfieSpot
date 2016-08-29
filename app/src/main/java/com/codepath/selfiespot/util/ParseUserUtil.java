package com.codepath.selfiespot.util;

import android.util.Log;

import com.codepath.selfiespot.models.SelfieSpot;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Arrays;
import java.util.List;

public class ParseUserUtil {
    private static final String TAG = ParseUserUtil.class.getSimpleName();

    private static final String PROPERTY_RELATION_BOOKMARKS = "bookmarks";
    private static final String PROPERTY_RELATION_LIKES = "likes";

    public static void isBookmarked(final ParseUser user, final SelfieSpot selfieSpot, final FindCallback<SelfieSpot> callback) {
        final ParseRelation<SelfieSpot> bookmarks = user.getRelation(PROPERTY_RELATION_BOOKMARKS);
        final ParseQuery<SelfieSpot> alreadyBookmarkedQuery = bookmarks.getQuery().whereEqualTo(PROPERTY_RELATION_BOOKMARKS, selfieSpot);
        alreadyBookmarkedQuery.findInBackground(callback);
    }

    public static void isLiked(final ParseUser user, final SelfieSpot selfieSpot, final FindCallback<SelfieSpot> callback) {
        final ParseRelation<SelfieSpot> likes = user.getRelation(PROPERTY_RELATION_LIKES);
        final ParseQuery<SelfieSpot> alreadyLikedQuery = likes.getQuery().whereEqualTo(PROPERTY_RELATION_LIKES, selfieSpot);
        alreadyLikedQuery.findInBackground(callback);
    }

    public static void bookmarkSelfieSpot(final ParseUser user, final SelfieSpot selfieSpot, final SaveCallback callback) {
        isBookmarked(user, selfieSpot, new FindCallback<SelfieSpot>() {
            @Override
            public void done(final List<SelfieSpot> objects, final ParseException e) {
                if (e != null) {
                    Log.w(TAG, "Unable to determine if the user bookmarked already");
                    callback.done(e);
                    return;
                }

                if (! CollectionUtils.isEmpty(objects)) {
                    Log.w(TAG, "Already bookmarked: " + selfieSpot.getObjectId());
                    return;
                }

                final ParseRelation<SelfieSpot> bookmarks = user.getRelation(PROPERTY_RELATION_BOOKMARKS);
                bookmarks.add(selfieSpot);
                user.saveInBackground(callback);

                ParseUser.saveAllInBackground(Arrays.asList(selfieSpot, user), callback);
            }
        });
    }

    public static void likeSelfieSpot(final ParseUser user, final SelfieSpot selfieSpot, final SaveCallback callback) {
        isLiked(user, selfieSpot, new FindCallback<SelfieSpot>() {
            @Override
            public void done(final List<SelfieSpot> objects, final ParseException e) {
                if (e != null) {
                    Log.w(TAG, "Unable to determine if the user liked");
                    callback.done(e);
                    return;
                }

                if (! CollectionUtils.isEmpty(objects)) {
                    Log.w(TAG, "Already liked: " + selfieSpot.getObjectId());
                    return;
                }

                final ParseRelation<SelfieSpot> likes = user.getRelation(PROPERTY_RELATION_LIKES);
                likes.add(selfieSpot);
                selfieSpot.like();

                ParseUser.saveAllInBackground(Arrays.asList(selfieSpot, user), callback);
            }
        });
    }
}
