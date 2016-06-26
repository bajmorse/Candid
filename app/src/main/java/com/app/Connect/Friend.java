package com.app.Connect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class Friend implements Serializable {

    /**
     * TileSize enum
     */
    private static final float LARGE_PERCENT = 0.2f, MEDIUM_PERCENT = 0.3f;
    public enum TileSize {
        LARGE, MEDIUM, SMALL;

        @Override
        public String toString() {
            switch (this) {
                case LARGE: return "Large";
                case MEDIUM: return "Medium";
                case SMALL: return "Small";
            }
            return super.toString();
        }
    }
    private TileSize mTileSize;

    /**
     * Variables
     */
    // Serializable constant
    private static final long serialVersionUID = 2025;
    // Friend data
    private String mUsername;
    private int mProfilePic;
    private int mNumCandids;
    private Calendar mLastCandid;

    /**
     * Constructor
     */
    public Friend(String username, int profilePic) {
        mUsername = username;
        mProfilePic = profilePic;
    }

    /**
     * Grid functions
     */
    public static ArrayList<Friend> setTileSizes(ArrayList<Friend> friends) {
        // Sort by size
        Collections.sort(friends, new FriendsCandidCountComparator());

        // Calculate number of friends of each size
        int numFriends = friends.size();
        int numLarge = Math.round(LARGE_PERCENT * numFriends);
        int numMedium = Math.round(MEDIUM_PERCENT * numFriends);

        // Assign sizes
        int idx = 0;
        for (int count = 0; count < numLarge; count++, idx++) {
            friends.get(idx).setTileSize(TileSize.LARGE);
        }
        for (int count = 0; count < numMedium; count++, idx++) {
            friends.get(idx).setTileSize(TileSize.MEDIUM);
        }
        for (; idx < friends.size(); idx++) {
            friends.get(idx).setTileSize(TileSize.SMALL);
        }

        // Sort by most recent
        Collections.sort(friends, new FriendsLastCandidComparator());

        // Return
        return friends;
    }

    /**
     * Comparators
     */
    private static class FriendsCandidCountComparator implements Comparator<Friend> {

        @Override
        public int compare(Friend lhs, Friend rhs) {
            return rhs.getNumCandids() - lhs.getNumCandids();
        }
    }

    private static class FriendsLastCandidComparator implements Comparator<Friend> {

        @Override
        public int compare(Friend lhs, Friend rhs) {
            return rhs.getLastCandidDate().getTime().compareTo(lhs.getLastCandidDate().getTime());
        }
    }

    /**
     * Getters
     */
    public String getUsername() {
        return mUsername;
    }

    public int getProfilePic() {
        return mProfilePic;
    }

    public int getNumCandids() {
        return mNumCandids;
    }

    public Calendar getLastCandidDate() {
        return mLastCandid;
    }

    public TileSize getTileSize() {
        return mTileSize;
    }

    /**
     * Setters
     */
    public void setUsername(String username) {
        mUsername = username;
    }

    public void setProfilePic(int profilePic) {
        mProfilePic = profilePic;
    }

    public void setNumCandids(int numCandids) {
        mNumCandids = numCandids;
    }

    public void setLastCandidDate(Calendar lastCandidDate) {
        mLastCandid = lastCandidDate;
    }

    public void setTileSize(TileSize tileSize) {
        mTileSize = tileSize;
    }
}
