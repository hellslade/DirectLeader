package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class HistoryDataSource {
    // Database fields
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private String[] allColumns = {DBHelper.HISTORY__ID, DBHelper.HISTORY_AUTHOR_CODE, DBHelper.HISTORY_DATE, DBHelper.HISTORY_MESSAGE, DBHelper.HISTORY_TASK_ID};
    private Context mContext;

    public HistoryDataSource(Context context) {
        dbHelper = DBHelper.getInstance(context);
        mContext = context;
    }
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }
    public void close() {
        dbHelper.close();
    }
    public void createHistory(History new_history) {
        ContentValues values = new ContentValues();
//        values.put(DBHelper.HISTORY__ID, new_history.getId());
        values.put(DBHelper.HISTORY_AUTHOR_CODE, new_history.getAuthorCode());
        values.put(DBHelper.HISTORY_DATE, new_history.getDate());
        values.put(DBHelper.HISTORY_MESSAGE, new_history.getMessage());
        values.put(DBHelper.HISTORY_TASK_ID, new_history.getTaskId());
        long insertId = database.insert(DBHelper.HISTORY_TABLE, null, values);
        
        /*Cursor cursor = database.query(DBHelper.HISTORY_TABLE,
                allColumns, DBHelper.HISTORY__ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        History newHistory = cursorToHistory(cursor);
        cursor.close();
        return newHistory;*/
    }
    public History[] getHistoriesByTaskId(long taskId) {
        History[] histories = null;
        Cursor cursor = database.query(DBHelper.HISTORY_TABLE,
                allColumns, DBHelper.HISTORY_TASK_ID + " = " + taskId, null, null, null, null);
        cursor.moveToFirst();
        histories = new History[cursor.getCount()];
        RabotnicDataSource rds = new RabotnicDataSource(mContext);
        rds.open();
        int i = 0;
        while (!cursor.isAfterLast()) {
            final History history = cursorToHistory(cursor);
            history.setUser(rds.getRabotnicByCode(history.getAuthorCode()));
            histories[i++] = (history);
            cursor.moveToNext();
        }
        //rds.close();
        cursor.close();
        return histories;
    }
    public List<History> getAllHistorys() {
        List<History> historys = new ArrayList<History>();

        Cursor cursor = database.query(DBHelper.HISTORY_TABLE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        RabotnicDataSource rds = new RabotnicDataSource(mContext);
        rds.open();
        while (!cursor.isAfterLast()) {
            final History history = cursorToHistory(cursor);
            history.setUser(rds.getRabotnicByCode(history.getAuthorCode()));
            historys.add(history);
            cursor.moveToNext();
        }
        //rds.close();
        // Make sure to close the cursor
        cursor.close();
        return historys;
    }
    public int deleteHistoriesByTaskId(long id) {
        int count = database.delete(DBHelper.HISTORY_TABLE, "task_id = ?", new String[] {String.valueOf(id)});
        return count;
    }
    
    public int deleteAllHistories() {
    	int count = database.delete(DBHelper.HISTORY_TABLE, "1", null);
        return count;
    }
    public void insertOrUpdate(History history) {
    	// History не может измениться, это по сути комментарий. Id отсутствует, поэтому смотрим соответствие даты и текста.
    	Cursor cursor = database.query(DBHelper.HISTORY_TABLE, allColumns, 
    			"date = ? AND message = ?", new String[] {history.getDate(), history.getMessage()}, null, null, null, "1");
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
        	//Log.v("HistoryDataSource", "history with " + history.getDate() + " " + history.getMessage() + " is alreadey created");
//        	updateHistory(history);
        } else {
        	createHistory(history);
        }
    }

    private History cursorToHistory(Cursor cursor) {
        //int id, String address, String point, String work_hours, int brand, String last_modified
//        DBHelper.HISTORY__ID, DBHelper.HISTORY_AUTHOR_CODE, DBHelper.HISTORY_DATE, DBHelper.HISTORY_MESSAGE, DBHelper.HISTORY_TASK_ID
//        String author_name, String ctitle, String created, String ext, long id, String modified, String name, boolean signed, long size, int version, long task_id
        History history = new History(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getLong(4));
//      Log.v("cursorToStore", cursor.getInt(0)+" "+cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getString(3)+" "+cursor.getInt(4)+" "+cursor.getString(5));
        return history;
    }
}
