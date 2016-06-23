package com.app.Chats;

import android.graphics.drawable.Drawable;

import com.app.R;

import java.io.Serializable;

/**
 * Created by brent on 2016-06-23.
 */
public class Candid implements Serializable {

    /**
     * Variables
     */
    // Serializable constant
    private static final long serialVersionUID = 2520;
    // Candid information
    public enum CandidSource {
        SENT, RECEIVED
    }
    private CandidSource mCandidSource;
    private int mCandidPicture;

    /**
     * Constructor
     */
    public Candid(int candidPicture, CandidSource candidSource) {
        mCandidPicture = candidPicture;
        mCandidSource = candidSource;
    }

    /**
     * Getters
     */
    public int getCandidPicture() {
        return mCandidPicture;
    }

    public CandidSource getCandidSource() {
        return mCandidSource;
    }

    /**
     * Setters
     */
    public void setCandidPicture(int candidPicture) {
        mCandidPicture = candidPicture;
    }

    public void setCandidSource(CandidSource candidSource) {
        mCandidSource = candidSource;
    }
}
