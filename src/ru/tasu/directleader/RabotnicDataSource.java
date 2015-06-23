package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class RabotnicDataSource {
    // Database fields
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private String[] allColumns = {DBHelper.RABOTNIC__ID, DBHelper.RABOTNIC_CODE, DBHelper.RABOTNIC_CODERAB, DBHelper.RABOTNIC_ID, DBHelper.RABOTNIC_LOGIN, DBHelper.RABOTNIC_NAME,
            DBHelper.RABOTNIC_PHOTO, DBHelper.RABOTNIC_PODR, DBHelper.RABOTNIC_POST_KIND};

    public RabotnicDataSource(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }
    public void close() {
        dbHelper.close();
    }
    public void createRabotnik(Rabotnic new_rabotnik) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.RABOTNIC_CODE, new_rabotnik.getCode());
        values.put(DBHelper.RABOTNIC_CODERAB, new_rabotnik.getCodeRab());
        values.put(DBHelper.RABOTNIC_ID, new_rabotnik.getId());
        values.put(DBHelper.RABOTNIC_LOGIN, new_rabotnik.getLogin());
        values.put(DBHelper.RABOTNIC_NAME, new_rabotnik.getName());
        values.put(DBHelper.RABOTNIC_PHOTO, new_rabotnik.getPhoto());
        values.put(DBHelper.RABOTNIC_PODR, new_rabotnik.getPodr());
        values.put(DBHelper.RABOTNIC_POST_KIND, new_rabotnik.getPostKind());
        long insertId = database.insert(DBHelper.RABOTNIC_TABLE, null, values);
        
        /*Cursor cursor = database.query(DBHelper.RABOTNIC_TABLE,
                allColumns, DBHelper.RABOTNIC__ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Rabotnic newRabotnik = cursorToRabotnic(cursor);
        cursor.close();
        return newRabotnik;*/
    }
    public boolean createRabotnikFromJSON(JSONObject json) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.RABOTNIC_CODE, json.optString("Code"));
        values.put(DBHelper.RABOTNIC_CODERAB, json.optString("CodeRab"));
        values.put(DBHelper.RABOTNIC_ID, json.optLong("Id"));
        values.put(DBHelper.RABOTNIC_LOGIN, json.optString("Login"));
        values.put(DBHelper.RABOTNIC_NAME, json.optString("Name"));
        values.put(DBHelper.RABOTNIC_PHOTO, json.optString("Photo"));
        values.put(DBHelper.RABOTNIC_PODR, json.optString("Podr"));
        values.put(DBHelper.RABOTNIC_POST_KIND, json.optString("PostKind"));
        long insertId = database.insert(DBHelper.RABOTNIC_TABLE, null, values);
        if (insertId != -1) {
            return true;
        } else {
            return false;
        }
    }
    public Rabotnic getRabotnicById(long Id) {
        Rabotnic rabotnic = null;
        Cursor cursor = database.query(DBHelper.RABOTNIC_TABLE,
                allColumns, DBHelper.RABOTNIC_ID + " = " + Id, null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            rabotnic = cursorToRabotnic(cursor);
        }
        cursor.close();
        return rabotnic;
    }
    public Rabotnic getRabotnicByCode(String code) {
        Rabotnic rabotnic = null;
        Cursor cursor = database.query(DBHelper.RABOTNIC_TABLE,
                allColumns, DBHelper.RABOTNIC_CODE + " = ?", new String[]{code}, null, null, null, "1");
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            rabotnic = cursorToRabotnic(cursor);
        }
        cursor.close();
        return rabotnic;
    }
    public Rabotnic getRabotnicByCodeRab(String codeRab) {
        Rabotnic rabotnic = null;
        Cursor cursor = database.query(DBHelper.RABOTNIC_TABLE,
                allColumns, DBHelper.RABOTNIC_CODERAB + " = ?", new String[]{codeRab}, null, null, null, "1");
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            rabotnic = cursorToRabotnic(cursor);
        }
        cursor.close();
        return rabotnic;
    }
    public Rabotnic[] getAllRabotnicsWithTaskCount() {
        Rabotnic[] rabotnics = null;
        
        String sql = "SELECT rabotnic.*, count(j._id) from rabotnic LEFT JOIN job as j ON j.performer=rabotnic.code GROUP BY (rabotnic._id);";
        Cursor cursor = database.rawQuery(sql, null);
        cursor.moveToFirst();
        rabotnics = new Rabotnic[cursor.getCount()];
        int i = 0;
        while (!cursor.isAfterLast()) {
            final Rabotnic rabotnic = cursorToRabotnic(cursor);
            rabotnic.setTotalJobs(cursor.getInt(8));
            // Получим просроченные задания
            final String sqlOverdue = "SELECT rabotnic.*, count(j._id) from rabotnic LEFT JOIN job as j ON j.performer=rabotnic.code AND j.final_date < date() AND j.final_date <> '1899-12-30 00:00:00' AND rabotnic.code == ?;";
            final Cursor cursorOverdue = database.rawQuery(sqlOverdue, new String[]{rabotnic.getCode()});
            cursorOverdue.moveToFirst();
            rabotnic.setOverdueJobs(cursorOverdue.getInt(8));
            // Получим текущие задания
            final String sqlCurrent = "SELECT rabotnic.*, count(j._id) from rabotnic LEFT JOIN job as j ON j.performer=rabotnic.code AND j.final_date = date() AND rabotnic.code == ?;";
            final Cursor cursorCurrent = database.rawQuery(sqlCurrent, new String[]{rabotnic.getCode()});
            cursorCurrent.moveToFirst();
            rabotnic.setCurrentJobs(cursorCurrent.getInt(8));
            rabotnics[i++] = rabotnic;
            
            cursor.moveToNext();
        }
        
        cursor.close();
        return rabotnics;
    }
    public void updateRabotnik(Rabotnic new_rabotnik) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.RABOTNIC_CODE, new_rabotnik.getCode());
        values.put(DBHelper.RABOTNIC_CODERAB, new_rabotnik.getCodeRab());
//        values.put(DBHelper.RABOTNIC_ID, new_rabotnik.getId());
        values.put(DBHelper.RABOTNIC_LOGIN, new_rabotnik.getLogin());
        values.put(DBHelper.RABOTNIC_NAME, new_rabotnik.getName());
        values.put(DBHelper.RABOTNIC_PHOTO, new_rabotnik.getPhoto());
        values.put(DBHelper.RABOTNIC_PODR, new_rabotnik.getPodr());
        values.put(DBHelper.RABOTNIC_POST_KIND, new_rabotnik.getPostKind());
//        long insertId = database.insert(DBHelper.RABOTNIC_TABLE, null, values);
        Log.v("RabotnicDataSource", "updateRabotnik " + new_rabotnik.getId());
        int num = database.update(DBHelper.RABOTNIC_TABLE, values, "id=?", new String[]{String.valueOf(new_rabotnik.getId())});
        
        /*Cursor cursor = database.query(DBHelper.RABOTNIC_TABLE,
                allColumns, DBHelper.RABOTNIC__ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Rabotnic newRabotnik = cursorToRabotnic(cursor);
        cursor.close();
        return newRabotnik;*/
    }
    public void insertOrUpdate(Rabotnic rabotnik) {
        Cursor cursor = database.query(DBHelper.RABOTNIC_TABLE,
                allColumns, DBHelper.RABOTNIC_ID + " = " + rabotnik.getId(), null, null, null, null, "1");
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
        	updateRabotnik(rabotnik);
        } else {
        	createRabotnik(rabotnik);
        }
    }
    
    public List<Rabotnic> getAllRabotnics() {
        List<Rabotnic> rabotnics = new ArrayList<Rabotnic>();

        Cursor cursor = database.query(DBHelper.RABOTNIC_TABLE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        Rabotnic rabotnic = null;
        while (!cursor.isAfterLast()) {
            rabotnic = cursorToRabotnic(cursor);
            rabotnics.add(rabotnic);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return rabotnics;
    }
    
    private Rabotnic cursorToRabotnic(Cursor cursor) {
//        Log.v("RabotnicDataSource", "cursorToRabotnic() START");
        //int id, String address, String point, String work_hours, int brand, String last_modified
//        DBHelper.RABOTNIC__ID, DBHelper.RABOTNIC_CODE, DBHelper.RABOTNIC_CODERAB, DBHelper.RABOTNIC_ID, DBHelper.RABOTNIC_LOGIN, DBHelper.RABOTNIC_NAME,
//        DBHelper.RABOTNIC_PHOTO, DBHelper.RABOTNIC_PODR, DBHelper.RABOTNIC_POST_KIND
//        String code, long id, String login, String name, String photo, String podr, String podt_kind
        // Вместо photo "cursor.getString(6)" передаю пустую строку, почему-то photo очень тормозит создание объекта 
        Rabotnic rabotnic = new Rabotnic(cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getString(4), cursor.getString(5), "", cursor.getString(7), cursor.getString(8));
//      Log.v("cursorToStore", cursor.getInt(0)+" "+cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getString(3)+" "+cursor.getInt(4)+" "+cursor.getString(5));
//        Log.v("RabotnicDataSource", "cursorToRabotnic() END");
        return rabotnic;
    }
}
