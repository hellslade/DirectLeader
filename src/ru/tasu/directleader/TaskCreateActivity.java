package ru.tasu.directleader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.tasu.directleader.UsersDialogFragment.OnUserSelectListener;
import ru.tasu.directleader.UsersDialogFragment.UserType;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

public class TaskCreateActivity extends Activity implements OnClickListener, OnUserSelectListener {
    private static final String TAG = "TaskCreateActivity";
    public static final String TITLE_KEY = "title_key";
    
    class GetClientSettingsAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            // Обновление данных Client Settings
            JSONObject result = mDirect.GetClientSettings();
            int statusCode = result.optInt("statusCode");
            result.remove("statusCode");
            if (statusCode == 200) {
                Log.v(TAG, "GetClientSettings() ok");
                // Сохранить в файл
                File path = mDirect.getApplicationCacheDir(mDirect);
                File settingsFile = new File(path, mDirect.mSettingsFilename);
                try {
                    if (!settingsFile.exists()) {
                        settingsFile.createNewFile();
                    }
                    FileOutputStream fOut = new FileOutputStream(settingsFile);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.write(result.toString());
                    myOutWriter.close();
                    fOut.close();
                } catch (IOException e) {
                    Log.v(TAG, "Exception in save client settings " + e.getLocalizedMessage());
                }
                return result;
            } else {
                Log.v(TAG, "Почему-то не удалось получить данные ClientSettings");
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            clientSettings = result;
        }
    }
    
    private class TaskCreateAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected JSONObject doInBackground(Void... task) {
            JSONObject taskJSON = new JSONObject();
            JSONArray observers = new JSONArray();
            JSONArray performers = new JSONArray();
            
            for (int i=1; i<observersLayout.getChildCount(); i++) {
                final Rabotnic user = (Rabotnic)observersLayout.getChildAt(i).getTag();
                observers.put(user.getCode());
            }
            for (int i=1; i<performersLayout.getChildCount(); i++) {
                final Rabotnic user = (Rabotnic)performersLayout.getChildAt(i).getTag();
                performers.put(user.getCode());
            }
            
            try {
                taskJSON.put("FinalDate", (String)deadlineTextView.getTag());
                taskJSON.put("Observers", observers);
                taskJSON.put("Performers", performers);
                taskJSON.put("RightsCode", rightsTextView.getText().toString());
                taskJSON.put("RouteType", routeTypeTextView.getText().toString());
//            taskJSON.put("StandartRouteCode", );
                taskJSON.put("Subject", titleEditText.getText().toString());
//            taskJSON.put("TaskTypeCode", );
                taskJSON.put("Text", descriptionEditText.getText().toString());
                return mDirect.PostCreateTask(taskJSON);
            } catch (JSONException e) {
                Log.v(TAG, "exception in create task asynctask" + e.getLocalizedMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            Log.v(TAG, "onPostExecute " + result);
            finish(); 
        }
    }
    
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    
    private String preTitle;
    
    private EditText titleEditText, descriptionEditText;
    private TextView performersLabelView, observersLabelView, rightsLabelView, routeTypeTextView, deadlineTextView, rightsTextView, performersTextView, observersTextView;
    private ImageButton attachmentButton, cancelButton, okButton;
    
    private CheckBox importanceView;
    
    private ViewGroup performersLayout, observersLayout;
    
    private JSONObject clientSettings;
    private UserType Usertype;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDirect = (DirectLeaderApplication) getApplication();
        mSettings = mDirect.getSettings();
        
        preTitle = getIntent().getExtras().getString(TITLE_KEY, "");
        
        setContentView(R.layout.task_create_activity);
        
        titleEditText = (EditText)findViewById(R.id.titleEditText);
        descriptionEditText = (EditText)findViewById(R.id.descriptionEditText);
        
        routeTypeTextView = (TextView)findViewById(R.id.routeTypeTextView);
        deadlineTextView = (TextView)findViewById(R.id.deadlineTextView);
        rightsTextView = (TextView)findViewById(R.id.rightsTextView);
        // Метки для списка исполнителей и наблюдателей
        performersTextView = (TextView)findViewById(R.id.performersTextView);
        observersTextView = (TextView)findViewById(R.id.observersTextView);
        
        performersLabelView = (TextView)findViewById(R.id.performersLabelView);
        observersLabelView = (TextView)findViewById(R.id.observersLabelView);
        rightsLabelView = (TextView)findViewById(R.id.rightsLabelView);
        
        attachmentButton = (ImageButton)findViewById(R.id.attachmentButton);
        cancelButton = (ImageButton)findViewById(R.id.cancelButton);
        okButton = (ImageButton)findViewById(R.id.okButton);
        
        performersLayout = (ViewGroup)findViewById(R.id.performersLayout);
        observersLayout = (ViewGroup)findViewById(R.id.observersLayout);
        
        importanceView = (CheckBox)findViewById(R.id.importanceView);
        
        setFonts();
        initData();
        this.setFinishOnTouchOutside(false);
    }
    private void setFonts() {
        titleEditText.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        descriptionEditText.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        routeTypeTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        deadlineTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        rightsTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        
        performersLabelView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        observersLabelView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        rightsLabelView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    }
    private void initData() {
        titleEditText.setText(preTitle);
        
        cancelButton.setOnClickListener(this);
        okButton.setOnClickListener(this);
        
        routeTypeTextView.setOnClickListener(this);
        deadlineTextView.setOnClickListener(this);
        rightsTextView.setOnClickListener(this);
        
        performersLabelView.setOnClickListener(this);
        observersLabelView.setOnClickListener(this);
        
        try {
            // Читаем из файла настройки
            File path = mDirect.getApplicationCacheDir(mDirect);
            File settingsFile = new File(path, mDirect.mSettingsFilename);
            FileInputStream fIn = new FileInputStream(settingsFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow = "";
            String aBuffer = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
            }
            clientSettings = new JSONObject(aBuffer);
            myReader.close();
            fIn.close();
        } catch (IOException e) {
            Log.v(TAG, "Неудалось прочитать настройки из файла");
        } catch (JSONException e) {
            Log.v(TAG, "Неудалось прочитать настройки из файла");
        }
        if (clientSettings == null) {
            new GetClientSettingsAsyncTask().execute();
        }
    }
    private void addUserToPerformers(Rabotnic user) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int padding = (int)(10 * metrics.density);
        
        final TextView userTextView = new TextView(this);
        userTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        userTextView.setText(user.getName());
        userTextView.setTag(user);
        userTextView.setPadding(padding, padding, padding, padding);
        userTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        // Create a generic swipe-to-dismiss touch listener.
        userTextView.setOnTouchListener(new SwipeDismissTouchListener(
                userTextView,
                null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }
                    @Override
                    public void onDismiss(View view, Object token) {
                        performersLayout.removeView(userTextView);
                        if (performersLayout.getChildCount() == 1) {
                            // Осталась только метка, ее надо скрыть
                            performersTextView.setVisibility(View.GONE);
                        }
                    }
                }));
        performersLayout.addView(userTextView);
        performersTextView.setVisibility(View.VISIBLE);
    }
    private void addUserToObservers(Rabotnic user) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int padding = (int)(10 * metrics.density);
        
        final TextView userTextView = new TextView(this);
        userTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        userTextView.setText(user.getName());
        userTextView.setTag(user);
        userTextView.setPadding(padding, padding, padding, padding);
        userTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        // Create a generic swipe-to-dismiss touch listener.
        userTextView.setOnTouchListener(new SwipeDismissTouchListener(
                userTextView,
                null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }
                    @Override
                    public void onDismiss(View view, Object token) {
                        observersLayout.removeView(userTextView);
                        if (observersLayout.getChildCount() == 1) {
                            // Осталась только метка, ее надо скрыть
                            observersTextView.setVisibility(View.GONE);
                        }
                    }
                }));
        observersLayout.addView(userTextView);
        observersTextView.setVisibility(View.VISIBLE);
    }
    /**
     *  the callback received when the user "sets" the date in the dialog
     */
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String myFormat = "dd MMMM yyyy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Utils.mLocale);
            final Calendar c = Calendar.getInstance();
            c.set(year, monthOfYear, dayOfMonth);
            deadlineTextView.setText(sdf.format(c.getTime()));
            deadlineTextView.setTag(String.format("%s-%s-%s", year, monthOfYear, dayOfMonth));
        }
    };
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancelButton:
                finish();
                break;
            case R.id.okButton:
                new TaskCreateAsyncTask().execute();
                break;
            case R.id.deadlineTextView:
                // get the current date and time
                final Calendar c = Calendar.getInstance();
                new DatePickerDialog(this, mDateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.routeTypeTextView:
                showRouteTypePopup(v);
                break;
            case R.id.rightsTextView:
                showRightsPopup(v);
                break;
            case R.id.performersLabelView:
                showUsersDialog(UserType.PERFORMER);
                break;
            case R.id.observersLabelView:
                showUsersDialog(UserType.OBSERVER);
                break;
        }
    }
    private void showUsersDialog(UserType type) {
        Log.v(TAG, "showUsersDialog " + type.ordinal());
        Usertype = type;
        UsersDialogFragment newFragment = UsersDialogFragment.newInstance();
        newFragment.show(getFragmentManager(), "users_dialog");
    }
    @Override
    public void onUserSelect(Rabotnic user) {
        switch (Usertype) {
            case PERFORMER:
                Log.v(TAG, "new Permormer " + user.getName());
                addUserToPerformers(user);
                break;
            case OBSERVER:
                Log.v(TAG, "new Observer " + user.getName());
                addUserToObservers(user);
                break;
        }
    }
    private void showRouteTypePopup(View v) {
     // Show popup menu
        PopupMenu popup = new PopupMenu(this, v);
        Menu menu = popup.getMenu();
        try {
            JSONArray routeTypes = clientSettings.optJSONArray("RouteType");
            for (int i=0; i<routeTypes.length(); i++) {
                final String type = routeTypes.getString(i);
                MenuItem item = menu.add(type);
            }
        } catch (JSONException e) {
            Log.v(TAG, "Ошибка при парсинге RouteType " + e.getMessage());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String title = (String)item.getTitle();
                routeTypeTextView.setText(title);
                return true;
            }
        });
        
        popup.show();
    }
    private void showRightsPopup(View v) {
        // Show popup menu
           PopupMenu popup = new PopupMenu(this, v);
           Menu menu = popup.getMenu();
           try {
               JSONArray routeTypes = clientSettings.optJSONArray("Rights");
               for (int i=0; i<routeTypes.length(); i++) {
                   final String type = routeTypes.getString(i);
                   menu.add(type);
               }
           } catch (JSONException e) {
               Log.v(TAG, "Ошибка при парсинге Rights " + e.getMessage());
           }
           popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
               @Override
               public boolean onMenuItemClick(MenuItem item) {
                   String title = (String)item.getTitle();
                   rightsTextView.setText(title);
                   return true;
               }
           });
           
           popup.show();
       }
}