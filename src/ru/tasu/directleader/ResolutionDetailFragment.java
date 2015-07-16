package ru.tasu.directleader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class ResolutionDetailFragment extends Fragment implements OnClickListener {
    private static final String TAG = "ResolutionDetailFragment";

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
	            for (final ReferenceDetail detail : refs) {
	            	if (detail.getAttributeByName("").optInt("Id") == 0) {
	            		continue;
	            	}
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
	                LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	            	lp.weight = 1;
	            	
	            	final TextView detailAuthorTextView = (TextView) detailItemLayout.findViewById(R.id.detailAuthorTextView);
	            	final TextView detailPropertyTextView = (TextView) detailItemLayout.findViewById(R.id.detailPropertyTextView);
	            	final ImageButton deleteButton = (ImageButton) detailItemLayout.findViewById(R.id.deleteButton);
	            	detailAuthorTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
	            	detailPropertyTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
	            	
	            	Rabotnic rabotnic = rds.getRabotnicByCodeRab(detail.getAttributeItemValue("PerformerT"));
	            	if (rabotnic != null) {
	            		detailAuthorTextView.setText(rabotnic.getName());
	            	}
	            	String propText = getResources().getString(R.string.resolution_detail_fragment_property_text);
	            	propText = String.format(propText, detail.getAttributeItemValue("ДаНетТ"), detail.getAttributeItemValue("Дата2Т"), detail.getAttributeItemValue("Доп2Т"));
	            	detailPropertyTextView.setText(propText);
	            	
	            	deleteButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							long detailId = detail.getAttributeByName("").optLong("Id");
							mDeletedDetailIds.add(detailId); // Запоминаем id для удаления
							referenceDetailLayout.removeView(detailItemLayout); // Удаляем из layout'a
							mReferenceDetail.remove(detail); // Удаляем из Reference
						}
					});
	            	
	            	final LinearLayout contentLayout = (LinearLayout) detailItemLayout.findViewById(R.id.contentLayout);
	            	
	            	List<JSONObject> data = detail.getVisibleAttributes();
	            	for (JSONObject obj : data) {
	            		final LinearLayout itemLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.reference_detail_item_layout, null);
	            		final TextView detailNameTextView = (TextView) itemLayout.findViewById(R.id.detailNameTextView);
	            		detailNameTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
	            		detailNameTextView.setText(obj.optString("Title"));
	            		
	            		final ImageButton clearButton = (ImageButton) itemLayout.findViewById(R.id.clearButton);
	            		
	            		final View valueView;
	            		if (obj.optString("DataType").equalsIgnoreCase("rdtString")) {
	            			valueView = getTextView(getActivity(), obj.optString("Name"), detail, clearButton, detailItemLayout);
	            			String value = obj.optString("Value");
	            			((TextView)valueView).setText(value);
	            			if (!value.isEmpty()) {
		            			// Сделать видимой кнопку очищения
		            			clearButton.setVisibility(View.VISIBLE);
		            			// Сохранить референсе в тэге
		            			clearButton.setTag(detail);
	            			}
	            			
		            	} else if (obj.optString("DataType").equalsIgnoreCase("rdtText")) {
		            		valueView = getEditTextView(getActivity(), obj.optString("Name"), detail, detailItemLayout);
		            		((EditText)valueView).setText(obj.optString("Value"));
		            		
		            	} else if (obj.optString("DataType").equalsIgnoreCase("rdtDate")) {
		            		valueView = getDateView(getActivity(), obj.optString("Name"), detail, clearButton, detailItemLayout);
		            		String value = obj.optString("Value");
	            			((TextView)valueView).setText(value);
	            			if (!value.isEmpty()) {
			            		// Сделать видимой кнопку очищения
		            			clearButton.setVisibility(View.VISIBLE);
		            			// Сохранить референсе в тэге
		            			clearButton.setTag(detail);
	            			}
		            		
		            	} else if (obj.optString("DataType").equalsIgnoreCase("rdtPick")) {
		            		valueView = getCheckBoxView(getActivity(), obj.optString("Name"), detail, "Да", "Нет", detailItemLayout);
		            		((Switch)valueView).setChecked(obj.optString("Value").equalsIgnoreCase("Да") ? true : false);
		            		
		            	} else if (obj.optString("DataType").equalsIgnoreCase("rdtReference")) {
		            		String value = "";
		            		if (obj.optString("TypeReference").equalsIgnoreCase("РАБ")) {
			            		Rabotnic user = rds.getRabotnicByCodeRab(obj.optString("Value"));
			            		valueView = getUserView(getActivity(), obj.optString("Name"), detail, clearButton, detailItemLayout);
			            		if (user != null) {
			            			value = user.getName();
			            			((TextView)valueView).setText(value);
			            		}
		            		} else {
			            		valueView = getEmptyTextView(getActivity(), obj.optString("Name"), detail, detailItemLayout);
			            		value = obj.optString("Value");
		            			((TextView)valueView).setText(value);
			            	}
		            		if (!value.isEmpty()) {
			            		// Сделать видимой кнопку очищения
		            			clearButton.setVisibility(View.VISIBLE);
		            			// Сохранить референсе в тэге
		            			clearButton.setTag(detail);
		            		}
		            	} else {
		            		valueView = getEmptyTextView(getActivity(), obj.optString("Name"), detail, detailItemLayout);
	            			((TextView)valueView).setText(obj.optString("Value"));
		            	}
	            		if (valueView != null) {
	            			valueView.setLayoutParams(lp);
	            			itemLayout.addView(valueView, 1);
	            			
	            			clearButton.setOnClickListener(new OnClickListener() {
	            				@Override
	            				public void onClick(View v) {
	            					String attrName = (String)valueView.getTag();
	            					String attrValue = "";
	            					((TextView)valueView).setText(attrValue);
	            					final Reference ref = (Reference)v.getTag();
	            					ref.setAttributeItemValue(attrName, attrValue);
	            					v.setVisibility(View.GONE);
	            				}
	            			});
	            		}
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
            	referenceHeaderLayout.removeAllViews();
            	LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            	lp.weight = 1;
            	
            	List<JSONObject> data = ref_header.getVisibleAttributes();
            	for (JSONObject obj : data) {
            		final LinearLayout headerItemLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.reference_header_item_layout, null);
            		final TextView headerNameTextView = (TextView) headerItemLayout.findViewById(R.id.headerNameTextView);
            		headerNameTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
            		headerNameTextView.setText(obj.optString("Title"));
            		
            		final ImageButton clearButton = (ImageButton) headerItemLayout.findViewById(R.id.clearButton);
            		
            		final View valueView;
            		if (obj.optString("DataType").equalsIgnoreCase("rdtString")) {
            			if (isSubResolution && obj.optString("Name").equalsIgnoreCase("Наименование") && obj.optString("Value").isEmpty()) {
//            				((TextView)valueView).setText(subResolutionTitle);
            				mReferenceHeader.setAttributeItemValue(obj.optString("Name"), subResolutionTitle);
            			}
            			valueView = getTextView(getActivity(), obj.optString("Name"), mReferenceHeader, clearButton);
            			String value = obj.optString("Value");
            			((TextView)valueView).setText(value);
            			if (!value.isEmpty()) {
		        			// Сделать видимой кнопку очищения
		        			clearButton.setVisibility(View.VISIBLE);
		        			// Сохранить референсе в тэге
		        			clearButton.setTag(mReferenceHeader);
            			}
            			
	            	} else if (obj.optString("DataType").equalsIgnoreCase("rdtText")) {
	            		valueView = getEditTextView(getActivity(), obj.optString("Name"), mReferenceHeader);
	            		((EditText)valueView).setText(obj.optString("Value"));
	            		
	            	} else if (obj.optString("DataType").equalsIgnoreCase("rdtDate")) {
	            		valueView = getDateView(getActivity(), obj.optString("Name"), mReferenceHeader, clearButton);
	            		String value = obj.optString("Value");
            			((TextView)valueView).setText(value);
            			if (!value.isEmpty()) {
		        			// Сделать видимой кнопку очищения
		        			clearButton.setVisibility(View.VISIBLE);
		        			// Сохранить референсе в тэге
		        			clearButton.setTag(mReferenceHeader);
            			}
	            		
	            	} else if (obj.optString("DataType").equalsIgnoreCase("rdtPick")) {
	            		valueView = getCheckBoxView(getActivity(), obj.optString("Name"), mReferenceHeader);
	            		String value = obj.optString("Value");
	            		Log.v(TAG, "Value " + value);
	            		((Switch)valueView).setChecked(value.equalsIgnoreCase("Yes") ? true : false);
	            		if (value.equalsIgnoreCase("")) {
	            			mReferenceHeader.setAttributeItemValue(obj.optString("Name"), "No");
	    				}
	            		
	            	} else if (obj.optString("DataType").equalsIgnoreCase("rdtReference")) {
	            		String value = "";
	            		if (obj.optString("TypeReference").equalsIgnoreCase("РАБ")) {
		            		Rabotnic user = rds.getRabotnicByCodeRab(obj.optString("Value"));
		            		valueView = getUserView(getActivity(), obj.optString("Name"), mReferenceHeader, clearButton);
		            		if (user != null) {
		            			value = user.getName();
		            			((TextView)valueView).setText(value);
			            	}
	            		} else {
		            		valueView = getEmptyTextView(getActivity(), obj.optString("Name"), mReferenceHeader);
		            		value = obj.optString("Value");
		            		((TextView)valueView).setText(value);
		            	}
            			if (!value.isEmpty()) {
		        			// Сделать видимой кнопку очищения
		        			clearButton.setVisibility(View.VISIBLE);
		        			// Сохранить референсе в тэге
		        			clearButton.setTag(mReferenceHeader);
            			}
	            		
	            	} else {
	            		valueView = getEmptyTextView(getActivity(), obj.optString("Name"), mReferenceHeader);
            			((TextView)valueView).setText(obj.optString("Value"));
	            	}
            		if (valueView != null) {
            			valueView.setLayoutParams(lp);
            			headerItemLayout.addView(valueView, 1);
            			
            			clearButton.setOnClickListener(new OnClickListener() {
	            			@Override
	            			public void onClick(View v) {
	            				String attrName = (String)valueView.getTag();
	    				    	String attrValue = "";
	    				    	((TextView)valueView).setText(attrValue);
	    				    	final Reference ref = (Reference)v.getTag();
	    				    	ref.setAttributeItemValue(attrName, attrValue);
	    				    	v.setVisibility(View.GONE);
	            			}
	            		});
            		}
            		
            		referenceHeaderLayout.addView(headerItemLayout);
            	}
            }
        }
    }
    class SaveReferenceAsyncTask extends AsyncTask<Boolean, Void, JSONObject> {
    	ProgressDialog pg = new ProgressDialog(getActivity());
    	JSONArray detail = new JSONArray(); // Array of JSONArray of JSONObject
		JSONArray header = new JSONArray(); // Array of JSONObject
    	@Override
    	protected void onPreExecute() {
    		pg.setCancelable(false);
    		pg.setMessage(getResources().getString(R.string.reference_save_progress_message_text));
    		pg.show();
    		super.onPreExecute();
    	}
    	@Override
    	protected JSONObject doInBackground(Boolean... params) {
    		boolean success = true;
    		JSONObject json = new JSONObject();
    		
    		List<JSONObject> data = mReferenceHeader.getData();
    		for (JSONObject headerItem : data) {
    			header.put(headerItem);
    		}
    		for (ReferenceDetail ref_detail : mReferenceDetail) {
	        	data = ref_detail.getData();
	        	final JSONArray detail_json_array = new JSONArray();
	        	for (JSONObject detailItem : data) {
	        		detail_json_array.put(detailItem);
	        	}
//	        	Log.v(TAG, "save reference detail " + detail_json_array);
	        	detail.put(detail_json_array);
    		}
//    		Log.v(TAG, "save reference header " + header);
    		try {
    			if (params.length > 0 && params[0]) {
    				json.put("AttachToJob", mJob.getId());
    			}
				json.put("ReferenceHeader", header);
				json.put("ReferenceDetail", detail);
				if (mDeletedDetailIds.size() > 0) {
					JSONArray ids = new JSONArray();
					for (Long l : mDeletedDetailIds) {
						ids.put(l);
					}
					json.put("DeleteReferenceDetailId", ids);	
				}
			} catch (JSONException e) {
				success = false;
				e.printStackTrace();
			}
    		if (success) {
    			return mDirect.PostSaveReference(json);
//    			return null;
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
    				boolean result = json.optBoolean("Result");
    				final String message = json.optString("Message");
    				AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
    				if (result) {
    					// Сохранение резолюции прошло успешно
    					alertDialog.setTitle("Успешно");
    					if (!isSubResolution) { // Главная резолюция
    						// Заменить ключи 
    						/*"CreatedKeysDetail":[{
    							"Key":2147483647,
    							"Value":2147483647
    						}],*/
    						JSONArray detailKeys = json.optJSONArray("CreatedKeysDetail");
    						for (int i=0; i<detailKeys.length(); i++) {
    							final JSONObject pair = detailKeys.optJSONObject(i);
    							final int key = pair.optInt("Key");
    							final int value = pair.optInt("Value");
    							for (int j=0; j<detail.length(); j++) {
    								final JSONArray detailItem = detail.optJSONArray(j);
									for (int k=0; k<detailItem.length(); k++) {
										final JSONObject item = detailItem.optJSONObject(k);
										if (item.optInt("Id") == key) {
											try {
												item.put("Id", value);
											} catch (JSONException e) {
												Log.v(TAG, "" + e.getMessage());
											}
										}
									}
//									Log.v(TAG, "detailItem " + detailItem);
    							}
    						}
    						// Теоретически, ключи в хедере не могут изменяться, при сохранении главной резолюции
    						/*"CreatedKeysHead":[{
    							"Key":2147483647,
    							"Value":2147483647
    						}],*/
	    					// Сохранить reference в Resolution
	    					mResolution.setReferenceHeader(header);
	    					mResolution.setReferenceDetail(detail);
	    					// Записать обновленный Resolution в БД
	    					ResolutionDataSource rds = new ResolutionDataSource(mDirect);
	    					rds.open();
	    					Log.v(TAG, "Обновляем резолюцию " + mResolution);
	    					mResolution = rds.updateResolutionById(mResolution);
	    					alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
	    				        public void onClick(DialogInterface dialog, int which) {
	    				            dialog.dismiss();
	    				        }
	    					});
    					} else { // Подчиненная резолюция
    						// Заменить ключи detail
    						/*"CreatedKeysDetail":[{
								"Key":2147483647,
								"Value":2147483647
							}],*/
							JSONArray detailKeys = json.optJSONArray("CreatedKeysDetail");
							for (int i=0; i<detailKeys.length(); i++) {
								final JSONObject pair = detailKeys.optJSONObject(i);
								final int key = pair.optInt("Key");
								final int value = pair.optInt("Value");
								for (int j=0; j<detail.length(); j++) {
									final JSONArray detailItem = detail.optJSONArray(j);
									for (int k=0; k<detailItem.length(); k++) {
										final JSONObject item = detailItem.optJSONObject(k);
										if (item.optInt("Id") == key) {
											try {
												item.put("Id", value);
											} catch (JSONException e) {
												Log.v(TAG, "" + e.getMessage());
											}
										}
									}
//									Log.v(TAG, "detailItem " + detailItem);
								}
							}
    						// Заменить ключи header
							/*"CreatedKeysHead":[{
								"Key":2147483647,
								"Value":2147483647
							}],*/
							JSONArray headerKeys = json.optJSONArray("CreatedKeysHead");
							for (int i=0; i<headerKeys.length(); i++) {
								final JSONObject pair = headerKeys.optJSONObject(i);
								final int key = pair.optInt("Key");
								final int value = pair.optInt("Value");
								for (int j=0; j<header.length(); j++) {
									final JSONObject item = header.optJSONObject(j);
									if (item.optInt("Id") == key) {
										try {
											item.put("Id", value);
										} catch (JSONException e) {
											Log.v(TAG, "" + e.getMessage());
										}
									}
//									Log.v(TAG, "header " + header);
								}
							}
							// Сохраняем резолюцию
							Resolution resl = new Resolution(detail.toString(), header.toString(), 0, mJob.getMainTaskJob());
							ResolutionDataSource rds = new ResolutionDataSource(mDirect);
							rds.open();
							Log.v(TAG, "Создаем резолюцию " + resl);
							rds.createResolution(resl);
	    					alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
	    				        public void onClick(DialogInterface dialog, int which) {
	    				            dialog.dismiss();
	    				            // Also need to close myself
	    				            getFragmentManager().popBackStackImmediate();
	    				        }
	    					});
    					}
    					// Сохраняем документ
						boolean resultAttach = json.optBoolean("ResultAttach");
						if (resultAttach) { // Если удалось создать документ, то нужно его приаттачить в заданию
							Attachment attachment = new Attachment(json.optJSONObject("ResultDocument"));
							attachment.setTaskId(mResolution.getTaskId());
							// Сохранить документ в БД
							AttachmentDataSource ads = new AttachmentDataSource(mDirect);
							ads.open();
							ads.insertOrUpdate(attachment);
						}
    				} else {
    					// В любом случае показать диалоговое окно с результатом.
    					alertDialog.setTitle("Ошибка");
    					alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
    				        public void onClick(DialogInterface dialog, int which) {
    				            dialog.dismiss();
    				        }
    					});
    				}
    				alertDialog.setMessage(message);
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
    
    public static final String JOB_KEY = "job_key";
    public static final String RESOLUTION_KEY = "resolution_key";
    public static final String REFERENCE_SUBRESOLUTION_TITLE_KEY = "reference_subresolution_title_key";
    public static final String REFERENCE_SUB_RESOLUTION_KEY = "reference_sub_resolution_key";
//    private Task mTask = null;
    private Job mJob = null;
    private Resolution mResolution;
    private boolean isSubResolution = false;
    private String subResolutionTitle = "";
    
    private HashSet<Long> mDeletedDetailIds = new HashSet<Long>();
    
    private ReferenceHeader mReferenceHeader = null;
    private List<ReferenceDetail> mReferenceDetail = null;
    
    private ImageButton saveButton, addSubResolutionButton, saveAndAttachButton;
    private LinearLayout referenceHeaderLayout, referenceDetailLayout;

    // View's for ReferenceHeader & ReferenceDetail
    private TextView getCheckBoxView(Context context, String attrName, final Reference ref) {
    	return getCheckBoxView(context, attrName, ref, "Yes", "No", null);
    }
    private TextView getCheckBoxView(Context context, String attrName, final Reference ref, final String trueValue, final String falseValue, final View layout) {
    	final Switch view = new Switch(context);
    	view.setTag(attrName);
    	view.setBackgroundResource(R.drawable.transparent_back_selector);
    	view.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				String attrName = (String)view.getTag();
				Log.v(TAG, "onCheckedChanged " + attrName);
				String attrValue = isChecked == true ? trueValue : falseValue;
				ref.setAttributeItemValue(attrName, attrValue);
				if (layout != null) {
	            	updateDetailSummary(layout, ref);
					if (isChecked) {
						// Выключить все остальные checkbox'ы с таким же Name
						setOffSwitchWithSameName(attrName, view);
					}
				}
			}
		}); 

    	return view;
    }
    private TextView getEditTextView(Context context, String attrName, final Reference ref) {
    	return getEditTextView(context, attrName, ref, null);
    }
    private TextView getEditTextView(Context context, String attrName, final Reference ref, final View layout) {
    	final EditText view = new EditText(context);
    	view.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    	view.setTag(attrName);
    	view.setBackgroundResource(R.drawable.edittext_background_rounded);
    	view.setGravity(Gravity.LEFT);
    	view.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    	view.setSingleLine(false);
    	view.setLines(3);
    	int padding = (int)(10 * getResources().getDisplayMetrics().density + 0.5f);
    	view.setPadding(padding, padding, padding, padding);
    	view.setClickable(true);
    	view.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				String attrName = (String)view.getTag();
				String attrValue = s.toString();
				ref.setAttributeItemValue(attrName, attrValue);
				if (layout != null) {
	            	updateDetailSummary(layout, ref);
	            }
			}
		}); 

    	return view;
    }
    private TextView getUserView(Context context, String attrName, final Reference ref, final ImageButton button) {
    	return getUserView(context, attrName, ref, button, null);
    }
    private TextView getUserView(Context context, String attrName, final Reference ref, final ImageButton button, final View layout) {
    	final TextView view = new TextView(context);
    	view.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    	view.setTag(attrName);
    	view.setBackgroundResource(R.drawable.transparent_back_selector);
    	int padding = (int)(10 * getResources().getDisplayMetrics().density + 0.5f);
    	view.setPadding(padding, padding, padding, padding);
    	view.setClickable(true);
    	view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UsersDialogFragment frag = UsersDialogFragment.newInstance(null, new OnUserSelectListener() {
					@Override
					public void onUserSelect(DialogFragment fragment, Rabotnic user) {
						fragment.dismiss();
						view.setText(user.getName());
						String attrName = (String)view.getTag();
						String attrValue = user.getCodeRab();
						ref.setAttributeItemValue(attrName, attrValue);
						if (button != null) {
	    		            if (!attrValue.isEmpty()) {
	    		            	button.setVisibility(View.VISIBLE);
	    		            } else {
	    		            	button.setVisibility(View.GONE);
	    		            }
    		            }
						if (layout != null) {
    		            	updateDetailSummary(layout, ref);
    		            }
					}
				});
				frag.show(getFragmentManager(), "userSelect");
			}
		}); 

    	return view;
    }
    private TextView getTextView(Context context, String attrName, final Reference ref, final ImageButton button) {
    	return getTextView(context, attrName, ref, button, null);
    }
    private TextView getTextView(Context context, String attrName, final Reference ref, final ImageButton button, final View layout) {
    	final TextView view = new TextView(context);
    	view.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    	view.setTag(attrName);
    	view.setBackgroundResource(R.drawable.transparent_back_selector);
    	int padding = (int)(10 * getResources().getDisplayMetrics().density + 0.5f);
    	view.setPadding(padding, padding, padding, padding);
    	view.setClickable(true);
    	view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Введите текст");

				// Set up the input
				final EditText input = new EditText(getActivity());
				// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
				input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
				input.setText(view.getText().toString());
				builder.setView(input);

				// Set up the buttons
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	String attrName = (String)view.getTag();
				    	String attrValue = input.getText().toString();
				    	view.setText(attrValue);
				    	ref.setAttributeItemValue(attrName, attrValue);
				    	if (button != null) {
	    		            if (!attrValue.isEmpty()) {
	    		            	button.setVisibility(View.VISIBLE);
	    		            } else {
	    		            	button.setVisibility(View.GONE);
	    		            }
    		            }
				    	if (layout != null) {
    		            	updateDetailSummary(layout, ref);
    		            }
				    }
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        dialog.cancel();
				    }
				});

				builder.show();
			}
		}); 
    	
    	return view;
    }
    private TextView getDateView(Context context, String attrName, final Reference ref, final ImageButton button) {
    	return getDateView(context, attrName, ref, button, null);
    }
    private TextView getDateView(Context context, String attrName, final Reference ref, final ImageButton button, final View layout) {
    	final TextView view = new TextView(context);
    	view.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    	view.setTag(attrName);
    	view.setBackgroundResource(R.drawable.transparent_back_selector);
    	int padding = (int)(10 * getResources().getDisplayMetrics().density + 0.5f);
    	view.setPadding(padding, padding, padding, padding);
    	view.setClickable(true);
    	view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Calendar c = Calendar.getInstance();
                new DatePickerDialog(getActivity(), new OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker picker, int year, int monthOfYear, int dayOfMonth) {
						String myFormat = "dd.MM.yyyy"; //In which you need put here
    		            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Utils.mLocale);
    		            final Calendar c = Calendar.getInstance();
    		            c.set(year, monthOfYear, dayOfMonth);
    		            String attrName = (String)view.getTag();
    		            String attrValue = sdf.format(c.getTime());
    		            view.setText(attrValue);
    		            ref.setAttributeItemValue(attrName, attrValue);
    		            if (button != null) {
	    		            if (!attrValue.isEmpty()) {
	    		            	button.setVisibility(View.VISIBLE);
	    		            } else {
	    		            	button.setVisibility(View.GONE);
	    		            }
    		            }
//    		            view.setTag(String.format("%s-%s-%s", year, monthOfYear, dayOfMonth));
    		            if (layout != null) {
    		            	updateDetailSummary(layout, ref);
    		            }
					}
				},
				c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
			}
		}); 

    	return view;
    }
    
    private TextView getEmptyTextView(Context context, String attrName, final Reference ref) {
    	return getEmptyTextView(context, attrName, ref, null);
    }
    private TextView getEmptyTextView(Context context, String attrName, final Reference ref, final View layout) {
    	final TextView view = new TextView(context);
    	view.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    	view.setTag(attrName);
//    	view.setBackgroundResource(R.drawable.transparent_back_selector);
    	int padding = (int)(10 * getResources().getDisplayMetrics().density + 0.5f);
    	view.setPadding(padding, padding, padding, padding);
//    	view.setClickable(true);

    	return view;
    }
    
    private void updateDetailSummary(View detailItemLayout, Reference detail) {
    	RabotnicDataSource rds = new RabotnicDataSource(mDirect);
        rds.open();
        
        final TextView detailAuthorTextView = (TextView) detailItemLayout.findViewById(R.id.detailAuthorTextView);
    	final TextView detailPropertyTextView = (TextView) detailItemLayout.findViewById(R.id.detailPropertyTextView);
    	
        Rabotnic rabotnic = rds.getRabotnicByCodeRab(detail.getAttributeItemValue("PerformerT"));
        if (rabotnic != null) {
    		detailAuthorTextView.setText(rabotnic.getName());
    	}
    	String propText = getResources().getString(R.string.resolution_detail_fragment_property_text);
    	propText = String.format(propText, detail.getAttributeItemValue("ДаНетТ"), detail.getAttributeItemValue("Дата2Т"), detail.getAttributeItemValue("Доп2Т"));
    	detailPropertyTextView.setText(propText);
    }
    private void setOffSwitchWithSameName(String attrName, View v) {
    	// Все переключатели с таким же AttributeName переведет в положение офф
    	int cc = referenceDetailLayout.getChildCount();
    	for (int i=0; i<cc; i++) {
    		final LinearLayout detailItemLayout = (LinearLayout)referenceDetailLayout.getChildAt(i);
    		final LinearLayout contentLayout = (LinearLayout) detailItemLayout.findViewById(R.id.contentLayout);
    		final int ccl = contentLayout.getChildCount();
    		for (int j=0; j<ccl; j++) {
    			final LinearLayout itemLayout = (LinearLayout)contentLayout.getChildAt(j);
    			final View view = itemLayout.getChildAt(1); // Всего 2 чайлда, нам нужен последний
    			if (((String)view.getTag()).equalsIgnoreCase(attrName) && !view.equals(v)) {
    				Switch swich = (Switch)view;
    				if (swich.isChecked()) {
    					swich.setChecked(false);
    				}
    			}
    		}
    	}
    }
    private void addCloningReferenceDetail() {
    	if (mReferenceDetail.size() == 0) {
    		return;
    	}
    	ReferenceDetail detail = mReferenceDetail.get(mReferenceDetail.size()-1);
    	JSONObject obj = detail.getAttributeByName("PerformerT");
    	if (detail.getAttributeByName("").optInt("Id") != 0 && obj.optString("Value").equalsIgnoreCase("")) {
    		return;
    	}
    	ReferenceDetail new_detail = new ReferenceDetail(detail);
    	new_detail.clearValues();
    	mReferenceDetail.add(new_detail);
    }
    private void createSubResolution() {
    	if (mReferenceDetail.size() == 0) {
    		return;
    	}
    	// Клонируем референс детаил
    	ReferenceDetail detail = mReferenceDetail.get(mReferenceDetail.size()-1);
    	ReferenceDetail new_detail = new ReferenceDetail(detail);
    	new_detail.clearValues(true);
    	// Клонируем референс хедер
    	ReferenceHeader new_header = new ReferenceHeader(mReferenceHeader);
    	new_header.clearValues();
    	JSONArray head = new_header.getDataJSONArray();
    	JSONArray det = new JSONArray();
    	det.put(new_detail.getDataJSONArray());
    	Resolution new_resolution = new Resolution(det.toString(), head.toString(), 0, mResolution.getId());
    	// Подчиненная резолюция от «Дата поручения» + «текст резолюции» + («id резолюции»)
    	String text = mReferenceHeader.getAttributeItemValue("Текст");
    	if (text.length() > 16) {
    		text = text.substring(0, 16);
    	}
    	String new_title = String.format("Подчиненная резолюция от %s %s (%s)", mReferenceHeader.getAttributeItemValue("Дата"), text, mReferenceHeader.getAttributeByName("Наименование").optString("Id"));
    	Bundle args = new Bundle();
        args.putParcelable(ResolutionDetailFragment.JOB_KEY, mJob);
        args.putParcelable(ResolutionDetailFragment.RESOLUTION_KEY, new_resolution);
        args.putString(ResolutionDetailFragment.REFERENCE_SUBRESOLUTION_TITLE_KEY, new_title);
        args.putBoolean(ResolutionDetailFragment.REFERENCE_SUB_RESOLUTION_KEY, true);
        mListener.OnOpenFragment(ResolutionDetailFragment.class.getName(), args);
    }
    
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
//        mTask = args.getParcelable(TASK_KEY);
        mJob = args.getParcelable(JOB_KEY);
        mResolution = args.getParcelable(RESOLUTION_KEY);
        if (mJob == null || mResolution == null) {
            // Закрыть фрагмент?
        	Toast.makeText(mDirect, "mJob == " + mJob + "; mResolution == " + mResolution, Toast.LENGTH_LONG).show();
        	getFragmentManager().popBackStackImmediate();
        }
        
        initViews(rootView);

        if (args.containsKey(REFERENCE_SUB_RESOLUTION_KEY)) {
//        	Log.v(TAG, "SubResolution");
        	addSubResolutionButton.setVisibility(View.GONE);
        	saveButton.setVisibility(View.GONE);
        	isSubResolution = args.getBoolean(REFERENCE_SUB_RESOLUTION_KEY);
        	Log.v(TAG, "isSubResolution " + isSubResolution);
        	subResolutionTitle = args.getString(REFERENCE_SUBRESOLUTION_TITLE_KEY);
        }
        
        setFonts();
        updateData();
        return rootView;
    }
    private void initViews(View v) {
    	referenceHeaderLayout = (LinearLayout) v.findViewById(R.id.referenceHeaderLayout);
    	referenceDetailLayout = (LinearLayout) v.findViewById(R.id.referenceDetailLayout);
    	
    	saveButton = (ImageButton) v.findViewById(R.id.saveButton);
    	saveButton.setOnClickListener(this);
    	
    	saveAndAttachButton = (ImageButton) v.findViewById(R.id.saveAndAttachButton);
    	saveAndAttachButton.setOnClickListener(this);
    	
    	Button addDetailReference = (Button) v.findViewById(R.id.addDetailReference);
    	addDetailReference.setOnClickListener(this);
    	
    	addSubResolutionButton = (ImageButton) v.findViewById(R.id.addSubResolutionButton);
    	addSubResolutionButton.setOnClickListener(this);
    	
    	JSONArray ref_header = mResolution.getReferenceHeaderJSON();
        mReferenceHeader = new ReferenceHeader(ref_header);
        
    	JSONArray ref_details = mResolution.getReferenceDetailJSON();
        mReferenceDetail = new ArrayList<ReferenceDetail>();
        for (int i=0; i<ref_details.length(); i++) {
        	mReferenceDetail.add(new ReferenceDetail(ref_details.optJSONArray(i)));
        }
        
        /*
        @Deprecated Всегда будет один пустой элемент для копирования
        if (mReferenceDetail.size() == 0) {
    		addDetailReference.setVisibility(View.GONE);
    	} else {
    		addDetailReference.setVisibility(View.VISIBLE);
    	} // */
    }
    private void setFonts() {
    }
    
    private void updateData() {
        new GetResolutionDetailAsyncTask().execute();
    	new GetResolutionHeaderAsyncTask().execute();
    }
    @Override
    public void onClick(View v) {
    	switch (v.getId()) {
    		case R.id.saveButton:
    			new SaveReferenceAsyncTask().execute();
    			break;
    		case R.id.saveAndAttachButton:
    			new SaveReferenceAsyncTask().execute(true);
    			break;
    		case R.id.addDetailReference:
    			addCloningReferenceDetail();
    			updateData();
    			break;
    		case R.id.addSubResolutionButton:
    			createSubResolution();
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
