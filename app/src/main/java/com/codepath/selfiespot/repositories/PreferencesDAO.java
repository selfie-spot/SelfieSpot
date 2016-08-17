package com.codepath.selfiespot.repositories;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PreferencesDAO {
    private static final String KEY_USER_ID = "SELFIE_SPOT_PREF_USER_ID";
    private static final String KEY_USER_NAME = "SELFIE_SPOT_PREF_USER_NAME";

    private final SharedPreferences mSharedPreferences;

    @Inject
    public PreferencesDAO(final SharedPreferences sharedPreferences) {
        this.mSharedPreferences = sharedPreferences;
    }

    public void setUserId(final long userId) {
        mSharedPreferences
                .edit()
                .putLong(KEY_USER_ID, userId)
                .commit();
    }

    public Long getUserId() {
        return mSharedPreferences.getLong(KEY_USER_ID, -1);
    }

    public void setUserName(final String userName) {
        mSharedPreferences
                .edit()
                .putString(KEY_USER_NAME, userName)
                .commit();
    }

    public String getUserName() {
        return mSharedPreferences.getString(KEY_USER_NAME, "");
    }

    // clear all preferences
    public void clearAll() {
        mSharedPreferences.edit().clear().commit();
    }
}
