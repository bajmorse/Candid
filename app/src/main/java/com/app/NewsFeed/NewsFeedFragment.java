package com.app.NewsFeed;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.app.R;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NewsFeedFragment extends Fragment {

    /**
     * Variables
     */
    // Logger Tag
    public static final String TAG = "NEWS FEED";
    // Listener for fragment interaction with host activity
    private OnFragmentInteractionListener mListener;
    // Recycler view variables
    private RecyclerView mNewsFeedRecyclerView;
    private RecyclerView.Adapter mNewsFeedAdapter;
    private RecyclerView.LayoutManager mNewsFeedLayoutManager;

    /**
     * Lifecycle functions
     */
    public static NewsFeedFragment newInstance() {
        return new NewsFeedFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_feed_fragment, container, false);

        // Get views
        mNewsFeedRecyclerView = (RecyclerView) view.findViewById(R.id.news_feed);

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
     * Listeners
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
