package com.codepath.selfiespot.config;

import android.content.Context;

import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;

import java.io.File;

/**
 * Motivated by {@link com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory}.
 */
public class InternalCacheDiskCacheFactory implements DiskCache.Factory {
    private final DiskLruCacheFactory.CacheDirectoryGetter mCacheDirectoryGetter;

    public InternalCacheDiskCacheFactory(final Context context) {
        mCacheDirectoryGetter = new DiskLruCacheFactory.CacheDirectoryGetter() {
            @Override
            public File getCacheDirectory() {
                final File cacheDirectory = context.getCacheDir();
                return new File(cacheDirectory, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR);
            }
        };
    }

    @Override
    public DiskCache build() {
        final File cacheDir = mCacheDirectoryGetter.getCacheDirectory();

        if (cacheDir == null) {
            return null;
        }

        if (!cacheDir.mkdirs() && (!cacheDir.exists() || !cacheDir.isDirectory())) {
            return null;
        }

        return SelfieSpotDiskLruCacheWrapper.get(cacheDir, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
    }
}

