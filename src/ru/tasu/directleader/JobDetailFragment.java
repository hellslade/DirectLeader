package ru.tasu.directleader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class JobDetailFragment extends Fragment implements OnClickListener {
    private static final String TAG = "JobDetailFragment";
    
    class GetJobAsyncTask extends AsyncTask<Void, Void, Job> {
        ProgressDialog pg;
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pg.setMessage(getResources().getString(R.string.task_loading_data_text));
            pg.show();
        };
        @Override
        protected Job doInBackground(Void... params) {
            JobDataSource jds = new JobDataSource(mDirect);
            jds.open();
            Job job = jds.getJobById(mJob.getId());
            jds.close();
            return job;
        }
        @Override
        protected void onPostExecute(Job result) {
            if (pg != null) {
                pg.dismiss();
            }
            super.onPostExecute(result);
        }
    }
    class GetDocumentsAsyncTask extends AsyncTask<Void, Void, Attachment[]> {
        @Override
        protected Attachment[] doInBackground(Void... params) {
            AttachmentDataSource ds = new AttachmentDataSource(mDirect);
            ds.open();
            return ds.getAttachmentsByTaskId(mJob.getMainTaskJob());
        }
        @Override
        protected void onPostExecute(Attachment[] attachments) {
            super.onPostExecute(attachments);
            documentsLayout.removeAllViews();
            
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
        }
    }
    class GetHistoriesAsyncTask extends AsyncTask<Void, Void, History[]> {
        @Override
        protected History[] doInBackground(Void... params) {
            HistoryDataSource ds = new HistoryDataSource(mDirect);
            ds.open();
            return ds.getHistoriesByTaskId(mJob.getMainTaskJob());
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
                
                authorNameTextView.setText(h.getUser().getName());
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
        }
    }
    class ExecJobActionAsyncTask extends AsyncTask<String, Void, JSONObject> {
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
            return mDirect.ExecJobAction(mJob.getId(), params[0], commentEditText.getText().toString().trim());
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            if (pg != null) {
                pg.dismiss();
            }
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
        }
    }
    //*/
    
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    private OnOpenFragmentListener mListener;
    
    public static final String JOB_KEY = "job_key";
    private Job mJob= null;
    
    private Button actionsView, subtaskView;
    private TextView taskTitleTextView, propertyTextView;
    // 
    private TextView performerTextView, dateTextView, stateTextView, importanceTextView, flagTextView, documentsTextView, startDateTextView;
    
    private TextView documentsListLabel, historiesLabel;
    private LinearLayout documentsLayout, historiesLayout;
    
    private EditText commentEditText;
    private CheckBox favoriteView;
    
    private ImageView importanceView, attachmentView, discussionView;
    
    private Map<Integer, String> actionsMaping = new HashMap<Integer, String>(); 
    
    public static Fragment newInstance(Bundle args) {  // must pass some args
        JobDetailFragment f = new JobDetailFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_job_detail, container, false);
        
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
        mJob = args.getParcelable(JOB_KEY);
        if (mJob == null) {
            // Закрыть фрагмент?
        }
        Log.v(TAG, "mJob " + mJob.getStartDate());
        
        actionsView = (Button)rootView.findViewById(R.id.actionsView);
        subtaskView = (Button)rootView.findViewById(R.id.subtaskView);
        taskTitleTextView = (TextView)rootView.findViewById(R.id.taskTitleTextView);
        propertyTextView = (TextView)rootView.findViewById(R.id.propertyTextView);
        
        initViews(rootView);
        
        setFonts();
        updateData();
        return rootView;
    }
    private void initViews(View v) {
        ((TextView) v.findViewById(R.id.performerLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        ((TextView) v.findViewById(R.id.dateLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        ((TextView) v.findViewById(R.id.stateLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        ((TextView) v.findViewById(R.id.importanceLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        ((TextView) v.findViewById(R.id.flagLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        ((TextView) v.findViewById(R.id.documentsLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        ((TextView) v.findViewById(R.id.startDateLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        ((TextView) v.findViewById(R.id.commentLabel)).setTypeface(mDirect.mPFDinDisplayPro_Reg);
        
        documentsListLabel = (TextView) v.findViewById(R.id.documentsListLabel);
        documentsListLabel.setTypeface(mDirect.mPFDinDisplayPro_Reg);;
        historiesLabel = (TextView) v.findViewById(R.id.historiesLabel);
        historiesLabel.setTypeface(mDirect.mPFDinDisplayPro_Reg);;
        
        performerTextView = (TextView) v.findViewById(R.id.performerTextView);
        dateTextView = (TextView) v.findViewById(R.id.dateTextView);
        stateTextView = (TextView) v.findViewById(R.id.stateTextView);
        importanceTextView = (TextView) v.findViewById(R.id.importanceTextView);
        flagTextView = (TextView) v.findViewById(R.id.flagTextView);
        documentsTextView = (TextView) v.findViewById(R.id.documentsTextView);
        startDateTextView = (TextView) v.findViewById(R.id.startDateTextView);
        
        documentsLayout = (LinearLayout) v.findViewById(R.id.documentsLayout);
        historiesLayout = (LinearLayout) v.findViewById(R.id.historiesLayout);
        
        commentEditText = (EditText) v.findViewById(R.id.commentEditText);
        favoriteView = (CheckBox) v.findViewById(R.id.favoriteView);
        
        importanceView = (ImageView) v.findViewById(R.id.importanceView);
        attachmentView = (ImageView) v.findViewById(R.id.attachmentView);
        discussionView = (ImageView) v.findViewById(R.id.discussionView);
        
        favoriteView.setOnCheckedChangeListener(favoriteCheckListener);
        
        actionsView.setOnClickListener(actionsClickListener);
        subtaskView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.OnTaskCreate(">> " + mJob.getSubject());
                }
            }
        });
    }
    private void setFonts() {
        actionsView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        subtaskView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        taskTitleTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        propertyTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        
        for (TextView tv : new TextView[]{performerTextView, dateTextView, stateTextView, importanceTextView,
                flagTextView, documentsTextView, startDateTextView}) {
            tv.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        }
        commentEditText.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    }
    private void updateData() {
        taskTitleTextView.setText(mJob.getSubject());
        String startDateString = mJob.getStartDate(true);
        if (startDateString.equals("1899-12-30")) {
            startDateString = "";
        }
        String endDateString = mJob.getEndDate(true);
        if (endDateString.equals("1899-12-30")) {
            endDateString = "";
        }
        Log.v(TAG, "mJob.getAuthor() " + mJob.getAuthor());
        String property = String.format(getResources().getString(R.string.job_detail_fragment_property_text), mJob.getAuthor().getName(), startDateString, endDateString);
        propertyTextView.setText(property);
        
        performerTextView.setText(mJob.getUser().getName());
        String dateString = mJob.getFinalDate(true);
        if (dateString.equals("1899-12-30")) {
            dateTextView.setVisibility(View.INVISIBLE);
        }
        dateTextView.setText(dateString);
        stateTextView.setText(mJob.getStateTitle());
        importanceTextView.setText(mJob.getImportance());
        flagTextView.setText("");
        documentsTextView.setText(String.valueOf(mJob.getAttachmentCount()));
        startDateString = mJob.getStartDate(true);
        if (startDateString.equals("1899-12-30")) {
            startDateTextView.setVisibility(View.INVISIBLE);
        }
        startDateTextView.setText(startDateString);
        
        importanceView.setVisibility(mJob.getImportance().equalsIgnoreCase("Высокая") ? View.VISIBLE : View.INVISIBLE);
        attachmentView.setVisibility(mJob.getAttachmentCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        
        favoriteView.setChecked(mJob.getFavorite());
        
        new GetDocumentsAsyncTask().execute();
        new GetHistoriesAsyncTask().execute();
    }
    OnCheckedChangeListener favoriteCheckListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            JobDataSource jds = new JobDataSource(mDirect);
            jds.open();
            mJob = jds.setJobFavorite(mJob.getId(), isChecked);
            favoriteView.setChecked(mJob.getFavorite());
            //updateData();
        }
    };
    OnClickListener actionsClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            Menu menu = popup.getMenu();
            actionsMaping.clear();
            try {
                JSONArray actions = new JSONArray(mJob.getActionList());
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
            if (mDirect.isOnline()) {
                new ExecJobActionAsyncTask().execute(actionName);
            } else {
                String errorText = getResources().getString(R.string.job_detail_fragment_internet_error_text);
                Toast.makeText(getActivity(), errorText, Toast.LENGTH_LONG).show();
            }
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
