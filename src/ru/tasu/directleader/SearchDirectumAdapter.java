package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

class SearchDirectumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "SearchDirectumAdapter";
    private List<Attachment> mDataSet;
    private OnClickListener mClickListenerCallback;
    
    private DirectLeaderApplication mDirect;
    
    private List<Long> checkedIds = new ArrayList<Long>();
    
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ImageView checkImage;
        public ViewHolder(View v) {
            super(v);
            checkImage = (ImageView)v.findViewById(R.id.checkImage);
            nameTextView = (TextView)v.findViewById(R.id.nameTextView);
            nameTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        }
    }
    public void updateCheckedItems(List<Long> ids) {
    	this.checkedIds = ids;
    }
    public SearchDirectumAdapter(DirectLeaderApplication app, List<Attachment> data, OnClickListener clickListenerCallback) {
        mDataSet = data;
        mClickListenerCallback = clickListenerCallback;
        mDirect = app;
    }
    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_search_directum_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        v.setOnClickListener(mClickListenerCallback);
        return vh;
    }
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {
        ViewHolder holder = (ViewHolder)vh;
        final Attachment attach = mDataSet.get(position);
        holder.nameTextView.setText(attach.getName());
    	holder.checkImage.setVisibility(checkedIds.contains(attach.getId()) ? View.VISIBLE : View.INVISIBLE);
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}