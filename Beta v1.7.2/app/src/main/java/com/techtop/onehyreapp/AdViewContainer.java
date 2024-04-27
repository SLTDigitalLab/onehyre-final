package com.techtop.onehyreapp;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

public class AdViewContainer extends FrameLayout {
    public AdViewContainer(Context context) {
        super(context);
        init();
    }

    public AdViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.ad_view_layout, this, true);
    }
}