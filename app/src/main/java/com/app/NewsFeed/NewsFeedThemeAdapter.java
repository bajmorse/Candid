package com.app.NewsFeed;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.R;

import java.util.ArrayList;

public class NewsFeedThemeAdapter extends RecyclerView.Adapter<NewsFeedThemeAdapter.NewsFeedThemeViewHolder> {

    public static Theme[] THEMES = {Theme.NIGHTLIFE, Theme.SCENERY, Theme.SELFIE, Theme.STAR, Theme.HOT};
    public enum Theme {
        NIGHTLIFE("Nightlife", R.color.nightlifeThemeColor, R.color.nightlifeThemeColorTranslucent),
        SCENERY("Scenery", R.color.sceneryThemeColor, R.color.sceneryThemeColorTranslucent),
        SELFIE("Selfie", R.color.selfieThemeColor, R.color.selfieThemeColorTranslucent),
        STAR("Star", R.color.starThemeColor, R.color.starThemeColorTranslucent),
        HOT("Hot", R.color.hotThemeColor, R.color.hotThemeColorTranslucent);

        private String mThemeTitle;
        private int mColor;
        private int mTransparentColor;
        private Theme(String theme, int color, int transparentColor) {
            mThemeTitle = theme;
            mColor = color;
            mTransparentColor = transparentColor;
        }

        @Override
        public String toString() {
            return mThemeTitle;
        }

        public int getColor() {
            return mColor;
        }

        public int getTransparentColor() {
            return mTransparentColor;
        }
    }

    /**
     * Variables
     */
    // Logging tag
    public static final String TAG = "HorizontalRecyclerView";
    // Context
    private Context mContext;
    // Data
    private ArrayList<NewsFeedData> mDataset;
    // Theme
    private Theme mTheme;

    /**
     * Constructor
     */
    public NewsFeedThemeAdapter(Context context, Theme theme, ArrayList<NewsFeedData> dataset) {
        mContext = context;
        mTheme = theme;
        mDataset = dataset;
    }

    /**
     * Lifecycle functions
     */
    @Override
    public NewsFeedThemeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate item view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_feed_theme_item, parent, false);

        // Set data holder
        return new NewsFeedThemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsFeedThemeViewHolder holder, int position) {
        holder.mCandidImageView.setBackgroundColor(mTheme.getColor());
    }

    /**
     * Data functions
     */
    public void addItem(NewsFeedData data, int index) {
        mDataset.add(data);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    /**
     * View holders
     */
    public static class NewsFeedThemeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView mCandidImageView;
        public TextView mCaptionTextView;

        public NewsFeedThemeViewHolder(View itemView) {
            super(itemView);
            mCandidImageView = (ImageView) itemView.findViewById(R.id.news_feed_candid_image_view);
            mCaptionTextView = (TextView) itemView.findViewById(R.id.news_feed_caption_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "Clicked item");
        }
    }
}
