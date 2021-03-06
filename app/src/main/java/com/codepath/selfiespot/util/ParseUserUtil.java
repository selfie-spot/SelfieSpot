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
    private static final String PROPERTY_PROFILE_PIC = "profilePic";
    private static final String PROPERTY_COVER_PIC = "coverPic";

    private static final String PROPERTY_OBJECT_ID = "objectId";
    private static final String CLASS_USER = "_User";

    public static ParseQuery<SelfieSpot> getBookmarksQuery(final ParseUser parseUser) {
        final ParseRelation<SelfieSpot> relation = parseUser.getRelation(PROPERTY_RELATION_BOOKMARKS);
        return relation.getQuery();
    }

    public static String getProfilePictureUrl(final ParseUser parseUser) {
        return parseUser.getString(PROPERTY_PROFILE_PIC);
    }

    public static void setProfilePictureUrl(final ParseUser parseUser, final String profilePicUrl) {
        parseUser.put(PROPERTY_PROFILE_PIC, profilePicUrl);
    }

    public static String getCoverPictureUrl(final ParseUser parseUser) {
        return parseUser.getString(PROPERTY_COVER_PIC);
    }

    public static void setCoverPictureUrl(final ParseUser parseUser, final String coverPicUrl) {
        parseUser.put(PROPERTY_COVER_PIC, coverPicUrl);
    }


    public static void isBookmarked(final ParseUser user, final SelfieSpot selfieSpot, final FindCallback<SelfieSpot> callback) {
        // relations are weird, require this type of "hack" to query relations
        final ParseQuery<SelfieSpot> query = ParseQuery.getQuery(CLASS_USER);

        query.whereEqualTo(PROPERTY_OBJECT_ID, user.getObjectId())
                .whereEqualTo(PROPERTY_RELATION_BOOKMARKS, selfieSpot);

        query.findInBackground(callback);
    }

    public static void isLiked(final ParseUser user, final SelfieSpot selfieSpot, final FindCallback<SelfieSpot> callback) {
        // relations are weird, require this type of "hack" to query relations
        final ParseQuery<SelfieSpot> query = ParseQuery.getQuery(CLASS_USER);

        query.whereEqualTo(PROPERTY_OBJECT_ID, user.getObjectId())
                .whereEqualTo(PROPERTY_RELATION_LIKES, selfieSpot);

        query.findInBackground(callback);
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

                if (!CollectionUtils.isEmpty(objects)) {
                    Log.w(TAG, "Already bookmarked: " + selfieSpot.getObjectId());
                    return;
                }

                final ParseRelation<SelfieSpot> bookmarks = user.getRelation(PROPERTY_RELATION_BOOKMARKS);
                bookmarks.add(selfieSpot);
                user.saveInBackground(callback);
            }
        });
    }

    public static void unbookmarkSelfieSpot(final ParseUser user, final SelfieSpot selfieSpot, final SaveCallback callback) {
        isBookmarked(user, selfieSpot, new FindCallback<SelfieSpot>() {
            @Override
            public void done(final List<SelfieSpot> objects, final ParseException e) {
                if (e != null) {
                    Log.w(TAG, "Unable to determine if the user bookmarked already");
                    callback.done(e);
                    return;
                }

                if (CollectionUtils.isEmpty(objects)) {
                    Log.w(TAG, "Not bookmarked: " + selfieSpot.getObjectId());
                    return;
                }

                final ParseRelation<SelfieSpot> bookmarks = user.getRelation(PROPERTY_RELATION_BOOKMARKS);
                bookmarks.remove(selfieSpot);
                user.saveInBackground(callback);
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

                if (!CollectionUtils.isEmpty(objects)) {
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
