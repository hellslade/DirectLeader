package ru.tasu.directleader;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ResolutionDetailFragment extends Fragment implements OnClickListener {
    private static final String TAG = "ResolutionDetailFragment";
    
    class GetResolutionDetailAsyncTask extends AsyncTask<Void, Void, ReferenceDetail[]> {
        @Override
        protected ReferenceDetail[] doInBackground(Void... params) {
        	JSONArray ref_details = mTask.getReferenceDetailJSON();
            if (ref_details.length() == 0) {
            	return null;
            }
            ReferenceDetail[] refs = new ReferenceDetail[ref_details.length()];
            for (int i=0; i<ref_details.length(); i++) {
            	refs[i] = new ReferenceDetail(ref_details.optJSONArray(i));
            }
            return refs;
        }
        @Override
        protected void onPostExecute(ReferenceDetail[] refs) {
            super.onPostExecute(refs);
            if (refs != null) {
            	
            }
            /*
            for (Attachment a : attachments) {
                final RelativeLayout itemLayout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.document_item_layout, null);
                final TextView documentNameTextView = (TextView) itemLayout.findViewById(R.id.documentNameTextView);
                documentNameTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
                documentNameTextView.setText(a.getName());
                itemLayout.setTag(a);
                itemLayout.setOnClickListener(documentClickListener);
                documentsLayout.addView(itemLayout);
            }
            if (attachments.length == 0) {
                documentsListLabel.setVisibility(View.GONE);
            }
            //*/
        }
    }
    class GetResolutionHeaderAsyncTask extends AsyncTask<Void, Void, ReferenceHeader> {
        @Override
        protected ReferenceHeader doInBackground(Void... params) {
            JSONArray ref_header = mTask.getReferenceHeaderJSON();
            if (ref_header.length() == 0) {
            	return null;
            }
            return new ReferenceHeader(ref_header);
        }
        @Override
        protected void onPostExecute(ReferenceHeader ref_header) {
            super.onPostExecute(ref_header);
            
            if (ref_header != null) {
            	Map<String, String> data = ref_header.getData();
            	String value;
            	for (String key : data.keySet()) {
            		value = data.get(key);
	                final LinearLayout itemLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.reference_header_item_layout, null);
	                final TextView headerKeyTextView = (TextView) itemLayout.findViewById(R.id.headerKeyTextView);
	                final TextView headerValueTextView = (TextView) itemLayout.findViewById(R.id.headerValueTextView);
	                headerKeyTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
	                headerValueTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
	                headerValueTextView.setOnClickListener(headerValueClickListener);
	                headerKeyTextView.setText(key);
	                headerValueTextView.setText(value);
	                itemLayout.setTag(ref_header);
	                referenceHeaderLayout.addView(itemLayout);
            	}
            } else {
//            	resolutionListLabel.setVisibility(View.GONE);
            }
        }
    }
    //*/
    
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    private OnOpenFragmentListener mListener;
    
    public static final String TASK_KEY = "task_key";
    private Task mTask = null;
    private List<ReferenceDetail> mReferenceDetail = null;
    
    private Button saveButtonView;
    private TextView taskTitleTextView, propertyTextView;
    private LinearLayout referenceHeaderLayout;
    // 
    
    public static Fragment newInstance(Bundle args) {  // must pass some args
        ResolutionDetailFragment f = new ResolutionDetailFragment();
        f.setArguments(args);
        return f;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDirect = (DirectLeaderApplication) getActivity().getApplication();
        mSettings = mDirect.getSettings();
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_resolution_detail, container, false);
        
        ((ImageView)rootView.findViewById(R.id.homeImageView)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        });
        ((ImageView)rootView.findViewById(R.id.newTaskButton)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.OnTaskCreate();
                }
            }
        });
        
        Bundle args = getArguments();
        mTask = args.getParcelable(TASK_KEY);
        if (mTask == null) {
            // Закрыть фрагмент?
        }
        Log.v(TAG, "mTask ");
        
//        taskTitleTextView = (TextView)rootView.findViewById(R.id.taskTitleTextView);
//        propertyTextView = (TextView)rootView.findViewById(R.id.propertyTextView);
        
        initViews(rootView);
        
        setFonts();
        updateData();
        return rootView;
    }
    private void initViews(View v) {
    	referenceHeaderLayout = (LinearLayout) v.findViewById(R.id.referenceHeaderLayout);
//        ((TextView) v.findViewById(R.id.performerLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
//        ((TextView) v.findViewById(R.id.dateLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
//        ((TextView) v.findViewById(R.id.stateLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
//        ((TextView) v.findViewById(R.id.importanceLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
//        ((TextView) v.findViewById(R.id.flagLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
//        ((TextView) v.findViewById(R.id.documentsLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
//        ((TextView) v.findViewById(R.id.startDateLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
//        ((TextView) v.findViewById(R.id.commentLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
    }
    private void setFonts() {
//        taskTitleTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
//        propertyTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        
//        for (TextView tv : new TextView[]{performerTextView, dateTextView, stateTextView, importanceTextView,
//                flagTextView, documentsTextView, startDateTextView}) {
//            tv.setTypeface(mDirect.mPFDinDisplayPro_Reg);
//        }
    }
    
    private void updateData() {
        new GetResolutionDetailAsyncTask().execute();
    	new GetResolutionHeaderAsyncTask().execute();
    }
    OnClickListener headerValueClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			TextView view = (TextView) v;
			Log.v(TAG, "view text " + view.getText().toString());
		}
	}; 
    @Override
    public void onClick(View v) {
        if (mListener != null) {
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnOpenFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnLoginListener");
        }
    }
}
