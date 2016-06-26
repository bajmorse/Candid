package com.app.NewsFeed;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.CandidViewPager;
import com.app.R;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NewsFeedFragment extends Fragment implements View.OnClickListener {

    /**
     * Variables
     */
    // Logger Tag
    public static final String TAG = "NEWS FEED";
    // Listener for fragment interaction with host activity
    private OnFragmentInteractionListener mListener;
    // Recycler view variables
    private int mScrolled;
    private RecyclerView mNewsFeedRecyclerView;
    private RecyclerView.Adapter mNewsFeedAdapter;
    private RecyclerView.LayoutManager mNewsFeedLayoutManager;
    // Header views
    private int mUsernameInitialY, mCameraInitialY, mCameraHeight;
    private View mNewsFeedHeaderView;
    private TextView mUsernameTextView;
    private ImageView mCameraImageView;
    // Camera animations
    private boolean mCameraOpen = false;

    /**
     * Lifecycle functions
     */
    public static NewsFeedFragment newInstance() {
        return new NewsFeedFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.news_feed_fragment, container, false);

        // Get views
        mNewsFeedHeaderView = view.findViewById(R.id.news_feed_header_top);
        mUsernameTextView = (TextView) view.findViewById(R.id.news_feed_fragment_username);
        mCameraImageView = (ImageView) view.findViewById(R.id.news_feed_fragment_camera_button);
        mCameraImageView.setOnClickListener(this);

        // Get username initial y
        mCameraHeight = (int) (getResources().getDimension(R.dimen.news_feed_header_height) - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                mUsernameInitialY = (int) mUsernameTextView.getY();
                mCameraInitialY = (int) mCameraImageView.getY();
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        // Setup recycler view
        mNewsFeedRecyclerView = (RecyclerView) view.findViewById(R.id.news_feed);
        mScrolled = 0;
        mNewsFeedRecyclerView.addOnScrollListener(new NewsFeedScrollListener());

        mNewsFeedLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mNewsFeedRecyclerView.setLayoutManager(mNewsFeedLayoutManager);

        mNewsFeedAdapter = new NewsFeedAdapter(getContext());
        mNewsFeedRecyclerView.setAdapter(mNewsFeedAdapter);

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

    /**
     * Animations
     */
    private void showCamera() {
        // Get window size
        Activity activity = getActivity();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // Animate news feed out
        mNewsFeedRecyclerView.animate().y(displayMetrics.heightPixels).setDuration(800).start();

        // Animate camera button down
        mCameraImageView.animate().y(mNewsFeedRecyclerView.getHeight() + mNewsFeedHeaderView.getHeight() + ((CandidViewPager) getView().getParent()).getPaddingTop()).setDuration(775).start();

        // Animate activity elements out
        mListener.showCamera();

        // Set camera as open
        mCameraOpen = true;
    }

    public void hideCamera() {
        // Get window size
        Activity activity = getActivity();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // Animate news feed in
        mNewsFeedRecyclerView.animate().y(mNewsFeedHeaderView.getHeight()).setDuration(800).start();

        // Animate camera button up
        mCameraImageView.animate().y(mCameraInitialY).setDuration(775).start();

        // Set camera as closed
        mCameraOpen = false;
    }

    /**
     * Click listener
     */
    @Override
    public void onClick(View view) {
        if (view == mCameraImageView) {
            if (!mCameraOpen) {
                showCamera();
            } else {

            }
        }
    }

    /**
     * Scroll listener
     */
    private class NewsFeedScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dy != 0) {
                mScrolled += dy;

                int usernameY = (int) mUsernameTextView.getY();
                int cameraY = (int) mCameraImageView.getY();
                int destinationY = usernameY - dy;

                if (dy > 0) {
                    if (destinationY > 0) {
                        mUsernameTextView.animate().y(usernameY - dy).setDuration(0).start();
                        mCameraImageView.animate().y(cameraY - dy).setDuration(0).start();
                    } else if (usernameY != 0) {
                        mUsernameTextView.animate().y(0).setDuration(0).start();
                        mCameraImageView.animate().y(mCameraInitialY - mUsernameInitialY).setDuration(0).start();
                    }
                } else if (mScrolled < mCameraHeight) {
                    if (destinationY < mUsernameInitialY) {
                        mUsernameTextView.animate().y(usernameY - dy).setDuration(0).start();
                        mCameraImageView.animate().y(cameraY - dy).setDuration(0).start();
                    } else if (usernameY != mUsernameInitialY) {
                        mUsernameTextView.animate().y(mUsernameInitialY).setDuration(0).start();
                        mCameraImageView.animate().y(mCameraInitialY).setDuration(0).start();
                    }
                }
            }
        }
    }

    /**
     * Fragment listener
     */
    public interface OnFragmentInteractionListener {
        void showCamera();
    }
}
