/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.buzzfeed.dfmndemo.exampleplayer.player.renderer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.os.Handler;

import com.buzzfeed.dfmndemo.exampleplayer.player.SampleVideoPlayer;
import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.hls.DefaultHlsTrackSelector;
import com.google.android.exoplayer.hls.HlsChunkSource;
import com.google.android.exoplayer.hls.HlsPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylistParser;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.hls.PtsTimestampAdjusterProvider;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.ManifestFetcher.ManifestCallback;

import java.io.IOException;

/**
 * A {@link RendererBuilder} for HLS.
 */
public class HlsRendererBuilder implements RendererBuilder {

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int MAIN_BUFFER_SEGMENTS = 256;

    private final Context mContext;
    private final String mUserAgent;
    private final String mUrl;

    private AsyncRendererBuilder mCurrentAsyncBuilder;

    public HlsRendererBuilder(Context context, String userAgent, String url) {
        mContext = context;
        mUserAgent = userAgent;
        mUrl = url;
    }

    @Override
    public void buildRenderers(SampleVideoPlayer player) {
        mCurrentAsyncBuilder = new AsyncRendererBuilder(mContext, mUserAgent, mUrl, player);
        mCurrentAsyncBuilder.init();
    }

    @Override
    public void cancel() {
        if (mCurrentAsyncBuilder != null) {
            mCurrentAsyncBuilder.cancel();
            mCurrentAsyncBuilder = null;
        }
    }

    private static final class AsyncRendererBuilder implements ManifestCallback<HlsPlaylist> {

        private final Context mContext;
        private final String mUserAgent;
        private final String mUrl;
        private final SampleVideoPlayer mPlayer;
        private final ManifestFetcher<HlsPlaylist> mPlaylistFetcher;

        private boolean mCanceled;

        public AsyncRendererBuilder(Context context, String userAgent,
                                    String url, SampleVideoPlayer player) {
            mContext = context;
            mUserAgent = userAgent;
            mUrl = url;
            mPlayer = player;
            HlsPlaylistParser parser = new HlsPlaylistParser();
            mPlaylistFetcher = new ManifestFetcher<>(url, new DefaultUriDataSource(
                    context, userAgent), parser);
        }

        public void init() {
            mPlaylistFetcher.singleLoad(mPlayer.getMainHandler().getLooper(), this);
        }

        public void cancel() {
            mCanceled = true;
        }

        @Override
        public void onSingleManifestError(IOException e) {
            if (mCanceled) {
                return;
            }

            mPlayer.onRenderersError(e);
        }

        @Override
        public void onSingleManifest(HlsPlaylist manifest) {
            if (mCanceled) {
                return;
            }

            Handler mainHandler = mPlayer.getMainHandler();

            LoadControl loadControl = new DefaultLoadControl(
                    new DefaultAllocator(BUFFER_SEGMENT_SIZE));
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            PtsTimestampAdjusterProvider timestampAdjusterProvider = new PtsTimestampAdjusterProvider();

            // Build the video / audio renders
            DataSource dataSource = new DefaultUriDataSource(mContext, bandwidthMeter, mUserAgent);
            HlsChunkSource chunkSource = new HlsChunkSource(true /* isMaster */, dataSource, mUrl,
                    manifest, DefaultHlsTrackSelector.newDefaultInstance(mContext), bandwidthMeter,
                    timestampAdjusterProvider, HlsChunkSource.ADAPTIVE_MODE_SPLICE);
            HlsSampleSource sampleSource = new HlsSampleSource(chunkSource, loadControl,
                    MAIN_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, mainHandler, null,
                    SampleVideoPlayer.TYPE_VIDEO);

            MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(mContext,
                    sampleSource, MediaCodecSelector.DEFAULT,
                    MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT,
                    5000, mainHandler, mPlayer, 50);
            MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(
                    sampleSource, MediaCodecSelector.DEFAULT, null, true,
                    mPlayer.getMainHandler(), null, AudioCapabilities.getCapabilities(mContext),
                    AudioManager.STREAM_MUSIC);

            TrackRenderer[] renderers = new TrackRenderer[SampleVideoPlayer.RENDERER_COUNT];
            renderers[SampleVideoPlayer.TYPE_VIDEO] = videoRenderer;
            renderers[SampleVideoPlayer.TYPE_AUDIO] = audioRenderer;
            mPlayer.onRenderers(renderers);
        }
    }
}
