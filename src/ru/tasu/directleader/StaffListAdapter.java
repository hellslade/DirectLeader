package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class StaffListAdapter extends ArrayAdapter<Rabotnic> implements Filterable {
    private static final String TAG = "StaffListAdapter";
    
	private ListView listView;
	private DirectLeaderApplication mDirect;
	private OnClickListener mClickListener; 
	
	private List<Rabotnic> mOriginalValues; // Original Values
    private List<Rabotnic> mDisplayedValues;    // Values to be displayed
	
	public StaffListAdapter(Context context, List<Rabotnic> items, ListView listView, OnClickListener clickListener) {
		super(context, 0, items);
		this.listView = listView;
		mClickListener = clickListener;
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
                    constraint = constraint.toString().toLowerCase(Utils.mLocale);
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        final Rabotnic user = mOriginalValues.get(i);
                        String data = user.getName();
                        if (data.toLowerCase(Utils.mLocale).contains(constraint.toString())) {
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
	/*public List<Rabotnic> getItems() {
	    List<Rabotnic> staffs = new ArrayList<Rabotnic>();
	    for (int i = 0; i < this.getCount(); i++) {
	        staffs.add(this.getItem(i));
	    }
	    return staffs;
	}*/
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Activity activity = (Activity) getContext();
        final Rabotnic staff = getItem(position);
        // Inflate the views from XML
        View rowView = convertView;
        ViewHolder viewHolder;
    	
    	if (rowView == null) {
    		LayoutInflater inflater = activity.getLayoutInflater();
        	rowView = inflater.inflate(R.layout.list_item_staff_layout, null);
            viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        } else {
        	viewHolder = (ViewHolder) rowView.getTag();
        }
    	
//    	ImageView photoImageView = viewHolder.getStatusImportanceView();
    	TextView fioTextView = viewHolder.getFioTextView();
    	TextView redTextView = viewHolder.getRedTextView();
    	TextView yellowTextView = viewHolder.getYellowTextView();
    	TextView greenTextView = viewHolder.getGreenTextView();
        
    	fioTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    	redTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    	yellowTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    	greenTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    	
    	fioTextView.setText(staff.getName());
    	redTextView.setText(String.valueOf(staff.getOverdueJobs()));
    	yellowTextView.setText(String.valueOf(staff.getCurrentJobs()));
    	greenTextView.setText(String.valueOf(staff.getTotalJobs()));
        
    	View photoLayout = viewHolder.getPhotoLayout();
    	photoLayout.measure(0, 0);
        int height = photoLayout.getMeasuredHeight();
    	
        redTextView.getLayoutParams().width = (int)(height*0.7f);
        redTextView.getLayoutParams().height = height;
        yellowTextView.getLayoutParams().width = (int)(height*0.7f);
        yellowTextView.getLayoutParams().height = height;
        greenTextView.getLayoutParams().width = (int)(height*0.7f);
        greenTextView.getLayoutParams().height = height;
        
        redTextView.setTag(position);
        yellowTextView.setTag(position);
        greenTextView.setTag(position);
        
        redTextView.setOnClickListener(mClickListener);
        yellowTextView.setOnClickListener(mClickListener);
        greenTextView.setOnClickListener(mClickListener);
        
        return rowView;
    }
	private class ViewHolder {
		
		private View baseView;
		
		private ImageView photoImageView; 
		private TextView fioTextView;
		private TextView redTextView;
		private TextView yellowTextView;
		private TextView greenTextView;
		private View photoLayout;
		
		public ViewHolder(View base) {
			this.baseView = base;
		}
		public ImageView getPhotoImageView() {
            if (photoImageView == null) {
                photoImageView = (ImageView)baseView.findViewById(R.id.photoImageView);
            }
            return photoImageView;
        }
	    public TextView getFioTextView() {
	        if (fioTextView == null) {
	            fioTextView = (TextView)baseView.findViewById(R.id.fioTextView);
	        }
	        return fioTextView;
	    }
	    public TextView getRedTextView() {
            if (redTextView == null) {
                redTextView = (TextView)baseView.findViewById(R.id.redTextView);
            }
            return redTextView;
        }
	    public TextView getYellowTextView() {
            if (yellowTextView == null) {
                yellowTextView = (TextView)baseView.findViewById(R.id.yellowTextView);
            }
            return yellowTextView;
        }
	    public TextView getGreenTextView() {
            if (greenTextView == null) {
                greenTextView = (TextView)baseView.findViewById(R.id.greenTextView);
            }
            return greenTextView;
        }
	    public View getPhotoLayout() {
	        if (photoLayout == null) {
	            photoLayout = (View)baseView.findViewById(R.id.photoLayout);
            }
            return photoLayout;
	    }
	}
}
