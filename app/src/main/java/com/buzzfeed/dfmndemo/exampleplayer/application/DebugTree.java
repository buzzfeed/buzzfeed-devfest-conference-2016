package com.buzzfeed.dfmndemo.exampleplayer.application;

import timber.log.Timber;

/**
 * Simple logging configuration
 */
class DebugTree extends Timber.DebugTree {

    @Override
    protected String createStackElementTag(StackTraceElement element) {
        return super.createStackElementTag(element) + "." + element.getMethodName();
    }
}
