package com.app.CustomViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by brent on 2016-06-14.
 */
public class AutoResizeTextureView extends TextureView {

    // TileSize ratio
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public AutoResizeTextureView(Context context) {
        this(context, null);
    }

    public AutoResizeTextureView(Context context, AttributeSet set) {
        this(context, set, 0);
    }

    public AutoResizeTextureView(Context context, AttributeSet set, int defStyle) {
        super(context, set, defStyle);
    }

    public void setAspectRatio(final int width, final int height) {
        if (width < 0 || height < 0) throw new IllegalArgumentException("TileSize cannot be negative");
        mRatioHeight = height;
        mRatioWidth = width;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Get width and height
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // Set aspect ratio
        if (mRatioHeight == 0 || mRatioWidth == 0) {
            setMeasuredDimension(width, height);
        } else {
            setMeasuredDimension(width, width * mRatioHeight/mRatioWidth);
        }
    }
}
