package ru.tasu.directleader;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.tasu.directleader.AuthorizeFragment.OnLoginListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements OnLoginListener, OnOpenFragmentListener {
    private static final String TAG = "MainActivity";
    
    class UpdateDBRabotnicAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            // ���������� ������ Rabotnic
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
                Log.v(TAG, "������-�� �� ������� �������� ������. ���������� �� ���������");
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
            // ���������� ������ Task
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
                Log.v(TAG, "������-�� �� ������� �������� ������. ���������� �� ���������");
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
        }
    }
    
    private static DirectLeaderApplication mDirect;
    private List<Task> mMyTasks;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDirect = (DirectLeaderApplication) getApplication();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, Fragment.instantiate(this, AuthorizeFragment.class.getName())).commit();
        }
        if (mDirect.isOnline()) {
            Log.v(TAG, "need to update");
            // ���� ������� ���������� ��� ������
//            new UpdateDBRabotnicAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, null);
//            new UpdateDBTaskAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    @Override
    public void OnLogin() {
        startMainMenuFragment();
    }
    private void startMainMenuFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = Fragment.instantiate(this, MainFragment.class.getName());
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }
    @Override
    public void OnOpenFragment(String fragmentClassName) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = Fragment.instantiate(this, fragmentClassName);
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    public List<Task> getMyTasks() {
        return mMyTasks;
    }
}