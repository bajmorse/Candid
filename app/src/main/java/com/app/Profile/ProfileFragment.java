package com.app.Profile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

public class ProfileFragment extends Fragment {

    // Logger tag
    public static final String TAG = "PROFILE_FRAG";
    // Fragment listener
    private OnFragmentInteractionListener mListener;
    // Map
    private GoogleMap mMap;
    private MapView mMapView;

    /**
     * Lifecycle functions
     */
    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Get map view
        mMapView = (MapView) view.findViewById(R.id.profile_map);
        mMapView.onCreate(savedInstanceState);

        // Get map
        mMap = mMapView.getMap();
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMyLocationEnabled(true);

        // Initialize map
        MapsInitializer.initialize(getActivity());

        // Map updater
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
        mMap.animateCamera(cameraUpdate);

        return view;
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
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
     * Fragment listener
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
