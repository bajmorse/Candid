package com.app.Connect;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.LruCache;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.Connect.Chat.ChatFragment;
import com.app.R;

import java.io.Serializable;

public class ConnectFragment extends Fragment implements View.OnClickListener {

    /**
     * Variables
     */
    // Logging tag
    public static final String TAG = "CONNECT_FRAG";
    // Sticky friends
    private RecyclerView mStickyFriendsView;
    private RecyclerView.LayoutManager mStickyFriendsLayoutManager;
    private RecyclerView.Adapter mStickyFriendsAdapter;
    // Friends list
    private RecyclerView mFriendsListView;
    private RecyclerView.LayoutManager mFriendsListLayoutManager;
    private RecyclerView.Adapter mFriendsListAdapter;

    public static ConnectFragment newInstance() {
        return new ConnectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mFriendsListView = (RecyclerView) view.findViewById(R.id.friends_grid);

        // Setup layout manager
        mFriendsListLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        mFriendsListView.setLayoutManager(mFriendsListLayoutManager);

        // Setup adapter
        mFriendsListAdapter = new FriendsListAdapter(getContext(), this);
        mFriendsListView.setAdapter(mFriendsListAdapter);
    }

    /**
     * Click listener
     */
    @Override
    public void onClick(View view) {
        // Get friend data for clicked friend
        Friend friend = null;
        if (view.getId() == R.id.sticky_friends_item) {
            friend = (Friend) ((StickyFriendsAdapter) mStickyFriendsAdapter).getItem((Integer) view.getTag());
        } else if (view.getId() == R.id.friends_list_item) {
            friend = (Friend) ((FriendsListAdapter) mFriendsListAdapter).getItem((Integer) view.getTag());
        }

        // Open chat with that friend
        if (friend != null) openChat(friend);
    }

    private void openChat(Friend friend) {
        // Create chat fragment
        ChatFragment friendChatFragment = ChatFragment.newInstance();
        Bundle friendBundle = new Bundle();
        friendBundle.putSerializable(ChatFragment.FRIEND_KEY, (Serializable) friend);
        friendChatFragment.setArguments(friendBundle);
        String chatFragmentTag = ChatFragment.class.getName();

        // Create fragment transaction
        FragmentTransaction chatFragmentTransaction = getChildFragmentManager().beginTransaction();
        chatFragmentTransaction.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
        chatFragmentTransaction.replace(R.id.connect_fragment, friendChatFragment);
        chatFragmentTransaction.addToBackStack(chatFragmentTag);
        chatFragmentTransaction.commit();
    }
}
