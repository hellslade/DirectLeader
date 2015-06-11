package ru.tasu.directleader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.tasu.directleader.UsersDialogFragment.OnUserSelectListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.DialogFragment;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class ResolutionDetailFragment extends Fragment implements OnClickListener {
    private static final String TAG = "ResolutionDetailFragment";

    private static final String[] visibleHeaderFields = new String[]{"Дата","Дата2", "Текст", "ControlType", "Работник"};
    private static final Map<String, String> titlesHeaderFields = new HashMap<String, String>();
    static {
    	titlesHeaderFields.put("Дата", "Дата поручения");
    	titlesHeaderFields.put("Дата2", "План. Дата");
    	titlesHeaderFields.put("Текст", "Текст резолюции");
    	titlesHeaderFields.put("ControlType", "На котроле");
    	titlesHeaderFields.put("Работник", "Контролер");
    }
    private static final String[] visibleDetailFields = new String[]{"Доп2Т","PerformerT", "ДаНетТ", "Дата2Т"};
    private static final Map<String, String> titlesDetailFields = new HashMap<String, String>();
    static {
    	titlesDetailFields.put("Доп2Т", "Поручение");
    	titlesDetailFields.put("PerformerT", "Исполнитель");
    	titlesDetailFields.put("ДаНетТ", "Сводящий");
    	titlesDetailFields.put("Дата2Т", "Срок исполнения");
    }
    
    class GetResolutionDetailAsyncTask extends AsyncTask<Void, Void, List<ReferenceDetail>> {
        @Override
        protected List<ReferenceDetail> doInBackground(Void... params) {
            return mReferenceDetail;
        }
        @Override
        protected void onPostExecute(List<ReferenceDetail> refs) {
            super.onPostExecute(refs);
            RabotnicDataSource rds = new RabotnicDataSource(mDirect);
            rds.open();
            if (refs != null) {
            	referenceDetailLayout.removeAllViews();
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
	                Map<String, String> data = detail.getData();
	                final TextView detailAuthorTextView = (TextView) detailItemLayout.findViewById(R.id.detailAuthorTextView);
	                final TextView detailPropertyTextView = (TextView) detailItemLayout.findViewById(R.id.detailPropertyTextView);
	                detailAuthorTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
	                detailPropertyTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
	                Rabotnic rabotnic = rds.getRabotnicByCodeRab(data.get("PerformerT"));
	                detailAuthorTextView.setText(rabotnic.getName());
	                String propText = getResources().getString(R.string.resolution_detail_fragment_property_text);
	                propText = String.format(propText, data.get("ДаНетТ"), detail.getDataText(), detail.getPoruchenieText());
	                detailPropertyTextView.setText(propText);
	                final LinearLayout contentLayout = (LinearLayout) detailItemLayout.findViewById(R.id.contentLayout);
	                
	            	String value;
//	            	for (String key : data.keySet()) {
            		for (String key : visibleDetailFields) {
	            		value = data.get(key);
		                final LinearLayout itemLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.reference_detail_item_layout, null);
		                final TextView detailKeyTextView = (TextView) itemLayout.findViewById(R.id.detailKeyTextView);
		                detailKeyTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
		                detailKeyTextView.setText(titlesDetailFields.get(key));
//		                detailKeyTextView.setText(key);

		                final TextView detailValueTextView = (TextView) itemLayout.findViewById(R.id.detailValueTextView);
		                detailValueTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
		                
		                if (key.equalsIgnoreCase("PerformerT")) {
		                	detailValueTextView.setText(rabotnic.getName());
		                	detailValueTextView.setOnClickListener(detailUserSelectClickListener);
		                	detailValueTextView.setTag(detail);
		                } else {
		                	detailValueTextView.setText(value);
		                }
		                detailItemLayout.setTag(detail);
		                detailValueTextView.setTag(detailItemLayout);
//		                itemLayout.setTag(detail);
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
            return mReferenceHeader;
        }
        @Override
        protected void onPostExecute(ReferenceHeader ref_header) {
            super.onPostExecute(ref_header);
            RabotnicDataSource rds = new RabotnicDataSource(mDirect);
            rds.open();
            if (ref_header != null) {
            	Map<String, String> data = ref_header.getData();
            	
            	final TextView dateTextView = (TextView) referenceHeaderLayout.findViewById(R.id.dateTextView);
            	final TextView dateValueTextView = (TextView) referenceHeaderLayout.findViewById(R.id.dateValueTextView);
            	final TextView datePlanTextView = (TextView) referenceHeaderLayout.findViewById(R.id.datePlanTextView);
            	final TextView datePlanValueTextView = (TextView) referenceHeaderLayout.findViewById(R.id.datePlanValueTextView);
            	final TextView controlTextView = (TextView) referenceHeaderLayout.findViewById(R.id.controlTextView);
            	final Switch contolValueCheckbox = (Switch) referenceHeaderLayout.findViewById(R.id.contolValueCheckbox);
            	final TextView controlerTextView = (TextView) referenceHeaderLayout.findViewById(R.id.controlerTextView);
            	final TextView controlerValueTextView = (TextView) referenceHeaderLayout.findViewById(R.id.controlerValueTextView);
            	final TextView descriptionValueEditText = (TextView) referenceHeaderLayout.findViewById(R.id.descriptionValueEditText);
            	
            	dateTextView			.setTypeface(mDirect.mPFDinDisplayPro_Bold);
    			dateValueTextView		.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    			datePlanTextView		.setTypeface(mDirect.mPFDinDisplayPro_Bold);
    			datePlanValueTextView	.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    			controlTextView			.setTypeface(mDirect.mPFDinDisplayPro_Bold);
    			contolValueCheckbox		.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    			controlerTextView		.setTypeface(mDirect.mPFDinDisplayPro_Bold);
    			controlerValueTextView	.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    			descriptionValueEditText.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    			
    			dateValueTextView		 .setText(data.get("Дата"));
    			datePlanValueTextView	 .setText(data.get("Дата2"));
    			contolValueCheckbox		 .setChecked(data.get("ControlType").equalsIgnoreCase("Yes") ? true : false);
    			controlerValueTextView	 .setText(rds.getRabotnicByCodeRab(data.get("Работник")).getName());
    			descriptionValueEditText .setText(data.get("Текст"));
    			
    			dateValueTextView		.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						final Calendar c = Calendar.getInstance();
		                new DatePickerDialog(getActivity(), new OnDateSetListener() {
							@Override
							public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
								String myFormat = "dd.MM.yyyy"; //In which you need put here
		    		            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Utils.mLocale);
		    		            final Calendar c = Calendar.getInstance();
		    		            c.set(year, monthOfYear, dayOfMonth);
		    		            dateValueTextView.setText(sdf.format(c.getTime()));
//		    		            dateValueTextView.setTag(String.format("%s-%s-%s", year, monthOfYear, dayOfMonth));
							}
						},
						c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
					}
				});
    			datePlanValueTextView	.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						final Calendar c = Calendar.getInstance();
		                new DatePickerDialog(getActivity(), new OnDateSetListener() {
							@Override
							public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
								String myFormat = "dd.MM.yyyy"; //In which you need put here
		    		            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Utils.mLocale);
		    		            final Calendar c = Calendar.getInstance();
		    		            c.set(year, monthOfYear, dayOfMonth);
		    		            datePlanValueTextView.setText(sdf.format(c.getTime()));
//		    		            datePlanValueTextView.setTag(String.format("%s-%s-%s", year, monthOfYear, dayOfMonth));
							}
						},
						c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
					}
				}); 
    			contolValueCheckbox		.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
						mReferenceHeader.setOnControl(isChecked == true ? "Yes" : "No");
					}
				}); 
    			controlerValueTextView	.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						UsersDialogFragment frag = UsersDialogFragment.newInstance(null, new OnUserSelectListener() {
							@Override
							public void onUserSelect(DialogFragment fragment, Rabotnic user) {
								mReferenceHeader.setCodeRab(user.getCodeRab());
								fragment.dismiss();
								controlerValueTextView.setText(user.getName());
							}
						});
						frag.show(getFragmentManager(), "userSelect");
					}
				}); 
    			descriptionValueEditText.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
					}
				}); 
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
//					if (value.equalsIgnoreCase("null")) {
//						pair.put("Value", null);
//					} else {
//						pair.put("Value", value);
//					}
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
    			} else {
    				AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
    				alertDialog.setTitle("Ошибка");
    				alertDialog.setMessage("");
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
    	
    	JSONArray ref_header = mTask.getReferenceHeaderJSON();
        mReferenceHeader = new ReferenceHeader(ref_header);
        
        JSONArray ref_details = mTask.getReferenceDetailJSON();
        mReferenceDetail = new ArrayList<ReferenceDetail>();
        for (int i=0; i<ref_details.length(); i++) {
        	mReferenceDetail.add(new ReferenceDetail(ref_details.optJSONArray(i)));
        }
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
	OnClickListener detailUserSelectClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final TextView view = (TextView) v;
			final LinearLayout layout = (LinearLayout) v.getTag();
			final ReferenceDetail detail = (ReferenceDetail)layout.getTag();
			Log.v(TAG, "view text " + view.getText().toString());
			UsersDialogFragment frag = UsersDialogFragment.newInstance(null, new OnUserSelectListener() {
				@Override
				public void onUserSelect(DialogFragment fragment, Rabotnic user) {
					detail.setCodeRab(user.getCodeRab());
					updateDetailItemLayout(layout, detail);
					fragment.dismiss();
				}
			});
			frag.show(getFragmentManager(), "userSelect");
		}
	};
	private void updateDetailItemLayout(LinearLayout detailItemLayout, ReferenceDetail detail) {
        final ExpandablePanel panel = (ExpandablePanel) detailItemLayout.findViewById(R.id.expandablePanel);
        panel.setOnExpandListener(new ExpandablePanel.OnExpandListener() {
		    public void onCollapse(View handle, View content) {
		    	panel.invalidate();
		    }
		    public void onExpand(View handle, View content) {
		    	panel.invalidate();
		    }
		});
        RabotnicDataSource rds = new RabotnicDataSource(mDirect);
        rds.open();
        Map<String, String> data = detail.getData();
        final TextView detailAuthorTextView = (TextView) detailItemLayout.findViewById(R.id.detailAuthorTextView);
        final TextView detailPropertyTextView = (TextView) detailItemLayout.findViewById(R.id.detailPropertyTextView);
        detailAuthorTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        detailPropertyTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        Rabotnic rabotnic = rds.getRabotnicByCodeRab(data.get("PerformerT"));
        detailAuthorTextView.setText(rabotnic.getName());
        String propText = getResources().getString(R.string.resolution_detail_fragment_property_text);
        propText = String.format(propText, data.get("ДаНетТ"), detail.getDataText(), detail.getPoruchenieText());
        detailPropertyTextView.setText(propText);
        final LinearLayout contentLayout = (LinearLayout) detailItemLayout.findViewById(R.id.contentLayout);
        contentLayout.removeAllViews();
    	String value;
		for (String key : visibleDetailFields) {
    		value = data.get(key);
            final LinearLayout itemLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.reference_detail_item_layout, null);
            final TextView detailKeyTextView = (TextView) itemLayout.findViewById(R.id.detailKeyTextView);
            detailKeyTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
            detailKeyTextView.setText(titlesDetailFields.get(key));
//            detailKeyTextView.setText(key);

            final TextView detailValueTextView = (TextView) itemLayout.findViewById(R.id.detailValueTextView);
            detailValueTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
            
            if (key.equalsIgnoreCase("PerformerT")) {
            	detailValueTextView.setText(rabotnic.getName());
            	detailValueTextView.setOnClickListener(detailUserSelectClickListener);
            	detailValueTextView.setTag(detail);
            } else {
            	detailValueTextView.setText(value);
            }
            detailItemLayout.setTag(detail);
            detailValueTextView.setTag(detailItemLayout);
            contentLayout.addView(itemLayout);
    	}
		detailItemLayout.invalidate();
	}
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
