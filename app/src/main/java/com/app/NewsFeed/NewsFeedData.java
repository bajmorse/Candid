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
    private Drawable mPicture;
    private String mUsername;
    private String mCaption;
    private int mTimeLeft;
    private int mScore;

    public NewsFeedData() {
        mPicture = null;
        mUsername = null;
        mCaption = null;
        mTimeLeft = MAX_TIME;
        mScore = 0;
    }

    public NewsFeedData(Drawable picture) {
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

    /**
     * Setters
     */
    public void setPicture(Drawable picture) {
        mPicture = picture;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    /**
     * Test Data
     */
    public static ArrayList<NewsFeedData> getTestData() {
        ArrayList<NewsFeedData> testData = new ArrayList<NewsFeedData>();
        for (int i = 0; i < 10; i++) {
            NewsFeedData data = new NewsFeedData();
            data.setCaption("Caption");
            testData.add(data);
        }
        return testData;
    }
}
