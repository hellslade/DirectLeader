package ru.tasu.directleader;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class JobMyListAdapter extends ArrayAdapter<Job> {
    private static final String TAG = "JobImportantListAdapter";
    
	private ListView listView;
	private DirectLeaderApplication mDirect;
	
	private OnClickListener toolsClickListener;
	
	public JobMyListAdapter(Context context, List<Job> items, ListView listView) {
		super(context, 0, items);
		this.listView = listView;
		mDirect = (DirectLeaderApplication)((Activity)context).getApplication();
	}
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Activity activity = (Activity) getContext();
        final Job job = getItem(position);
        // Inflate the views from XML
        View rowView = convertView;
        ViewHolder viewHolder;
    	
    	int type = getItemViewType(position);
    	if (rowView == null) {
    		LayoutInflater inflater = activity.getLayoutInflater();
        	rowView = inflater.inflate(R.layout.list_item_job_important_layout, null);
            viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        } else {
        	viewHolder = (ViewHolder) rowView.getTag();
        }
    	
    	TextView titleTextView = viewHolder.getTitleTextView();
    	TextView dateTextView = viewHolder.getDateTextView();
    	TextView dateCreatedTextView = viewHolder.getDateCreatedTextView();
    	TextView attachmentTextView = viewHolder.getAttachmentTextView();
        TextView subtaskTextView = viewHolder.getSubtaskTextView();
        TextView usernameTextView = viewHolder.getUsernameTextView();
        
        titleTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        dateTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        dateCreatedTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        attachmentTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        subtaskTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        usernameTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
    	
    	dateTextView.setTextColor(Color.parseColor(activity.getResources().getString(R.color.gray33)));
    	if (job.isOverdue()) {
    	    dateTextView.setBackgroundResource(R.color.red);
    	    dateTextView.setTextColor(Color.parseColor(activity.getResources().getString(R.color.white)));
    	} else if (job.isCurrent()) {
    	    dateTextView.setBackgroundResource(R.color.yellow);
    	} else {
    	    dateTextView.setBackgroundResource(android.R.color.transparent);
    	}
    	
    	// Делать здесь запрос в БД не вариант, тормозить интерфейс будет. Нужно в класс Job добавить дополнительные поля 
        int acount = job.getAttachmentCount();
        int scount = job.getSubtaskCount();
        String attachmentCount = String.format(activity.getResources().getString(R.string.myjob_fragment_listitem_attachment_text), acount);
        String subtaskCount = String.format(activity.getResources().getString(R.string.myjob_fragment_listitem_subtask_text), scount);
        
    	titleTextView.setText(job.getSubject());
    	dateTextView.setText(job.getEndDate(true));
    	dateCreatedTextView.setText(job.getStartDate(true));
    	attachmentTextView.setText(attachmentCount);
        subtaskTextView.setText(subtaskCount);
        usernameTextView.setText(job.getUser().getName());
        
        if (acount == 0) {
            attachmentTextView.setVisibility(View.GONE);
        } else {
            attachmentTextView.setVisibility(View.VISIBLE);
        }
        if (scount == 0) {
            subtaskTextView.setVisibility(View.GONE);
        } else {
            subtaskTextView.setVisibility(View.VISIBLE);
        }
        
        return rowView;
    }
	private class ViewHolder {
		
		private View baseView;
		
		private TextView titleTextView;
		private TextView dateTextView;
		private TextView dateCreatedTextView;
		private TextView attachmentTextView;
        private TextView subtaskTextView;
        private TextView usernameTextView;
		
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
	    public TextView getDateCreatedTextView() {
            if (dateCreatedTextView == null) {
                dateCreatedTextView = (TextView)baseView.findViewById(R.id.dateCreatedTextView);
            }
            return dateCreatedTextView;
        }
        public TextView getUsernameTextView() {
            if (usernameTextView == null) {
                usernameTextView = (TextView)baseView.findViewById(R.id.usernameTextView);
            }
            return usernameTextView;
        }
        public TextView getAttachmentTextView() {
            if (attachmentTextView == null) {
                attachmentTextView = (TextView)baseView.findViewById(R.id.attachmentTextView);
            }
            return attachmentTextView;
        }
        public TextView getSubtaskTextView() {
            if (subtaskTextView == null) {
                subtaskTextView = (TextView)baseView.findViewById(R.id.subtaskTextView);
            }
            return subtaskTextView;
        }
	}
}
