package com.codepath.selfiespot.di.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.codepath.selfiespot.SelfieSpotApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
    private final SelfieSpotApplication mApplication;

    public ApplicationModule(final SelfieSpotApplication application) {
        mApplication = application;
    }

    @Singleton
    @NonNull
    @Provides
    Context provideApplicationContext() {
        return mApplication;
    }

    @Singleton
    @NonNull
    @Provides
    SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mApplication);
    }
}
