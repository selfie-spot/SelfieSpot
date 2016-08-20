package com.codepath.selfiespot.di;


import com.codepath.selfiespot.activities.MainActivity;
import com.codepath.selfiespot.di.modules.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    void inject(MainActivity activity);

}
