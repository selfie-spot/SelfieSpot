package com.codepath.selfiespot.config;

import android.util.Log;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;

import java.io.File;

public class SelfieSpotDiskLruCacheWrapper extends DiskLruCacheWrapper {
    private static final String TAG = SelfieSpotDiskLruCacheWrapper.class.getSimpleName();
    private static DiskLruCacheWrapper wrapper = null;

    protected SelfieSpotDiskLruCacheWrapper(final File directory, final int maxSize) {
        super(directory, maxSize);
    }

    public static synchronized DiskCache get(File directory, int maxSize) {
        // TODO calling twice with different arguments makes it return the cache for the same directory, it's public!
        if (wrapper == null) {
            wrapper = new SelfieSpotDiskLruCacheWrapper(directory, maxSize);
        }
        return wrapper;
    }

    @Override
    public File get(final Key key) {
        final File file = super.get(key);
        if (file == null) {
            Log.d(TAG, "file NOT found for key: " + key);
        } else {
            Log.d(TAG, "file found: " + file.getAbsolutePath() + "; key: " + key);
        }
        return file;
    }

    @Override
    public void put(final Key key, final Writer writer) {
        super.put(key, writer);
        Log.d(TAG, "trying to put key: " + key);
    }

    @Override
    public void delete(final Key key) {
        super.delete(key);
        Log.d(TAG, "deleting key: " + key);
    }
}
