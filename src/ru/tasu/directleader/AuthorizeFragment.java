package ru.tasu.directleader;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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
            return mDirect.CheckAuth(loginEditText.getText().toString().trim(), passwordEditText.getText().toString().trim());
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
    class AddNewDeviceAsyncTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog pg;
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pg.setMessage(getResources().getString(R.string.authorize_add_device_message_text));
            pg.setCancelable(false);
            pg.show();
        };
        @Override
        protected JSONObject doInBackground(String... params) {
            if (params != null && params.length == 2) {
                return mDirect.AddNewDevice(params[0], params[1]);
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            if (pg != null) {
                pg.dismiss();
            }
            Log.v(TAG, "" + result);
            if (result != null) {
                // Активация успешна
                Toast.makeText(getActivity(), getResources().getString(R.string.authorize_activate_success_message_text), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.authorize_activate_failed_message_text), Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }
    }
    class RemoveDeviceAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        ProgressDialog pg;
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pg.setMessage(getResources().getString(R.string.authorize_remove_device_message_text));
            pg.setCancelable(false);
            pg.show();
        };
        @Override
        protected JSONObject doInBackground(Void... params) {
            return mDirect.RemoveDevice();
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            if (pg != null) {
                pg.dismiss();
            }
            Log.v(TAG, "" + result);
            if (result != null) {
                // Деактивация успешна
                Toast.makeText(getActivity(), getResources().getString(R.string.authorize_deactivate_success_message_text), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.authorize_deactivate_failed_message_text), Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }
    }
    class GetURLAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        ProgressDialog pg;
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pg.setMessage(getResources().getString(R.string.authorize_get_url_message_text));
            pg.setCancelable(false);
            pg.show();
        };
        @Override
        protected JSONObject doInBackground(Void... params) {
            return mDirect.GetURLForService();
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            if (pg != null) {
                pg.dismiss();
            }
            Log.v(TAG, "" + result);
            if (result == null) {
                Toast.makeText(getActivity(), getResources().getString(R.string.authorize_deactivate_failed_message_text), Toast.LENGTH_LONG).show();
            } else {
                String url = result.optString("url");
                showGetUrlDialog(url);
            }
            super.onPostExecute(result);
        }
    }
    
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    private OnLoginListener mListener;
    
    private Button loginButton, continueButton; 
    private ImageButton settingsButton;
    private EditText loginEditText, passwordEditText;
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
        passwordEditText = (EditText)rootView.findViewById(R.id.passwordEditText);
        continueButton = (Button)rootView.findViewById(R.id.continueButton);
        successTextView = (TextView)rootView.findViewById(R.id.successTextView);
        
        bottomLinear = (LinearLayout) rootView.findViewById(R.id.bottomLinear);
        continueLinear = (LinearLayout) rootView.findViewById(R.id.continueLinear);
        
        loginButton.setOnClickListener(this);
        continueButton.setOnClickListener(this);
        
        settingsButton = (ImageButton) rootView.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(settingsClickListener);
        
        loginEditText.setText(mDirect.getUserName());
        
        setFonts();
        checkAuth();
        return rootView;
    }
    private void setFonts() {
        loginButton.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        loginEditText.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        passwordEditText.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        loginButton.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        continueButton.setTypeface(mDirect.mPFDinDisplayPro_Bold);
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
            popup.inflate(R.menu.auth_popup_menu);
            Menu popupMenu = popup.getMenu();
            if (mDirect.isDeviceActivated()) {
                popupMenu.findItem(R.id.action_activate).setVisible(false);
                popupMenu.findItem(R.id.action_deactivate).setVisible(true);
                popupMenu.findItem(R.id.action_geturl).setVisible(true);
            } else { 
                popupMenu.findItem(R.id.action_deactivate).setVisible(false);
                popupMenu.findItem(R.id.action_activate).setVisible(true);
                popupMenu.findItem(R.id.action_geturl).setVisible(false);
            }
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_more:
                            // Открыть настройки?
                            return true;
                        case R.id.action_userchange:
                            // Разлогиниться
                            // Удалить БД?
                            mDirect.Logout();
                            checkAuth();
                            return true;
                        case R.id.action_activate:
                            AddNewDevice();
                            return true;
                        case R.id.action_deactivate:
                            deactivate();
                            return true;
                        case R.id.action_geturl:
                            new GetURLAsyncTask().execute();
                            return true;
                        case R.id.action_settings:
                            // open settings activity
                            FragmentManager manager = getFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            Fragment fragment = Fragment.instantiate(getActivity(), SettingsFragment.class.getName(), null);
                            transaction.replace(R.id.container, fragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                            break;
                    }
                    return false;
                }
            });
            
            popup.show();
        }
    };
    private void showGetUrlDialog(String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(String.format(getResources().getString(R.string.authorize_geturl_success_message_text), url));
        
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private void deactivate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.authorize_deactivate_warning_message_text));
        
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Разлогиниться
                // Удалить БД?
                mDirect.Logout();
                checkAuth();
                new RemoveDeviceAsyncTask().execute();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private void AddNewDevice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Введите данные для активации");

        // Set up the input
        LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.VERTICAL);
        final EditText inputName = new EditText(getActivity());
        final EditText inputKey = new EditText(getActivity());
        inputName.setHint(R.string.authorize_device_name_hint_message_text);
        inputKey.setHint(R.string.authorize_device_key_hint_message_text);
        ll.addView(inputName);
        ll.addView(inputKey);
        builder.setView(ll);
        
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = inputName.getText().toString();
                String key = inputKey.getText().toString();
                new AddNewDeviceAsyncTask().execute(key, name);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:
            	if (!loginEditText.getText().toString().isEmpty()) {
            		new AuthorizeAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            	} else {
            		final String text = getResources().getString(R.string.authorize_login_empty_message_text);
            		Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
            	}
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
