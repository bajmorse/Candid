package com.app.Profile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.app.Connect.Chat.ChatFragment;
import com.app.Profile.Support.SupportFragment;
import com.app.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Objects;

public class ProfileFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    // Logger tag
    public static final String TAG = "PROFILE_FRAG";
    // Social media constants
    private static final String FACEBOOK_TAG = "facebook";
    private static final String TWITTER_TAG = "twitter";
    private static final String GOOGLE_TAG = "google";
    private static final String WEB_TAG = "web";
    // Map
    private GoogleMap mMap;
    private MapView mMapView;
    private Location mMyLocation;
    private GoogleApiClient mGoogleApiClient;

    /**
     * Lifecycle functions
     */
    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        // Setup map
        setupMap(view, savedInstanceState);

        // Setup profile buttons
        setupButtons(view);

        // Setup social media links
        setupLinks(view);

        return view;
    }

    @Override
    public void onStart() {
        if (mGoogleApiClient != null) mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();
        super.onStop();
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

    /**
     * Setup functions
     */
    private void setupMap(View view, Bundle savedInstanceState) {
        // Get map view
        mMapView = (MapView) view.findViewById(R.id.profile_map);
        mMapView.onCreate(savedInstanceState);

        // Get map
        mMap = mMapView.getMap();
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.setMyLocationEnabled(true);

        // Initialize map
        MapsInitializer.initialize(getActivity());
    }

    private void setupButtons(View view) {
        // Support
        View supportView = view.findViewById(R.id.profile_support);
        supportView.setOnClickListener(this);
    }

    private void setupLinks(View view) {
        // Facebook
        View facebookButton = view.findViewById(R.id.facebook_button);
        facebookButton.setOnClickListener(this);

        // Twitter
        View twitterButton = view.findViewById(R.id.twitter_button);
        twitterButton.setOnClickListener(this);

        // Google+
        View googleButton = view.findViewById(R.id.google_plus_button);
        googleButton.setOnClickListener(this);

        // Website
        View webButton = view.findViewById(R.id.website_button);
        webButton.setOnClickListener(this);
    }

    /**
     * Click listener
     */
    @Override
    public void onClick(View view) {
        Log.d(TAG, "Clicked!");

        // Clicked support
        if (view.getId() == R.id.profile_support) {
            Log.d(TAG, "Clicked support!");

            // Create support fragment
            SupportFragment supportFragment = SupportFragment.newInstance();
            String supportFragmentTag = SupportFragment.class.getName();

            // Create fragment transaction
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_left);
            fragmentTransaction.replace(R.id.profile_fragment, supportFragment);
            fragmentTransaction.addToBackStack(supportFragmentTag);
            fragmentTransaction.commit();
        }

        // Clicked on a social media link
        if (((View) view.getParent()).getId() == R.id.social_media_bar) {
            Intent socialLinkIntent = new Intent(Intent.ACTION_VIEW);
            PackageManager packageManager = getContext().getPackageManager();
            if (view.getId() == R.id.facebook_button) {
                String facebookUrl = "https://www.facebook.com/brenton.morse";
                try {
                    int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
                    if (versionCode > 3002850) { // New facebook url
                        facebookUrl = "fb://facewebmodal/f?href=" + facebookUrl;
                    } else if (versionCode > 0) {
                        facebookUrl = "fb://profile/561570385";
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // Facebook app not installed
                }
                socialLinkIntent.setData(Uri.parse(facebookUrl));
            } else if (view.getId() == R.id.twitter_button) {
                String twitterUrl = "https://twitter.com/bajmorse";
                try {
                    packageManager.getPackageInfo("com.twitter.android", 0);
                    twitterUrl = "twitter://user?user_id=829333471";
                } catch (PackageManager.NameNotFoundException e) {
                    // Twitter app not installed
                }
                socialLinkIntent.setData(Uri.parse(twitterUrl));
            } else if (view.getId() == R.id.google_plus_button) {
                String googleUrl = "https://plus.google.com/u/0/+BrentonMorse";
                socialLinkIntent.setData(Uri.parse(googleUrl));
            } else if (view.getId() == R.id.website_button) {
                socialLinkIntent.setData(Uri.parse("http://www.google.com"));
            }
            getActivity().startActivity(socialLinkIntent);
        }
    }

    /**
     * Google maps listeners and callbacks
     */
    @Override
    public void onConnected(Bundle bundle) {
        // Get location
        mMyLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        // Update map to location
        if (mMyLocation != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mMyLocation.getLatitude(), mMyLocation.getLongitude()), 14);
            mMap.moveCamera(cameraUpdate);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
