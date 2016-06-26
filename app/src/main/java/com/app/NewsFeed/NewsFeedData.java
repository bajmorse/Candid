package com.app.NewsFeed;

import android.graphics.drawable.Drawable;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by brent on 2016-06-17.
 */
public class NewsFeedData {

    // Test Items
    public static ArrayList<NewsFeedData> getTestNewsFeedItems() {
        ArrayList<NewsFeedData> testNewsFeedData = new ArrayList<NewsFeedData>();
        return testNewsFeedData;
    }

    // Constants
    private final int MAX_TIME = 15;
    // Variables
    private int mPictureId;
    private String mUsername;
    private String mCaption;
    private int mTimeLeft;
    private int mScore;

    public NewsFeedData(int pictureId) {
        mPictureId = pictureId;
    }

    /**
     * Getters
     */
    public int getPictureId() {
        return mPictureId;
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

    /**
     * Setters
     */
    public void setPictureId(int pictureId) {
        mPictureId = pictureId;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }
}
