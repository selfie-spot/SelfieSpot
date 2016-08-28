package com.codepath.selfiespot.util;

import java.util.Collection;

public class CollectionUtils {

    public static boolean isEmpty(final Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }
}
