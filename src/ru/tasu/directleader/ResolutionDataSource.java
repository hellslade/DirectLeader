package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ResolutionDataSource {
    // Database fields
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private String[] allColumns = {DBHelper.RESOLUTION__ID, DBHelper.RESOLUTION__ID, DBHelper.RESOLUTION_TASK_ID, DBHelper.RESOLUTION_DETAIL, DBHelper.RESOLUTION_HEADER};
    private Context mContext;

    public ResolutionDataSource(Context context) {
        dbHelper = DBHelper.getInstance(context);
        mContext = context;
    }
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }
    public void close() {
        dbHelper.close();
    }
    public void createResolution(Resolution resolution) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.RESOLUTION_ID, resolution.getId());
        values.put(DBHelper.RESOLUTION_TASK_ID, resolution.getTaskId());
        values.put(DBHelper.RESOLUTION_DETAIL, resolution.getReferenceDetail());
        values.put(DBHelper.RESOLUTION_HEADER, resolution.getReferenceHeader());
        long insertId = database.insert(DBHelper.RESOLUTION_TABLE, null, values);
        
        /*Cursor cursor = database.query(DBHelper.HISTORY_TABLE,
                allColumns, DBHelper.HISTORY__ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        History newHistory = cursorToHistory(cursor);
        cursor.close();
        return newHistory;*/
    }
    public Resolution[] getResolutionsByTaskId(long taskId) {
    	Resolution[] resolutions = null;
        Cursor cursor = database.query(DBHelper.RESOLUTION_TABLE,
                allColumns, DBHelper.RESOLUTION_TASK_ID + " = " + taskId, null, null, null, null);
        cursor.moveToFirst();
        resolutions = new Resolution[cursor.getCount()];
        int i = 0;
        while (!cursor.isAfterLast()) {
            final Resolution resl = cursorToResolution(cursor);
            resolutions[i++] = (resl);
            cursor.moveToNext();
        }
        //rds.close();
        cursor.close();
        return resolutions;
    }
    public List<Resolution> getAllResolutions() {
        List<Resolution> resolutions = new ArrayList<Resolution>();

        Cursor cursor = database.query(DBHelper.RESOLUTION_TABLE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            final Resolution resl = cursorToResolution(cursor);
            resolutions.add(resl);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return resolutions;
    }
    public Resolution updateResolutionById(Resolution resolution) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.RESOLUTION_TASK_ID, resolution.getTaskId());
        values.put(DBHelper.RESOLUTION_DETAIL, resolution.getReferenceDetail());
        values.put(DBHelper.RESOLUTION_HEADER, resolution.getReferenceHeader());
//        long insertId = database.insert(DBHelper.RESOLUTION_TABLE, null, values);
        int num = database.update(DBHelper.RESOLUTION_TABLE, values, "id=?", new String[]{String.valueOf(resolution.getId())});
        
        Cursor cursor = database.query(DBHelper.RESOLUTION_TABLE,
                allColumns, DBHelper.RESOLUTION_ID + " = " + resolution.getId(), null, null, null, null);
        cursor.moveToFirst();
        Resolution resl = cursorToResolution(cursor);
        cursor.close();
        return resl;
    }
    public int deleteResolutionsByTaskId(long id) {
        int count = database.delete(DBHelper.RESOLUTION_TABLE, DBHelper.RESOLUTION_TASK_ID + " = ?", new String[] {String.valueOf(id)});
        return count;
    }
    
    public int deleteAllResolutions() {
    	int count = database.delete(DBHelper.RESOLUTION_TABLE, "1", null);
        return count;
    }
    @Deprecated
    public void insertOrUpdate(Resolution resolution) {
    	// Resolution может изменитьс€ и, веро€тно, часто это будет делать. Ќо у мен€ резолюци€ хранитс€ как json строка, поэтому обновить ее € могу только целиком.
    	// ѕридетс€ при каждом обновлении task удал€ть все резолюции и создавать заново.
    }

    private Resolution cursorToResolution(Cursor cursor) {
//    	allColumns = {DBHelper.RESOLUTION__ID, DBHelper.RESOLUTION__ID, DBHelper.RESOLUTION_TASK_ID, DBHelper.RESOLUTION_DETAIL, DBHelper.RESOLUTION_HEADER};
    	Resolution resl = new Resolution(cursor.getString(3), cursor.getString(4), 0, cursor.getLong(2));
        return resl;
    }
}
