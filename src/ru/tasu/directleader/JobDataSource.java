package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class JobDataSource {
    // Database fields
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private Context mContext; 
    private String[] allColumns = {DBHelper.JOB__ID, DBHelper.JOB_ACTION_LIST, DBHelper.JOB_END_DATE, DBHelper.JOB_FINAL_DATE, DBHelper.JOB_ID, DBHelper.JOB_MAIN_TASK_JOB, 
            DBHelper.JOB_PERFORMER, DBHelper.JOB_READED, DBHelper.JOB_RESULT_TITLE, 
            DBHelper.JOB_START_DATE, DBHelper.JOB_STATE, DBHelper.JOB_STATE_TITLE, DBHelper.JOB_SUBJECT};

    public JobDataSource(Context context) {
        dbHelper = DBHelper.getInstance(context);
        mContext = context;
    }
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }
    public void close() {
        dbHelper.close();
    }
    public Job createJob(Job new_job) {
        ContentValues values = new ContentValues();
//        values.put(DBHelper.JOB__ID, new_job.getId());
        values.put(DBHelper.JOB_ACTION_LIST, new_job.getActionList());
        values.put(DBHelper.JOB_END_DATE, new_job.getEndDate());
        values.put(DBHelper.JOB_FINAL_DATE, new_job.getFinalDate());
        values.put(DBHelper.JOB_ID, new_job.getId());
        values.put(DBHelper.JOB_MAIN_TASK_JOB, new_job.getMainTaskJob());
        values.put(DBHelper.JOB_PERFORMER, new_job.getPerformer());
        values.put(DBHelper.JOB_READED, new_job.getReaded());
        values.put(DBHelper.JOB_RESULT_TITLE, new_job.getResultTitle());
        values.put(DBHelper.JOB_START_DATE, new_job.getStartDate());
        values.put(DBHelper.JOB_STATE, new_job.getState());
        values.put(DBHelper.JOB_STATE_TITLE, new_job.getStateTitle());
        values.put(DBHelper.JOB_SUBJECT, new_job.getSubject());
        long insertId = database.insert(DBHelper.JOB_TABLE, null, values);
        
        Cursor cursor = database.query(DBHelper.JOB_TABLE,
                allColumns, DBHelper.JOB__ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Job newJob = cursorToJob(cursor);
        cursor.close();
        return newJob;
    }
    public Job[] getJobsByTaskId(long taskId) {
        Job[] jobs = null;
        Cursor cursor = database.query(DBHelper.JOB_TABLE,
                allColumns, DBHelper.JOB_MAIN_TASK_JOB + " = " + taskId, null, null, null, null);
        cursor.moveToFirst();
        jobs = new Job[cursor.getCount()];
        int i = 0;
        while (!cursor.isAfterLast()) {
            jobs[i++] = (cursorToJob(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return jobs;
    }
    public List<Job> getAllJobs() {
        List<Job> jobs = new ArrayList<Job>();

        Cursor cursor = database.query(DBHelper.JOB_TABLE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        Job job = null;
        RabotnicDataSource rabotnic_ds = new RabotnicDataSource(mContext);
        rabotnic_ds.open();
        while (!cursor.isAfterLast()) {
            job = cursorToJob(cursor);
            job.setUser(rabotnic_ds.getRabotnicByCode(job.getPerformer()));
            jobs.add(job);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return jobs;
    }
    public List<Job> getImportantJobByPerformerCode(String code) {
        List<Job> jobs = new ArrayList<Job>();
        
        String sql = String.format("SELECT %s.* FROM %s, %s, %s WHERE %s.%s='�������' AND %s.%s=%s.%s AND %s.%s=%s.%s AND %s.%s=0 AND %s.%s=?;", 
                DBHelper.JOB_TABLE, DBHelper.JOB_TABLE, DBHelper.TASK_TABLE, DBHelper.RABOTNIC_TABLE,
                DBHelper.TASK_TABLE, DBHelper.TASK_IMPORTANCE, DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER, DBHelper.RABOTNIC_TABLE, DBHelper.RABOTNIC_CODE,
                DBHelper.JOB_TABLE, DBHelper.JOB_MAIN_TASK_JOB, DBHelper.TASK_TABLE, DBHelper.TASK_ID, DBHelper.JOB_TABLE, DBHelper.JOB_STATE,
                DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER);
//        Log.v("getImportanceJobByPerformerCode()", "sql " + sql);
        Cursor cursor = database.rawQuery(sql, new String[]{code});
        cursor.moveToFirst();
        Job job = null;
        RabotnicDataSource rabotnic_ds = new RabotnicDataSource(mContext);
        rabotnic_ds.open();
        AttachmentDataSource attach_ds = new AttachmentDataSource(mContext);
        attach_ds.open();
        TaskDataSource task_ds = new TaskDataSource(mContext);
        task_ds.open();
        while (!cursor.isAfterLast()) {
            job = cursorToJob(cursor);
            job.setAttachmentCount(attach_ds.getAttachmentsCountByTaskId(job.getMainTaskJob()));
            job.setSubtaskCount(task_ds.getTaskById(job.getMainTaskJob()).getSubtaskCount());
            job.setUser(rabotnic_ds.getRabotnicByCode(job.getPerformer()));
            jobs.add(job);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return jobs;
    }
    public List<Job> getJobByPerformerCode(String code) {
        List<Job> jobs = new ArrayList<Job>();
        
        String sql = String.format("SELECT %s.* FROM %s, %s, %s WHERE %s.%s=%s.%s AND %s.%s=%s.%s AND %s.%s=0 AND %s.%s=?;", 
                DBHelper.JOB_TABLE, DBHelper.JOB_TABLE, DBHelper.TASK_TABLE, DBHelper.RABOTNIC_TABLE,
                DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER, DBHelper.RABOTNIC_TABLE, DBHelper.RABOTNIC_CODE,
                DBHelper.JOB_TABLE, DBHelper.JOB_MAIN_TASK_JOB, DBHelper.TASK_TABLE, DBHelper.TASK_ID, DBHelper.JOB_TABLE, DBHelper.JOB_STATE,
                DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER);
//        Log.v("getJobByPerformerCode()", "sql " + sql);
        Cursor cursor = database.rawQuery(sql, new String[]{code});
        cursor.moveToFirst();
        Job job = null;
        RabotnicDataSource rabotnic_ds = new RabotnicDataSource(mContext);
        rabotnic_ds.open();
        while (!cursor.isAfterLast()) {
            job = cursorToJob(cursor);
            job.setUser(rabotnic_ds.getRabotnicByCode(job.getPerformer()));
            jobs.add(job);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return jobs;
    }
    public int[] getCountOfImportantJobByPerformerCode(String code) {
        int[] count = {0, 0, 0};
        String sql = String.format("SELECT count(%s._id) FROM %s, %s, %s WHERE %s.%s='�������' AND %s.%s=%s.%s AND %s.%s=%s.%s AND %s.%s=? AND job.state=0 AND job.final_date > date();", 
                DBHelper.JOB_TABLE, DBHelper.JOB_TABLE, DBHelper.TASK_TABLE, DBHelper.RABOTNIC_TABLE,
                DBHelper.TASK_TABLE, DBHelper.TASK_IMPORTANCE, DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER, DBHelper.RABOTNIC_TABLE, DBHelper.RABOTNIC_CODE,
                DBHelper.JOB_TABLE, DBHelper.JOB_MAIN_TASK_JOB, DBHelper.TASK_TABLE, DBHelper.TASK_ID, DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER,
                DBHelper.JOB_TABLE, DBHelper.JOB_STATE, DBHelper.JOB_TABLE, DBHelper.JOB_FINAL_DATE);
        Cursor cursor = database.rawQuery(sql, new String[]{code});
        cursor.moveToFirst();
        count[0] = cursor.getInt(0);
        cursor.close();
        
        sql = String.format("SELECT count(%s._id) FROM %s, %s, %s WHERE %s.%s='�������' AND %s.%s=%s.%s AND %s.%s=%s.%s AND %s.%s=? AND job.state=0 AND job.final_date = date();", 
                DBHelper.JOB_TABLE, DBHelper.JOB_TABLE, DBHelper.TASK_TABLE, DBHelper.RABOTNIC_TABLE,
                DBHelper.TASK_TABLE, DBHelper.TASK_IMPORTANCE, DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER, DBHelper.RABOTNIC_TABLE, DBHelper.RABOTNIC_CODE,
                DBHelper.JOB_TABLE, DBHelper.JOB_MAIN_TASK_JOB, DBHelper.TASK_TABLE, DBHelper.TASK_ID, DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER,
                DBHelper.JOB_TABLE, DBHelper.JOB_STATE, DBHelper.JOB_TABLE, DBHelper.JOB_FINAL_DATE);
        cursor = database.rawQuery(sql, new String[]{code});
        cursor.moveToFirst();
        count[1] = cursor.getInt(0);
        cursor.close();

        sql = String.format("SELECT count(%s._id) FROM %s, %s, %s WHERE %s.%s='�������' AND %s.%s=%s.%s AND %s.%s=%s.%s AND %s.%s=? AND job.state=0 AND job.final_date < date();", 
                DBHelper.JOB_TABLE, DBHelper.JOB_TABLE, DBHelper.TASK_TABLE, DBHelper.RABOTNIC_TABLE,
                DBHelper.TASK_TABLE, DBHelper.TASK_IMPORTANCE, DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER, DBHelper.RABOTNIC_TABLE, DBHelper.RABOTNIC_CODE,
                DBHelper.JOB_TABLE, DBHelper.JOB_MAIN_TASK_JOB, DBHelper.TASK_TABLE, DBHelper.TASK_ID, DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER,
                DBHelper.JOB_TABLE, DBHelper.JOB_STATE, DBHelper.JOB_TABLE, DBHelper.JOB_FINAL_DATE);
        cursor = database.rawQuery(sql, new String[]{code});
        cursor.moveToFirst();
        count[2] = cursor.getInt(0);
        cursor.close();

        return count;
    }
    public int[] getCountOfJobByPerformerCode(String code) {
        int[] count = {0, 0, 0};
        String sql = String.format("SELECT count(%s._id) FROM %s, %s, %s WHERE %s.%s=%s.%s AND %s.%s=%s.%s AND %s.%s=? AND job.state=0 AND job.final_date > date();", 
                DBHelper.JOB_TABLE, DBHelper.JOB_TABLE, DBHelper.TASK_TABLE, DBHelper.RABOTNIC_TABLE,
                DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER, DBHelper.RABOTNIC_TABLE, DBHelper.RABOTNIC_CODE,
                DBHelper.JOB_TABLE, DBHelper.JOB_MAIN_TASK_JOB, DBHelper.TASK_TABLE, DBHelper.TASK_ID, DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER,
                DBHelper.JOB_TABLE, DBHelper.JOB_STATE, DBHelper.JOB_TABLE, DBHelper.JOB_FINAL_DATE);
        Cursor cursor = database.rawQuery(sql, new String[]{code});
        cursor.moveToFirst();
        count[0] = cursor.getInt(0);
        cursor.close();
        
        sql = String.format("SELECT count(%s._id) FROM %s, %s, %s WHERE %s.%s=%s.%s AND %s.%s=%s.%s AND %s.%s=? AND job.state=0 AND job.final_date = date();", 
                DBHelper.JOB_TABLE, DBHelper.JOB_TABLE, DBHelper.TASK_TABLE, DBHelper.RABOTNIC_TABLE,
                DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER, DBHelper.RABOTNIC_TABLE, DBHelper.RABOTNIC_CODE,
                DBHelper.JOB_TABLE, DBHelper.JOB_MAIN_TASK_JOB, DBHelper.TASK_TABLE, DBHelper.TASK_ID, DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER,
                DBHelper.JOB_TABLE, DBHelper.JOB_STATE, DBHelper.JOB_TABLE, DBHelper.JOB_FINAL_DATE);
        cursor = database.rawQuery(sql, new String[]{code});
        cursor.moveToFirst();
        count[1] = cursor.getInt(0);
        cursor.close();

        sql = String.format("SELECT count(%s._id) FROM %s, %s, %s WHERE %s.%s=%s.%s AND %s.%s=%s.%s AND %s.%s=? AND job.state=0 AND job.final_date < date();", 
                DBHelper.JOB_TABLE, DBHelper.JOB_TABLE, DBHelper.TASK_TABLE, DBHelper.RABOTNIC_TABLE,
                DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER, DBHelper.RABOTNIC_TABLE, DBHelper.RABOTNIC_CODE,
                DBHelper.JOB_TABLE, DBHelper.JOB_MAIN_TASK_JOB, DBHelper.TASK_TABLE, DBHelper.TASK_ID, DBHelper.JOB_TABLE, DBHelper.JOB_PERFORMER,
                DBHelper.JOB_TABLE, DBHelper.JOB_STATE, DBHelper.JOB_TABLE, DBHelper.JOB_FINAL_DATE);
        cursor = database.rawQuery(sql, new String[]{code});
        cursor.moveToFirst();
        count[2] = cursor.getInt(0);
        cursor.close();

        return count;
    }
    public int deleteAllJobs() {
        int count = database.delete(DBHelper.JOB_TABLE, "1", null);
        return count;
    }

    private Job cursorToJob(Cursor cursor) {
        //int id, String address, String point, String work_hours, int brand, String last_modified
//        DBHelper.JOB__ID, DBHelper.JOB_ACTION_LIST, DBHelper.JOB_END_DATE, DBHelper.JOB_FINAL_DATE, DBHelper.JOB_ID, DBHelper.JOB_MAIN_TASK_JOB, 
//        DBHelper.JOB_PERFORMER, DBHelper.JOB_READED, DBHelper.JOB_SIGNING_REQUIRED, 
//        DBHelper.JOB_START_DATE, DBHelper.JOB_STATE, DBHelper.JOB_STATE_TITLE, DBHelper.JOB_SUBJECT
//        JSONArray action_list, String end_date, String final_date, long id, long main_task_job, String performer, 
//        boolean readed, boolean signature_requared, String start_date, boolean state, String state_title, String subject
        JSONArray actionList;
        try {
            actionList = new JSONArray(cursor.getString(1));
        } catch (JSONException e) {
            Log.v("JobDataSource", ""+e.getLocalizedMessage());
            actionList = new JSONArray();
        }
        boolean readed = cursor.getInt(7) == 1;
        boolean state = cursor.getInt(10) == 1;
        Job job = new Job(actionList, cursor.getString(2), cursor.getString(3), cursor.getLong(4), cursor.getLong(5), cursor.getString(6), readed, cursor.getString(8), cursor.getString(9), state, cursor.getString(11), cursor.getString(12));
//      Log.v("cursorToStore", cursor.getInt(0)+" "+cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getString(3)+" "+cursor.getInt(4)+" "+cursor.getString(5));
        return job;
    }
}