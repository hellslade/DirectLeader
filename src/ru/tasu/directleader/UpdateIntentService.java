package ru.tasu.directleader;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.tasu.directleader.MainActivity.UpdateCheckFinishedTasksAsyncTask;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class UpdateIntentService extends Service {
    private static final String TAG = "UpdateIntentService";
    public final static String UPDATE_COMPLETE_ACTION = "update_complete_action";
    
    private DirectLeaderApplication mDirect;
    
    private static String lastSyncDate = "";
    
    class UpdateCheckFinishedTasksAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // Получаем список завершенных Task
        	TaskDataSource tds = new TaskDataSource(mDirect);
        	tds.open();
        	JSONArray jsonIds = tds.getAllTaskIds();
            JSONObject result = mDirect.PostCheckFinishedTasks(jsonIds);
            JSONArray data = new JSONArray();
            int statusCode = 0;
            if (result != null) {
                data = result.optJSONArray("data");
                statusCode = result.optInt("statusCode");
            }
            if (statusCode == 200) {
                Log.v(TAG, "CheckFinishedTasks() ok " + data);
                TaskDataSource task_ds = new TaskDataSource(mDirect);
                task_ds.open();
                
                for (int i = 0; i < data.length(); i++) {
                    final long taskId = Long.valueOf(data.optString(i, "-1"));
                    if (taskId != -1) {
                        task_ds.deleteTaskById(taskId);
                    }
                }
                Log.v(TAG, "CheckFinishedTasks() UPDATED");
            } else {
                Log.v(TAG, "Почему-то не удалось получить данные. Обновление не произошло");
            }
            return null;
        }
    }
    class UpdateDBRabotnicAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
        	// Обновление данных Rabotnic
        	// Запоминаем текущую дату
        	Calendar c = Calendar.getInstance();
        	SimpleDateFormat df = new SimpleDateFormat(mDirect.mLastSyncDateFormat);
        	lastSyncDate = df.format(c.getTime());
        	Log.v(TAG, "lastSyncDate " + lastSyncDate);
            JSONObject result = mDirect.getRabotnics();
            JSONArray data = new JSONArray();
            int statusCode = 0;
            if (result != null) {
                data = result.optJSONArray("data");
                statusCode = result.optInt("statusCode");
            }
            if (statusCode == 200) {
                Log.v(TAG, "getRabotnics() ok");
                RabotnicDataSource rab_ds = new RabotnicDataSource(mDirect);
                rab_ds.open();
                JSONObject rabotnicJson;
                for (int i = 0; i < data.length(); i++) {
                    rabotnicJson = data.optJSONObject(i);
                    if (rabotnicJson != null) {
                        final Rabotnic rabotnic = new Rabotnic(rabotnicJson);
                        rab_ds.insertOrUpdate(rabotnic);
                    }
                }
                Log.v(TAG, "getRabotnics() UPDATED");
                // После обновления, нужно сохранить время последнего обновления
                mDirect.saveLastSyncDateRabotnic(lastSyncDate);
            } else {
                Log.v(TAG, "Почему-то не удалось получить данные. Обновление не произошло");
            }
            return null;
        }
    }
    // /*
    class UpdateDBTaskAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... arg0) {
        	// Обновление данных Task
        	// Запоминаем текущую дату
        	Calendar c = Calendar.getInstance();
        	SimpleDateFormat df = new SimpleDateFormat(mDirect.mLastSyncDateFormat);
        	lastSyncDate = df.format(c.getTime());
        	Log.v(TAG, "lastSyncDate " + lastSyncDate);
            JSONObject result = mDirect.GetMyTasks();
            JSONArray data = new JSONArray();
            int statusCode = 0;
            if (result != null) {
                data = result.optJSONArray("data");
                statusCode = result.optInt("statusCode");
            }
            if (statusCode == 200) {
                Log.v(TAG, "getMyTask() ok");
                TaskDataSource task_ds = new TaskDataSource(mDirect);
                task_ds.open();
                JSONObject taskJson;
                for (int i = 0; i < data.length(); i++) {
                    taskJson = data.optJSONObject(i);
                    if (taskJson != null) {
                        final Task task = new Task(taskJson);
                        task_ds.insertOrUpdate(task);
                    }
                }
                Log.v(TAG, "getMyTask() UPDATED");
                // После обновления, нужно сохранить время последнего обновления
                mDirect.saveLastSyncDateTask(lastSyncDate);
            } else {
                Log.v(TAG, "Почему-то не удалось получить данные. Обновление не произошло");
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            if (result != null) {
                sendUpdateAction();
            }
        }
    }
    // */
    class ExecStoredActionsAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            Set<String> urls = mDirect.getSettings().getStringSet(mDirect.SETTINGS_EXEC_KEY, null);
            if (urls != null) {
                Set<String> newUrls = new HashSet<String>();
                newUrls.addAll(urls);
                for (String url : newUrls) {
                    final JSONObject result = mDirect.ExecAction(url);
                    if (result != null) {
                        final int statusCode = result.optInt("statusCode");
                        if (statusCode == 200) {
                            urls.remove(url);
                        }
                    }
                }
            }
            Editor e = mDirect.getSettings().edit();
            e.putStringSet(mDirect.SETTINGS_EXEC_KEY, urls);
            e.commit();
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDirect = (DirectLeaderApplication)getApplication();
        Log.v(TAG, "onStartCommand " + mDirect);
        String userCode = mDirect.getUserCode();
        if (!userCode.equalsIgnoreCase("")) {
            new ExecStoredActionsAsyncTask().execute();
            new UpdateCheckFinishedTasksAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, null);
            new UpdateDBRabotnicAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, null);
            new UpdateDBTaskAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, null);
        }
        return super.onStartCommand(intent, flags, startId);
    }
    private void sendUpdateAction() {
        Intent i= new Intent();
        i.setAction(UPDATE_COMPLETE_ACTION);
        sendBroadcast(i);
        stopSelf();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
}
