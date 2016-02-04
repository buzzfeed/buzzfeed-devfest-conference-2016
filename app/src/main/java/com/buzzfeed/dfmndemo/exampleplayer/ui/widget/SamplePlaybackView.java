package com.buzzfeed.dfmndemo.exampleplayer.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.buzzfeed.dfmndemo.exampleplayer.R;
import com.google.android.exoplayer.AspectRatioFrameLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Simple view used for displaying video content (and showing a progress indicator).
 */
public class SamplePlaybackView extends FrameLayout {

    @Bind(R.id.sample_frame) AspectRatioFrameLayout mVideoFrame;
    @Bind(R.id.sample_surface) TextureView mTextureView;
    @Bind(R.id.sample_progress) ProgressBar mProgressBar;
    @Bind(R.id.sample_shutter) View mShutterView;

    public SamplePlaybackView(Context context) {
        this(context, null, 0);
    }

    public SamplePlaybackView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SamplePlaybackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        inflate(getContext(), R.layout.view_sample_playback, this);
        ButterKnife.bind(this);
    }

    public void updateAspectRatio(float aspectRatio) {
        mVideoFrame.setAspectRatio(aspectRatio);
    }

    public void setProgressVisibility(boolean visible) {
        mProgressBar.setVisibility(visible ? VISIBLE : INVISIBLE);
    }

    public void setShutterVisibility(boolean visible) {
        mShutterView.setVisibility(visible ? VISIBLE : INVISIBLE);
    }

    public TextureView getTextureView() {
        return mTextureView;
    }
}
