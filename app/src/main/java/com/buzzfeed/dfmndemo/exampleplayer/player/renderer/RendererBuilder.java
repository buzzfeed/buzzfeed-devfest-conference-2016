package com.buzzfeed.dfmndemo.exampleplayer.player.renderer;

import com.buzzfeed.dfmndemo.exampleplayer.player.SampleVideoPlayer;

/**
 * Builds renderers for the {@link SampleVideoPlayer}
 */
public interface RendererBuilder {
    /**
     * Builds renderers for playback.
     *
     * @param player The player for which renderers are being built. {@link SampleVideoPlayer#onRenderers}
     *               should be invoked once the renderers have been built. If building fails,
     *               {@link SampleVideoPlayer#onRenderersError} should be invoked.
     */
    void buildRenderers(SampleVideoPlayer player);

    /**
     * Cancels the current build operation, if there is one. Else does nothing.
     * <p/>
     * A canceled build operation must not invoke {@link SampleVideoPlayer#onRenderers} or
     * {@link SampleVideoPlayer#onRenderersError} on the player, which may have been released.
     */
    void cancel();
}
