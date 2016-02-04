package com.buzzfeed.dfmndemo.exampleplayer.application;

import android.app.Application;
import android.support.v4.BuildConfig;

import timber.log.Timber;

public class ExamplePlayerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        }
    }
}
