package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TaskFragment extends Fragment implements OnClickListener {
    private static final String TAG = "JobImportantFragment";
    
    class GetTaskAsyncTask extends AsyncTask<Void, Void, List<Task>> {
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
        protected List<Task> doInBackground(Void... params) {
            TaskDataSource tds = new TaskDataSource(mDirect);
            tds.open();
            List<Task> tasks = tds.getAllTasksWithoutJobsSQL();
            tds.close();
            return tasks;
        }
        @Override
        protected void onPostExecute(List<Task> result) {
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
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    private OnOpenFragmentListener mListener;
    
    private ListView taskListView;
    private TaskListAdapter mAdapter;
//    private RelativeLayout listViewHeader;
    private CheckedTextView sortStateView, sortReadedView, sortDateView, sortTitleView;
    private ImageButton sortDirectionView;
    private boolean sortDesc = false;
    private EditText searchEditText;
    
    private boolean mRetained;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDirect = (DirectLeaderApplication) getActivity().getApplication();
        mSettings = mDirect.getSettings();
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task, container, false);
        
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
        
        taskListView = (ListView) rootView.findViewById(R.id.taskListView);

//        listViewHeader = (RelativeLayout)getActivity().getLayoutInflater().inflate(R.layout.list_header_job_important_layout, null);
//        taskListView.addHeaderView(listViewHeader, null, false);
//        taskListView.setHeaderDividersEnabled(false);
        
        mRetained = true;
        if (mAdapter == null) {
            mAdapter = new TaskListAdapter(getActivity(), new ArrayList<Task>(), taskListView);
            mRetained = false;
        }
//        mAdapter = new TaskListAdapter(getActivity(), new ArrayList<Task>(), taskListView);
        taskListView.setAdapter(mAdapter);
        taskListView.setOnItemClickListener(itemClickListener);
        
        setFonts();
        
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
        
        new GetTaskAsyncTask().execute();
        initSearch();
        retainListViewPosition();
        retainSortState();
        return rootView;
    }
    private void setFonts() {
        /*int count = listViewHeader.getChildCount();
        for (int i=0; i<count; i++) {
            final View view = listViewHeader.getChildAt(i);
            if (view.getClass().isInstance(TextView.class)) {
                ((TextView)view).setTypeface(mDirect.mPFDinDisplayPro_Reg);
            }
        }*/
        
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
    final private Comparator<Task> comp = new Comparator<Task>() {
        public int compare(Task t1, Task t2) {
            int result = 0;
            if (sortStateView.isChecked()) {
                result = t1.getState().toUpperCase(Utils.mLocale).compareTo(t2.getState().toUpperCase(Utils.mLocale));
            }
            if (sortReadedView.isChecked()) {
                result = t1.getCreated().toUpperCase(Utils.mLocale).compareTo(t2.getCreated().toUpperCase(Utils.mLocale));
            }
            if (sortDateView.isChecked()) {
                result = t1.getDeadline().compareTo(t2.getDeadline());
            }
            if (sortTitleView.isChecked()) {
                result = t1.getTitle().toUpperCase(Utils.mLocale).compareTo(t2.getTitle().toUpperCase(Utils.mLocale));
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
                Task task = mAdapter.getItem(pos-taskListView.getHeaderViewsCount());
                Bundle args = new Bundle();
                args.putParcelable(TaskDetailFragment.TASK_KEY, task);
                mListener.OnOpenFragment(TaskDetailFragment.class.getName(), args);
                saveListViewPosition();
                saveSortState();
            }
        }
    };
    private int listViewIndex = -1;
    private int listViewTop;
    private void saveListViewPosition() {
        try{
            listViewIndex = taskListView.getFirstVisiblePosition();
            View v = taskListView.getChildAt(0);
            listViewTop = (v == null) ? 0 : v.getTop();
         }
         catch(Throwable t){
            t.printStackTrace();
         }
    }
    private void retainListViewPosition() {
        if(listViewIndex != -1){
            taskListView.setSelectionFromTop(listViewIndex, listViewTop);
         }
    }
    private void saveSortState() {
        Editor e = mSettings.edit();
        e.putBoolean("task_sort_state", sortStateView.isChecked());
        e.putBoolean("task_sort_readed", sortReadedView.isChecked());
        e.putBoolean("task_sort_date", sortDateView.isChecked());
        e.putBoolean("task_sort_title", sortTitleView.isChecked());
        e.commit();
    }
    private void retainSortState() {
        sortStateView.setChecked(mSettings.getBoolean("task_sort_state", false));
        sortReadedView.setChecked(mSettings.getBoolean("task_sort_readed", false));
        sortDateView.setChecked(mSettings.getBoolean("task_sort_date", false));
        sortTitleView.setChecked(mSettings.getBoolean("task_sort_title", false));
    }
    @Override
    public void onClick(View v) {
        if (mListener != null) {
//            mListener.OnOpenFragment();
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnOpenFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnOpenFragmentListener");
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveSortState();
    }
}
