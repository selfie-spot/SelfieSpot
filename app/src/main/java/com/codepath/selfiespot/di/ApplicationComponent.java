package com.codepath.selfiespot.di;


import com.codepath.selfiespot.activities.SettingsActivity;
import com.codepath.selfiespot.di.modules.ApplicationModule;
import com.codepath.selfiespot.services.GeofenceTransitionsIntentService;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    void inject(GeofenceTransitionsIntentService geofenceTransitionsIntentService);

    void inject(SettingsActivity settingsActivity);
}
