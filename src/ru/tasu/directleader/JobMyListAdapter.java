package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class JobMyListAdapter extends ArrayAdapter<Job> implements Filterable {
    private static final String TAG = "JobImportantListAdapter";
    
	private ListView listView;
	private DirectLeaderApplication mDirect;
	
	private List<Job> mOriginalValues; // Original Values
    private List<Job> mDisplayedValues;    // Values to be displayed
	
	public JobMyListAdapter(Context context, List<Job> items, ListView listView) {
		super(context, 0, items);
		this.listView = listView;
		this.mOriginalValues = items;
        this.mDisplayedValues = items;
		mDirect = (DirectLeaderApplication)((Activity)context).getApplication();
	}
	@Override
    public int getCount() {
        return mDisplayedValues.size();
    }
    @Override
    public Job getItem(int position) {
        return mDisplayedValues.get(position);
    }
    @Override
    public void remove(Job object) {
        mDisplayedValues.remove(object);
        mOriginalValues.remove(object);
        super.remove(object);
    }
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mDisplayedValues = (ArrayList<Job>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<Job> FilteredArrList = new ArrayList<Job>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<Job>(mDisplayedValues); // saves the original data in mOriginalValues
                }
                /**
                 * 
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)  
                 *
                 **/
                if (constraint == null || constraint.length() == 0) {
                    // set the Original result to return  
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    constraint = constraint.toString().toLowerCase(Utils.mLocale);
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        final Job job = mOriginalValues.get(i);
                        String data = String.format("%s %s %s %s %s %s", job.getSubject(), job.getUser().getName(), job.getStateTitle(), job.getAuthor().getName(), job.getResultTitle(), job.getImportance());
                        if (data.toLowerCase(Utils.mLocale).contains(constraint.toString())) {
                            FilteredArrList.add(job);
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Activity activity = (Activity) getContext();
        final Job job = getItem(position);
        // Inflate the views from XML
        View rowView = convertView;
        ViewHolder viewHolder;
    	
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
    	TextView propertyTextView = viewHolder.getPropertyTextView();
        ImageView statusImportance = viewHolder.getStatusImportanceView();
        ImageView statusReaded = viewHolder.getStatusReadedView();
        ImageView attachmentIcon = viewHolder.getAttachmentView();
        
        titleTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        dateTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        propertyTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    	
        String dateString = job.getFinalDate(true);
        if (dateString.equalsIgnoreCase("1899-12-30")) {
            dateTextView.setVisibility(View.INVISIBLE);
        } else {
            dateTextView.setVisibility(View.VISIBLE);
        }
    	dateTextView.setTextColor(Color.parseColor(activity.getResources().getString(R.color.gray33)));
    	if (job.isOverdue()) {
    	    dateTextView.setBackgroundResource(R.color.red);
    	    dateTextView.setTextColor(Color.parseColor(activity.getResources().getString(R.color.white)));
    	} else if (job.isCurrent()) {
    	    dateTextView.setBackgroundResource(R.color.yellow);
    	} else {
    	    dateTextView.setBackgroundResource(android.R.color.transparent);
    	}
    	
    	String startDateString = job.getStartDate(true);
        if (startDateString.equalsIgnoreCase("1899-12-30")) {
            startDateString = "";
        }
        if (job.getAuthor() != null) {
        	String propertyText = String.format(activity.getResources().getString(R.string.myjob_fragment_listitem_property_text), startDateString, job.getAuthor().getName());
        	propertyTextView.setText(propertyText);
        }
        
    	titleTextView.setText(job.getSubject());
    	dateTextView.setText(dateString);
        
        if (job.getImportance().equalsIgnoreCase("Высокая")) {
            statusImportance.setVisibility(View.VISIBLE);
        } else {
            statusImportance.setVisibility(View.INVISIBLE);
        }
        statusReaded.setEnabled(job.getReaded());
        
        attachmentIcon.setVisibility(job.getAttachmentCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        
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
	}
}
