package com.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainFragment extends ListFragment {

    /**
     * Variables
     */
    // Logger Tag
    public static final String TAG = "NEWS FEED";
    // Listener for fragment interaction with host activity
    private OnFragmentInteractionListener mListener;

    /**
     * Lifecycle functions
     */
    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up the camera button as header
        View mCameraButton = getLayoutInflater(savedInstanceState).inflate(R.layout.camera_button, null);
        getListView().addHeaderView(mCameraButton);

        // Set adapter for the news feed
        setListAdapter(new NewsFeedAdapter(getContext()));
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

    /**
     * NewsFeedAdapter
     * Adapter to populate the news feed
     */
    public class NewsFeedAdapter extends ArrayAdapter<String> {
        final Context context;

        public NewsFeedAdapter(Context context) {
            super(context, R.layout.news_feed_item);
            this.context = context;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.news_feed_item, parent, false);
            return view;
        }
    }
}
