package com.codepath.selfiespot.models;

import android.util.Log;

import com.parse.ParseException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class SelfieSpotProxy implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String TAG = SelfieSpotProxy.class.getSimpleName();

    private String mObjectId;
    private String mName;
    private String mUserName;
    private String mUserObjectId;
    private int mLikes;
    private ArrayList<String> mTags;
    private Date mCreatedAt;
    private String mImageUrl;
    private int mImageWidth;
    private int mImageHeight;

    public static SelfieSpotProxy createInstance(final SelfieSpot selfieSpot) {
        final SelfieSpotProxy proxy = new SelfieSpotProxy();
        proxy.setObjectId(selfieSpot.getObjectId());
        proxy.setName(selfieSpot.getName());

        try {
            proxy.setUserName(selfieSpot.getUser().fetchIfNeeded().getUsername());
            proxy.setUserObjectId(selfieSpot.getUser().getObjectId());
        } catch (ParseException e) {
            Log.e(TAG, "Unable to retrieve user", e);
        }

        proxy.setImageUrl(selfieSpot.getMediaFile().getUrl());
        proxy.setImageHeight(selfieSpot.getMediaHeight());
        proxy.setImageWidth(selfieSpot.getMediaWidth());

        proxy.setCreatedAt(selfieSpot.getCreatedAt());
        proxy.setLikes(selfieSpot.getLikesCount());
        proxy.setTags(new ArrayList<String>(selfieSpot.getTags()));

        return proxy;
    }

    public String getObjectId() {
        return mObjectId;
    }

    public void setObjectId(String objectId) {
        mObjectId = objectId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getUserObjectId() {
        return mUserObjectId;
    }

    public void setUserObjectId(String userObjectId) {
        mUserObjectId = userObjectId;
    }

    public int getLikes() {
        return mLikes;
    }

    public void setLikes(int likes) {
        mLikes = likes;
    }

    public ArrayList<String> getTags() {
        return mTags;
    }

    public void setTags(ArrayList<String> tags) {
        mTags = tags;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(Date createdAt) {
        mCreatedAt = createdAt;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public int getImageWidth() {
        return mImageWidth;
    }

    public void setImageWidth(int imageWidth) {
        mImageWidth = imageWidth;
    }

    public int getImageHeight() {
        return mImageHeight;
    }

    public void setImageHeight(int imageHeight) {
        mImageHeight = imageHeight;
    }
}
