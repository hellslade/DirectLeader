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

public class DocumentsListAdapter extends ArrayAdapter<Attachment> implements Filterable {
    private static final String TAG = "DocumentsListAdapter";
    
	private ListView listView;
	private DirectLeaderApplication mDirect;
	
	private List<Attachment> mOriginalValues; // Original Values
    private List<Attachment> mDisplayedValues;    // Values to be displayed
	
	public DocumentsListAdapter(Context context, List<Attachment> items, ListView listView) {
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
    public Attachment getItem(int position) {
        return mDisplayedValues.get(position);
    }
    @Override
    public void remove(Attachment object) {
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
                mDisplayedValues = (ArrayList<Attachment>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<Attachment> FilteredArrList = new ArrayList<Attachment>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<Attachment>(mDisplayedValues); // saves the original data in mOriginalValues
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
                        final Attachment attachment = mOriginalValues.get(i);
                        String data = String.format("%s %s %s", attachment.getName(), attachment.getAuthorName(), attachment.getCreated(true));
                        if (data.toLowerCase().contains(constraint.toString())) {
                            FilteredArrList.add(attachment);
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
        final Attachment attachment = getItem(position);
        // Inflate the views from XML
        View rowView = convertView;
        ViewHolder viewHolder;
    	
    	if (rowView == null) {
    		LayoutInflater inflater = activity.getLayoutInflater();
        	rowView = inflater.inflate(R.layout.list_item_documents_layout, null);
            viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        } else {
        	viewHolder = (ViewHolder) rowView.getTag();
        }
    	
//    	ImageView documentStatusView = viewHolder.getDocumentStatusView();
    	TextView filenameTextView = viewHolder.getFilenameTextView();
    	TextView docPropertyTextView = viewHolder.getDocPropertyTextView();
    	TextView dateTextView = viewHolder.getDateTextView();
        
    	filenameTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        dateTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        docPropertyTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    	
        
        //String size = Utils.formatFileSize(attachment.getSize());
//        Автор: %s, Дата создания: %s, Версия: %s, Расширение: %s
        String docPropString = String.format(activity.getResources().getString(R.string.documents_fragment_listitem_attachment_text), 
                attachment.getAuthorName(), attachment.getCreated(true), attachment.getVersion(), attachment.getExt());
        
    	filenameTextView.setText(attachment.getName());
    	docPropertyTextView.setText(docPropString);
    	dateTextView.setText(attachment.getModified(true));

    	return rowView;
    }
	private class ViewHolder {
		
		private View baseView;
		
		private ImageView documentStatus;
		private TextView filenameTextView;
		private TextView docPropertyTextView;
        private TextView dateTextView;
		
		public ViewHolder(View base) {
			this.baseView = base;
		}
		public ImageView getDocumentStatusView() {
            if (documentStatus == null) {
                documentStatus = (ImageView)baseView.findViewById(R.id.documentStatus);
            }
            return documentStatus;
        }
	    public TextView getFilenameTextView() {
            if (filenameTextView == null) {
                filenameTextView = (TextView)baseView.findViewById(R.id.filenameTextView);
            }
            return filenameTextView;
        }
        public TextView getDocPropertyTextView() {
            if (docPropertyTextView == null) {
                docPropertyTextView = (TextView)baseView.findViewById(R.id.docPropertyTextView);
            }
            return docPropertyTextView;
        }
        public TextView getDateTextView() {
            if (dateTextView == null) {
                dateTextView = (TextView)baseView.findViewById(R.id.dateTextView);
            }
            return dateTextView;
        }
	}
}
