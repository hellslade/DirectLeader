package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AttachmentDataSource {
    // Database fields
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private Context mContext;
    private String[] allColumns = {DBHelper.ATTACHMENT__ID, DBHelper.ATTACHMENT_AUTHOR_NAME, DBHelper.ATTACHMENT_CTITLE, DBHelper.ATTACHMENT_CREATED, 
            DBHelper.ATTACHMENT_EXT, DBHelper.ATTACHMENT_ID, DBHelper.ATTACHMENT_MODIFIED, DBHelper.ATTACHMENT_NAME, DBHelper.ATTACHMENT_SIGNED, 
            DBHelper.ATTACHMENT_SIZE, DBHelper.ATTACHMENT_VERSION, DBHelper.ATTACHMENT_TASK_ID};
    
    public AttachmentDataSource(Context context) {
        dbHelper = DBHelper.getInstance(context);
        mContext = context;
    }
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }
    public void close() {
        dbHelper.close();
    }
    public void createAttachment(Attachment new_attachment) {
        ContentValues values = new ContentValues();
//        values.put(DBHelper.ATTACHMENT__ID, new_attachment.getId());
        values.put(DBHelper.ATTACHMENT_AUTHOR_NAME, new_attachment.getAuthorName());
        values.put(DBHelper.ATTACHMENT_CTITLE, new_attachment.getCTitle());
        values.put(DBHelper.ATTACHMENT_CREATED, new_attachment.getCreated());
        values.put(DBHelper.ATTACHMENT_EXT, new_attachment.getExt());
        values.put(DBHelper.ATTACHMENT_ID, new_attachment.getId());
        values.put(DBHelper.ATTACHMENT_MODIFIED, new_attachment.getModified());
        values.put(DBHelper.ATTACHMENT_NAME, new_attachment.getName());
        values.put(DBHelper.ATTACHMENT_SIGNED, new_attachment.getSigned());
        values.put(DBHelper.ATTACHMENT_SIZE, new_attachment.getSize());
        values.put(DBHelper.ATTACHMENT_VERSION, new_attachment.getVersion());
        values.put(DBHelper.ATTACHMENT_TASK_ID, new_attachment.getTaskId());
        long insertId = database.insert(DBHelper.ATTACHMENT_TABLE, null, values);
        
        /*Cursor cursor = database.query(DBHelper.ATTACHMENT_TABLE,
                allColumns, DBHelper.ATTACHMENT__ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Attachment newAttachment = cursorToAttachment(cursor);
        cursor.close();
        return newAttachment;*/
    }
    public void deleteAttachment(Attachment attachment) {
        long id = attachment.getId();
        System.out.println("Attachment deleted with id: " + id);
        database.delete(DBHelper.ATTACHMENT_TABLE, DBHelper.ATTACHMENT_ID
                + " = " + id, null);
    }
    public Attachment getAttachmentById(long id) {
        Attachment attachment = null;
        Cursor cursor = database.query(DBHelper.ATTACHMENT_TABLE,
                allColumns, DBHelper.ATTACHMENT_ID + " = " + id, null, null, null, null);
        cursor.moveToFirst();
        TaskDataSource tds = new TaskDataSource(mContext);
        tds.open();
        if (cursor.getCount() > 0) {
            attachment = cursorToAttachment(cursor);
            attachment.setTaskTitle(tds.getTaskTitleById(attachment.getTaskId()));
        }
        cursor.close();
        return attachment;
    }
    public Attachment[] getAttachmentsByTaskId(long taskId) {
        Attachment[] attachments = null;
        Cursor cursor = database.query(DBHelper.ATTACHMENT_TABLE,
                allColumns, DBHelper.ATTACHMENT_TASK_ID + " = " + taskId, null, null, null, null);
        cursor.moveToFirst();
        attachments = new Attachment[cursor.getCount()];
        TaskDataSource tds = new TaskDataSource(mContext);
        tds.open();
        int i = 0;
        while (!cursor.isAfterLast()) {
            final Attachment attachment = cursorToAttachment(cursor);
            attachment.setTaskTitle(tds.getTaskTitleById(attachment.getTaskId()));
            attachments[i++] = (attachment);
            cursor.moveToNext();
        }
        cursor.close();
        return attachments;
    }
    public int getAttachmentsCountByTaskId(long taskId) {
        int count = 0;
        Cursor cursor = database.query(true, DBHelper.ATTACHMENT_TABLE,
                new String[]{DBHelper.ATTACHMENT_ID}, DBHelper.ATTACHMENT_TASK_ID + " = " + taskId, null, null, null, null, null);
//        cursor.moveToFirst();
        count = cursor.getCount();
        cursor.close();
        return count;
    }
    public List<Attachment> getAllAttachments() {
        List<Attachment> attachments = new ArrayList<Attachment>();
        
//        Cursor cursor = database.query(true, DBHelper.ATTACHMENT_TABLE,
//                allColumns, null, null, null, null, null, null);
        String sql = "select a1.* from attachment as a1 left join attachment as a2 on a1.id=a2.id group by a1.id";
        Cursor cursor = database.rawQuery(sql, null);
        
        cursor.moveToFirst();
        TaskDataSource tds = new TaskDataSource(mContext);
        tds.open();
        while (!cursor.isAfterLast()) {
            final Attachment attachment = cursorToAttachment(cursor);
            attachment.setTaskTitle(tds.getTaskTitleById(attachment.getTaskId()));
            attachments.add(attachment);
            cursor.moveToNext();
        }
        //tds.close();
        // Make sure to close the cursor
        cursor.close();
        return attachments;
    }
    public int deleteAttachmentsByTaskId(long id) {
        int count = database.delete(DBHelper.ATTACHMENT_TABLE, "task_id = ?", new String[] {String.valueOf(id)});
        return count;
    }
    public void updateAttachment(Attachment attachment) {
    	ContentValues values = new ContentValues();
      values.put(DBHelper.ATTACHMENT_AUTHOR_NAME, attachment.getAuthorName());
      values.put(DBHelper.ATTACHMENT_CTITLE, attachment.getCTitle());
      values.put(DBHelper.ATTACHMENT_CREATED, attachment.getCreated());
      values.put(DBHelper.ATTACHMENT_EXT, attachment.getExt());
//      values.put(DBHelper.ATTACHMENT_ID, attachment.getId());
      values.put(DBHelper.ATTACHMENT_MODIFIED, attachment.getModified());
      values.put(DBHelper.ATTACHMENT_NAME, attachment.getName());
      values.put(DBHelper.ATTACHMENT_SIGNED, attachment.getSigned());
      values.put(DBHelper.ATTACHMENT_SIZE, attachment.getSize());
      values.put(DBHelper.ATTACHMENT_VERSION, attachment.getVersion());
      values.put(DBHelper.ATTACHMENT_TASK_ID, attachment.getTaskId());
      int num = database.update(DBHelper.ATTACHMENT_TABLE, values, "id=?", new String[]{String.valueOf(attachment.getId())});
      
//      Attachment newAttachment = getAttachmentById(attachment.getId());
//      return newAttachment;
    }
    public void insertOrUpdate(Attachment attachment) {
    	Cursor cursor = database.query(DBHelper.ATTACHMENT_TABLE,
                allColumns, DBHelper.ATTACHMENT_ID + " = " + attachment.getId(), null, null, null, null, "1");
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
        	updateAttachment(attachment);
        } else {
        	createAttachment(attachment);
        }
    }
    /**
     * ѕолучить общее количество документов
     * @return
     */
    public int getCountOfAttachments() {
        int count = 0;
        Cursor cursor = database.query(true, DBHelper.ATTACHMENT_TABLE,
                new String[]{DBHelper.ATTACHMENT_ID}, null, null, null, null, null, null);
//        cursor.moveToFirst();
        count = cursor.getCount();
        
        // Make sure to close the cursor
        cursor.close();
        return count;
    }

    private Attachment cursorToAttachment(Cursor cursor) {
        //int id, String address, String point, String work_hours, int brand, String last_modified
//        DBHelper.ATTACHMENT__ID, DBHelper.ATTACHMENT_AUTHOR_NAME, DBHelper.ATTACHMENT_CTITLE, DBHelper.ATTACHMENT_CREATED, 
//        DBHelper.ATTACHMENT_EXT, DBHelper.ATTACHMENT_ID, DBHelper.ATTACHMENT_MODIFIED, DBHelper.ATTACHMENT_NAME, DBHelper.ATTACHMENT_SIGNED, 
//        DBHelper.ATTACHMENT_SIZE, DBHelper.ATTACHMENT_VERSION, DBHelper.ATTACHMENT_TASK_ID
        boolean signed = cursor.getInt(8) == 1;
//        String author_name, String ctitle, String created, String ext, long id, String modified, String name, boolean signed, long size, int version, long task_id
        Attachment attachment = new Attachment(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getLong(5), cursor.getString(6), cursor.getString(7),
                signed, cursor.getLong(9), cursor.getInt(10), cursor.getLong(11));
//      Log.v("cursorToStore", cursor.getInt(0)+" "+cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getString(3)+" "+cursor.getInt(4)+" "+cursor.getString(5));
        return attachment;
    }
}
