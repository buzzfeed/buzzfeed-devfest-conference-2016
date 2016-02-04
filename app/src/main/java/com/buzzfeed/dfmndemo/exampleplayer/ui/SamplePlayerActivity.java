package com.buzzfeed.dfmndemo.exampleplayer.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;

import com.buzzfeed.dfmndemo.exampleplayer.R;
import com.buzzfeed.dfmndemo.exampleplayer.model.SampleVideo;
import com.buzzfeed.dfmndemo.exampleplayer.presenter.SamplePlayerPresenter;
import com.buzzfeed.dfmndemo.exampleplayer.ui.widget.SamplePlaybackView;
import com.google.android.exoplayer.util.PlayerControl;
import com.google.android.exoplayer.util.Util;

import butterknife.Bind;
import butterknife.OnClick;

import static butterknife.ButterKnife.bind;
import static butterknife.ButterKnife.unbind;

/**
 * Activity for sample playback.
 */
public class SamplePlayerActivity extends AppCompatActivity implements
        SamplePlayerPresenter.PlayerPreparedListener {

    private static final String EXTRA_VIDEO_TYPE = "video_type";
    private static final String EXTRA_VIDEO_URI = "video_uri";

    @Bind(R.id.main_content) SamplePlaybackView mView;

    private SamplePlayerPresenter mPresenter;
    private MediaController mMediaController;

    public static void start(Context context, SampleVideo video) {
        Intent starter = new Intent(context, SamplePlayerActivity.class);
        starter.putExtra(EXTRA_VIDEO_TYPE, video.getContentType());
        starter.putExtra(EXTRA_VIDEO_URI, video.getContentUri());
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_player);
        bind(this);

        mMediaController = new MediaController(this);
        mMediaController.setAnchorView(mView);

        mPresenter = new SamplePlayerPresenter(this, mView);
        mPresenter.setPlayerPreparedListener(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Intent intent = getIntent();

        final String contentUri = intent.getStringExtra(EXTRA_VIDEO_URI);
        final int contentType = intent.getIntExtra(EXTRA_VIDEO_TYPE, Util.TYPE_HLS);

        mPresenter.setContent(contentUri, contentType);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.preparePlayer();
    }

    @Override
    protected void onPause() {
        mPresenter.releasePlayer();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.cleanUp();
        unbind(this);
    }

    @OnClick(R.id.main_content)
    void onMainContentClicked() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show(0);
        }
    }

    @Override
    public void onPlayerPrepared(PlayerControl playerControl) {
        mMediaController.setMediaPlayer(playerControl);
        mMediaController.setEnabled(true);
    }
}
