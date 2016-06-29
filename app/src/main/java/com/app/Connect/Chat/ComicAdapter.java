package com.app.Connect.Chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.R;

import java.util.ArrayList;

/**
 * Created by brent on 2016-06-23.
 */
public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.ComicViewHolder> {

    /**
     * Variables
     */
    // Logging tag
    public static final String TAG = "COMIC_ADAPTER";
    // Data
    ArrayList<PrivateCandid> mComicCandids;
    // Context
    private Context mContext;
    private View.OnClickListener mItemClickListener;
    // View types
    private static final int SENT_COMIC_ITEM = 1;
    private static final int RECEIVED_COMIC_ITEM = 2;

    /**
     * Constructor
     */
    public ComicAdapter(Context context, View.OnClickListener clickListener) {
        mContext = context;
        mItemClickListener = clickListener;
        mComicCandids = ChatFragment.getTestData();
    }

    @Override
    public ComicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case SENT_COMIC_ITEM: {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_comic_sent_item, parent, false);
                break;
            }
            case RECEIVED_COMIC_ITEM: {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_comic_received_item, parent, false);
                break;
            }
        }
        return new ComicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ComicViewHolder holder, int position) {
        PrivateCandid candid = mComicCandids.get(position);
        holder.mCandidImage.setImageResource(candid.getCandidPicture());
        holder.mCandidCaption.setText(candid.getCandidCaption());
    }

    @Override
    public int getItemCount() {
        return mComicCandids.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (mComicCandids.get(position).getCandidSource()) {
            case SENT: {
                return SENT_COMIC_ITEM;
            }
            case RECEIVED: {
                return RECEIVED_COMIC_ITEM;
            }
        }
        return super.getItemViewType(position);
    }

    /**
     * View holder
     */
    public static class ComicViewHolder extends RecyclerView.ViewHolder {
        ImageView mCandidImage;
        TextView mCandidCaption;

        public ComicViewHolder(View itemView) {
            super(itemView);
            mCandidImage = (ImageView) itemView.findViewById(R.id.chats_comic_picture);
            mCandidCaption = (TextView) itemView.findViewById(R.id.chats_comic_caption);
        }
    }
}
