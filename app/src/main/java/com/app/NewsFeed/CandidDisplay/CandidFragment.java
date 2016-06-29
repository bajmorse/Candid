package com.app.NewsFeed.CandidDisplay;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.NewsFeed.Candid;
import com.app.OnFragmentInteractionListener;
import com.app.R;

public class CandidFragment extends Fragment implements View.OnTouchListener {

    /**
     * Variables
     */
    // Logger tag
    public static final String TAG = "CANDID_FRAG";
    // Fragment listener
    private OnFragmentInteractionListener mListener;
    // Keys
    public static final String CANDID_KEY = "CANDID_DATA";
    // PrivateCandid
    private Candid mCandid;
    private ImageView mImageView;
    // Swipe
    private GestureDetectorCompat mGestureDetector;
    private float mLastY;
    private int mHeight;

    /**
     * Lifecycle functions
     */
    public static CandidFragment newInstance() {
        return new CandidFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCandid = (Candid) getArguments().get(CANDID_KEY);
        mListener.onFragmentOpened();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.candid_fragment, container, false);
        view.setOnTouchListener(this);
        mGestureDetector = new GestureDetectorCompat(getContext(), new CandidGestureListener());

        // Get height
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mHeight = metrics.heightPixels;

        // Set candid image
        mImageView = (ImageView) view.findViewById(R.id.candid_image_view);
        if (mImageView != null && mCandid != null) mImageView.setImageResource(mCandid.getPictureId());

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
     * Touch listener
     */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        final float y = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLastY = y;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float deltaY = y - mLastY;
                float newY = view.getY() + deltaY;
                if (newY > 0) {
                    view.animate().y(newY).setDuration(0).start();
                }
                mLastY = y;
                break;
            }
            case MotionEvent.ACTION_UP: {
                float viewY = view.getY();
                if (viewY > mHeight/3) {
                    getParentFragment().getChildFragmentManager().popBackStack();
                    break;
                } else if (viewY > 0){
                    int duration = (int) (25 * Math.log(viewY));
                    view.animate().y(0).setDuration(duration).start();
                }
                break;
            }
        }
        return true;
    }

    /**
     * Gesture detector
     */
    private class CandidGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int flingVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
            if (velocityY > flingVelocity) {
                getParentFragment().getChildFragmentManager().popBackStack();
                return true;
            }
            return false;
        }
    }
}
