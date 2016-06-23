package com.app.Connect;

import android.graphics.drawable.Drawable;
import android.media.Image;

/**
 * Created by brent on 2016-06-22.
 */
public class Friend {
    private String mUsername;
    private int mProfilePic;

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
