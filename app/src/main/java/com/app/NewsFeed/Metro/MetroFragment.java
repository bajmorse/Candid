package com.app.NewsFeed.Metro;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.R;

public class MetroFragment extends Fragment {

    /**
     * Variables
     */
    // Logging tag
    public static final String TAG = "METRO_FRAG";

    /**
     * Lifecycle functions
     */
    public static MetroFragment newInstance() {
        return new MetroFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.metro_fragment, container, false);
    }
}
