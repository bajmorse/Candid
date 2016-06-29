package com.app.Profile.Support;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import com.app.OnFragmentInteractionListener;
import com.app.R;

public class SupportFragment extends Fragment implements
        View.OnTouchListener,
        View.OnFocusChangeListener,
        ViewTreeObserver.OnGlobalLayoutListener {

    /**
     * Variables
     */
    // Logging tag
    public static final String TAG = "SUPPORT_FRAG";
    // Fragment listener
    private OnFragmentInteractionListener mListener;
    // Swipe
    private GestureDetectorCompat mGestureDetector;
    private float mLastX;
    private int mWidth;
    // Keyboard adjustment
    private EditText mBody;
    private boolean mBodyHasFocus = false;

    /**
     * Lifecycle functions
     */
    public static SupportFragment newInstance() {
        return new SupportFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListener.onFragmentOpened();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.support_fragment, container, false);
        view.setOnTouchListener(this);
        view.getViewTreeObserver().addOnGlobalLayoutListener(this);

        // Create gesture listener
        mGestureDetector = new GestureDetectorCompat(getContext(), new SupportGestureListener());

        // Get width
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mWidth = metrics.widthPixels;

        // Set edit text listener
        mBody = (EditText) view.findViewById(R.id.support_body_text);
        mBody.setOnFocusChangeListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener.onFragmentClosed();
    }

    /**
     * Click listeners
     */
    @Override
    public boolean onTouch(final View view, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        final float x = event.getRawX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLastX = x;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float deltaX = x - mLastX;
                float newX = view.getX() + deltaX;
                if (newX < 0) {
                    view.animate().x(newX).setDuration(0).start();
                }
                mLastX = x;
                break;
            }
            case MotionEvent.ACTION_UP: {
                float viewX = view.getX();
                if (viewX < -mWidth/2) {
                    getParentFragment().getChildFragmentManager().popBackStack();
                    break;
                } else if (viewX < 0) {
                    int duration = (int) (25 * Math.log(-viewX));
                    view.animate().x(0).setDuration(duration).start();
                }
                break;
            }
        }
        return true;
    }

    /**
     * Edit text focus listener
     */
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        mBodyHasFocus = hasFocus;
    }

    /**
     * Gloabl layout listener
     */
    @Override
    public void onGlobalLayout() {
        View view = getView();
        if (view == null) return;

        // Get screen height
        int screenHeight = view.getRootView().getHeight();

        // Get visible height
        Rect visibleDisplay = new Rect();
        view.getWindowVisibleDisplayFrame(visibleDisplay);

        // Get view position
        int[] viewPosition = new int[2];
        view.getLocationOnScreen(viewPosition);

        // Set adjustment required
        int viewAdjustment = visibleDisplay.bottom - mBody.getBottom();
        int heightDifference = screenHeight - visibleDisplay.bottom;
        if (mBodyHasFocus && heightDifference > 400) {
            getView().animate().y(viewAdjustment - viewPosition[1]).setDuration(200).start();
        } else if (view.getY() < 0) {
            getView().animate().y(0).setDuration(200).start();
        }
    }

    /**
     * Gesture detector
     */
    private class SupportGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int flingVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
            if (velocityX < -flingVelocity) {
                getParentFragment().getChildFragmentManager().popBackStack();
                return true;
            }
            return false;
        }
    }
}
