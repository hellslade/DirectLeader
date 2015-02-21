package ru.tasu.directleader;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class AuthorizeFragment extends Fragment implements OnClickListener {
    private static final String TAG = "AuthorizeFragment";
    
    class AuthorizeAsyncTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog pg;
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pg.setMessage(getResources().getString(R.string.authorize_process_message_text));
            pg.setCancelable(false);
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
                    mListener.OnLogin(true);
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
    
    private Button loginButton, continueButton; 
    private ImageButton settingsButton;
    private EditText loginEditText;
    private TextView successTextView;
    private LinearLayout bottomLinear, continueLinear;
    
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
        continueButton = (Button)rootView.findViewById(R.id.continueButton);
        successTextView = (TextView)rootView.findViewById(R.id.successTextView);
        
        bottomLinear = (LinearLayout) rootView.findViewById(R.id.bottomLinear);
        continueLinear = (LinearLayout) rootView.findViewById(R.id.continueLinear);
        
        loginButton.setOnClickListener(this);
        continueButton.setOnClickListener(this);
        
        settingsButton = (ImageButton) rootView.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(settingsClickListener);
        
        setFonts();
        checkAuth();
        return rootView;
    }
    private void setFonts() {
        loginButton.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        loginEditText.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        loginButton.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        successTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    }
    private void checkAuth() {
        String userCode = mDirect.getUserCode();
        if (!userCode.isEmpty()) {
            // Код пользователя сохранен в настройках, значит авторизация уже пройдена
            bottomLinear.setVisibility(View.GONE);
            continueLinear.setVisibility(View.VISIBLE);
        } else {
            // Кода пользователя нет в настройках, нужно пройти авторизацию
            bottomLinear.setVisibility(View.VISIBLE);
            continueLinear.setVisibility(View.GONE);
        }
    }
    OnClickListener settingsClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // Show popup menu
            PopupMenu popup = new PopupMenu(getActivity(), v);
            if (!mDirect.getUserCode().isEmpty()) {
                popup.inflate(R.menu.unauth_popup_menu);
            } else {
                popup.inflate(R.menu.auth_popup_menu);
            }
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_more:
                            return true;
                        case R.id.action_userchange:
                            // Разлогиниться
                            // Удалить БД?
                            mDirect.Logout();
                            checkAuth();
                            return true;
                        case R.id.action_activate:
                            return true;
                        case R.id.action_deactivate:
                            return true;
                        case R.id.action_geturl:
                            return true;
                    }
                    return false;
                }
            });
            
            popup.show();
        }
    };
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:
                new AuthorizeAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
                break;
            case R.id.continueButton:
                if (mListener != null) {
                    mListener.OnLogin(false);
                }
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
        public void OnLogin(boolean update);
    }
}
