package com.buzzfeed.dfmndemo.exampleplayer;

import com.buzzfeed.dfmndemo.exampleplayer.model.SampleVideo;
import com.google.android.exoplayer.util.Util;

public class Config {

    // Player Configuration

    /**
     * A minimum duration of data that must be buffered for playback to start
     * or resume following a user action such as a seek.
     */
    public static final int MIN_BUFFER_TIME_MS = 1000;

    /**
     * A minimum duration of data that must be buffered for playback to resume
     * after a player invoked rebuffer (i.e. a rebuffer that occurs due to buffer depletion, and
     * not due to a user action such as starting playback or seeking)
     */
    public static final int MIN_RE_BUFFER_TIME_MS = 5000;

    // Sample Videos

    public static final SampleVideo SAMPLE_VIDEO_1 = new SampleVideo(
            "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/"
                    + "bipbop_16x9_variant.m3u8",
            Util.TYPE_HLS);

    public static final SampleVideo SAMPLE_VIDEO_2 = new SampleVideo(
            "http://content.jwplatform.com/manifests/vM7nH0Kl.m3u8",
            Util.TYPE_HLS);

    public static final SampleVideo SAMPLE_VIDEO_3 = new SampleVideo(
            "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/"
                    + "bipbop_4x3_variant.m3u8",
            Util.TYPE_HLS);
}
