package ru.tasu.directleader;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TaskListAdapter extends ArrayAdapter<Task> {
    private static final String TAG = "TaskListAdapter";
    
	private ListView listView;
	private DirectLeaderApplication mDirect;
	
	private OnClickListener toolsClickListener;
	
	public TaskListAdapter(Context context, List<Task> items, ListView listView) {
		super(context, 0, items);
		this.listView = listView;
		mDirect = (DirectLeaderApplication)((Activity)context).getApplication();
	}
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Activity activity = (Activity) getContext();
        final Task task = getItem(position);
        // Inflate the views from XML
        View rowView = convertView;
        ViewHolder viewHolder;
    	
    	if (rowView == null) {
    		LayoutInflater inflater = activity.getLayoutInflater();
        	rowView = inflater.inflate(R.layout.list_item_task_layout, null);
            viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        } else {
        	viewHolder = (ViewHolder) rowView.getTag();
        }
    	
    	TextView titleTextView = viewHolder.getTitleTextView();
    	TextView dateTextView = viewHolder.getDateTextView();
    	TextView propertyTextView = viewHolder.getPropertyTextView();
    	ImageView statusImportance = viewHolder.getStatusImportanceView();
    	ImageView statusReaded = viewHolder.getStatusReadedView();
    	ImageView attachmentIcon = viewHolder.getAttachmentView();
    	ImageView discussionIcon = viewHolder.getDiscussionView();
    	
    	titleTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
    	dateTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
    	propertyTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    	
    	String dateString = task.getDeadline(true);
        if (dateString.equalsIgnoreCase("30/12/1899")) {
            dateTextView.setVisibility(View.INVISIBLE);
        } else {
            dateTextView.setVisibility(View.VISIBLE);
        }
    	dateTextView.setTextColor(Color.parseColor(activity.getResources().getString(R.color.gray33)));
    	if (task.isOverdue()) {
    	    dateTextView.setBackgroundResource(R.color.red);
    	    dateTextView.setTextColor(Color.parseColor(activity.getResources().getString(R.color.white)));
    	} else if (task.isCurrent()) {
    	    dateTextView.setBackgroundResource(R.color.yellow);
    	} else {
    	    dateTextView.setBackgroundResource(android.R.color.transparent);
    	}
    	
    	String dateCreatedString = task.getCreated(true);
        if (dateCreatedString.equalsIgnoreCase("30/12/1899")) {
            dateCreatedString = "";
        }
    	String propertyText = String.format(activity.getResources().getString(R.string.task_fragment_listitem_property_text), dateCreatedString, task.getAuthor().getName());
    	
    	titleTextView.setText(task.getTitle());
    	dateTextView.setText(dateString);
    	propertyTextView.setText(propertyText);
    	
        statusReaded.setEnabled(false); // Чтобы иконка оранжевая была
        
        statusImportance.setVisibility(task.getImportance().equalsIgnoreCase("Высокая") ? View.VISIBLE : View.INVISIBLE);
        attachmentIcon.setVisibility(task.getAttachmentCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        discussionIcon.setVisibility(task.getHistoryCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        
        return rowView;
    }
	private class ViewHolder {
		
		private View baseView;
		
		private TextView titleTextView;
		private TextView dateTextView;
		private TextView propertyTextView;
		private ImageView statusImportance; 
		private ImageView statusReaded;
		private ImageView attachmentIcon;
		private ImageView discussionIcon;
		
		public ViewHolder(View base) {
			this.baseView = base;
		}
	    public TextView getTitleTextView() {
	        if (titleTextView == null) {
	            titleTextView = (TextView)baseView.findViewById(R.id.titleTextView);
	        }
	        return titleTextView;
	    }
	    public TextView getDateTextView() {
            if (dateTextView == null) {
                dateTextView = (TextView)baseView.findViewById(R.id.dateTextView);
            }
            return dateTextView;
        }
	    public TextView getPropertyTextView() {
            if (propertyTextView == null) {
                propertyTextView = (TextView)baseView.findViewById(R.id.propertyTextView);
            }
            return propertyTextView;
        }
	    public ImageView getStatusImportanceView() {
            if (statusImportance == null) {
                statusImportance = (ImageView)baseView.findViewById(R.id.statusImportance);
            }
            return statusImportance;
        }
	    public ImageView getStatusReadedView() {
            if (statusReaded == null) {
                statusReaded = (ImageView)baseView.findViewById(R.id.statusReaded);
            }
            return statusReaded;
        }
	    public ImageView getAttachmentView() {
            if (attachmentIcon == null) {
                attachmentIcon = (ImageView)baseView.findViewById(R.id.attachmentIcon);
            }
            return attachmentIcon;
        }
	    public ImageView getDiscussionView() {
            if (discussionIcon == null) {
                discussionIcon = (ImageView)baseView.findViewById(R.id.discussionIcon);
            }
            return discussionIcon;
        }
	}
}
