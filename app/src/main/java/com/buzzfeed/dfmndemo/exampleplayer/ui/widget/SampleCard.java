package com.buzzfeed.dfmndemo.exampleplayer.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.buzzfeed.dfmndemo.exampleplayer.R;

import butterknife.Bind;

import static butterknife.ButterKnife.bind;

/**
 * Simple card used for sample video items
 */
public class SampleCard extends CardView {

    @Bind(R.id.sample_card_title) TextView mTitleTextView;
    @Bind(R.id.sample_card_subtitle) TextView mSubtitleTextView;

    public SampleCard(Context context) {
        this(context, null, 0);
    }

    public SampleCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SampleCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.view_sample_card, this);
        bind(this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.SampleCard, defStyleAttr, 0);

        String title = typedArray.getString(R.styleable.SampleCard_sampleTitle);
        mTitleTextView.setText(!TextUtils.isEmpty(title) ? title : "");

        String subtitle = typedArray.getString(R.styleable.SampleCard_sampleSubtitle);
        mSubtitleTextView.setText(!TextUtils.isEmpty(subtitle) ? subtitle : "");

        int color = typedArray.getColor(R.styleable.SampleCard_sampleColor, 0);
        setCardBackgroundColor(color);

        typedArray.recycle();

        setRadius(getResources().getDimensionPixelSize(R.dimen.sample_card_corner_radius));
        setCardElevation(getResources().getDimensionPixelSize(R.dimen.sample_card_elevation));
        setPreventCornerOverlap(false);
    }
}
