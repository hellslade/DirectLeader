package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class UsersDialogFragment extends DialogFragment {
    private static final String TAG = "UsersDialogFragment";
    
    public enum UserType {
        PERFORMER,
        OBSERVER
    }
    
    class GetUsersAsyncTask extends AsyncTask<Void, Void, List<Rabotnic>> {
        ProgressDialog pg = new ProgressDialog(getActivity());
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pg.setCancelable(false);
            pg.setMessage(getResources().getString(R.string.task_loading_data_text));
            pg.show();
        }
        @Override
        protected List<Rabotnic> doInBackground(Void... params) {
            RabotnicDataSource rds = new RabotnicDataSource(getActivity());
            rds.open();
            List<Rabotnic> rabs = rds.getAllRabotnics();
            rds.close();
            return rabs;
        }
        @Override
        protected void onPostExecute(List<Rabotnic> result) {
            super.onPostExecute(result);
            if (pg != null) {
                pg.dismiss();
            }
            mAdapter.clear();
            mAdapter.addAll(result);
            mAdapter.notifyDataSetChanged();
        }
    }
    
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    
    private TextView searchEditText;
    private ListView usersListView;
    
    private UsersListAdapter mAdapter;
    
    private OnUserSelectListener mListener;
    
    static UsersDialogFragment newInstance() {
        UsersDialogFragment f = new UsersDialogFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDirect = (DirectLeaderApplication) getActivity().getApplication();
        mSettings = mDirect.getSettings();
        
        int style = DialogFragment.STYLE_NO_TITLE;
        int theme = android.R.style.Theme_Holo_Light_Dialog;
        setCancelable(true);
        setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_users_dialog, container, false);
        
        searchEditText = (TextView)v.findViewById(R.id.searchEditText);
        usersListView = (ListView)v.findViewById(R.id.usersListView);
        
        mAdapter = new UsersListAdapter(getActivity(), new ArrayList<Rabotnic>(), usersListView);
        usersListView.setAdapter(mAdapter);
        
        setFonts();
        initSearch();
        
        new GetUsersAsyncTask().execute();
        usersListView.setOnItemClickListener(itemClickListener);
        return v;
    }
    private void setFonts() {
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
    OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View v, int pos, long id) {
            if (mListener != null) {
                mListener.onUserSelect(mAdapter.getItem(pos));
                mAdapter.remove(mAdapter.getItem(pos));
            }
        }
    };
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnUserSelectListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnUserSelectListener");
        }
    };
    public interface OnUserSelectListener {
        public void onUserSelect(Rabotnic user);
    }
}