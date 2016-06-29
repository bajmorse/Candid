package com.app.NewsFeed;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by brent on 2016-06-17.
 */
public class Candid implements Serializable {

    // Constants
    private final int MAX_TIME = 15;
    private static final long serialVersionUID = 2520;
    // Variables
    private int mPictureId;
    private String mUsername;
    private String mCaption;
    private int mTimeLeft;
    private int mScore;

    public Candid(int pictureId) {
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
