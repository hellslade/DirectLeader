package ru.tasu.directleader;

import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

class TaskAttachmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "TaskAttachmentAdapter";
    private List<Attachment> mDataSet;
    private SwipeDismissTouchListener.DismissCallbacks mSwipeCallback;
    
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ViewHolder(View v) {
            super(v);
            nameTextView = (TextView)v.findViewById(R.id.nameTextView);
            nameTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            // Create a generic swipe-to-dismiss touch listener.
            if (mSwipeCallback != null) {
                nameTextView.setOnTouchListener(new SwipeDismissTouchListener(nameTextView, null, mSwipeCallback));
            }
        }
    }

    public TaskAttachmentAdapter(List<Attachment> data, SwipeDismissTouchListener.DismissCallbacks swipeCallback) {
        mDataSet = data;
        mSwipeCallback = swipeCallback;
    }
    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_task_attachment_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {
        ViewHolder holder = (ViewHolder)vh;
        final Attachment attach = mDataSet.get(position);
        holder.nameTextView.setText(attach.getName());
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}