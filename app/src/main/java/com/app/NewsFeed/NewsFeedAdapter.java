package com.app.NewsFeed;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.R;

public class NewsFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * Variables
     */
    // Logger tag
    public static final String TAG = "NewsFeedAdapter";
    // Context
    private Context mContext;
    // View types
    private static final int HEADER_VIEW = 1;
    private static final int NORMAL_VIEW = 2;
    private static final int FOOTER_VIEW = 3;

    /**
     * Lifecycle functions
     */
    public NewsFeedAdapter(Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case HEADER_VIEW: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_feed_header, null);
                return new NewsFeedHeaderViewHolder(view);
            }
            case FOOTER_VIEW: {
                // No footer at the moment
            }
            case NORMAL_VIEW:
            default: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_feed_item, null);
                return new NewsFeedViewHolder(view);
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NewsFeedHeaderViewHolder) {
            NewsFeedHeaderViewHolder viewHolder = (NewsFeedHeaderViewHolder) holder;
        } else if (holder instanceof NewsFeedViewHolder) {
            NewsFeedViewHolder viewHolder = (NewsFeedViewHolder) holder;

            // Set theme color
            int themeColor = NewsFeedThemeAdapter.THEMES[position-1].getColor();
            viewHolder.mNewsFeedTheme.setBackgroundColor(mContext.getResources().getColor(themeColor));

            // Setup layout manager
            viewHolder.mNewsFeedThemeLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            viewHolder.mNewsFeedTheme.setLayoutManager(viewHolder.mNewsFeedThemeLayoutManager);

            // Setup adapter
            viewHolder.mNewsFeedThemeAdapter = new NewsFeedThemeAdapter(mContext, NewsFeedThemeAdapter.THEMES[position-1], NewsFeedData.getTestData());
            viewHolder.mNewsFeedTheme.setAdapter(viewHolder.mNewsFeedThemeAdapter);
        }
    }

    @Override
    public int getItemCount() {
        return NewsFeedThemeAdapter.THEMES.length + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return HEADER_VIEW;
        return NORMAL_VIEW;
    }

    /**
     * View holders
     */
    public static class NewsFeedViewHolder extends RecyclerView.ViewHolder {
        public RecyclerView mNewsFeedTheme;
        private RecyclerView.Adapter mNewsFeedThemeAdapter;
        private RecyclerView.LayoutManager mNewsFeedThemeLayoutManager;

        public NewsFeedViewHolder(View itemView) {
            super(itemView);
            mNewsFeedTheme = (RecyclerView) itemView.findViewById(R.id.news_feed_theme);
        }
    }

    public static class NewsFeedHeaderViewHolder extends RecyclerView.ViewHolder {
        public NewsFeedHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}
