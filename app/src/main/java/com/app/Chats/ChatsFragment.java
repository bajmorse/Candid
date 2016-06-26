package com.app.Chats;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.app.Connect.ConnectFragment;
import com.app.Connect.Friend;
import com.app.R;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment implements
        View.OnClickListener,
        View.OnTouchListener {

    /**
     * Variables
     */
    // Logging tag
    public static final String TAG = "CHATS_FRAG";
    // Fragment listener
    private OnFragmentInteractionListener mListener;
    // Friend's data
    public static final String FRIEND_KEY = "FRIEND_DATA";
    private Friend mFriend;
    // Storyline
    private RecyclerView mStoryline;
    private RecyclerView.LayoutManager mStorylineLayoutManager;
    private RecyclerView.Adapter mStorylineAdapter;
    // Comic
    private RecyclerView mComic;
    private RecyclerView.LayoutManager mComicLayoutManager;
    private RecyclerView.Adapter mComicAdapter;
    // Swipe
    private GestureDetectorCompat mGestureDetector;
    private Point mTouchPoint;
    private int mDeltaX;
    private int mWidth;

    /**
     * Lifecycle functions
     */
    public static ChatsFragment newInstance() {
        return new ChatsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFriend = (Friend) getArguments().getSerializable(FRIEND_KEY);
        mListener.onChatFragmentOpened();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chats_fragment, container, false);
        view.setOnTouchListener(this);
        mGestureDetector = new GestureDetectorCompat(getContext(), new ChatsGestureListener());

        // Get width
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mWidth = metrics.widthPixels;

        // Update profile picture
        ImageView friendProfilePicture = (ImageView) view.findViewById(R.id.chats_profile_picture);
        friendProfilePicture.setImageResource(mFriend.getProfilePic());

        //Setup storyline
        setupStoryline(view);
        setupComic(view);

        return view;
    }

    private void setupStoryline(View view) {
        // Get storyline view
        mStoryline = (RecyclerView) view.findViewById(R.id.chats_storyline);

        // Setup layout manager
        mStorylineLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mStoryline.setLayoutManager(mStorylineLayoutManager);

        // Setup adapter
        mStorylineAdapter = new StorylineAdapter(getContext(), this);
        mStoryline.setAdapter(mStorylineAdapter);
    }

    private void setupComic(View view) {
        // Get comic view
        mComic = (RecyclerView) view.findViewById(R.id.chats_comic);

        // Setup layout manager
        mComicLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mComic.setLayoutManager(mComicLayoutManager);

        // Setup adapter
        mComicAdapter = new ComicAdapter(getContext(), this);
        mComic.setAdapter(mComicAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Fragment Destroyed");
        mListener.onChatFragmentClosed();
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
     * Click listeners
     */
    @Override
    public void onClick(View v) {
        Log.d(TAG, "Clicked!");
    }

    @Override
    public boolean onTouch(final View view, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        final int x = (int) event.getRawX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                mDeltaX = x - layoutParams.leftMargin;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                int leftMargin = x - mDeltaX;
                if (leftMargin < 0) {
                    leftMargin = 0;
                    if (layoutParams.leftMargin == 0) {
                        return false;
                    }
                }
                layoutParams.leftMargin = leftMargin;
                layoutParams.rightMargin = -leftMargin;
                view.setLayoutParams(layoutParams);
                break;
            }
            case MotionEvent.ACTION_UP: {
                final int startLeftMargin = ((RelativeLayout.LayoutParams) view.getLayoutParams()).leftMargin;
                if (startLeftMargin <= 0) break;

                Log.d(TAG, "Start margin: " + startLeftMargin + "     Width: " + mWidth);

                Animation animation;
                int duration;
                if (startLeftMargin > mWidth/2) {
                    getParentFragment().getChildFragmentManager().popBackStack();
                    break;
                } else {
                    duration = (int) (25 * Math.log(startLeftMargin));
                    animation = new Animation() {

                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                            layoutParams.leftMargin = (int) (startLeftMargin - (startLeftMargin * interpolatedTime));
                            view.setLayoutParams(layoutParams);
                        }
                    };
                }
                animation.setDuration(duration);
                view.startAnimation(animation);
                break;
            }
        }
        return true;
    }

    /**
     * Gesture detector
     */
    private class ChatsGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityX > 0) {
                getParentFragment().getChildFragmentManager().popBackStack();
                return true;
            }
            return false;
        }
    }

    /**
     * Fragment listener
     */
    public interface OnFragmentInteractionListener {
        void onChatFragmentOpened();
        void onChatFragmentClosed();
    }

    /**
     * Test data
     */
    public static ArrayList<Candid> getTestData() {
        ArrayList<Candid> testData = new ArrayList<>();
        for (int idx = 0; idx < 10; idx++) {
            Candid candid;
            if (idx % 2 == 0) {
                candid = new Candid(R.drawable.test_friend, "Candid #" + idx, Candid.CandidSource.SENT);
            } else {
                candid = new Candid(R.drawable.test_sticky_friend, "Candid #" + idx, Candid.CandidSource.RECEIVED);
            }
            testData.add(candid);
        }
        return testData;
    }
}
