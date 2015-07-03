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
    
    private DirectLeaderApplication mDirect;
    
    private OnItemClickListener mOnItemClickListener;
    
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        private Attachment mAttachment;
        public ViewHolder(View v) {
            super(v);
            nameTextView = (TextView)v.findViewById(R.id.nameTextView);
            nameTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
            nameTextView.setClickable(false);
            /*v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mAttachment);
                    }
                }
            });*/
        }
        public void bind(Attachment attachment) {
            mAttachment = attachment;
        }
        
    }

    public TaskAttachmentAdapter(DirectLeaderApplication app, List<Attachment> data) {
        mDataSet = data;
        mDirect = app;
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
        holder.bind(attach);
        if (attach.getId() == -1) { // local file
        	holder.nameTextView.setText(attach.getCTitle());
        } else { // Directum file
        	holder.nameTextView.setText(attach.getName());
        }
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }
    public static interface OnItemClickListener {
        public void onItemClick(Attachment attachment);
    }
}