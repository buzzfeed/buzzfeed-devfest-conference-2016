package com.buzzfeed.dfmndemo.exampleplayer.presenter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.buzzfeed.dfmndemo.exampleplayer.player.SampleVideoPlayer;
import com.buzzfeed.dfmndemo.exampleplayer.player.renderer.HlsRendererBuilder;
import com.buzzfeed.dfmndemo.exampleplayer.player.renderer.RendererBuilder;
import com.buzzfeed.dfmndemo.exampleplayer.ui.widget.SamplePlaybackView;
import com.google.android.exoplayer.util.PlayerControl;
import com.google.android.exoplayer.util.Util;

import timber.log.Timber;

import static com.google.android.exoplayer.ExoPlayer.STATE_BUFFERING;
import static com.google.android.exoplayer.ExoPlayer.STATE_ENDED;
import static com.google.android.exoplayer.ExoPlayer.STATE_IDLE;
import static com.google.android.exoplayer.ExoPlayer.STATE_PREPARING;
import static com.google.android.exoplayer.ExoPlayer.STATE_READY;

/**
 * A simple "Presenter" responsible for displaying video content. For
 * the purposes of this demo, we are using a TextureView as the surface
 * provider.
 */
public final class SamplePlayerPresenter implements SampleVideoPlayer.Listener,
        TextureView.SurfaceTextureListener {

    public interface PlayerPreparedListener {
        void onPlayerPrepared(PlayerControl playerControl);
    }

    private final Context mAppContext;

    private SampleVideoPlayer mVideoPlayer;
    private SamplePlaybackView mView;

    private String mContentUri;
    private int mContentType;

    private boolean mPlayerNeedsPrepare = true;
    private long mPlayerPosition = 0L;

    @Nullable
    private PlayerPreparedListener mPlayerPreparedListener;

    public SamplePlayerPresenter(Context context, SamplePlaybackView view) {
        mAppContext = context.getApplicationContext();
        mView = view;
        configureTextureView();
    }

    private void configureTextureView() {
        final TextureView textureView = mView.getTextureView();
        textureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onError(Exception e) {
        mPlayerNeedsPrepare = true;
        Toast.makeText(mAppContext, "An error occurred.", Toast.LENGTH_SHORT).show();
    }

    @CallSuper
    public void preparePlayer() {
        if (mVideoPlayer == null) {
            mVideoPlayer = new SampleVideoPlayer(getRendererBuilder());
            mVideoPlayer.addListener(this);
            mVideoPlayer.seekTo(mPlayerPosition);
            mPlayerNeedsPrepare = true;
        }

        if (mPlayerNeedsPrepare) {
            mVideoPlayer.prepare();
            mPlayerNeedsPrepare = false;

            if (mPlayerPreparedListener != null) {
                mPlayerPreparedListener.onPlayerPrepared(mVideoPlayer.getPlayerControl());
            }
        }

        mVideoPlayer.setPlayWhenReady(true);
    }

    @CallSuper
    public void releasePlayer() {
        if (mVideoPlayer != null) {
            mPlayerPosition = mVideoPlayer.getCurrentPosition();
            mVideoPlayer.release();
            mVideoPlayer = null;
        }
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        boolean shouldShowProgress = false;

        switch (playbackState) {
            case STATE_IDLE:
                break;
            case STATE_BUFFERING:
            case STATE_PREPARING:
                shouldShowProgress = true;
                break;
            case STATE_READY:
                break;
            case STATE_ENDED:
                mView.setShutterVisibility(true);
                mView.setKeepScreenOn(false);
                break;

            default:
                Timber.d("Unknown playback state: " + playbackState);
        }

        mView.setProgressVisibility(shouldShowProgress);
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                   float pixelWidthHeightRatio) {
        mView.setShutterVisibility(false);
        mView.setKeepScreenOn(true);
        mView.updateAspectRatio(height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mVideoPlayer != null) {
            mVideoPlayer.setSurface(new Surface(surface));
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        /* do nothing */
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mVideoPlayer != null) {
            mVideoPlayer.blockingClearSurface();
        }
        mPlayerNeedsPrepare = true;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        /* do nothing */
    }

    private RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(mAppContext, "SampleVideoPlayer");
        switch (mContentType) {
            case Util.TYPE_HLS:
                return new HlsRendererBuilder(mAppContext, userAgent, mContentUri);
            default:
                throw new IllegalStateException("Unsupported type: " + mContentType);
        }
    }

    public void setContent(@NonNull String contentUri, int contentType) {
        mContentUri = contentUri;
        mContentType = contentType;
    }

    public void setPlayerPreparedListener(@Nullable PlayerPreparedListener listener) {
        mPlayerPreparedListener = listener;
    }

    public void cleanUp() {
        mView = null;
    }
}
