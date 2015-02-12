package ru.tasu.directleader;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AuthorizeFragment extends Fragment implements OnClickListener {
    private static final String TAG = "AuthorizeFragment";
    
    class AuthorizeAsyncTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog pg;
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pg.setMessage(getResources().getString(R.string.authorize_process_message_text));
            pg.show();
        };
        @Override
        protected JSONObject doInBackground(String... params) {
            return mDirect.CheckAuth();
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            if (pg != null) {
                pg.dismiss();
            }
            Log.v(TAG, "" + result);
            String userId = result.optString("user_id");
            if (!userId.isEmpty()) {
                // Авторизация удачна, получили идентификатор пользователя
                if (mListener != null) {
                    mListener.OnLogin();
                }
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.authorize_failed_message_text), Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }
    }
    
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    private OnLoginListener mListener;
    
    private Button loginButton;
    private EditText loginEditText;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDirect = (DirectLeaderApplication) getActivity().getApplication();
        mSettings = mDirect.getSettings();
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_authorize, container, false);
        
        loginButton = (Button)rootView.findViewById(R.id.loginButton);
        loginEditText = (EditText)rootView.findViewById(R.id.loginEditText);
        
        loginButton.setOnClickListener(this);
        
        setFonts();
        return rootView;
    }
    private void setFonts() {
        loginButton.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        loginEditText.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:
                new AuthorizeAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
                break;
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnLoginListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnLoginListener");
        }
    }
    public interface OnLoginListener {
        public void OnLogin();
    }
}
