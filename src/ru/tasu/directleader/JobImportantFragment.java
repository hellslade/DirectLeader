package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;

public class JobImportantFragment extends Fragment implements OnClickListener {
    private static final String TAG = "JobImportantFragment";
    
    class GetImportantJobAsyncTask extends AsyncTask<ViewType, Void, List<Job>> {
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
        protected List<Job> doInBackground(ViewType... params) {
            ViewType type = params[0];
            JobDataSource jds = new JobDataSource(mDirect);
            jds.open();
            List<Job> jobs = new ArrayList<Job>();
            switch (type) {
                case ALL:
                    Log.v(TAG, "ALL");
                    jobs = jds.getImportantFavoriteJobByPerformerCode(mDirect.getUserCode());
                    break;
                case FAVORITE:
                    Log.v(TAG, "FAVORITE");
                    jobs = jds.getFavoriteJobByPerformerCode(mDirect.getUserCode());
                    break;
                case IMPORTANT:
                    Log.v(TAG, "IMPORTANT");
                    jobs = jds.getImportantJobByPerformerCode(mDirect.getUserCode());
                    break;
            }
//            List<Job> jobs = jds.getImportantJobByPerformerCode(mDirect.getUserCode());
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
    
    private enum ViewType {
        ALL,
        IMPORTANT,
        FAVORITE
    }
    
    //*/
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    private OnOpenFragmentListener mListener;
    
    private ListView jobListView;
    private JobImportantListAdapter mAdapter;
//    private RelativeLayout listViewHeader;
    private CheckedTextView sortStateView, sortReadedView, sortDateView, sortTitleView;
    private ImageButton sortDirectionView;
    private Button viewButton;
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
        View rootView = inflater.inflate(R.layout.fragment_job_important, container, false);
        
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
        viewButton = (Button) rootView.findViewById(R.id.viewButton);
        
        searchEditText = (EditText)rootView.findViewById(R.id.searchEditText);

        jobListView = (ListView) rootView.findViewById(R.id.jobsListView);

        mRetained = true;
        if (mAdapter == null) {
            mAdapter = new JobImportantListAdapter(getActivity(), new ArrayList<Job>(), jobListView);
            mRetained = false;
        }
        jobListView.setAdapter(mAdapter);
        jobListView.setOnItemClickListener(itemClickListener);
        
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
        
        viewButton.setOnClickListener(viewClickListener);
        
        new GetImportantJobAsyncTask().execute(ViewType.ALL);
        initSearch();
        retainListViewPosition();
        retainSortState();
        return rootView;
    }
    private void setFonts() {
        sortStateView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortReadedView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortDateView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortTitleView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        viewButton.setTypeface(mDirect.mPFDinDisplayPro_Reg);

        searchEditText.setTypeface(mDirect.mPFDinDisplayPro_Reg);
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
        e.putBoolean("jobimportant_sort_state", sortStateView.isChecked());
        e.putBoolean("jobimportant_sort_readed", sortReadedView.isChecked());
        e.putBoolean("jobimportant_sort_date", sortDateView.isChecked());
        e.putBoolean("jobimportant_sort_title", sortTitleView.isChecked());
        e.commit();
    }
    private void retainSortState() {
        sortStateView.setChecked(mSettings.getBoolean("jobimportant_sort_state", false));
        sortReadedView.setChecked(mSettings.getBoolean("jobimportant_sort_readed", false));
        sortDateView.setChecked(mSettings.getBoolean("jobimportant_sort_date", false));
        sortTitleView.setChecked(mSettings.getBoolean("jobimportant_sort_title", false));
    }
    OnClickListener viewClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            popup.inflate(R.menu.view_favorite_popup_menu);

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_all:
                            new GetImportantJobAsyncTask().execute(ViewType.ALL);
                            return true;
                        case R.id.action_favorite:
                            new GetImportantJobAsyncTask().execute(ViewType.FAVORITE);
                            return true;
                        case R.id.action_important:
                            new GetImportantJobAsyncTask().execute(ViewType.IMPORTANT);
                            return true;
                    }
                    return false;
                }
            });
            
            popup.show();
        }
    };
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
                Log.v(TAG, "job.author " + job.getAuthor());
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
