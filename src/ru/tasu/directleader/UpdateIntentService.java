package ru.tasu.directleader;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class UpdateIntentService extends Service {
    private static final String TAG = "UpdateIntentService";
    public final static String UPDATE_COMPLETE_ACTION = "update_complete_action";
    
    private DirectLeaderApplication mDirect;
    
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
    }
    // /*
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
            sendUpdateAction();
        }
    }
    // */
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDirect = (DirectLeaderApplication)getApplication();
        Log.v(TAG, "onStartCommand " + mDirect);
        String userCode = mDirect.getUserCode();
        if (!userCode.equalsIgnoreCase("")) {
            new UpdateDBRabotnicAsyncTask().execute();
            new UpdateDBTaskAsyncTask().execute();
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
