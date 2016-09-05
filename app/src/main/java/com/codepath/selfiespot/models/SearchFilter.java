package com.codepath.selfiespot.models;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SearchFilter implements Serializable {
    private static final long serialVersionUID = 1L;

    private Set<String> mTags = new HashSet<>();
    private boolean mHideZeroLikes;

    public Set<String> getTags() {
        return mTags;
    }

    public void setTags(final Set<String> tags) {
        mTags = tags;
    }

    public void addTag(final String tag) {
        mTags.add(tag);
    }

    public void removeTag(final String tag) {
        mTags.remove(tag);
    }

    public boolean isHideZeroLikes() {
        return mHideZeroLikes;
    }

    public void setHideZeroLikes(final boolean hideZeroLikes) {
        mHideZeroLikes = hideZeroLikes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Hide Zero Likes: ");
        sb.append(mHideZeroLikes);
        sb.append("; Tags: ");
        sb.append(Arrays.toString(mTags.toArray()));
        return sb.toString();
    }
}
