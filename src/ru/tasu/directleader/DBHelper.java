package ru.tasu.directleader;

import java.io.File;

import org.json.JSONArray;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
private static final String TAG = "DBHelper";
    
    private static DBHelper mInstance = null;
    private static Context mContext;
    private static File mCacheDir;
    
    private static final String DB_NAME = "/database/directleader";
    private static String DB_PATH = null;

    private static final int DB_VERSION = 1;
    
    /* Описание таблиц БД */
    /* таблица "Рубрики" */
    public static final String ATTACHMENT_TABLE = "attachment";
    public static final String ATTACHMENT__ID = "_id";
    public static final String ATTACHMENT_AUTHOR_NAME = "author_name";
    public static final String ATTACHMENT_CTITLE = "ctitle";
    public static final String ATTACHMENT_CREATED = "created";
    public static final String ATTACHMENT_EXT = "ext";
    public static final String ATTACHMENT_ID = "id";
    public static final String ATTACHMENT_MODIFIED = "modified";
    public static final String ATTACHMENT_NAME = "name";
    public static final String ATTACHMENT_SIGNED = "signed";
    public static final String ATTACHMENT_SIZE = "size";
    public static final String ATTACHMENT_VERSION = "version";
    public static final String ATTACHMENT_TASK_ID = "task_id";
    private static final String ATTACHMENT_CREATE_TABLE = 
            String.format("create table %s (" +
                    "%s integer primary key, " +
                    "%s text, " +
                    "%s text, " +
                    "%s timestamp, " +
                    "%s varchar, " +
                    "%s integer, " +
                    "%s timestamp, " +
                    "%s text, " +
                    "%s integer, " +
                    "%s integer, " +
                    "%s integer, " +
                    "%s integer)",
            ATTACHMENT_TABLE,
            ATTACHMENT__ID, ATTACHMENT_AUTHOR_NAME, ATTACHMENT_CTITLE, ATTACHMENT_CREATED, 
            ATTACHMENT_EXT, ATTACHMENT_ID, ATTACHMENT_MODIFIED, ATTACHMENT_NAME, ATTACHMENT_SIGNED, ATTACHMENT_SIZE, ATTACHMENT_VERSION, ATTACHMENT_TASK_ID);

    public static final String HISTORY_TABLE = "history";
    public static final String HISTORY__ID = "_id";
    public static final String HISTORY_AUTHOR_CODE = "author_code";
    public static final String HISTORY_DATE = "date";
    public static final String HISTORY_MESSAGE = "message";
    public static final String HISTORY_TASK_ID = "task_id";
    private static final String HISTORY_CREATE_TABLE = 
            String.format("create table %s (" +
                    "%s integer primary key, " +
                    "%s varchar, " +
                    "%s timestamp, " +
                    "%s text, " +
                    "%s integer)",
                    HISTORY_TABLE,
                    HISTORY__ID, HISTORY_AUTHOR_CODE, HISTORY_DATE, HISTORY_MESSAGE, HISTORY_TASK_ID);
    
    public static final String JOB_TABLE = "job";
    public static final String JOB__ID = "_id";
    public static final String JOB_ACTION_LIST = "action_list";
    public static final String JOB_END_DATE = "end_date";
    public static final String JOB_FINAL_DATE = "final_date";
    public static final String JOB_ID = "id";
    public static final String JOB_MAIN_TASK_JOB = "main_task_job";
    public static final String JOB_PERFORMER = "performer";
    public static final String JOB_READED = "readed";
    public static final String JOB_RESULT_TITLE = "result_title";
    public static final String JOB_START_DATE = "start_date";
    public static final String JOB_STATE = "state";
    public static final String JOB_STATE_TITLE = "state_title";
    public static final String JOB_SUBJECT = "subject";
    public static final String JOB_FAVORITE = "favorite";
    private static final String JOB_CREATE_TABLE = 
            String.format("create table %s (" +
                    "%s integer primary key, " +
                    "%s text, " +
                    "%s timestamp, " +
                    "%s timestamp, " +
                    "%s integer, " +
                    "%s integer, " +
                    "%s varchar, " +
                    "%s integer, " +
                    "%s text, " +
                    "%s timestamp, " +
                    "%s integer, " +
                    "%s text, " +
                    "%s text, " +
                    "%s integer)",
                    JOB_TABLE,
                    JOB__ID, JOB_ACTION_LIST, JOB_END_DATE, JOB_FINAL_DATE, JOB_ID, 
                    JOB_MAIN_TASK_JOB, JOB_PERFORMER, JOB_READED, JOB_RESULT_TITLE, JOB_START_DATE, JOB_STATE, JOB_STATE_TITLE, JOB_SUBJECT, JOB_FAVORITE);
    
    public static final String RABOTNIC_TABLE = "rabotnic";
    public static final String RABOTNIC__ID = "_id";
    public static final String RABOTNIC_CODE = "code";
    public static final String RABOTNIC_CODERAB = "coderab";
    public static final String RABOTNIC_ID = "id";
    public static final String RABOTNIC_LOGIN = "login";
    public static final String RABOTNIC_NAME = "name";
    public static final String RABOTNIC_PHOTO = "photo";
    public static final String RABOTNIC_PODR = "podr";
    public static final String RABOTNIC_POST_KIND = "post_kind";
    private static final String RABOTNIC_CREATE_TABLE = 
            String.format("create table %s (" +
                    "%s integer primary key, " +
                    "%s text, " +
                    "%s text, " +
                    "%s integer, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text)",
                    RABOTNIC_TABLE,
                    RABOTNIC__ID, RABOTNIC_CODE, RABOTNIC_CODERAB, RABOTNIC_ID, RABOTNIC_LOGIN, RABOTNIC_NAME, RABOTNIC_PHOTO, RABOTNIC_PODR, RABOTNIC_POST_KIND);
    
    private static final String RABOTNIC_CREATE_INDEX = String.format("CREATE INDEX %s_%s ON %s (%s COLLATE NOCASE);", RABOTNIC_TABLE, RABOTNIC_CODE, RABOTNIC_TABLE, RABOTNIC_CODE);
            
    public static final String TASK_TABLE = "task";
    public static final String TASK__ID = "_id";
    public static final String TASK_ACTION_LIST = "action_list";
    public static final String TASK_AUTHOR_CODE = "author_code";
    public static final String TASK_CREATED = "created";
    public static final String TASK_DEADLINE = "deadline";
    public static final String TASK_EXECUTED = "executed";
    public static final String TASK_ID = "id";
    public static final String TASK_IMPORTANCE = "importance";
    public static final String TASK_OBSERVERS = "observers";
    public static final String TASK_PARTICIPANTS = "participants";
    public static final String TASK_ROUTE_NAME = "route_name";
    public static final String TASK_STATE = "state";
    public static final String TASK_SUBTASK_IDS = "subtask_ids";
    public static final String TASK_TITLE = "title";
    private static final String TASK_CREATE_TABLE = 
            String.format("create table %s (" +
                    "%s integer primary key, " +
                    "%s text, " +
                    "%s varchar, " +
                    "%s timestamp, " +
                    "%s timestamp, " +
                    "%s timestamp, " +
                    "%s integer, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text)",
                    TASK_TABLE,
                    TASK__ID, TASK_ACTION_LIST, TASK_AUTHOR_CODE, TASK_CREATED, TASK_DEADLINE, TASK_EXECUTED, TASK_ID, TASK_IMPORTANCE, 
                    TASK_OBSERVERS, TASK_PARTICIPANTS, TASK_ROUTE_NAME, TASK_STATE, TASK_SUBTASK_IDS, TASK_TITLE);
    
    public static final String RESOLUTION_TABLE = "resolution";
    public static final String RESOLUTION__ID = "_id";
    public static final String RESOLUTION_ID = "id";
    public static final String RESOLUTION_TASK_ID = "task_id";
    public static final String RESOLUTION_DETAIL = "detail";
    public static final String RESOLUTION_HEADER = "header";
    private static final String RESOLUTION_CREATE_TABLE = 
            String.format("create table %s (" +
                    "%s integer primary key, " +
                    "%s integer, " +
                    "%s integer, " +
                    "%s text, " +
                    "%s text)",
                    RESOLUTION_TABLE,
                    RESOLUTION__ID, RESOLUTION_ID, RESOLUTION_TASK_ID, RESOLUTION_DETAIL, RESOLUTION_HEADER);
    
    static public File getCacheDir(Context context) {
        File cache = null;
        String state = Environment.getExternalStorageState();
//        Log.v(TAG, "storage state is " + state);
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // SD карта в наличии, будем писать на нее
            cache = context.getExternalCacheDir();
        } else {
            // Карты нет, нужно по-другому :)
            cache = context.getCacheDir();
            if (cache == null) {
                // Почему-то путь до кэша всеравно null
                cache = new File("/data/data/ru.tasu.directleader/cache/");
            }
        }
//        Log.v(TAG, "DBHelper.java cache dir " + cache);
        return cache;
    }
    public static DBHelper getInstance(Context context) {
        mContext = context;
        mCacheDir = getCacheDir(mContext);
        if (mInstance == null) {
            DB_PATH = mCacheDir + DB_NAME;
            mInstance = new DBHelper(context.getApplicationContext());
        }
        return mInstance;
    }
    public DBHelper(Context context) {
        super(context, DB_PATH, null, DB_VERSION);
        Log.v("DB PATH", this.DB_PATH);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(ATTACHMENT_CREATE_TABLE);
        sqLiteDatabase.execSQL(HISTORY_CREATE_TABLE);
        sqLiteDatabase.execSQL(JOB_CREATE_TABLE);
        sqLiteDatabase.execSQL(RABOTNIC_CREATE_TABLE);
        sqLiteDatabase.execSQL(TASK_CREATE_TABLE);
        sqLiteDatabase.execSQL(RESOLUTION_CREATE_TABLE);
        
        sqLiteDatabase.execSQL(RABOTNIC_CREATE_INDEX);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
