package ru.tasu.directleader;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainFragment extends Fragment implements OnClickListener, OnUpdateDataListener {
    private static final String TAG = "MainFragment";
    
    class GetCountOfAttachments extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            AttachmentDataSource ds = new AttachmentDataSource(mDirect);
            ds.open();
            int count = ds.getCountOfAttachments();
            ds.close();
            return count;
        }
        @Override
        protected void onPostExecute(Integer result) {
            documentsView.setGreenCount(result);
            super.onPostExecute(result);
        }
    }
    class GetCountOfStaffTasks extends AsyncTask<Void, Void, Integer[]> {
        @Override
        protected Integer[] doInBackground(Void... params) {
            TaskDataSource ds = new TaskDataSource(mDirect);
            ds.open();
            int[] count = ds.getCountOfStaffTasks();
            ds.close();
            return new Integer[]{count[0], count[1], count[2]};
        }
        @Override
        protected void onPostExecute(Integer[] result) {
            myStaffTaskView.setGreenCount(result[0]);
            myStaffTaskView.setYellowCount(result[1]);
            myStaffTaskView.setRedCount(result[2]);
            super.onPostExecute(result);
        }
    }
    
    class GetCountOfImportantJobAsyncTask extends AsyncTask<Void, Void, Integer[]> {
        ProgressDialog pg;
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pg.setMessage(getResources().getString(R.string.task_loading_data_text));
//            pg.show();
        };
        protected Integer[] doInBackground(Void... params) {
            JobDataSource ds = new JobDataSource(mDirect);
            ds.open();
            int[] count = ds.getCountOfImportantJobByPerformerCode(mDirect.getUserCode());
            ds.close();
            return new Integer[]{count[0], count[1], count[2]};
        }
        @Override
        protected void onPostExecute(Integer[] result) {
            if (pg != null) {
                pg.dismiss();
            }
            jobImportantView.setGreenCount(result[0]);
            jobImportantView.setYellowCount(result[1]);
            jobImportantView.setRedCount(result[2]);
            super.onPostExecute(result);
        }
    }
    class GetCountOfJobAsyncTask extends AsyncTask<Void, Void, Integer[]> {
        ProgressDialog pg;
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pg.setMessage(getResources().getString(R.string.task_loading_data_text));
//            pg.show();
        };
        protected Integer[] doInBackground(Void... params) {
            JobDataSource ds = new JobDataSource(mDirect);
            ds.open();
            int[] count = ds.getCountOfJobByPerformerCode(mDirect.getUserCode());
            ds.close();
            return new Integer[]{count[0], count[1], count[2]};
        }
        @Override
        protected void onPostExecute(Integer[] result) {
            if (pg != null) {
                pg.dismiss();
            }
            myJobView.setGreenCount(result[0]);
            myJobView.setYellowCount(result[1]);
            myJobView.setRedCount(result[2]);
            Log.v(TAG, "greenCount " + result[0]);
            Log.v(TAG, "yellowCount " + result[1]);
            Log.v(TAG, "redCount " + result[2]);
            super.onPostExecute(result);
        }
    }
    //*/
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    private OnOpenFragmentListener mListener;

    private ImageButton refreshDataView;
    
    private BookCompoundView jobImportantView, myJobView, myStaffTaskView, documentsView, staffView, reportView, calendarView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDirect = (DirectLeaderApplication) getActivity().getApplication();
        mSettings = mDirect.getSettings();
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        jobImportantView = (BookCompoundView)rootView.findViewById(R.id.jobImportantView);
        myJobView = (BookCompoundView)rootView.findViewById(R.id.myJobView);
        myStaffTaskView = (BookCompoundView)rootView.findViewById(R.id.myStaffTaskView);
        documentsView = (BookCompoundView)rootView.findViewById(R.id.documentsView);
        
        staffView = (BookCompoundView)rootView.findViewById(R.id.staffView);
        reportView = (BookCompoundView)rootView.findViewById(R.id.reportView);
        calendarView = (BookCompoundView)rootView.findViewById(R.id.calendarView);
        
        jobImportantView.setOnClickListener(this);
        myJobView.setOnClickListener(this);
        myStaffTaskView.setOnClickListener(this);
        documentsView.setOnClickListener(this);
        staffView.setOnClickListener(this);
        reportView.setOnClickListener(this);
        calendarView.setOnClickListener(this);
        
        refreshDataView = (ImageButton) rootView.findViewById(R.id.refreshDataView);
        refreshDataView.setOnClickListener(this);
        
        setFonts();
        updateData();
        return rootView;
    }
    private void updateData() {
        new GetCountOfImportantJobAsyncTask().execute();
        new GetCountOfJobAsyncTask().execute();
        new GetCountOfAttachments().execute();
        new GetCountOfStaffTasks().execute();
    }
    private void setFonts() {
        jobImportantView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        myJobView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        myStaffTaskView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        documentsView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        staffView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        reportView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        calendarView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
    }
    @Override
    public void onClick(View v) {
        if (mListener != null) {
            switch (v.getId()) {
                case R.id.jobImportantView:
                    mListener.OnOpenFragment(JobImportantFragment.class.getName());
                    break;
                case R.id.myJobView:
                    mListener.OnOpenFragment(JobMyFragment.class.getName());
                    break;
                case R.id.documentsView:
                    mListener.OnOpenFragment(DocumentsFragment.class.getName());
                    break;
                case R.id.myStaffTaskView:
                    mListener.OnOpenFragment(TaskFragment.class.getName());
                    break;
                case R.id.staffView:
                    mListener.OnOpenFragment(StaffFragment.class.getName());
                    break;
                case R.id.reportView:
//                    mListener.OnOpenFragment(JobImportantFragment.class.getName());
                    break;
                case R.id.calendarView:
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("content://com.android.calendar/time"));
                    startActivity(i);
                    break;
                case R.id.refreshDataView:
                    mListener.OnRefreshData();
                    break;
            }
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
    public void OnUpdateData() {
        Log.v(TAG, "OnUpdateData");
        updateData();
        Toast.makeText(getActivity(), "updated", Toast.LENGTH_LONG).show();
    }

}
abstract interface OnUpdateDataListener {
    public void OnUpdateData();
}
abstract interface OnOpenFragmentListener {
    public void OnOpenFragment(String fragmentClassName);
    public void OnOpenFragment(String fragmentClassName, Bundle args);
    public void OnRefreshData();
}

