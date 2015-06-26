package ru.tasu.directleader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.tasu.directleader.JobDetailFragment.ExecJobActionAsyncTask;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class TaskDetailFragment extends Fragment implements OnClickListener {
    private static final String TAG = "TaskDetailFragment";
    
    class GetJobsAsyncTask extends AsyncTask<Void, Void, Job[]> {
        ProgressDialog pg;
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pg.setMessage(getResources().getString(R.string.task_loading_data_text));
            pg.show();
        };
        @Override
        protected Job[] doInBackground(Void... params) {
            JobDataSource jds = new JobDataSource(mDirect);
            jds.open();
            Job[] jobs = jds.getJobsByTaskId(mTask.getId());
            jds.close();
            return jobs;
        }
        @Override
        protected void onPostExecute(Job[] jobs) {
            if (pg != null) {
                pg.dismiss();
            }
            jobsTextView.setText(String.valueOf(jobs.length));
            
            // Исполнители
            for (Job j : jobs) {
                final RelativeLayout itemLayout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.performer_item_layout, null);
                final TextView performerNameTextView = (TextView) itemLayout.findViewById(R.id.performerNameTextView);
                
                performerNameTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
                if (j.getUser() != null) {
                    performerNameTextView.setText(j.getUser().getName());
                }
                itemLayout.setTag(j);
//                itemLayout.setOnClickListener(performerClickListener);
                performersLayout.addView(itemLayout);
            }
            if (jobs.length == 0) {
                performersLayout.setVisibility(View.GONE);
            }
            super.onPostExecute(jobs);
        }
    }
    class GetDocumentsAsyncTask extends AsyncTask<Void, Void, Attachment[]> {
        @Override
        protected Attachment[] doInBackground(Void... params) {
            AttachmentDataSource ds = new AttachmentDataSource(mDirect);
            ds.open();
            return ds.getAttachmentsByTaskId(mTask.getId());
        }
        @Override
        protected void onPostExecute(Attachment[] attachments) {
            super.onPostExecute(attachments);
            documentsLayout.removeAllViews();
            Log.v(TAG, "attachments.length " + attachments.length);
            for (Attachment a : attachments) {
            	Log.v(TAG, "a.getId() " + a.getId());
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
        }
    }
    class GetResolutionAsyncTask extends AsyncTask<Void, Void, ReferenceHeader> {
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
            resolutionLayout.removeAllViews();
            
            if (ref_header != null) {
                final RelativeLayout itemLayout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.resolution_item_layout, null);
                final TextView resolutionNameTextView = (TextView) itemLayout.findViewById(R.id.resolutionNameTextView);
                resolutionNameTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
                resolutionNameTextView.setText(ref_header.getResolutionTitle());
                itemLayout.setTag(ref_header);
                itemLayout.setOnClickListener(resolutionClickListener);
                resolutionLayout.addView(itemLayout);
            } else {
            	resolutionListLabel.setVisibility(View.GONE);
            }
        }
    }
    class GetHistoriesAsyncTask extends AsyncTask<Void, Void, History[]> {
        @Override
        protected History[] doInBackground(Void... params) {
            HistoryDataSource ds = new HistoryDataSource(mDirect);
            ds.open();
            return ds.getHistoriesByTaskId(mTask.getId());
        }
        @Override
        protected void onPostExecute(History[] histories) {
            super.onPostExecute(histories);
            historiesLayout.removeAllViews();
            
            for (History h : histories) {
                final RelativeLayout itemLayout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.history_item_layout, null);
                final TextView authorNameTextView = (TextView) itemLayout.findViewById(R.id.authorNameTextView);
                final TextView commentTextView = (TextView) itemLayout.findViewById(R.id.commentTextView);
                final TextView dataTextView = (TextView) itemLayout.findViewById(R.id.dataTextView);
//                final ImageView photoIcon = (ImageView) itemLayout.findViewById(R.id.photoIcon);
//                photoIcon.setImageDrawable(h.getUser().getUserDrawable());
                
                authorNameTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
                commentTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
                dataTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
                
                if (h.getUser() != null) {
                    authorNameTextView.setText(h.getUser().getName());
                }
                commentTextView.setText(h.getMessage());
                dataTextView.setText(h.getDate(true));
                
                itemLayout.setTag(h);
//                itemLayout.setOnClickListener(documentClickListener);
                historiesLayout.addView(itemLayout);
            }
            if (histories.length == 0) {
                historiesLayout.setVisibility(View.GONE);
                discussionView.setVisibility(View.GONE);
            }
            
            commentsTextView.setText(String.valueOf(histories.length));
        }
    }
    class ExecTaskActionAsyncTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog pg = new ProgressDialog(getActivity());
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pg.setMessage(getResources().getString(R.string.exec_job_message_text));
            pg.setCancelable(false);
            pg.show();
        }
        @Override
        protected JSONObject doInBackground(String... params) {
            TaskDataSource tds = new TaskDataSource(mDirect);
            tds.open();
            tds.deleteTaskById(mTask.getId());
            return mDirect.ExecTaskAction(mTask.getId(), params[0]);
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            if (pg != null) {
                pg.dismiss();
            }
            getFragmentManager().popBackStackImmediate();
            /*
            int statusCode = result.optInt("statusCode");
            if (statusCode == 200) {
                String resultString = result.optString("result");
                if (resultString.equalsIgnoreCase("true")) {
                    Log.v(TAG, "Успешно");
                    Toast.makeText(getActivity(), "Успешно", Toast.LENGTH_LONG).show();
                } else {
                    Log.v(TAG, "Неудача");
                    Toast.makeText(getActivity(), "Неудача", Toast.LENGTH_LONG).show();
                }
            }
            // */
        }
    }
    //*/
    
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    private OnOpenFragmentListener mListener;
    
    public static final String TASK_KEY = "task_key";
    private Task mTask= null;
    
    private Button actionsView;
    private TextView taskTitleTextView, propertyTextView;
    // 
    private TextView dateTextView, stateTextView, importanceTextView, flagTextView, documentsTextView, jobsTextView, subtasksTextView, commentsTextView;
    
    private TextView documentsListLabel, historiesLabel, performersListLabel, resolutionListLabel;
    private LinearLayout documentsLayout, historiesLayout, performersLayout, resolutionLayout;
    
    private RelativeLayout taskCountLayout;
    private ScrollView mScrollView;
    
    private ImageView importanceView, attachmentView, discussionView;
    
    private Map<Integer, String> actionsMaping = new HashMap<Integer, String>(); 
    
    public static Fragment newInstance(Bundle args) {  // must pass some args
        TaskDetailFragment f = new TaskDetailFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_task_detail, container, false);
        
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
        Log.v(TAG, "mTask " + mTask.getCreated(true));
        
        actionsView = (Button)rootView.findViewById(R.id.actionsView);
        taskTitleTextView = (TextView)rootView.findViewById(R.id.taskTitleTextView);
        propertyTextView = (TextView)rootView.findViewById(R.id.propertyTextView);
        
        initViews(rootView);
        
        setFonts();
        updateData();
        //retainScrollViewPosition();
        return rootView;
    }
    private void initViews(View v) {
    	mScrollView = (ScrollView) v.findViewById(R.id.taskScrollView);
        ((TextView) v.findViewById(R.id.dateLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        ((TextView) v.findViewById(R.id.stateLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        ((TextView) v.findViewById(R.id.importanceLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        ((TextView) v.findViewById(R.id.flagLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        ((TextView) v.findViewById(R.id.documentsLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        ((TextView) v.findViewById(R.id.subtasksLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        ((TextView) v.findViewById(R.id.commentsLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        
        documentsListLabel = (TextView) v.findViewById(R.id.documentsListLabel);
        historiesLabel = (TextView) v.findViewById(R.id.historiesLabel);
        documentsListLabel.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        performersListLabel = (TextView) v.findViewById(R.id.performersListLabel);
        resolutionListLabel = (TextView) v.findViewById(R.id.resolutionListLabel);
        
        dateTextView = (TextView) v.findViewById(R.id.dateTextView);
        stateTextView = (TextView) v.findViewById(R.id.stateTextView);
        importanceTextView = (TextView) v.findViewById(R.id.importanceTextView);
        flagTextView = (TextView) v.findViewById(R.id.flagTextView);
        documentsTextView = (TextView) v.findViewById(R.id.documentsTextView);
        jobsTextView = (TextView) v.findViewById(R.id.jobsTextView);
        subtasksTextView = (TextView) v.findViewById(R.id.subtasksTextView);
        commentsTextView = (TextView) v.findViewById(R.id.commentsTextView);
        
        documentsLayout = (LinearLayout) v.findViewById(R.id.documentsLayout);
        historiesLayout = (LinearLayout) v.findViewById(R.id.historiesLayout);
        performersLayout = (LinearLayout) v.findViewById(R.id.performersLayout);
        resolutionLayout = (LinearLayout) v.findViewById(R.id.resolutionLayout);
        taskCountLayout = (RelativeLayout) v.findViewById(R.id.taskCountLayout);
        taskCountLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    Bundle args = new Bundle();
                    args.putLong(JobMyFragment.TASK_ID_KEY, mTask.getId());
                    mListener.OnOpenFragment(JobMyFragment.class.getName(), args);
                }
            }
        });
        
        importanceView = (ImageView) v.findViewById(R.id.importanceView);
        attachmentView = (ImageView) v.findViewById(R.id.attachmentView);
        discussionView = (ImageView) v.findViewById(R.id.discussionView);
        
        actionsView.setOnClickListener(actionsClickListener);
    }
    private void setFonts() {
        actionsView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        taskTitleTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        propertyTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        
        historiesLabel.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        performersListLabel.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        resolutionListLabel.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        
        for (TextView tv : new TextView[]{dateTextView, stateTextView, importanceTextView,
                flagTextView, documentsTextView, jobsTextView, subtasksTextView, commentsTextView}) {
            tv.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        }
    }
    private void updateData() {
        taskTitleTextView.setText(mTask.getTitle());
        String dateCreatedString = mTask.getCreated(true);
        if (dateCreatedString.equals("30/12/1899")) {
            dateCreatedString = "";
        }
        String deadlineString = mTask.getDeadline(true);
        if (deadlineString.equals("30/12/1899")) {
            deadlineString = "";
        }
        if (mTask.getAuthor() != null) {
            String property = String.format(getResources().getString(R.string.task_detail_fragment_property_text), mTask.getAuthor().getName(), dateCreatedString, deadlineString);
            propertyTextView.setText(property);
        }
        
        String dateString = mTask.getDeadline(true);
        if (dateString.equals("30/12/1899")) {
            dateTextView.setVisibility(View.INVISIBLE);
        }
        dateTextView.setText(dateString);
        stateTextView.setText(mTask.getState());
        importanceTextView.setText(mTask.getImportance());
        flagTextView.setText("");
        documentsTextView.setText(String.valueOf(mTask.getAttachmentCount()));
        jobsTextView.setText("0");
        subtasksTextView.setText(String.valueOf(mTask.getSubtaskCount()));
        commentsTextView.setText("0");
        
        importanceView.setVisibility(mTask.getImportance().equalsIgnoreCase("Высокая") ? View.VISIBLE : View.INVISIBLE);
        attachmentView.setVisibility(mTask.getAttachmentCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        
        new GetJobsAsyncTask().execute();
        new GetDocumentsAsyncTask().execute();
        new GetHistoriesAsyncTask().execute();
        new GetResolutionAsyncTask().execute();
    }
    OnClickListener actionsClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            Menu menu = popup.getMenu();
            actionsMaping.clear();
            try {
                JSONArray actions = new JSONArray(mTask.getActionList());
                for (int i=0; i<actions.length(); i++) {
                    final JSONObject action = actions.getJSONObject(i);
                    final String name = action.optString("Name");
                    final String title = action.optString("Title");
                    // Добавить в popupMenu
                    final MenuItem item = menu.add(Menu.NONE, new Random().nextInt(), Menu.NONE, title);
                    actionsMaping.put(item.getItemId(), name);
                }
            } catch (JSONException e) {
                Log.v(TAG, "Ошибка при парсинге Actions " + e.getMessage());
            }
            popup.setOnMenuItemClickListener(actionsMenuItemClickListener);
            popup.show();
        }
    };
    OnMenuItemClickListener actionsMenuItemClickListener = new OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            String actionName = actionsMaping.get(item.getItemId());
            new ExecTaskActionAsyncTask().execute(actionName);
            /*if (mDirect.isServiceAvailable()) {
                new ExecTaskActionAsyncTask().execute(actionName);
            } else {
                String errorText = getResources().getString(R.string.task_detail_fragment_internet_error_text);
                Toast.makeText(getActivity(), errorText, Toast.LENGTH_LONG).show();
            }
            // */
            return true;
        }
    };
    OnClickListener documentClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // Загрузка и открытие документа.
            Attachment doc = (Attachment)v.getTag();
//            Log.v(TAG, "documentClickListener " + doc.getName());
            boolean exist = mDirect.checkDocumentExist(doc);
//            Log.v(TAG, "doc exist " + exist);
            if (exist) {
//                Log.v(TAG, "open document");
                File myFile = mDirect.getDocumentFile(doc);
                try {
                    FileOpen.openFile(getActivity(), myFile);
                } catch (IOException e) {
                    Log.v(TAG, "Неудалось открыть документ " + e.getMessage());
                }
            } else {
                showDownloadDialog(doc);
            }
        }
    };
    OnClickListener resolutionClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	Log.v(TAG, "resolutionClickListener ");
        	if (mListener != null) {
        		// Открытие резолюции
//        		ReferenceHeader ref_header = (ReferenceHeader)v.getTag();
                Bundle args = new Bundle();
                args.putParcelable(ResolutionDetailFragment.TASK_KEY, mTask);
                mListener.OnOpenFragment(ResolutionDetailFragment.class.getName(), args);
                //saveScrollViewPosition();
            }
        }
    };
    private int scrollX, scrollY;
    private void saveScrollViewPosition() {
    	scrollX = mScrollView.getScrollX();
    	scrollY = mScrollView.getScrollY();
    }
    private void retainScrollViewPosition() {
    	mScrollView.post(new Runnable() {
			@Override
			public void run() {
				mScrollView.scrollTo(scrollX, scrollY);
			}
		});
    }
    private void showDownloadDialog(Attachment doc) {
        Log.v(TAG, "showDownloadDialog " + doc.getName());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = DocumentDownloadDialogFragment.newInstance(doc);
        newFragment.show(ft, "download_dialog");
    }
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
