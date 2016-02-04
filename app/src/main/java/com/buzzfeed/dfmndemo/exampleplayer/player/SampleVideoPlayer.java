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
package com.buzzfeed.dfmndemo.exampleplayer.player;

import android.media.MediaCodec.CryptoException;
import android.os.Handler;
import android.view.Surface;

import com.buzzfeed.dfmndemo.exampleplayer.Config;
import com.buzzfeed.dfmndemo.exampleplayer.player.renderer.RendererBuilder;
import com.google.android.exoplayer.DummyTrackRenderer;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.util.PlayerControl;

import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.android.exoplayer.ExoPlayer.STATE_IDLE;
import static com.google.android.exoplayer.ExoPlayer.STATE_PREPARING;

/**
 * Adapted from Google's ExoPlayer sample and modified accordingly
 */
public class SampleVideoPlayer implements ExoPlayer.Listener,
        MediaCodecVideoTrackRenderer.EventListener {

    /**
     * A listener for core events.
     */
    public interface Listener {
        void onStateChanged(boolean playWhenReady, int playbackState);
        void onError(Exception e);
        void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                float pixelWidthHeightRatio);
    }

    private enum RendererBuildingState {
        IDLE, BUILDING, BUILT
    }

    public static final int RENDERER_COUNT = 2;
    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_AUDIO = 1;

    private final RendererBuilder mRendererBuilder;
    private final ExoPlayer mPlayer;
    private final PlayerControl mPlayerControl;
    private final Handler mMainHandler;
    private final CopyOnWriteArrayList<Listener> mListeners;

    private RendererBuildingState mRendererBuildingState;
    private int mLastReportedPlaybackState;
    private boolean mLastReportedPlayWhenReady;

    private Surface mSurface;
    private TrackRenderer mVideoRenderer;

    public SampleVideoPlayer(RendererBuilder rendererBuilder) {
        mRendererBuilder = rendererBuilder;
        mPlayer = ExoPlayer.Factory.newInstance(RENDERER_COUNT,
                Config.MIN_BUFFER_TIME_MS, Config.MIN_RE_BUFFER_TIME_MS);
        mPlayer.addListener(this);
        mPlayerControl = new PlayerControl(mPlayer);
        mMainHandler = new Handler();
        mListeners = new CopyOnWriteArrayList<>();
        mLastReportedPlaybackState = STATE_IDLE;
        mRendererBuildingState = RendererBuildingState.IDLE;
    }

    public PlayerControl getPlayerControl() {
        return mPlayerControl;
    }

    public void addListener(Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        mListeners.remove(listener);
    }

    public void setSurface(Surface surface) {
        mSurface = surface;
        pushSurface(false);
    }

    public void blockingClearSurface() {
        mSurface = null;
        pushSurface(true);
    }

    public void prepare() {
        if (mRendererBuildingState == RendererBuildingState.BUILT) {
            mPlayer.stop();
        }
        mRendererBuilder.cancel();
        mVideoRenderer = null;
        mRendererBuildingState = RendererBuildingState.BUILDING;
        maybeReportPlayerState();
        mRendererBuilder.buildRenderers(this);
    }

    /**
     * Invoked with the results from a {@link RendererBuilder}.
     *
     * @param renderers Renderers indexed by {@link SampleVideoPlayer} TYPE_* constants.
     *                  An individual element may be null if there do not exist tracks
     *                  of the corresponding type.
     */
    public void onRenderers(TrackRenderer[] renderers) {
        for (int i = 0; i < RENDERER_COUNT; i++) {
            if (renderers[i] == null) {
                // Convert a null renderer to a dummy renderer.
                renderers[i] = new DummyTrackRenderer();
            }
        }
        // Complete preparation.
        mVideoRenderer = renderers[TYPE_VIDEO];
        pushSurface(false);
        mPlayer.prepare(renderers);
        mRendererBuildingState = RendererBuildingState.BUILT;
    }

    /**
     * Invoked if a {@link RendererBuilder} encounters an error.
     *
     * @param e Describes the error.
     */
    public void onRenderersError(Exception e) {
        for (Listener listener : mListeners) {
            listener.onError(e);
        }
        mRendererBuildingState = RendererBuildingState.IDLE;
        maybeReportPlayerState();
    }

    public void seekTo(long positionMs) {
        mPlayer.seekTo(positionMs);
    }

    public void release() {
        mRendererBuilder.cancel();
        mRendererBuildingState = RendererBuildingState.IDLE;
        mSurface = null;
        mPlayer.release();
    }

    public int getPlaybackState() {
        if (mRendererBuildingState == RendererBuildingState.BUILDING) {
            return STATE_PREPARING;
        }
        int playerState = mPlayer.getPlaybackState();
        if (mRendererBuildingState == RendererBuildingState.BUILT
                && playerState == STATE_IDLE) {
            return STATE_PREPARING;
        }
        return playerState;
    }

    public long getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        mPlayer.setPlayWhenReady(playWhenReady);
    }

    public Handler getMainHandler() {
        return mMainHandler;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int state) {
        maybeReportPlayerState();
    }

    @Override
    public void onPlayerError(ExoPlaybackException exception) {
        mRendererBuildingState = RendererBuildingState.IDLE;
        for (Listener listener : mListeners) {
            listener.onError(exception);
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                   float pixelWidthHeightRatio) {
        for (Listener listener : mListeners) {
            listener.onVideoSizeChanged(width, height, unappliedRotationDegrees,
                    pixelWidthHeightRatio);
        }
    }

    @Override
    public void onDroppedFrames(int count, long elapsed) {
        /* do nothing */
    }

    @Override
    public void onDecoderInitializationError(DecoderInitializationException e) {
        /* do nothing */
    }

    @Override
    public void onCryptoError(CryptoException e) {
        /* do nothing */
    }

    @Override
    public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs,
                                     long initializationDurationMs) {
        /* do nothing */
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        /* do nothing */
    }

    @Override
    public void onDrawnToSurface(Surface surface) {
        /* do nothing */
    }

    private void maybeReportPlayerState() {
        boolean playWhenReady = mPlayer.getPlayWhenReady();
        int playbackState = getPlaybackState();
        if (mLastReportedPlayWhenReady != playWhenReady
                || mLastReportedPlaybackState != playbackState) {
            for (Listener listener : mListeners) {
                listener.onStateChanged(playWhenReady, playbackState);
            }
            mLastReportedPlayWhenReady = playWhenReady;
            mLastReportedPlaybackState = playbackState;
        }
    }

    private void pushSurface(boolean blockForSurfacePush) {
        if (mVideoRenderer == null) {
            return;
        }

        if (blockForSurfacePush) {
            mPlayer.blockingSendMessage(
                    mVideoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, mSurface);
        } else {
            mPlayer.sendMessage(
                    mVideoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, mSurface);
        }
    }
}
