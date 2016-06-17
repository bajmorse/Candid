package com.app;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.TextureView;

import java.io.IOException;
import java.util.AbstractSet;

/**
 * Created by brent on 2016-06-14.
 */
public class CameraTextureView extends TextureView {

    // Size ratio
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public CameraTextureView(Context context) {
        this(context, null);
    }

    public CameraTextureView(Context context, AttributeSet set) {
        this(context, set, 0);
    }

    public CameraTextureView(Context context, AttributeSet set, int defStyle) {
        super(context, set, defStyle);
    }

    public void setAspectRatio(final int width, final int height) {
        if (width < 0 || height < 0) throw new IllegalArgumentException("Size cannot be negative");
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
