package com.app.Connect;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.R;
import com.app.Utils.CandidUtils;

import java.util.ArrayList;

/**
 * Created by brent on 2016-06-22.
 */
public class StickyFriendsAdapter extends RecyclerView.Adapter<StickyFriendsAdapter.StickyFriendsViewHolder>{

    /**
     * Variables
     */
    // Logging tag
    public static final String TAG = "STICKY_FRIEND_ADAPTER";
    // Context
    private Context mContext;
    private View.OnClickListener mItemClickListener;
    // Data
    private ArrayList<Friend> mStickyFriends;

    /**
     * Constructor
     */
    public StickyFriendsAdapter(Context context, View.OnClickListener itemClickListener) {
        mContext = context;
        mItemClickListener = itemClickListener;
        mStickyFriends = getTestData();
    }

    /**
     * Lifecycle functions
     */
    @Override
    public StickyFriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.connect_sticky_friend_item, parent, false);

        // Create view holder
        return new StickyFriendsViewHolder(view, mItemClickListener);
    }

    @Override
    public void onBindViewHolder(StickyFriendsViewHolder holder, int position) {
        final int width = mContext.getResources().getDimensionPixelSize(R.dimen.sticky_friends_item_width);
        final int height = mContext.getResources().getDimensionPixelSize(R.dimen.sticky_friends_height);

        CandidUtils.loadBitmap(mStickyFriends.get(position).getProfilePic(), holder.mProfilePictureImageView, width, height, mContext);
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mStickyFriends.size();
    }

    public Object getItem(int position) {
        return mStickyFriends.get(position);
    }

    /**
     * View holder
     */
    public static class StickyFriendsViewHolder extends RecyclerView.ViewHolder {
        ImageView mProfilePictureImageView;

        public StickyFriendsViewHolder(View itemView, View.OnClickListener clickListener) {
            super(itemView);
            mProfilePictureImageView = (ImageView) itemView.findViewById(R.id.sticky_friend_profile_picture);
            itemView.setOnClickListener(clickListener);
        }
    }

    /**
     * Test data
     */
    private ArrayList<Friend> getTestData() {
        ArrayList<Friend> testFriends = new ArrayList<>();
        for (int idx = 0; idx < 6; idx++) {
            Friend friend = new Friend("Friend", R.drawable.test_sticky_friend);
            testFriends.add(friend);
        }
        return testFriends;
    }
}
