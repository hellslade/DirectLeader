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
    
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ViewHolder(View v) {
            super(v);
            nameTextView = (TextView)v.findViewById(R.id.nameTextView);
            nameTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
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
        holder.nameTextView.setText(attach.getName());
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}