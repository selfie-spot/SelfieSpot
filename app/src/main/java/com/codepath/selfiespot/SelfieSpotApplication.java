package com.codepath.selfiespot;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;
import com.parse.interceptors.ParseLogInterceptor;

public class SelfieSpotApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initParse();
        initDagger();
    }

    private void initDagger() {

    }

    private void initParse() {
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(BuildConfig.PARSE_ID)
//                .clientKey(BuildConfig.PARSE_KEY)
                .server(BuildConfig.PARSE_SERVER_URL)
                .addNetworkInterceptor(new ParseLogInterceptor())
                .build());

        ParseUser.enableAutomaticUser();
        final ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        // defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }

    private void initFacebook() {
        // TODO - update facebook API key
    }
}
