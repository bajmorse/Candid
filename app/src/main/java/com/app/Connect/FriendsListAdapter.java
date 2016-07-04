package com.app.Connect;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.app.R;
import com.app.Utils.CandidUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendsListViewHolder>{

    /**
     * Variables
     */
    // Logging tag
    public static final String TAG = "FRIENDS_LIST_ADAPTER";
    // Context
    private Context mContext;
    private View.OnClickListener mItemClickListener;
    // Data
    private ArrayList<Friend> mFriends;
    // Tile sizes
    private static final Point LARGE_SIZE = new Point(178, 270);
    private static final Point MEDIUM_SIZE = new Point(90, 135);
    private static final Point SMALL_SIZE = new Point(43, 67);

    /**
     * Constructor
     */
    public FriendsListAdapter(Context context, View.OnClickListener clickListener) {
        mContext = context;
        mItemClickListener = clickListener;
        mFriends = getTestData();
    }

    /**
     * Lifecycle functions
     */
    @Override
    public FriendsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.connect_friend_list_item, parent, false);

        // Create and return view holder
        return new FriendsListViewHolder(view, mItemClickListener);
    }

    @Override
    public void onBindViewHolder(FriendsListViewHolder holder, int position) {
        // Get friend and set view tag
        Friend friend = mFriends.get(position);
        holder.itemView.setTag(position);

//        // Resize view based on friend's correspondence level
//        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
//        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.mFriendView.getLayoutParams();
//        switch (friend.getTileSize()) {
//            case LARGE: {
//                layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, LARGE_SIZE.x, displayMetrics);
//                layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, LARGE_SIZE.y, displayMetrics);
//                break;
//            }
//            case MEDIUM: {
//                layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MEDIUM_SIZE.x, displayMetrics);
//                layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MEDIUM_SIZE.y, displayMetrics);
//                break;
//            }
//            case SMALL: {
//                layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SMALL_SIZE.x, displayMetrics);
//                layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SMALL_SIZE.y, displayMetrics);
//            }
//        }

        // Set friend's photo
        final int width = mContext.getResources().getDimensionPixelSize(R.dimen.friends_list_item_width);
        final int height = mContext.getResources().getDimensionPixelSize(R.dimen.friends_list_item_height);
        CandidUtils.loadBitmap(friend.getProfilePic(), holder.mProfilePictureImageView, width, height, mContext);
    }

    @Override
    public int getItemCount() {
        return mFriends.size();
    }

    public Object getItem(int position) {
        return mFriends.get(position);
    }

    /**
     * View holder
     */
    public static class FriendsListViewHolder extends RecyclerView.ViewHolder {
        ImageView mProfilePictureImageView;

        public FriendsListViewHolder(View itemView, View.OnClickListener clickListener) {
            super(itemView);
            mProfilePictureImageView = (ImageView) itemView.findViewById(R.id.friends_list_profile_picture);
            itemView.setOnClickListener(clickListener);
        }
    }

    /**
     * Test data
     */
    private ArrayList<Friend> getTestData() {
        ArrayList<Friend> testFriends = new ArrayList<>();
        for (int idx = 0; idx < 20; idx++) {
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
        Friend.setTileSizes(testFriends);
        return testFriends;
    }

    /**
     * Test methods
     * TODO: Delete these when done testing
     */
    private void testAlgo() {
    }

    private void printFriends() {
        for (Friend friend : mFriends) {
            Log.d(TAG, "Friend: " + friend.getUsername() + "     Candids: " + friend.getNumCandids() + "     Last PrivateCandid: " + getDate(friend.getLastCandidDate()) + "     Tile Size: " + friend.getTileSize());
        }
    }

    private String getDate(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        return dateFormat.format(calendar.getTime());
    }
}
