package com.codepath.selfiespot;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import com.codepath.selfiespot.di.ApplicationComponent;
import com.codepath.selfiespot.di.DaggerApplicationComponent;
import com.codepath.selfiespot.di.modules.ApplicationModule;
import com.codepath.selfiespot.models.SelfieSpot;
import com.facebook.stetho.Stetho;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.interceptors.ParseLogInterceptor;

public class SelfieSpotApplication extends Application {
    private ApplicationComponent mComponent;

    @Override
    protected void attachBaseContext(final Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initParse();
        initDagger();
        initStetho();
    }

    private void initDagger() {
        mComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    private void initParse() {
        // init classes
        ParseObject.registerSubclass(SelfieSpot.class);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(BuildConfig.PARSE_ID)
//                .clientKey(BuildConfig.PARSE_KEY)
                .server(BuildConfig.PARSE_SERVER_URL)
                .addNetworkInterceptor(new ParseLogInterceptor())
                .build());

//        ParseUser.enableAutomaticUser();
        final ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        // defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

        ParseFacebookUtils.initialize(getApplicationContext());
    }

    private void initStetho() {
        Stetho.initializeWithDefaults(this);
    }

    public ApplicationComponent getComponent() {
        return mComponent;
    }

    public static SelfieSpotApplication from(@NonNull final Context context) {
        return (SelfieSpotApplication) context.getApplicationContext();
    }
}
