package ru.tasu.directleader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.tasu.directleader.AuthorizeFragment.OnLoginListener;
import ru.tasu.directleader.DocumentDownloadDialogFragment.OnDocumentDownloadListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class MainActivity extends Activity implements OnLoginListener, OnOpenFragmentListener, OnDocumentDownloadListener {
    private static final String TAG = "MainActivity";
    
    class UpdateDBRabotnicAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            // Обновление данных Rabotnic
            JSONObject result = mDirect.getRabotnics();
            JSONArray data = result.optJSONArray("data");
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
        }
    }
    class UpdateDBTaskAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... arg0) {
            // Обновление данных Task
            JSONObject result = mDirect.GetMyTasks();
            JSONArray data = result.optJSONArray("data");
            if (data.length() > 0) {
                Log.v(TAG, "getMyTask() ok");
                TaskDataSource task_ds = new TaskDataSource(mDirect);
                task_ds.open();
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
                Log.v(TAG, "getMyTask() UPDATED");
            } else {
                Log.v(TAG, "Почему-то не удалось получить данные. Обновление не произошло");
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            MainFragment fragment = (MainFragment)getFragmentManager().findFragmentByTag("main_fragment");
            if (fragment != null) {
                ((OnUpdateDataListener)fragment).OnUpdateData();
            }
        }
    }
    class UpdateDBClientSettingsAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... arg0) {
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
                
            } else {
                Log.v(TAG, "Почему-то не удалось получить данные ClientSettings");
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
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
        
//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        Log.v(TAG, "metrics.density " + metrics.density);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main_popup_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_exit) {
//            Log.v(TAG, "action_exit");
//            return true;
//        }
        return super.onOptionsItemSelected(item);
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
        // Вызвать диалог создания задачи
        Log.v(TAG, "showCreateTaskDialog");
        Intent i = new Intent(this, TaskCreateActivity.class);
        i.putExtra(TaskCreateActivity.TITLE_KEY, preTitle);
        startActivity(i);
    }
}
