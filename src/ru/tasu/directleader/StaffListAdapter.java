package ru.tasu.directleader;

import java.util.ArrayList;
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

public class StaffListAdapter extends ArrayAdapter<Rabotnic> {
    private static final String TAG = "StaffListAdapter";
    
	private ListView listView;
	private DirectLeaderApplication mDirect;
	
	public StaffListAdapter(Context context, List<Rabotnic> items, ListView listView) {
		super(context, 0, items);
		this.listView = listView;
		mDirect = (DirectLeaderApplication)((Activity)context).getApplication();
	}
	public List<Rabotnic> getItems() {
	    List<Rabotnic> staffs = new ArrayList<Rabotnic>();
	    for (int i = 0; i < this.getCount(); i++) {
	        staffs.add(this.getItem(i));
	    }
	    return staffs;
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
    	
        redTextView.getLayoutParams().width = height;
        redTextView.getLayoutParams().height = height;
        yellowTextView.getLayoutParams().width = height;
        yellowTextView.getLayoutParams().height = height;
        greenTextView.getLayoutParams().width = height;
        greenTextView.getLayoutParams().height = height;
        
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
