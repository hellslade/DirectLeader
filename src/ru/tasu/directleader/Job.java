package ru.tasu.directleader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Job implements Parcelable {
private static final String TAG = "Job";
    
    // Описание полей
    private JSONArray _action_list;
    private String _end_date;
    private String _final_date;
    private long _id;
    private long _main_task_job;
    private String _performer;
    private boolean _readed;
    private String _result_title;
    private String _start_date;
    private boolean _state;
    private String _state_title;
    private String _subject;
    private Rabotnic _user; // Исполнитель задания
    private Rabotnic _author; // Исполнитель задания
    // Служебные поля, незаписываемые в БД
    private int _attachment_count = 0;
    private int _subtask_count = 0;
    private String _importance;
    private boolean _favorite;
    
    /*
    {
        ActionList: [
            {
                Name: "",
                Title: "Выполнить"
            }
        ],
        EndDate: "2013-12-05 13:08:05",
        FinalDate: "2013-10-31 00:00:00",
        Id: 122904,
        MainTaskJob: 34364,
        Performer: "Д000086",
        Readed: true,
        ResultTitle: null,
        SigningRequired: false,
        StartDate: "2013-10-02 17:04:16",
        State: true,
        StateTitle: "Выполнено",
        Subject: ">> Зарегистрировать Письмо входящее из Банк "Левобережный" ОАО г. Новосибирск, Об участии в выставке"
    }
    // */
    public Job(JSONObject json) {
        updateData(json);
    }
    public Job(JSONArray action_list, String end_date, String final_date, long id, long main_task_job, String performer, 
            boolean readed, String result_title, String start_date, boolean state, String state_title, String subject, boolean favorite) {
        this._action_list = action_list;
        this._end_date = end_date;
        this._final_date = final_date;
        this._id = id;
        this._main_task_job = main_task_job;
        this._performer = performer;
        this._readed = readed;
        this._result_title = result_title;
        this._start_date = start_date;
        this._state = state;
        this._state_title = state_title;
        this._subject = subject;
        this._favorite = favorite;
    }
    public Job(Parcel in) {
        try {
            this._action_list = new JSONArray(in.readString());
        } catch (JSONException e) {
            Log.v(TAG, "_action_list read exception " + e.getMessage());
        }
        this._end_date = in.readString();
        this._final_date = in.readString();
        this._id = in.readLong();
        this._main_task_job = in.readLong();
        this._performer = in.readString();
        this._readed = in.readByte() != 0;
        this._result_title = in.readString();
        this._start_date = in.readString();
        this._state = in.readByte() != 0;
        this._state_title = in.readString();
        this._subject = in.readString();
        this._user = in.readParcelable(Rabotnic.class.getClassLoader());
        this._author = in.readParcelable(Rabotnic.class.getClassLoader());
        this._attachment_count = in.readInt();
        this._subtask_count = in.readInt();
        this._importance = in.readString();
        this._favorite = in.readByte() != 0;
    }
    public void updateData(JSONObject data) {
        this._action_list = data.optJSONArray("ActionList");
        this._end_date = data.optString("EndDate");
        this._final_date = data.optString("FinalDate");
        this._id = data.optLong("Id");
        this._main_task_job = data.optLong("MainTaskJob");
        this._performer = data.optString("Performer");
        this._readed = data.optBoolean("Readed");
        this._result_title = data.optString("ResultTitle");
        this._start_date = data.optString("StartDate");
        this._state = data.optBoolean("State");
        this._state_title = data.optString("StateTitle");
        this._subject = data.optString("Subject");   
    }
    public String getActionList() {
        return this._action_list.toString();
    }
    public String getEndDate() {
        return getEndDate(false);
    }
    /**
     * Дата фактического завершения задания
     * @param formatted
     * @return
     */
    public String getEndDate(boolean formatted) {
        if (formatted) {
            return Utils.formatDateTime(this._end_date);
        } else {
            return this._end_date;
        }
    }
    public String getFinalDate() {
        return getFinalDate(false);
    }
    /**
     * Дата планового завершения задания (Срок)
     * @param formatted
     * @return
     */
    public String getFinalDate(boolean formatted) {
        if (formatted) {
            return Utils.formatDateTime(this._final_date);
        } else {
            return this._final_date;
        }
    }
    public long getId() {
        return this._id;
    }
    public long getMainTaskJob() {
        return this._main_task_job;
    }
    public String getPerformer() {
        return this._performer;
    }
    public boolean getReaded() {
        return this._readed;
    }
    public String getResultTitle() {
        return this._result_title;
    }
    public String getStartDate() {
        return getStartDate(false);
    }
    public String getStartDate(boolean formatted) {
        if (formatted) {
            return Utils.formatDateTime(this._start_date);
        } else {
            return this._start_date;
        }
    }
    public boolean getState() {
        return this._state;
    }
    public String getStateTitle() {
        return this._state_title;
    }
    public String getSubject() {
        return this._subject;
    }
    
    public void setAttachmentCount(int count) {
        this._attachment_count = count;
    }
    public void setSubtaskCount(int count) {
        this._subtask_count = count;
    }
    public int getAttachmentCount() {
        return this._attachment_count;
    }
    public int  getSubtaskCount() {
        return this._subtask_count;
    }
    
    public boolean isOverdue() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Utils.mLocale);
        Date deadline;
        try {
            deadline = format.parse(this._final_date);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        
        Calendar now = Calendar.getInstance();
        // Считаем количество дней до deadline
        // Если задача уже просрочена, то вернется -1
        long daysCount = Utils.daysBetween(now.getTime(), deadline);
        if (daysCount == -1) {
            return true;
        }
        return false;
    }
    public boolean isCurrent() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Utils.mLocale);
        Date deadline;
        try {
            deadline = format.parse(this._final_date);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        
        Calendar now = Calendar.getInstance();
        // Считаем количество дней до deadline
        // Если задача уже просрочена, то вернется -1
        long daysCount = Utils.daysBetween(now.getTime(), deadline);
        if (daysCount == 0) {
            return true;
        }
        return false;
    }
    
    public Rabotnic getUser() {
        return this._user;
    }
    public void setUser(Rabotnic user) {
        this._user = user;
    }
    public Rabotnic getAuthor() {
        return this._author;
    }
    public void setAuthor(Rabotnic author) {
        this._author = author;
    }
    public void setImportance(String importance) {
        this._importance = importance;
    }
    public String getImportance() {
        return this._importance;
    }
    public void setFavorite(boolean favorite) {
        this._favorite = favorite;
    }
    public boolean getFavorite() {
        return this._favorite;
    }
    
    public static final Parcelable.Creator<Job> CREATOR = new Parcelable.Creator<Job>() {

        public Job createFromParcel(Parcel in) {
            return new Job(in);
        }

        public Job[] newArray(int size) {
            return new Job[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this._action_list.toString());
        parcel.writeString(this._end_date);
        parcel.writeString(this._final_date);
        parcel.writeLong(this._id);
        parcel.writeLong(this._main_task_job);
        parcel.writeString(this._performer);
        parcel.writeByte((byte) (this._readed ? 1 : 0));
        parcel.writeString(this._result_title);
        parcel.writeString(this._start_date);
        parcel.writeByte((byte) (this._state ? 1 : 0));
        parcel.writeString(this._state_title);
        parcel.writeString(this._subject);
        parcel.writeParcelable(this._user, flags);
        parcel.writeParcelable(this._author, flags);
        parcel.writeInt(this._attachment_count);
        parcel.writeInt(this._subtask_count);
        parcel.writeString(this._importance);
        parcel.writeByte((byte) (this._favorite ? 1 : 0));
    }
}
