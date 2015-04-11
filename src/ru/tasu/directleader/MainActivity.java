package ru.tasu.directleader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.tasu.directleader.AuthorizeFragment.OnLoginListener;
import ru.tasu.directleader.DocumentDownloadDialogFragment.OnDocumentDownloadListener;
import ru.tasu.directleader.UpdateService.LocalBinder;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity implements OnLoginListener, OnOpenFragmentListener, OnDocumentDownloadListener, OnPreferenceChangeListener {
    private static final String TAG = "MainActivity";
    
    class UpdateDBRabotnicAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        ProgressDialog pg = new ProgressDialog(MainActivity.this);
        private PowerManager.WakeLock mWakeLock;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pg.setMessage(getResources().getString(R.string.update_db_process_rabotnic_message_text));
            pg.setCancelable(false);
            // take CPU lock to prevent CPU from going off if the user 
            // presses the power button during download
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
            pg.show();
        }
        @Override
        protected JSONObject doInBackground(Void... params) {
            // Обновление данных Rabotnic
            JSONObject result = mDirect.getRabotnics();
            JSONArray data = new JSONArray();
            if (result != null) {
                data = result.optJSONArray("data");
            }
            if (data.length() > 0) {
                Log.v(TAG, "getRabotnics() ok");
                RabotnicDataSource rab_ds = new RabotnicDataSource(mDirect);
                rab_ds.open();
                int remove_count = rab_ds.deleteAllRabotnics();
                Log.v(TAG, "remove_count " + remove_count);
                JSONObject rabotnicJson;
                for (int i = 0; i < data.length(); i++) {
                    rabotnicJson = data.optJSONObject(i);
                    if (rabotnicJson != null) {
                        final Rabotnic rabotnic = new Rabotnic(rabotnicJson);
                        rab_ds.createRabotnik(rabotnic);
                    }
                }
                Log.v(TAG, "getRabotnics() UPDATED");
            } else {
                Log.v(TAG, "Почему-то не удалось получить данные. Обновление не произошло");
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            if (pg != null) {
                pg.dismiss();
            }
            mWakeLock.release();
        }
    }
    class UpdateDBTaskAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        ProgressDialog pg = new ProgressDialog(MainActivity.this);
        private PowerManager.WakeLock mWakeLock;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pg.setMessage(getResources().getString(R.string.update_db_process_task_message_text));
            pg.setCancelable(false);
            // take CPU lock to prevent CPU from going off if the user 
            // presses the power button during download
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
            pg.show();
        }
        @Override
        protected JSONObject doInBackground(Void... arg0) {
            // Обновление данных Task
            JSONObject result = mDirect.GetMyTasks();
            JSONArray data = new JSONArray();
            if (result != null) {
                data = result.optJSONArray("data");
            }
            if (data.length() > 0) {
                Log.v(TAG, "getMyTask() ok");
                TaskDataSource task_ds = new TaskDataSource(mDirect);
                task_ds.open();
                // Получить список id favorites
                JobDataSource jds = new JobDataSource(mDirect);
                jds.open();
                long[] ids = jds.getFavoriteJobsIds();
                int remove_count = task_ds.deleteAllTasks();
                Log.v(TAG, "remove_count " + remove_count);
                JSONObject taskJson;
                for (int i = 0; i < data.length(); i++) {
                    taskJson = data.optJSONObject(i);
                    if (taskJson != null) {
                        final Task task = new Task(taskJson);
                        task_ds.createTask(task);
                    }
                }
                // Восстановить id favorites
                jds.setFavoriteJobsIds(ids);
                Log.v(TAG, "getMyTask() UPDATED");
            } else {
                Log.v(TAG, "Почему-то не удалось получить данные. Обновление не произошло");
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            if (pg != null) {
                pg.dismiss();
            }
            mWakeLock.release();
            MainFragment fragment = (MainFragment)getFragmentManager().findFragmentByTag("main_fragment");
            if (fragment != null) {
                ((OnUpdateDataListener)fragment).OnUpdateData();
            }
        }
    }
    class UpdateDBClientSettingsAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        ProgressDialog pg = new ProgressDialog(MainActivity.this);
        private PowerManager.WakeLock mWakeLock;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pg.setMessage(getResources().getString(R.string.update_db_process_client_settings_message_text));
            pg.setCancelable(false);
            // take CPU lock to prevent CPU from going off if the user 
            // presses the power button during download
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
            pg.show();
        }
        @Override
        protected JSONObject doInBackground(Void... arg0) {
            // Обновление данных Client Settings
            JSONObject result = mDirect.GetClientSettings();
            int statusCode = 400;
            if (result != null) {
                statusCode = result.optInt("statusCode");
                result.remove("statusCode");
            }
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
                
            } else {
                Log.v(TAG, "Почему-то не удалось получить данные ClientSettings");
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            if (pg != null) {
                pg.dismiss();
            }
            mWakeLock.release();
            super.onPostExecute(result);
            
        }
    }
    
    private static DirectLeaderApplication mDirect;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mDirect = (DirectLeaderApplication) getApplication();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, Fragment.instantiate(this, AuthorizeFragment.class.getName())).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onPreferenceChange() {
        // Через биндер остановить Alarm и снова запустить
        mService.cancelAlarm();
        mService.setAlarm();
    }
    @Override
    public void OnLogin(boolean update) {
        if (update) {
            OnRefreshData();
        }
        startMainMenuFragment();
    }
    private void startMainMenuFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = Fragment.instantiate(this, MainFragment.class.getName());
        transaction.replace(R.id.container, fragment, "main_fragment");
        transaction.commit();
    }
    @Override
    public void OnOpenFragment(String fragmentClassName) {
        OnOpenFragment(fragmentClassName, null);
    }
    @Override
    public void OnOpenFragment(String fragmentClassName, Bundle args) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = Fragment.instantiate(this, fragmentClassName, args);
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    @Override
    public void OnRefreshData() {
        Log.v(TAG, "OnRefreshData()");
        new UpdateDBRabotnicAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, null);
        new UpdateDBTaskAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, null);
        new UpdateDBClientSettingsAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, null);
    }
    @Override
    public void onDocumentDownload(Attachment doc) {
        // открытие документа.
        boolean exist = mDirect.checkDocumentExist(doc);
        if (exist) {
            File myFile = mDirect.getDocumentFile(doc);
            try {
                FileOpen.openFile(this, myFile);
            } catch (IOException e) {
                Log.v(TAG, "Неудалось открыть документ " + e.getMessage());
            }
        }
    }
    @Override
    public void OnTaskCreate() {
        OnTaskCreate("");
    }
    @Override
    public void OnTaskCreate(String preTitle) {
        if (mDirect.isOnline()) {
            // Вызвать диалог создания задачи
            Log.v(TAG, "showCreateTaskDialog");
            Intent i = new Intent(this, TaskCreateActivity.class);
            i.putExtra(TaskCreateActivity.TITLE_KEY, preTitle);
            startActivity(i);
        } else {
            String errorText = getResources().getString(R.string.create_task_dialog_fragment_internet_error_text);
            Toast.makeText(this, errorText, Toast.LENGTH_LONG).show();
        }
    }
    private UpdateReceiver receiver;
    @Override
    protected void onStart() {
        super.onStart();
        //Register BroadcastReceiver
        //to receive event from our service
        receiver = new UpdateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpdateIntentService.UPDATE_COMPLETE_ACTION);
        registerReceiver(receiver, intentFilter);
        startService(new Intent(this, UpdateService.class));
        
        // Bind to UpdateService
        Intent intent = new Intent(this, UpdateService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.v(TAG, " " + e.getLocalizedMessage());
        }
        
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    UpdateService mService;
    boolean mBound = false;
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    private class UpdateReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(MainActivity.this, "Update from UpdateService complete", Toast.LENGTH_LONG).show();
            MainFragment fragment = (MainFragment)getFragmentManager().findFragmentByTag("main_fragment");
            if (fragment != null) {
                ((OnUpdateDataListener)fragment).OnUpdateData();
            }
        }
    }
}
