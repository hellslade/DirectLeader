package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class JobImportantFragment extends Fragment implements OnClickListener {
    private static final String TAG = "JobImportantFragment";
    
    class GetImportantJobAsyncTask extends AsyncTask<Void, Void, List<Job>> {
        ProgressDialog pg;
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pg.setMessage(getResources().getString(R.string.task_loading_data_text));
            pg.show();
        };
        @Override
        protected List<Job> doInBackground(Void... params) {
            JobDataSource jds = new JobDataSource(mDirect);
            jds.open();
            List<Job> jobs = jds.getImportantJobByPerformerCode(mDirect.getUserCode());
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
            mAdapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }
    }
    //*/
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    private OnOpenFragmentListener mListener;
    
    private ListView jobListView;
    private JobImportantListAdapter mAdapter;
    private RelativeLayout listViewHeader;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDirect = (DirectLeaderApplication) getActivity().getApplication();
        mSettings = mDirect.getSettings();
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_job_important, container, false);

        jobListView = (ListView) rootView.findViewById(R.id.jobsListView);

        listViewHeader = (RelativeLayout)getActivity().getLayoutInflater().inflate(R.layout.list_header_job_important_layout, null);
        jobListView.addHeaderView(listViewHeader, null, false);
        jobListView.setHeaderDividersEnabled(false);
        
        mAdapter = new JobImportantListAdapter(getActivity(), new ArrayList<Job>(), jobListView);
        jobListView.setAdapter(mAdapter);
        
        new GetImportantJobAsyncTask().execute();
        setFonts();
        return rootView;
    }
    private void setFonts() {
        int count = listViewHeader.getChildCount();
        for (int i=0; i<count; i++) {
            final View view = listViewHeader.getChildAt(i);
            if (view.getClass().isInstance(TextView.class)) {
                ((TextView)view).setTypeface(mDirect.mPFDinDisplayPro_Reg);
            }
        }
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
}
