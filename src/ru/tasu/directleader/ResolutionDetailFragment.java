package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ResolutionDetailFragment extends Fragment implements OnClickListener {
    private static final String TAG = "ResolutionDetailFragment";
    
    class GetResolutionDetailAsyncTask extends AsyncTask<Void, Void, List<ReferenceDetail>> {
        @Override
        protected List<ReferenceDetail> doInBackground(Void... params) {
        	JSONArray ref_details = mTask.getReferenceDetailJSON();
            if (ref_details.length() == 0) {
            	return null;
            }
            mReferenceDetail = new ArrayList<ReferenceDetail>();
            for (int i=0; i<ref_details.length(); i++) {
            	mReferenceDetail.add(new ReferenceDetail(ref_details.optJSONArray(i)));
            }
            return mReferenceDetail;
        }
        @Override
        protected void onPostExecute(List<ReferenceDetail> refs) {
            super.onPostExecute(refs);
            if (refs != null) {
//            	detailAuthorTextView
//            	detailPropertyTextView
//            	detailKeyTextView
//            	detailValueTextView
            
	            for (ReferenceDetail detail : refs) {
	                final LinearLayout detailItemLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.resolution_detail_item_layout, null);
	                final ExpandablePanel panel = (ExpandablePanel) detailItemLayout.findViewById(R.id.expandablePanel);
	                panel.setOnExpandListener(new ExpandablePanel.OnExpandListener() {
	        		    public void onCollapse(View handle, View content) {
	        		    	panel.invalidate();
	        		    }
	        		    public void onExpand(View handle, View content) {
	        		    	panel.invalidate();
	        		    }
	        		});
	                final TextView detailAuthorTextView = (TextView) detailItemLayout.findViewById(R.id.detailAuthorTextView);
	                final TextView detailPropertyTextView = (TextView) detailItemLayout.findViewById(R.id.detailPropertyTextView);
	                detailAuthorTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
	                detailPropertyTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
	                detailAuthorTextView.setText("Имя автора");
	                String propText = getResources().getString(R.string.resolution_detail_fragment_property_text);
	                propText = String.format(propText, "", detail.getDataText(), detail.getPoruchenieText());
	                detailPropertyTextView.setText(propText);
	                final LinearLayout contentLayout = (LinearLayout) detailItemLayout.findViewById(R.id.contentLayout);
	                
	                Map<String, String> data = detail.getData();
	            	String value;
	            	for (String key : data.keySet()) {
	            		value = data.get(key);
		                final LinearLayout itemLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.reference_detail_item_layout, null);
		                final TextView detailKeyTextView = (TextView) itemLayout.findViewById(R.id.detailKeyTextView);
		                final TextView detailValueTextView = (TextView) itemLayout.findViewById(R.id.detailValueTextView);
		                detailKeyTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
		                detailValueTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
		                detailValueTextView.setOnClickListener(detailValueClickListener);
		                detailKeyTextView.setText(key);
		                detailValueTextView.setText(value);
		                itemLayout.setTag(detail);
		                contentLayout.addView(itemLayout);
	            	}
	                referenceDetailLayout.addView(detailItemLayout);
	            }
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
            mReferenceHeader = new ReferenceHeader(ref_header);
            return mReferenceHeader;
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
	                headerKeyTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
	                headerValueTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
	                headerValueTextView.setOnClickListener(headerValueClickListener);
	                headerKeyTextView.setText(key);
	                headerValueTextView.setText(value);
	                itemLayout.setTag(ref_header);
	                referenceHeaderLayout.addView(itemLayout);
            	}
            }
        }
    }
    class SaveReferenceAsyncTask extends AsyncTask<Void, Void, JSONObject> {
    	ProgressDialog pg = new ProgressDialog(getActivity());
    	@Override
    	protected void onPreExecute() {
    		pg.setCancelable(false);
    		pg.setMessage(getResources().getString(R.string.reference_save_progress_message_text));
    		pg.show();
    		super.onPreExecute();
    	}
    	@Override
    	protected JSONObject doInBackground(Void... params) {
    		boolean success = true;
    		JSONObject json = new JSONObject();
    		JSONArray detail = new JSONArray(); // Array of JSONArray of JSONObject
    		JSONArray header = new JSONArray(); // Array of JSONObject
    		
    		Map<String, String> data = mReferenceHeader.getData();
        	String value;
        	for (String key : data.keySet()) {
        		value = data.get(key);
        		final JSONObject pair = new JSONObject();
        		try {
					pair.put("Key", key);
					pair.put("Value", value);
				} catch (JSONException e) {
					success = false;
					e.printStackTrace();
				}
        		header.put(pair);
        	}
        	
        	for (ReferenceDetail ref_detail : mReferenceDetail) {
	        	data = ref_detail.getData();
	        	value = "";
	        	final JSONArray detail_json_array = new JSONArray();
	        	for (String key : data.keySet()) {
	        		value = data.get(key);
	        		final JSONObject pair = new JSONObject();
	        		try {
						pair.put("Key", key);
						pair.put("Value", value);
					} catch (JSONException e) {
						success = false;
						e.printStackTrace();
					}
	        		detail_json_array.put(pair);
	        	}
	        	detail.put(detail_json_array);
        	}
        	
    		try {
				json.put("ReferenceHeader", header);
				json.put("ReferenceDetail", detail);
			} catch (JSONException e) {
				success = false;
				e.printStackTrace();
			}
    		if (success) {
    			return mDirect.PostSaveReference(json);
    		} else {
    			return null;
    		}
    	}
    	protected void onPostExecute(JSONObject json) {
    		if (pg != null) {
    			pg.dismiss();
    		}
    		if (json != null) {
    			final int statusCode = json.optInt("statusCode");
    			if (statusCode == 200) {
    				// Получен успешный ответ от сервера, теперь нужно проверить переменную result
    				boolean result = json.optBoolean("result");
    				final String message = json.optString("Message");
    				AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
    				if (result) {
    					// Сохранение резолюции прошло успешно
    					alertDialog.setTitle("Успешно");
    				}
    				// В любом случае показать диалоговое окно с результатом.
    				alertDialog.setTitle("Ошибка");
    				alertDialog.setMessage(message);
    				alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
    				        public void onClick(DialogInterface dialog, int which) {
    				            dialog.dismiss();
    				        }
				    });
    				alertDialog.show();
    			}
    		} else {
    			final String text = getResources().getString(R.string.reference_save_progress_nointernet_message_text);
    			Toast.makeText(mDirect, text, Toast.LENGTH_LONG).show();
    		}
    		super.onPostExecute(json);
    	};
    }
    
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    private OnOpenFragmentListener mListener;
    
    public static final String TASK_KEY = "task_key";
    private Task mTask = null;
    
    private ReferenceHeader mReferenceHeader = null;
    private List<ReferenceDetail> mReferenceDetail = null;
    
    private ImageButton saveButton;
//    private TextView taskTitleTextView, propertyTextView;
    private LinearLayout referenceHeaderLayout, referenceDetailLayout;
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
    	referenceDetailLayout = (LinearLayout) v.findViewById(R.id.referenceDetailLayout);
    	saveButton = (ImageButton) v.findViewById(R.id.saveButton);
    	saveButton.setOnClickListener(this);
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
	OnClickListener detailValueClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			TextView view = (TextView) v;
			Log.v(TAG, "view text " + view.getText().toString());
		}
	}; 
    @Override
    public void onClick(View v) {
    	switch (v.getId()) {
    		case R.id.saveButton:
    			new SaveReferenceAsyncTask().execute();
    			break;
    	}
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
