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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class StaffFragment extends Fragment implements OnClickListener {
    private static final String TAG = "StaffFragment";
    
    class GetRabotnicsAsyncTask extends AsyncTask<Void, Void, Rabotnic[]> {
        ProgressDialog pg;
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pg.setMessage(getResources().getString(R.string.task_loading_data_text));
            pg.show();
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
    private RelativeLayout listViewHeader;
    private CheckedTextView sortFioView, sortOverdueView, sortTodayView, sortTotalView;
    private ImageButton sortDirectionView;
    private boolean sortDesc = false;
    
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

        sortFioView = (CheckedTextView) rootView.findViewById(R.id.sortFioView);
        sortOverdueView = (CheckedTextView) rootView.findViewById(R.id.sortOverdueView);
        sortTodayView = (CheckedTextView) rootView.findViewById(R.id.sortTodayView);
        sortTotalView = (CheckedTextView) rootView.findViewById(R.id.sortTotalView);
        sortDirectionView = (ImageButton) rootView.findViewById(R.id.sortDirectionView);

        staffListView = (ListView) rootView.findViewById(R.id.staffListView);

//        listViewHeader = (RelativeLayout)getActivity().getLayoutInflater().inflate(R.layout.list_header_job_important_layout, null);
//        staffListView.addHeaderView(listViewHeader, null, false);
//        staffListView.setHeaderDividersEnabled(false);
        
        mAdapter = new StaffListAdapter(getActivity(), new ArrayList<Rabotnic>(), staffListView);
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
        
        sortFioView.performClick();
        
        new GetRabotnicsAsyncTask().execute();
        
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
        
        sortFioView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortOverdueView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortTodayView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortTotalView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
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
//                Bundle args = new Bundle();
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
    
}
