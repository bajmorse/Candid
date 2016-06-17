package com.app;

import android.graphics.drawable.Drawable;
import android.media.Image;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by brent on 2016-06-17.
 */
public class NewsFeedItem {

    // Test Items
    public static ArrayList<NewsFeedItem> getTestNewsFeedItems() {
        ArrayList<NewsFeedItem> testNewsFeedItems = new ArrayList<NewsFeedItem>();
        return testNewsFeedItems;
    }

    // Constants
    private final int MAX_TIME = 15;
    // Variables
    private Drawable mPicture;
    private String mUsername;
    private String mCaption;
    private int mTimeLeft;
    private int mScore;

    public NewsFeedItem(Drawable picture) {
        mPicture = picture;
        mUsername = null;
        mCaption = null;
        mTimeLeft = MAX_TIME;
        mScore = 0;
    }

    /**
     * Getters
     */

    public Drawable getPicture() {
        return mPicture;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getCaption() {
        return mCaption;
    }

    public int getTimeLeft() {
        return mTimeLeft;
    }

    public int getScore() {
        return mScore;
    }
}
