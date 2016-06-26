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
import java.util.Random;

public class NewsFeedThemeAdapter extends RecyclerView.Adapter<NewsFeedThemeAdapter.NewsFeedThemeViewHolder> {

    public static Theme[] THEMES = {Theme.NIGHTLIFE, Theme.SCENERY, Theme.SELFIE, Theme.STAR, Theme.HOT};
    public enum Theme {
        NIGHTLIFE("nightlife", R.color.nightlifeThemeColor, R.color.nightlifeThemeColorTranslucent),
        SCENERY("scenery", R.color.sceneryThemeColor, R.color.sceneryThemeColorTranslucent),
        SELFIE("selfie", R.color.selfieThemeColor, R.color.selfieThemeColorTranslucent),
        STAR("star", R.color.starThemeColor, R.color.starThemeColorTranslucent),
        HOT("hot", R.color.hotThemeColor, R.color.hotThemeColorTranslucent);

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
    public NewsFeedThemeAdapter(Context context, Theme theme) {
        mContext = context;
        mTheme = theme;
        mDataset = getTestData(theme, context);
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
        holder.mCandidImageView.setImageResource(mDataset.get(position).getPictureId());

//        Random rng = new Random();
//        int heartNumber = rng.nextInt(7);
//        int heartResource;
//        switch (heartNumber) {
//            case 0: heartResource = R.drawable.candid_purpleheartwithstars; break;
//            case 1: heartResource = R.drawable.candid_goldheart; break;
//            case 2: heartResource = R.drawable.candid_redheart; break;
//            case 3: heartResource = R.drawable.candid_greenheart; break;
//            case 4: heartResource = R.drawable.candid_blueheart; break;
//            case 5: heartResource = R.drawable.candid_greyheart; break;
//            case 6:default: heartResource = R.drawable.candid_brokenheart;
//        }
//        holder.mCandidHeartView.setImageResource(heartResource);
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
        public ImageView mCandidHeartView;

        public NewsFeedThemeViewHolder(View itemView) {
            super(itemView);
            mCandidImageView = (ImageView) itemView.findViewById(R.id.news_feed_candid_image_view);
            mCaptionTextView = (TextView) itemView.findViewById(R.id.news_feed_caption_text_view);
            mCandidHeartView = (ImageView) itemView.findViewById(R.id.news_feed_candid_heart);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "Clicked item");
        }
    }

    /**
     * Test Data
     */
    public static ArrayList<NewsFeedData> getTestData(Theme theme, Context context) {
        ArrayList<NewsFeedData> testData = new ArrayList<NewsFeedData>();
        for (int i = 0; i < 10; i++) {
            String testPhoto = "theme_" + theme.toString() + "_example";
            int testPhotoResource = context.getResources().getIdentifier(testPhoto, "drawable", context.getPackageName());
            NewsFeedData data = new NewsFeedData(testPhotoResource);
            data.setCaption("Caption");
            testData.add(data);
        }
        return testData;
    }
}
