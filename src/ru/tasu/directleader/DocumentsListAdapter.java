package ru.tasu.directleader;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DocumentsListAdapter extends ArrayAdapter<Attachment> {
    private static final String TAG = "DocumentsListAdapter";
    
	private ListView listView;
	private DirectLeaderApplication mDirect;
	
	private OnClickListener toolsClickListener;
	
	public DocumentsListAdapter(Context context, List<Attachment> items, ListView listView) {
		super(context, 0, items);
		this.listView = listView;
		mDirect = (DirectLeaderApplication)((Activity)context).getApplication();
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
