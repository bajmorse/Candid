package com.app.Connect;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.app.R;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.Random;

/**
 * Created by brent on 2016-06-22.
 */
public class FriendsListAdapter extends BaseAdapter {

    /**
     * Variables
     */
    // Logging tag
    public static final String TAG = "FRIENDS_LIST_ADAPTER";
    // Context
    private Context mContext;
    // Data
    private ArrayList<Friend> mFriends;

    /**
     * Constructor
     */
    public FriendsListAdapter(Context context) {
        mContext = context;
        mFriends = getTestData();
        testAlgo();
    }

    /**
     * Lifecycle functions
     */
    @Override
    public int getCount() {
        return mFriends.size();
    }

    @Override
    public Object getItem(int position) {
        return mFriends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the items view
        View view;
        if (convertView == null) view = LayoutInflater.from(parent.getContext()).inflate(R.layout.connect_friend_list_item, parent, false);
        else view = convertView;

        // Set profile picture
        ImageView profilePictureView = (ImageView) view.findViewById(R.id.friends_list_profile_picture);
        profilePictureView.setImageResource(mFriends.get(position).getProfilePic());

        // Return the view
        return view;
    }

    /**
     * Test data
     */
    private ArrayList<Friend> getTestData() {
        ArrayList<Friend> testFriends = new ArrayList<>();
        for (int idx = 0; idx < 16; idx++) {
            Friend friend = new Friend("Friend" + idx, R.drawable.test_friend);

            Random rng = new Random();
            int numCandids = rng.nextInt(100);
            friend.setNumCandids(numCandids);

            int timeOffset = rng.nextInt();
            Date time = new Date();
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(time);
            calendar.setTimeInMillis(calendar.getTimeInMillis() - timeOffset);
            friend.setLastCandidDate(calendar);

            testFriends.add(friend);
        }
        return testFriends;
    }

    /**
     * Test methods
     * TODO: Delete these when done testing
     */
    private void testAlgo() {
        Friend.setTileSizes(mFriends);
        printFriends();
    }

    private void printFriends() {
        for (Friend friend : mFriends) {
            Log.d(TAG, "Friend: " + friend.getUsername() + "     Candids: " + friend.getNumCandids() + "     Last Candid: " + getDate(friend.getLastCandidDate()) + "     Tile Size: " + friend.getTileSize());
        }
    }

    private String getDate(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        return dateFormat.format(calendar.getTime());
    }
}
