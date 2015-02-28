package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class JobMyFragment extends Fragment implements OnClickListener {
    private static final String TAG = "JobMyFragment";
    
    class GetJobAsyncTask extends AsyncTask<String, Void, List<Job>> {
        ProgressDialog pg;
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pg.setMessage(getResources().getString(R.string.task_loading_data_text));
            if (!mRetained) {
                pg.show();
            }
        };
        @Override
        protected List<Job> doInBackground(String... params) {
            String filter = "";
            if (params.length > 1) {
                filter = params[1];
            }
            JobDataSource jds = new JobDataSource(mDirect);
            jds.open();
            List<Job> jobs = jds.getJobByPerformerCode(params[0], filter);
            jds.close();
            return jobs;
        }
        @Override
        protected void onPostExecute(List<Job> result) {
            if (pg != null) {
                pg.dismiss();
            }
            mAdapter.clear();
            mAdapter.addAll(result);
            mAdapter.sort(comp);
            mAdapter.getFilter().filter(searchEditText.getText().toString());
            mAdapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }
    }
    class GetJobsByTaskIdAsyncTask extends AsyncTask<Void, Void, Job[]> {
        ProgressDialog pg;
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pg.setMessage(getResources().getString(R.string.task_loading_data_text));
            if (!mRetained) {
                pg.show();
            }
        };
        @Override
        protected Job[] doInBackground(Void... params) {
            JobDataSource jds = new JobDataSource(mDirect);
            jds.open();
            Job[] jobs = jds.getJobsByTaskIdWithAdditionalData(mTaskId);
            jds.close();
            return jobs;
        }
        @Override
        protected void onPostExecute(Job[] result) {
            if (pg != null) {
                pg.dismiss();
            }
            mAdapter.clear();
            mAdapter.addAll(result);
            mAdapter.sort(comp);
            mAdapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }
    }
    //*/
    public enum JobType {
        OVERDUE,
        CURRENT,
        ALL
    }
    public static final String TASK_ID_KEY = "task_id";
    public static final String STAFF_CODE_KEY = "staff_code";
    public static final String STAFF_FILTER_KEY = "staff_filter";
    private long mTaskId;
    
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    private OnOpenFragmentListener mListener;
    
    private ListView jobListView;
    private JobMyListAdapter mAdapter;
//    private RelativeLayout listViewHeader;
    private CheckedTextView sortStateView, sortReadedView, sortDateView, sortTitleView;
    private ImageButton sortDirectionView;
    private boolean sortDesc = false;
    private EditText searchEditText;
    
    private boolean mRetained;
    
    public static Fragment newInstance(Bundle args) {  // must pass some args
        JobMyFragment f = new JobMyFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_job_my, container, false);

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
        
        sortStateView = (CheckedTextView) rootView.findViewById(R.id.sortStateView);
        sortReadedView = (CheckedTextView) rootView.findViewById(R.id.sortReadedView);
        sortDateView = (CheckedTextView) rootView.findViewById(R.id.sortDateView);
        sortTitleView = (CheckedTextView) rootView.findViewById(R.id.sortTitleView);
        sortDirectionView = (ImageButton) rootView.findViewById(R.id.sortDirectionView);
        
        searchEditText = (EditText)rootView.findViewById(R.id.searchEditText);

        jobListView = (ListView) rootView.findViewById(R.id.jobsListView);

//        listViewHeader = (RelativeLayout)getActivity().getLayoutInflater().inflate(R.layout.list_header_job_important_layout, null);
//        jobListView.addHeaderView(listViewHeader, null, false);
//        jobListView.setHeaderDividersEnabled(false);
        
        mRetained = true;
        if (mAdapter == null) {
            mAdapter = new JobMyListAdapter(getActivity(), new ArrayList<Job>(), jobListView);
            mRetained = false;
        }
        jobListView.setAdapter(mAdapter);
        jobListView.setOnItemClickListener(itemClickListener);
        
        sortStateView.setOnClickListener(sortClickListener);
        sortReadedView.setOnClickListener(sortClickListener);
        sortDateView.setOnClickListener(sortClickListener);
        sortTitleView.setOnClickListener(sortClickListener);
        sortDirectionView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sortDesc = !sortDesc;
                mAdapter.sort(comp);
                mAdapter.notifyDataSetChanged();
            }
        });
        
        setFonts();
        initSearch();
        retainListViewPosition();
        retainSortState();
        
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(TASK_ID_KEY)) {
                mTaskId = args.getLong(TASK_ID_KEY);
                new GetJobsByTaskIdAsyncTask().execute();
            } else if (args.containsKey(STAFF_CODE_KEY)) {
                final String staffCode = args.getString(STAFF_CODE_KEY);
                final JobType filter = JobType.values()[args.getInt(STAFF_FILTER_KEY)];
                String filterStr = "";
                switch (filter) {
                    case CURRENT:
                        filterStr = " AND job.final_date = date()";
                        break;
                    case OVERDUE:
                        filterStr = " AND job.final_date < date()";
                        break;
                    case ALL:
                        filterStr = "";
                        break;
                }
                new GetJobAsyncTask().execute(staffCode, filterStr);
            }
        } else {
            new GetJobAsyncTask().execute(mDirect.getUserCode());
        }
        
        return rootView;
    }
    private void setFonts() {
//        int count = listViewHeader.getChildCount();
//        for (int i=0; i<count; i++) {
//            final View view = listViewHeader.getChildAt(i);
//            if (view.getClass().isInstance(TextView.class)) {
//                ((TextView)view).setTypeface(mDirect.mPFDinDisplayPro_Reg);
//            }
//        }
        
        sortStateView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortReadedView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortDateView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortTitleView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    }
    private void initSearch() {
        // Add Text Change Listener to EditText
        searchEditText.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call back the Adapter with current character to Filter
                Log.v(TAG, "onTextChanged");
                mAdapter.getFilter().filter(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    private int listViewIndex = -1;
    private int listViewTop;
    private void saveListViewPosition() {
        try{
            listViewIndex = jobListView.getFirstVisiblePosition();
            View v = jobListView.getChildAt(0);
            listViewTop = (v == null) ? 0 : v.getTop();
         }
         catch(Throwable t){
            t.printStackTrace();
         }
    }
    private void retainListViewPosition() {
        if(listViewIndex != -1){
            jobListView.setSelectionFromTop(listViewIndex, listViewTop);
         }
    }
    private void saveSortState() {
        Editor e = mSettings.edit();
        e.putBoolean("job_sort_state", sortStateView.isChecked());
        e.putBoolean("job_sort_readed", sortReadedView.isChecked());
        e.putBoolean("job_sort_date", sortDateView.isChecked());
        e.putBoolean("job_sort_title", sortTitleView.isChecked());
        e.commit();
    }
    private void retainSortState() {
        sortStateView.setChecked(mSettings.getBoolean("job_sort_state", false));
        sortReadedView.setChecked(mSettings.getBoolean("job_sort_readed", false));
        sortDateView.setChecked(mSettings.getBoolean("job_sort_date", false));
        sortTitleView.setChecked(mSettings.getBoolean("job_sort_title", false));
    }
    OnClickListener sortClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckedTextView view = (CheckedTextView)v;
            // Если кликнули по тому, который уже выделен, то ничего не делать.
            if (view.isChecked()) {
                return;
            }
            //Снять выделение со всех
            for (CheckedTextView cv : new CheckedTextView[]{sortStateView, sortReadedView, sortDateView, sortTitleView}) {
                cv.setChecked(false);
            }
            view.setChecked(true);
            mAdapter.sort(comp);
            mAdapter.notifyDataSetChanged();
        }
    };
    final private Comparator<Job> comp = new Comparator<Job>() {
        public int compare(Job j1, Job j2) {
            int result = 0;
            if (sortStateView.isChecked()) {
                boolean b1 = j1.getState();
                boolean b2 = j2.getState();
                if (b1 && !b2) {
                    result = 1;
                } else if (!b1 && b2) {
                    result = -1;
                } else {
                    result = 0;
                }
                if (sortDesc) {
                    return -result;
                } else {
                    return result;
                }
            }
            if (sortReadedView.isChecked()) {
                boolean b1 = j1.getReaded();
                boolean b2 = j2.getReaded();
                if (b1 && !b2) {
                    result = 1;
                } else if (!b1 && b2) {
                    result = -1;
                } else {
                    result = 0;
                }
                if (sortDesc) {
                    return -result;
                } else {
                    return result;
                }
            }
            if (sortDateView.isChecked()) {
                result = j1.getFinalDate().compareTo(j2.getFinalDate());
            }
            if (sortTitleView.isChecked()) {
                result = j1.getSubject().toUpperCase(Utils.mLocale).compareTo(j2.getSubject().toUpperCase(Utils.mLocale));
            }
            if (sortDesc) {
                return -result;
            } else {
                return result;
            }
        }
    };
    OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
            if (mListener != null) {
                Job job = mAdapter.getItem(pos-jobListView.getHeaderViewsCount());
                Log.v(TAG, "job.date_created " + job.getStartDate(true));
                Bundle args = new Bundle();
                args.putParcelable(JobDetailFragment.JOB_KEY, job);
                mListener.OnOpenFragment(JobDetailFragment.class.getName(), args);
                saveListViewPosition();
                saveSortState();
            }
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveSortState();
    }
}
