package com.buzzfeed.dfmndemo.exampleplayer.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.buzzfeed.dfmndemo.exampleplayer.Config;
import com.buzzfeed.dfmndemo.exampleplayer.R;

import butterknife.OnClick;

import static butterknife.ButterKnife.bind;
import static butterknife.ButterKnife.findById;

public class SampleChooserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_chooser);
        bind(this);

        Toolbar toolbar = findById(this, R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
    }

    @OnClick(R.id.button_sample_one)
    void onSampleOneClicked() {
        SamplePlayerActivity.start(this, Config.SAMPLE_VIDEO_1);
    }

    @OnClick(R.id.button_sample_two)
    void onSampleTwoClicked() {
        SamplePlayerActivity.start(this, Config.SAMPLE_VIDEO_2);
    }

    @OnClick(R.id.button_sample_three)
    void onSampleThreeClicked() {
        SamplePlayerActivity.start(this, Config.SAMPLE_VIDEO_3);
    }
}
