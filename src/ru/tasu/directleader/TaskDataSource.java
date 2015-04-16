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

public class TaskDataSource {
    // Database fields
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private Context mContext; 
    private String[] allColumns = {DBHelper.TASK__ID, DBHelper.TASK_ACTION_LIST, DBHelper.TASK_AUTHOR_CODE, DBHelper.TASK_CREATED, DBHelper.TASK_DEADLINE, DBHelper.TASK_EXECUTED,
            DBHelper.TASK_ID, DBHelper.TASK_IMPORTANCE, DBHelper.TASK_OBSERVERS, DBHelper.TASK_PARTICIPANTS, DBHelper.TASK_ROUTE_NAME, 
            DBHelper.TASK_STATE, DBHelper.TASK_SUBTASK_IDS, DBHelper.TASK_TITLE};

    public TaskDataSource(Context context) {
        dbHelper = DBHelper.getInstance(context);
        mContext = context;
    }
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }
    public void close() {
        dbHelper.close();
    }
    public Task createTask(Task new_task) {
        ContentValues values = new ContentValues();
//        values.put(DBHelper.Task__ID, new_Task.getId());
        values.put(DBHelper.TASK_ACTION_LIST, new_task.getActionList());
        values.put(DBHelper.TASK_AUTHOR_CODE, new_task.getAuthorCode());
        values.put(DBHelper.TASK_CREATED, new_task.getCreated());
        values.put(DBHelper.TASK_DEADLINE, new_task.getDeadline());
        values.put(DBHelper.TASK_EXECUTED, new_task.getExecuted());
        values.put(DBHelper.TASK_ID, new_task.getId());
        Log.v("createTask", "TASK_IMPORTANCE " + new_task.getImportance());
        values.put(DBHelper.TASK_IMPORTANCE, new_task.getImportance());
        values.put(DBHelper.TASK_OBSERVERS, new_task.getObservers());
        values.put(DBHelper.TASK_PARTICIPANTS, new_task.getParticipants());
        values.put(DBHelper.TASK_ROUTE_NAME, new_task.getRouteName());
        values.put(DBHelper.TASK_STATE, new_task.getState());
        values.put(DBHelper.TASK_SUBTASK_IDS, new_task.getSubtaskIds());
        values.put(DBHelper.TASK_TITLE, new_task.getTitle());
        long insertId = database.insert(DBHelper.TASK_TABLE, null, values);
        
        // Нужно создать соответствующие записи в таблице attachment, job, history
        Attachment[] attachments = new_task.getAttachment();
        AttachmentDataSource attachment_ds = new AttachmentDataSource(mContext);
        attachment_ds.open();
        for (Attachment attach : attachments) {
            attach.setTaskId(new_task.getId());
            attachment_ds.createAttachment(attach);
        }
        History[] histories = new_task.getHistory();
        HistoryDataSource history_ds = new HistoryDataSource(mContext);
        history_ds.open();
        for (History history : histories) {
            history.setTaskId(new_task.getId());
            history_ds.createHistory(history);
        }
        Job[] jobs = new_task.getJob();
        JobDataSource job_ds = new JobDataSource(mContext);
        job_ds.open();
        for (Job job : jobs) {
            job_ds.createJob(job);
        }
        
        Cursor cursor = database.query(DBHelper.TASK_TABLE,
                allColumns, DBHelper.TASK__ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Task newTask = cursorToTask(cursor);
        cursor.close();
        return newTask;
    }
    public Task getTaskById(long taskId) {
        Task task = null;
        Cursor cursor = database.query(DBHelper.TASK_TABLE,
                allColumns, DBHelper.TASK_ID + " = " + taskId, null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            task = cursorToTask(cursor);
            // Получить attachement, history, job для текущей task
            AttachmentDataSource attachment_ds = new AttachmentDataSource(mContext);
            HistoryDataSource history_ds = new HistoryDataSource(mContext);
            JobDataSource job_ds = new JobDataSource(mContext);
            RabotnicDataSource rabotnic_ds = new RabotnicDataSource(mContext);
            attachment_ds.open();
            history_ds.open();
            job_ds.open();
            rabotnic_ds.open();
            task.setAttachment(attachment_ds.getAttachmentsByTaskId(task.getId()));
            task.setHistory(history_ds.getHistoriesByTaskId(task.getId()));
            task.setJob(job_ds.getJobsByTaskId(task.getId()));
            // Получить автора задания
            task.setAuthor(rabotnic_ds.getRabotnicByCode(task.getAuthorCode()));
        }
        cursor.close();
        return task;
    }
    public List<Task> getAllTasksWithoutJobs() {
        Log.v("TaskDataSource", "START getAllTasksWithoutJobs");
        List<Task> tasks = new ArrayList<Task>();

        Cursor cursor = database.query(DBHelper.TASK_TABLE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        Task task = null;
        AttachmentDataSource attachment_ds = new AttachmentDataSource(mContext);
        HistoryDataSource history_ds = new HistoryDataSource(mContext);
//        JobDataSource job_ds = new JobDataSource(mContext);
        RabotnicDataSource rabotnic_ds = new RabotnicDataSource(mContext);
        attachment_ds.open();
        history_ds.open();
//        job_ds.open();
        rabotnic_ds.open();
        while (!cursor.isAfterLast()) {
            task = cursorToTask(cursor);
            // Получить attachement, history, job для текущей task
            task.setAttachment(attachment_ds.getAttachmentsByTaskId(task.getId()));
            task.setHistory(history_ds.getHistoriesByTaskId(task.getId()));
//            task.setJob(job_ds.getJobsByTaskId(task.getId()));
            // Получить автора задания
            task.setAuthor(rabotnic_ds.getRabotnicByCode(task.getAuthorCode()));
            tasks.add(task);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        Log.v("TaskDataSource", "END getAllTasksWithoutJobs");
        return tasks;
    }
    public List<Task> getAllTasksWithoutJobsSQL() {
        Log.v("TaskDataSource", "START getAllTasksWithoutJobsSQL");
        List<Task> tasks = new ArrayList<Task>();

        String sql = String.format("SELECT task.*, count(attachment.id) as a FROM " +
        		"(SELECT task.*, count(history._id) as h FROM task LEFT OUTER JOIN history ON task.id=history.task_id GROUP BY (task.id)) as task " +
        		"LEFT OUTER JOIN attachment ON task.id=attachment.task_id GROUP BY (task.id);", 
                null);
        
        Cursor cursor = database.rawQuery(sql, null);

        cursor.moveToFirst();
        RabotnicDataSource rabotnic_ds = new RabotnicDataSource(mContext);
//        job_ds.open();
        rabotnic_ds.open();
        while (!cursor.isAfterLast()) {
            final Task task = cursorToTask(cursor);
            // Получить attachement, history для текущей task
            task.setAttachmentCount(cursor.getInt(15));
            task.setHistoryCount(cursor.getInt(14));
            // Получить автора задания
            task.setAuthor(rabotnic_ds.getRabotnicByCode(task.getAuthorCode()));
            tasks.add(task);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        Log.v("TaskDataSource", "END getAllTasksWithoutJobsSQL");
        return tasks;
    }
    public List<Task> getAllTasksWithoutJobsHistory() {
        List<Task> tasks = new ArrayList<Task>();

        Cursor cursor = database.query(DBHelper.TASK_TABLE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        Task task = null;
        AttachmentDataSource attachment_ds = new AttachmentDataSource(mContext);
        RabotnicDataSource rabotnic_ds = new RabotnicDataSource(mContext);
        attachment_ds.open();
        rabotnic_ds.open();
        while (!cursor.isAfterLast()) {
            task = cursorToTask(cursor);
            // Получить attachement для текущей task
            task.setAttachment(attachment_ds.getAttachmentsByTaskId(task.getId()));
            // Получить автора задания
            task.setAuthor(rabotnic_ds.getRabotnicByCode(task.getAuthorCode()));
            tasks.add(task);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return tasks;
    }
    /**
     * Получить список заданий без дополнительных данных, такие как Attachments, Jobs, History
     * для экономии времени запроса
     * @return
     */
    public List<Task> getAllTasksWithoutAdditionalData() {
        List<Task> tasks = new ArrayList<Task>();

        Cursor cursor = database.query(DBHelper.TASK_TABLE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        Task task = null;
        while (!cursor.isAfterLast()) {
            task = cursorToTask(cursor);
            tasks.add(task);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return tasks;
    }
    /**
     * Получить список заданий сотрудников
     * @return
     */
    public int getCountOfStaffTasksOld() {
        int count = 0;

        Cursor cursor = database.query(DBHelper.TASK_TABLE,
                new String[]{DBHelper.TASK__ID}, null, null, null, null, null);

//        cursor.moveToFirst();
        count = cursor.getCount();
        // Make sure to close the cursor
        cursor.close();
        return count;
    }
    public int[] getCountOfStaffTasks() {
        int[] count = {0, 0, 0};
        String sql = String.format("SELECT count(%s.%s) FROM %s;", 
                DBHelper.TASK_TABLE, DBHelper.TASK__ID, DBHelper.TASK_TABLE);
        Cursor cursor = database.rawQuery(sql, null);
        cursor.moveToFirst();
        count[0] = cursor.getInt(0);
        cursor.close();
        
        sql = String.format("SELECT count(%s.%s) FROM %s WHERE date(%s.%s) = date();", 
                DBHelper.TASK_TABLE, DBHelper.TASK__ID, DBHelper.TASK_TABLE, DBHelper.TASK_TABLE, DBHelper.TASK_DEADLINE);
        cursor = database.rawQuery(sql, null);
        cursor.moveToFirst();
        count[1] = cursor.getInt(0);
        cursor.close();

        sql = String.format("SELECT count(%s.%s) FROM %s WHERE date(%s.%s) < date() AND %s.%s <> '1899-12-30 00:00:00';", 
                DBHelper.TASK_TABLE, DBHelper.TASK__ID, DBHelper.TASK_TABLE, DBHelper.TASK_TABLE, DBHelper.TASK_DEADLINE, DBHelper.TASK_TABLE, DBHelper.TASK_DEADLINE);
        cursor = database.rawQuery(sql, null);
        cursor.moveToFirst();
        count[2] = cursor.getInt(0);
        cursor.close();

        return count;
    }
    public String getTaskTitleById(long taskId) {
        String title = "";

        Cursor cursor = database.query(DBHelper.TASK_TABLE,
                new String[]{DBHelper.TASK_TITLE}, DBHelper.TASK_ID + " = " + taskId, null, null, null, null);

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            title = cursor.getString(0);
        }
        // Make sure to close the cursor
        cursor.close();
        return title;
    }
    
    public int deleteAllTasks() {
        AttachmentDataSource attachment_ds = new AttachmentDataSource(mContext);
        HistoryDataSource history_ds = new HistoryDataSource(mContext);
        JobDataSource job_ds = new JobDataSource(mContext);
        attachment_ds.open();
        history_ds.open();
        job_ds.open();
        attachment_ds.deleteAllAttachments();
        history_ds.deleteAllHistories();
        job_ds.deleteAllJobs();
        int count = database.delete(DBHelper.TASK_TABLE, "1", null);
        return count;
    }
    public int deleteTaskById(long id) {
        AttachmentDataSource attachment_ds = new AttachmentDataSource(mContext);
        HistoryDataSource history_ds = new HistoryDataSource(mContext);
        JobDataSource job_ds = new JobDataSource(mContext);
        attachment_ds.open();
        history_ds.open();
        job_ds.open();
        
        int c;
        c = attachment_ds.deleteAttachmentsByTaskId(id);
        Log.v("deleteTaskById", "deleted attachments count " + c);
        c = history_ds.deleteHistoriesByTaskId(id);
        Log.v("deleteTaskById", "deleted histories count " + c);
        c = job_ds.deleteJobsByTaskId(id);
        Log.v("deleteTaskById", "deleted jobs count " + c);
        
        int count = database.delete(DBHelper.TASK_TABLE, "id = ?", new String[] {String.valueOf(id)});
        return count;
    }

    private Task cursorToTask(Cursor cursor) {
        //int id, String address, String point, String work_hours, int brand, String last_modified
//        DBHelper.TASK__ID, DBHelper.TASK_ACTION_LIST, DBHelper.TASK_AUTHOR_CODE, DBHelper.TASK_CREATED, DBHelper.TASK_DEADLINE, DBHelper.TASK_EXECUTED,
//        DBHelper.TASK_ID, DBHelper.TASK_IMPORTANCE, DBHelper.TASK_OBSERVERS, DBHelper.TASK_PARTICIPANTS, DBHelper.TASK_ROUTE_NAME, 
//        DBHelper.TASK_STATE, DBHelper.TASK_SUBTASK_IDS, DBHelper.TASK_TITLE
//        
//        JSONArray action_list, JSONArray attachments, JSONArray history, JSONArray jobs, JSONArray observers, JSONArray participants, JSONArray subtask_ids,
//        String author_code, String created, String deadline, String executed, long id, String importance, String route_name, String state, String title
        JSONArray action_list = new JSONArray();
        JSONArray attachments = new JSONArray();
        JSONArray history = new JSONArray();
        JSONArray jobs = new JSONArray();
        JSONArray observers = new JSONArray();
        JSONArray participants = new JSONArray();
        JSONArray subtask_ids = new JSONArray();
        try {
            action_list = new JSONArray(cursor.getString(1));
            observers = new JSONArray(cursor.getString(8));
            participants = new JSONArray(cursor.getString(9));
            subtask_ids = new JSONArray(cursor.getString(12));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        Task task = new Task(action_list, attachments, history, jobs, observers, participants, subtask_ids, 
                cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getLong(6), cursor.getString(7), cursor.getString(10), 
                cursor.getString(11), cursor.getString(13));
//      Log.v("cursorToStore", cursor.getInt(0)+" "+cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getString(3)+" "+cursor.getInt(4)+" "+cursor.getString(5));
        return task;
    }
}
