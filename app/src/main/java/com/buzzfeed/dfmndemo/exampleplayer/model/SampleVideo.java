package com.buzzfeed.dfmndemo.exampleplayer.model;

/**
 * Simple video model
 */
public class SampleVideo {

    private final String mContentUri;
    private final int mContentType;

    public SampleVideo(String contentUri, int contentType) {
        mContentUri = contentUri;
        mContentType = contentType;
    }

    public String getContentUri() {
        return mContentUri;
    }

    public int getContentType() {
        return mContentType;
    }
}
