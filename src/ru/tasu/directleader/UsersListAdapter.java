package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

public class UsersListAdapter extends ArrayAdapter<Rabotnic> implements Filterable {
    private static final String TAG = "UsersListAdapter";
    
	private ListView listView;
	private DirectLeaderApplication mDirect;
	
	private List<Rabotnic> mOriginalValues; // Original Values
	private List<Rabotnic> mDisplayedValues;    // Values to be displayed
	
	public UsersListAdapter(Context context, List<Rabotnic> items, ListView listView) {
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
	public Rabotnic getItem(int position) {
	    return mDisplayedValues.get(position);
	}
	@Override
	public void remove(Rabotnic object) {
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
	            mDisplayedValues = (ArrayList<Rabotnic>) results.values; // has the filtered values
	            notifyDataSetChanged();  // notifies the data with new filtered values
	        }
	        @Override
	        protected FilterResults performFiltering(CharSequence constraint) {
	            FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
	            ArrayList<Rabotnic> FilteredArrList = new ArrayList<Rabotnic>();

	            if (mOriginalValues == null) {
	                mOriginalValues = new ArrayList<Rabotnic>(mDisplayedValues); // saves the original data in mOriginalValues
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
	                constraint = constraint.toString().toLowerCase();
	                for (int i = 0; i < mOriginalValues.size(); i++) {
	                    final Rabotnic user = mOriginalValues.get(i);
	                    String data = user.getName();
	                    if (data.toLowerCase().startsWith(constraint.toString())) {
	                        FilteredArrList.add(user);
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
        final Rabotnic staff = getItem(position);
        // Inflate the views from XML
        View rowView = convertView;
        ViewHolder viewHolder;
    	
    	if (rowView == null) {
    		LayoutInflater inflater = activity.getLayoutInflater();
        	rowView = inflater.inflate(R.layout.list_item_users_layout, null);
            viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        } else {
        	viewHolder = (ViewHolder) rowView.getTag();
        }
    	
//    	ImageView photoImageView = viewHolder.getStatusImportanceView();
    	TextView fioTextView = viewHolder.getFioTextView();
    	TextView subtitleTextView = viewHolder.getSubtitleTextView();
        
    	fioTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    	subtitleTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    	
    	fioTextView.setText(staff.getName());
    	subtitleTextView.setText(staff.getPostKind());
        
        return rowView;
    }
	private class ViewHolder {
		
		private View baseView;
		
		private TextView fioTextView;
		private TextView subtitleTextView;
		
		public ViewHolder(View base) {
			this.baseView = base;
		}
	    public TextView getFioTextView() {
	        if (fioTextView == null) {
	            fioTextView = (TextView)baseView.findViewById(R.id.fioTextView);
	        }
	        return fioTextView;
	    }
	    public TextView getSubtitleTextView() {
            if (subtitleTextView == null) {
                subtitleTextView = (TextView)baseView.findViewById(R.id.subtitleTextView);
            }
            return subtitleTextView;
        }
	}
}
