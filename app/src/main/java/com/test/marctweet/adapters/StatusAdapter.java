package com.test.marctweet.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.test.marctweet.R;
import com.test.marctweet.model.Status;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

    private Status[] mStatuses;
    private Context mContext;
    private OnStatusClickedListener mOnClickListener;

    /**
     * Initializes a new instance of the StatusAdapter class
     * @param context The context in which the adapter is going to operate
     * @param statuses The data with which to initialize the adapter
     * @param onClickListener The onClickListener
     */
    public StatusAdapter(Context context, Status[] statuses, OnStatusClickedListener onClickListener) {
        this.mStatuses = statuses;
        this.mContext = context;
        this.mOnClickListener = onClickListener;
    }

    /**
     * Will update the data for this adapter and do a call notifyDataSetChanged()
     * @param statuses the new data to display
     */
    public void setData(Status[] statuses) {
        this.mStatuses = statuses;
        notifyDataSetChanged();
    }

    /**
     * Can be used to retrieve the adapter's status array
     * @return The current data set that the adapter is representing
     */
    public Status[] getData() {
        return this.mStatuses;
    }

    @Override
    public StatusViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_status, viewGroup, false);
        return new StatusViewHolder(view, mOnClickListener);
    }

    @Override
    public void onBindViewHolder(StatusViewHolder viewHolder, int i) {
        Status status = mStatuses[i];
        viewHolder.setStatus(status);

        // NOTE: as an optimisation we could store the result
        // from this instead of doing it on every bind
        viewHolder.mTxtTweetBody.setText(Html.fromHtml(status.text));
        viewHolder.mTxtUserHandle.setText(mContext.getString(R.string.user_handle_format, status.user.screenName));
        Picasso.with(mContext)
                .load(status.user.profileImageUrl)
                .into(viewHolder.mImgProfile);
    }

    @Override
    public int getItemCount() {
        return (null != mStatuses ? mStatuses.length : 0);
    }

    public interface OnStatusClickedListener {
        void onClick(Status status);
    }

    /**
     * ViewHolder class that the RecycleView will use to hold the views for each item.
     * This class extends OnClickListener which allows us to capture the clicking of the view.
     */
    public static class StatusViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mImgProfile;
        private TextView mTxtUserHandle;
        private TextView mTxtTweetBody;
        private Status mStatus;
        private OnStatusClickedListener mClickListener;

        public StatusViewHolder(View itemView, OnStatusClickedListener listener) {
            super(itemView);
            mClickListener = listener;
            mImgProfile = (ImageView) itemView.findViewById(R.id.imgUser);
            mTxtUserHandle = (TextView) itemView.findViewById(R.id.txtUserHandle);
            mTxtTweetBody = (TextView) itemView.findViewById(R.id.txtTweetBody);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mClickListener.onClick(mStatus);
        }

        public void setStatus(Status status) {
            this.mStatus = status;
        }
    }
}
