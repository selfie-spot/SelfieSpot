package com.codepath.selfiespot.models;

import java.util.ArrayList;
import java.util.List;

public enum Tag {
    ADVENTUROUS, SCENIC, ELECTRIC, HISTORIC;

    private static String[] sAllTagsArray;

    static {
        final List<String> tags = new ArrayList<String>();
        for (final Tag tag : Tag.values()) {
            tags.add(tag.name());
        }
        sAllTagsArray = tags.toArray(new String[0]);
    }

    public static String[] getAllTagsAsStringArray() {
        return sAllTagsArray;
    }
}
