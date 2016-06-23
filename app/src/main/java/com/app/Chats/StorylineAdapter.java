package com.app.Chats;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.R;

import java.util.ArrayList;

/**
 * Created by brent on 2016-06-23.
 */
public class StorylineAdapter extends RecyclerView.Adapter<StorylineAdapter.StorylineViewHolder> {

    /**
     * Variables
     */
    // Logging tag
    public static final String TAG = "STORYLINE_ADAPTER";
    // Data
    ArrayList<Candid> mStorylineCandids;
    // Context
    private Context mContext;
    private View.OnClickListener mItemClickListener;

    /**
     * Constructor
     */
    public StorylineAdapter(Context context, View.OnClickListener itemClickListener) {
        mContext = context;
        mItemClickListener = itemClickListener;
        mStorylineCandids = ChatsFragment.getTestData();
    }

    @Override
    public StorylineAdapter.StorylineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_storyline_item, parent, false);

        // Create view holder
        return new StorylineViewHolder(view, mItemClickListener);
    }

    @Override
    public void onBindViewHolder(StorylineAdapter.StorylineViewHolder holder, int position) {
        Candid candid = mStorylineCandids.get(position);
        holder.mCandidImage.setImageResource(candid.getCandidPicture());
        switch (candid.getCandidSource()) {
            case SENT: {
                holder.mCandidSourceIndicator.setBackgroundColor(mContext.getResources().getColor(R.color.blue));
                break;
            }
            case RECEIVED: {
                holder.mCandidSourceIndicator.setBackgroundColor(mContext.getResources().getColor(R.color.red));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mStorylineCandids.size();
    }

    /**
     * View holder
     */
    public static class StorylineViewHolder extends RecyclerView.ViewHolder {
        ImageView mCandidImage;
        View mCandidSourceIndicator;

        public StorylineViewHolder(View itemView, View.OnClickListener clickListener) {
            super(itemView);
            mCandidImage = (ImageView) itemView.findViewById(R.id.chats_storyline_thumbnail);
            mCandidSourceIndicator = itemView.findViewById(R.id.chats_storyline_thumbnail_sender_indicator);
            itemView.setOnClickListener(clickListener);
        }
    }
}