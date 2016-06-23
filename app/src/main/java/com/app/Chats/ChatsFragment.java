package com.app.Chats;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.Connect.Friend;
import com.app.Connect.StickyFriendsAdapter;
import com.app.R;

public class ChatsFragment extends Fragment implements View.OnClickListener {

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chats_fragment, container, false);

        // Update profile picture
        ImageView friendProfilePicture = (ImageView) view.findViewById(R.id.chats_profile_picture);
        friendProfilePicture.setImageResource(mFriend.getProfilePic());

        //Setup storyline
        setupStoryline(view);

        return view;
    }

    private void setupStoryline(View view) {
        // Get sticky friends view
        mStoryline = (RecyclerView) view.findViewById(R.id.chats_storyline);

        // Setup layout manager
        mStorylineLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mStoryline.setLayoutManager(mStorylineLayoutManager);

        // Setup adapter
        mStorylineAdapter = new StorylineAdapter(getContext(), this);
        mStoryline.setAdapter(mStorylineAdapter);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        Log.d(TAG, "Clicked storyboard thumbnail");
    }

    /**
     * Fragment listener
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
