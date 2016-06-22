package com.app._TestingFragments;

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
import android.widget.TextView;

import com.app.R;
import com.app.Utils.CandidUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class HorizontalScrollTestFragment extends Fragment {

    /**
     * Variables
     */
    // Logging tag
    public static final String TAG = "HorizScrollTestFrag";
    // Fragment listener
    private OnFragmentInteractionListener mListener;
    // Horizontal scrolling view
    private RecyclerView mHorizontalRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    /**
     * Lifecycle functions\
     */
    public static HorizontalScrollTestFragment newInstance() {
        return new HorizontalScrollTestFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.horizontal_scroll_test_fragment, container, false);

        // Get views
        mHorizontalRecyclerView = (RecyclerView) view.findViewById(R.id.horizontal_recycler_view);

        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mHorizontalRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new HorizontalScrollViewAdapter(getTestData());
        mHorizontalRecyclerView.setAdapter(mAdapter);

        return view;
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
     * Fragment listener-
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Adapter
     */
    private class HorizontalScrollViewAdapter extends RecyclerView.Adapter<HorizontalScrollViewAdapter.DataObjectHolder> {
        private ArrayList<Data> mData;

        /**
         * Constructor
         */
        public HorizontalScrollViewAdapter(ArrayList<Data> data) {
            mData = data;
        }

        /**
         * Lifecycle functions
         */
        @Override
        public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Inflate item view
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_list_item, parent, false);

            // Set data holder
            DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
            return dataObjectHolder;
        }

        @Override
        public void onBindViewHolder(DataObjectHolder holder, int position) {
            holder.one.setBackgroundColor(CandidUtils.getRandomColor(getContext()));
            holder.one.setText(mData.get(position).getOne());
            holder.two.setText(mData.get(position).getTwo());
        }

        @Override
        public int getItemCount() {
            return 10;
        }

        /**
         * DataObjectHolder
         * Holds
         */
        public class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView one, two;

            public DataObjectHolder(View itemView) {
                super(itemView);
                one = (TextView) itemView.findViewById(R.id.text_view_one);
                two = (TextView) itemView.findViewById(R.id.text_view_two);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked item");
            }
        }
    }

    /**
     * Data
     */
    public class Data {
        private String mOne, mTwo;

        public Data(final String one, final String two) {
            mOne = one;
            mTwo = two;
        }

        public String getOne() {
            return mOne;
        }

        public void setOne(String one) {
            mOne = one;
        }

        public String getTwo() {
            return mTwo;
        }

        public void setTwo(String two) {
            mTwo = two;
        }
    }

    private ArrayList<Data> getTestData() {
        ArrayList<Data> testData = new ArrayList<Data>();
        for (int idx = 0; idx < 10; idx++) {
            Data data = new Data("Some text " + idx, "More text " + idx);
            testData.add(data);
        }
        return testData;
    }
}
