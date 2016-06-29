package com.app.Connect.Chat;

import android.content.Context;
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
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.Connect.Friend;
import com.app.OnFragmentInteractionListener;
import com.app.R;

import java.util.ArrayList;

public class ChatFragment extends Fragment implements
        View.OnClickListener,
        View.OnTouchListener {

    /**
     * Variables
     */
    // Logging tag
    public static final String TAG = "CHAT_FRAG";
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
    private float mLastX;
    private int mWidth;

    /**
     * Lifecycle functions
     */
    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFriend = (Friend) getArguments().getSerializable(FRIEND_KEY);
        mListener.onFragmentOpened();
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
    public void onClick(View v) {
        Log.d(TAG, "Clicked!");
    }

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
                if (newX > 0) {
                    view.animate().x(newX).setDuration(0).start();
                }
                mLastX = x;
                break;
            }
            case MotionEvent.ACTION_UP: {
                float viewX = view.getX();
                if (viewX > mWidth/2) {
                    getParentFragment().getChildFragmentManager().popBackStack();
                    break;
                } else if (viewX > 0) {
                    int duration = (int) (25 * Math.log(viewX));
                    view.animate().x(0).setDuration(duration).start();
                }
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
            int flingVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
            if (velocityX > flingVelocity) {
                getParentFragment().getChildFragmentManager().popBackStack();
                return true;
            }
            return false;
        }
    }

    /**
     * Test data
     */
    public static ArrayList<PrivateCandid> getTestData() {
        ArrayList<PrivateCandid> testData = new ArrayList<>();
        for (int idx = 0; idx < 10; idx++) {
            PrivateCandid candid;
            if (idx % 2 == 0) {
                candid = new PrivateCandid(R.drawable.test_friend, "PrivateCandid #" + idx, PrivateCandid.CandidSource.SENT);
            } else {
                candid = new PrivateCandid(R.drawable.test_sticky_friend, "PrivateCandid #" + idx, PrivateCandid.CandidSource.RECEIVED);
            }
            testData.add(candid);
        }
        return testData;
    }
}
