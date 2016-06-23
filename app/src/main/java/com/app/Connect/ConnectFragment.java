package com.app.Connect;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.app.CustomViews.AutoFormattingGridView;
import com.app.R;

public class ConnectFragment extends Fragment implements
        AdapterView.OnItemClickListener,
        View.OnClickListener {

    /**
     * Variables
     */
    // Logging tag
    public static final String TAG = "CONNECT_FRAG";
    // Fragment Listener
    private OnFragmentInteractionListener mListener;
    // Sticky friends
    private RecyclerView mStickyFriendsView;
    private RecyclerView.LayoutManager mStickyFriendsLayoutManager;
    private RecyclerView.Adapter mStickyFriendsAdapter;
    // Friends list
    private GridView mFriendsListView;
    private BaseAdapter mFriendsListAdapter;

    public static ConnectFragment newInstance() {
        return new ConnectFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.connect_fragment, container, false);

        // Setup friend views
        setupStickyFriends(view);
        setupFriendsList(view);

        return view;
    }

    private void setupStickyFriends(View view) {
        // Get sticky friends view
        mStickyFriendsView = (RecyclerView) view.findViewById(R.id.sticky_friends);

        // Setup layout manager
        mStickyFriendsLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mStickyFriendsView.setLayoutManager(mStickyFriendsLayoutManager);

        // Setup adapter
        mStickyFriendsAdapter = new StickyFriendsAdapter(getContext(), this);
        mStickyFriendsView.setAdapter(mStickyFriendsAdapter);
    }

    private void setupFriendsList(View view) {
        // Get friends list view
        mFriendsListView = (GridView) view.findViewById(R.id.friends_grid);

        // Setup adapter
        mFriendsListAdapter = new FriendsListAdapter(getContext());
        mFriendsListView.setAdapter(mFriendsListAdapter);

        // Set click listener
        mFriendsListView.setOnItemClickListener(this);
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
     * Friends list click listener
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "Selected friend: " + position);
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "Selected sticky friend");
    }

    /**
     * Fragment Listener
     */
     public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
