package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

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
    private String[] allColumns = {DBHelper.RABOTNIC__ID, DBHelper.RABOTNIC_CODE, DBHelper.RABOTNIC_ID, DBHelper.RABOTNIC_LOGIN, DBHelper.RABOTNIC_NAME,
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
    public Rabotnic createRabotnik(Rabotnic new_rabotnik) {
        ContentValues values = new ContentValues();
//        values.put(DBHelper.RABOTNIC__ID, new_history.getId());
        values.put(DBHelper.RABOTNIC_CODE, new_rabotnik.getCode());
        values.put(DBHelper.RABOTNIC_ID, new_rabotnik.getId());
        values.put(DBHelper.RABOTNIC_LOGIN, new_rabotnik.getLogin());
        values.put(DBHelper.RABOTNIC_NAME, new_rabotnik.getName());
        values.put(DBHelper.RABOTNIC_PHOTO, new_rabotnik.getPhoto());
        values.put(DBHelper.RABOTNIC_PODR, new_rabotnik.getPodr());
        values.put(DBHelper.RABOTNIC_POST_KIND, new_rabotnik.getPostKind());
        long insertId = database.insert(DBHelper.RABOTNIC_TABLE, null, values);
        
        Cursor cursor = database.query(DBHelper.RABOTNIC_TABLE,
                allColumns, DBHelper.RABOTNIC__ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Rabotnic newRabotnik = cursorToRabotnic(cursor);
        cursor.close();
        return newRabotnik;
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
//        String sql = "SELECT * FROM rabotnic WHERE code = ? LIMIT 1;";
//        Log.v("RabotnicDataSource", "getRabotnicByCode() START");
//        Cursor cursor = database.rawQuery(sql, new String[]{code});
//        Log.v("RabotnicDataSource", "getRabotnicByCode() END");
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            rabotnic = cursorToRabotnic(cursor);
        }
        cursor.close();
        return rabotnic;
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
    public int deleteAllRabotnics() {
        int count = database.delete(DBHelper.RABOTNIC_TABLE, "1", null);
        return count;
    }

    private Rabotnic cursorToRabotnic(Cursor cursor) {
//        Log.v("RabotnicDataSource", "cursorToRabotnic() START");
        //int id, String address, String point, String work_hours, int brand, String last_modified
//        DBHelper.RABOTNIC__ID, DBHelper.RABOTNIC_CODE, DBHelper.RABOTNIC_ID, DBHelper.RABOTNIC_LOGIN, DBHelper.RABOTNIC_NAME,
//        DBHelper.RABOTNIC_PHOTO, DBHelper.RABOTNIC_PODR, DBHelper.RABOTNIC_POST_KIND
//        String code, long id, String login, String name, String photo, String podr, String podt_kind
        // Вместо photo "cursor.getString(5)" передаю пустую строку, почему-то photo очень тормозит создание объекта 
        Rabotnic rabotnic = new Rabotnic(cursor.getString(1), cursor.getLong(2), cursor.getString(3), cursor.getString(4), "", cursor.getString(6), cursor.getString(7));
//      Log.v("cursorToStore", cursor.getInt(0)+" "+cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getString(3)+" "+cursor.getInt(4)+" "+cursor.getString(5));
//        Log.v("RabotnicDataSource", "cursorToRabotnic() END");
        return rabotnic;
    }
}
