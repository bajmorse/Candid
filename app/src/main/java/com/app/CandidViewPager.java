package com.app;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.AbstractSet;

/**
 * Created by brent on 2016-06-23.
 */
public class CandidViewPager extends ViewPager {

    /**
     * Variables
     */
    // Logging tag
    public static final String TAG = "CANDID_VIEW_PAGER";
    // Swiping variables
    private boolean mSwipeEnabled;

    /**
     * Constructor
     */
    public CandidViewPager(Context context) {
        super(context);
        mSwipeEnabled = true;
    }

    public CandidViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSwipeEnabled = true;
    }

    /**
     * Swiping functions
     */
    public void setSwipeEnabled(boolean swipeEnabled) {
        mSwipeEnabled = swipeEnabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mSwipeEnabled && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mSwipeEnabled && super.onInterceptTouchEvent(ev);
    }
}
