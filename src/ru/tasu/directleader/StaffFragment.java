package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.Comparator;

import ru.tasu.directleader.JobMyFragment.JobType;
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

public class StaffFragment extends Fragment implements OnClickListener {
    private static final String TAG = "StaffFragment";
    
    class GetRabotnicsAsyncTask extends AsyncTask<Void, Void, Rabotnic[]> {
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
        protected Rabotnic[] doInBackground(Void... params) {
            RabotnicDataSource rds = new RabotnicDataSource(mDirect);
            rds.open();
            Rabotnic[] jobs = rds.getAllRabotnicsWithTaskCount();
            rds.close();
            return jobs;
        }
        @Override
        protected void onPostExecute(Rabotnic[] result) {
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
    //*/
    private long mTaskId;
    
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    private OnOpenFragmentListener mListener;
    
    private ListView staffListView;
    private StaffListAdapter mAdapter;
//    private RelativeLayout listViewHeader;
    private CheckedTextView sortFioView, sortOverdueView, sortTodayView, sortTotalView;
    private ImageButton sortDirectionView;
    private boolean sortDesc = false;
    private EditText searchEditText;
    
    private boolean mRetained;
    
    public static Fragment newInstance(Bundle args) {  // must pass some args
        StaffFragment f = new StaffFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_staff, container, false);

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
        
        sortFioView = (CheckedTextView) rootView.findViewById(R.id.sortFioView);
        sortOverdueView = (CheckedTextView) rootView.findViewById(R.id.sortOverdueView);
        sortTodayView = (CheckedTextView) rootView.findViewById(R.id.sortTodayView);
        sortTotalView = (CheckedTextView) rootView.findViewById(R.id.sortTotalView);
        sortDirectionView = (ImageButton) rootView.findViewById(R.id.sortDirectionView);
        
        searchEditText = (EditText)rootView.findViewById(R.id.searchEditText);

        staffListView = (ListView) rootView.findViewById(R.id.staffListView);

//        listViewHeader = (RelativeLayout)getActivity().getLayoutInflater().inflate(R.layout.list_header_job_important_layout, null);
//        staffListView.addHeaderView(listViewHeader, null, false);
//        staffListView.setHeaderDividersEnabled(false);
        
        mRetained = true;
        if (mAdapter == null) {
            mAdapter = new StaffListAdapter(getActivity(), new ArrayList<Rabotnic>(), staffListView, jobsClickListener);
            mRetained = false;
        }
        staffListView.setAdapter(mAdapter);
        staffListView.setOnItemClickListener(itemClickListener);
        
        setFonts();
        
        sortFioView.setOnClickListener(sortClickListener);
        sortOverdueView.setOnClickListener(sortClickListener);
        sortTodayView.setOnClickListener(sortClickListener);
        sortTotalView.setOnClickListener(sortClickListener);
        sortDirectionView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sortDesc = !sortDesc;
                mAdapter.sort(comp);
                mAdapter.notifyDataSetChanged();
            }
        });
        
        new GetRabotnicsAsyncTask().execute();
        initSearch();
        retainListViewPosition();
        retainSortState();
        return rootView;
    }
    private void setFonts() {
        sortFioView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortOverdueView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortTodayView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortTotalView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        
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
    OnClickListener jobsClickListener = new OnClickListener() {
        // Тап по количеству заданий сотрудника
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                JobType filter = JobType.ALL;
                switch (v.getId()) {
                    case R.id.redTextView:
                        filter = JobType.OVERDUE;
                        break;
                    case R.id.yellowTextView:
                        filter = JobType.CURRENT;
                        break;
                    case R.id.greenTextView:
                        filter = JobType.ALL;
                        break;
                }
                int pos = (Integer) v.getTag();
                Rabotnic rabotnic = mAdapter.getItem(pos);
                Bundle args = new Bundle();
                args.putString(JobMyFragment.STAFF_CODE_KEY, rabotnic.getCode());
                args.putInt(JobMyFragment.STAFF_FILTER_KEY, filter.ordinal());
                mListener.OnOpenFragment(JobMyFragment.class.getName(), args);
                saveListViewPosition();
                saveSortState();
            }
        }
    };
    private int listViewIndex = -1;
    private int listViewTop;
    private void saveListViewPosition() {
        try{
            listViewIndex = staffListView.getFirstVisiblePosition();
            View v = staffListView.getChildAt(0);
            listViewTop = (v == null) ? 0 : v.getTop();
         }
         catch(Throwable t){
            t.printStackTrace();
         }
    }
    private void retainListViewPosition() {
        if(listViewIndex != -1){
            staffListView.setSelectionFromTop(listViewIndex, listViewTop);
         }
    }
    private void saveSortState() {
        Editor e = mSettings.edit();
        e.putBoolean("staff_sort_fio", sortFioView.isChecked());
        e.putBoolean("staff_sort_overdue", sortOverdueView.isChecked());
        e.putBoolean("staff_sort_today", sortTodayView.isChecked());
        e.putBoolean("staff_sort_total", sortTotalView.isChecked());
        e.commit();
    }
    private void retainSortState() {
        sortFioView.setChecked(mSettings.getBoolean("staff_sort_fio", false));
        sortOverdueView.setChecked(mSettings.getBoolean("staff_sort_overdue", false));
        sortTodayView.setChecked(mSettings.getBoolean("staff_sort_today", false));
        sortTotalView.setChecked(mSettings.getBoolean("staff_sort_total", false));
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
            for (CheckedTextView cv : new CheckedTextView[]{sortFioView, sortOverdueView, sortTodayView, sortTotalView}) {
                cv.setChecked(false);
            }
            view.setChecked(true);
            mAdapter.sort(comp);
            mAdapter.notifyDataSetChanged();
        }
    };
    final private Comparator<Rabotnic> comp = new Comparator<Rabotnic>() {
        public int compare(Rabotnic r1, Rabotnic r2) {
            int result = 0;
            if (sortFioView.isChecked()) {
                result = r1.getName().toUpperCase(Utils.mLocale).compareTo(r2.getName().toUpperCase(Utils.mLocale));
            }
            if (sortOverdueView.isChecked()) {
                int b1 = r1.getOverdueJobs();
                int b2 = r2.getOverdueJobs();
                if (b1 > b2) {
                    result = -1;
                } else if (b1 < b2) {
                    result = 1;
                } else {
                    result = 0;
                }
            }
            if (sortTodayView.isChecked()) {
                int b1 = r1.getCurrentJobs();
                int b2 = r2.getCurrentJobs();
                if (b1 > b2) {
                    result = -1;
                } else if (b1 < b2) {
                    result = 1;
                } else {
                    result = 0;
                }
            }
            if (sortTotalView.isChecked()) {
                int b1 = r1.getTotalJobs();
                int b2 = r2.getTotalJobs();
                if (b1 > b2) {
                    result = -1;
                } else if (b1 < b2) {
                    result = 1;
                } else {
                    result = 0;
                }
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
                Rabotnic rabotnic = mAdapter.getItem(pos);
                Bundle args = new Bundle();
//                args.putParcelable(StaffDetailFragment.STAFF_KEY, rabotnic);
//                mListener.OnOpenFragment(StaffDetailFragment.class.getName(), args);
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
