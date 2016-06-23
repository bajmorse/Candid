package com.app.Connect;

import android.graphics.drawable.Drawable;
import android.media.Image;

import java.io.Serializable;

public class Friend implements Serializable {

    /**
     * Variables
     */
    // Serializable constant
    private static final long serialVersionUID = 2025;
    // Friend data
    private String mUsername;
    private int mProfilePic;

    /**
     * Constructor
     */
    public Friend(String username, int profilePic) {
        mUsername = username;
        mProfilePic = profilePic;
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

    /**
     * Setters
     */
    public void setUsername(String username) {
        mUsername = username;
    }

    public void setProfilePic(int profilePic) {
        mProfilePic = profilePic;
    }
}
