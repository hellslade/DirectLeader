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

public class Task implements Parcelable {
    private static final String TAG = "Task";
    
    /*
    {
        ActionList: [
        {
            Name: "Abort",
            Title: "Прекратить"
        },
        {
            Name: "ReInit",
            Title: "Рестарт"
        }
        ],
        Attachments: [ ],
        AuthorCode: "Д000086",
        Created: "2013-10-02 17:03:47",
        Deadline: "2013-10-31 00:00:00",
        Executed: "1899-12-30 00:00:00",
        History: [ ],
        Id: 34364,
        Importance: "Обычная",
        Jobs: [ ],
        Observers: [ ],
        Participants: ["Д000086"],
        RouteName: "Произвольный маршрут",
        SigningRequired: false,
        State: "На контроле",
        SubTasksId: [ ],
        Title: ">> Зарегистрировать Письмо входящее из Банк "Левобережный" ОАО г. Новосибирск, Об участии в выставке",
        UserStatus: "None"
    }
    // */
    
    // Описание полей
    private JSONArray _action_list;
    private Attachment[] _attachments;
    private String _author_code;
    private String _created;
    private String _deadline;
    private String _executed;
    private History[] _history;
    private long _id;
    private String _importance;
    private Job[] _jobs;
    private String[] _observers;
    private String[] _participants;
    private String _route_name;
//    private boolean _signing_required;
    private String _state;
    private long[] _subtask_ids;
    private String _title;
    private Rabotnic _author;
    private int _attachment_count;
    private int _history_count;
//    private ?? _user_status; // непонятная херня
    
    public Task(JSONObject json) {
        updateData(json);
    }
    public Task(JSONArray action_list, JSONArray attachments, JSONArray history, JSONArray jobs, JSONArray observers, JSONArray participants, JSONArray subtask_ids,
            String author_code, String created, String deadline, String executed, long id, String importance, String route_name, String state, String title) {
        this._action_list = action_list;
        
        if (attachments != null) {
            this._attachments = new Attachment[attachments.length()];
            for (int i=0; i<attachments.length(); i++) {
                this._attachments[i] = new Attachment(attachments.optJSONObject(i));
            }
        } else {
            this._attachments = new Attachment[0];
        }
        
        if (history != null) {
            this._history = new History[history.length()];
            for (int i=0; i<history.length(); i++) {
                this._history[i] = new History(history.optJSONObject(i));
            }
        } else {
            this._history = new History[0];
        }

        if (jobs != null) {
            this._jobs = new Job[jobs.length()];
            for (int i=0; i<jobs.length(); i++) {
                this._jobs[i] = new Job(jobs.optJSONObject(i));
            }
        } else {
            this._jobs = new Job[0];
        }
        
        if (observers != null) {
            this._observers = new String[observers.length()];
            for (int i=0; i<observers.length(); i++) {
                this._observers[i] = observers.optString(i);
            }
        } else {
            this._observers = new String[0];
        }
        
        if (participants != null) {
            this._participants = new String[participants.length()];
            for (int i=0; i<participants.length(); i++) {
                this._participants[i] = participants.optString(i);
            }
        } else {
            this._participants = new String[0];
        }
        
        if (subtask_ids != null) {
            this._subtask_ids = new long[subtask_ids.length()];
            for (int i=0; i<subtask_ids.length(); i++) {
                this._subtask_ids[i] = subtask_ids.optLong(i);
            }
        } else {
            this._subtask_ids = new long[0];
        }
        
        this._author_code = author_code;
        this._created = created;
        this._deadline = deadline;
        this._executed = executed;
        this._id = id;
        this._importance = importance;
        this._route_name = route_name;
        this._state = state;
        this._title = title;   
    }
    public Task(Parcel in) {
        try {
            this._action_list = new JSONArray(in.readString());
        } catch (JSONException e) {
            Log.v(TAG, "_action_list read exception " + e.getMessage());
        }
        this._author_code = in.readString();
        this._created = in.readString();
        this._deadline = in.readString();
        this._executed = in.readString();
        this._id = in.readLong();
        this._importance = in.readString();
        this._route_name = in.readString();
        this._state = in.readString();
        this._title = in.readString();
        this._author = in.readParcelable(Rabotnic.class.getClassLoader());
        this._attachment_count = in.readInt();
        this._history_count = in.readInt();
        
        int len = in.readInt(); // Сначала читаем длину списка 
        this._attachments = new Attachment[len];
        in.readTypedArray(this._attachments, Attachment.CREATOR);
        
        len = in.readInt(); // Сначала читаем длину списка 
        this._history = new History[len];
        in.readTypedArray(this._history, History.CREATOR);
        
        len = in.readInt(); // Сначала читаем длину списка
        this._jobs = new Job[len];
        in.readTypedArray(this._jobs, Job.CREATOR);
        
        len = in.readInt(); // Сначала читаем длину списка
        this._observers = new String[len];
        in.readStringArray(this._observers);
        
        len = in.readInt(); // Сначала читаем длину списка
        this._participants = new String[len];
        in.readStringArray(this._participants);
        
        len = in.readInt(); // Сначала читаем длину списка
        this._subtask_ids = new long[len];
        in.readLongArray(this._subtask_ids);
    }
    public void updateData(JSONObject data) {
        this._action_list = data.optJSONArray("ActionList");
        
        JSONArray attachments = data.optJSONArray("Attachments");
        if (attachments != null) {
            _attachments = new Attachment[attachments.length()];
            for (int i=0; i<attachments.length(); i++) {
                _attachments[i] = new Attachment(attachments.optJSONObject(i));
            }
        } else {
            _attachments = new Attachment[0];
        }
        
        JSONArray history = data.optJSONArray("History");
        if (history != null) {
            _history = new History[history.length()];
            for (int i=0; i<history.length(); i++) {
                _history[i] = new History(history.optJSONObject(i));
            }
        } else {
            _history = new History[0];
        }
        
        JSONArray jobs = data.optJSONArray("Jobs");
        if (jobs != null) {
            _jobs = new Job[jobs.length()];
            for (int i=0; i<jobs.length(); i++) {
                _jobs[i] = new Job(jobs.optJSONObject(i));
            }
        } else {
            _jobs = new Job[0];
        }
        
        JSONArray observers = data.optJSONArray("Observers");
        if (observers != null) {
            _observers = new String[observers.length()];
            for (int i=0; i<observers.length(); i++) {
                _observers[i] = observers.optString(i);
            }
        } else {
            _observers = new String[0];
        }
        
        JSONArray participants = data.optJSONArray("Participants");
        if (participants != null) {
            _participants = new String[participants.length()];
            for (int i=0; i<participants.length(); i++) {
                _participants[i] = participants.optString(i);
            }
        } else {
            _participants = new String[0];
        }
        
        JSONArray subtask_ids = data.optJSONArray("SubTasksId");
        if (subtask_ids != null) {
            _subtask_ids = new long[subtask_ids.length()];
            for (int i=0; i<subtask_ids.length(); i++) {
                _subtask_ids[i] = subtask_ids.optLong(i);
            }
        } else {
            _subtask_ids = new long[0];
        }
        
        this._author_code = data.optString("AuthorCode");
        this._created = data.optString("Created");
        this._deadline = data.optString("Deadline");
        this._executed = data.optString("Executed");
        this._id = data.optLong("Id");
        this._importance = data.optString("Importance");
        this._route_name = data.optString("RouteName");
        this._state = data.optString("State");
        this._title = data.optString("Title");   
    }
    public String getActionList() {
        return this._action_list.toString();
    }
    public String getAuthorCode() {
        return this._author_code;
    }
    public String getCreated() {
        return getCreated(false);
    }
    public String getCreated(boolean formatted) {
        if (formatted) {
            return Utils.formatDateTime(this._created, "dd/MM/yyyy");
        } else {
            return this._created;
        }
    }
    public String getDeadline() {
        return getDeadline(false);
    }
    public String getDeadline(boolean formatted) {
        if (formatted) {
            return Utils.formatDateTime(this._deadline, "dd/MM/yyyy");
        } else {
            return this._deadline;
        }
    }
    public String getExecuted() {
        return getExecuted(false);
    }
    public String getExecuted(boolean formatted) {
        if (formatted) {
            return Utils.formatDateTime(this._executed, "dd/MM/yyyy");
        } else {
            return this._executed;
        }
    }
    public long getId() {
        return this._id;
    }
    public String getImportance() {
        return this._importance;
    }
    public String getObservers() {
        JSONArray observers = new JSONArray();
        for (String observer : this._observers) {
            observers.put(observer);
        }
        return observers.toString();
    }
    public String getParticipants() {
        JSONArray participants = new JSONArray();
        for (String participant : this._participants) {
            participants.put(participant);
        }
        return participants.toString();
    }
    public String getRouteName() {
        return this._route_name;
    }
    public String getState() {
        return this._state;
    }
    public String getSubtaskIds() {
        JSONArray subtask_ids = new JSONArray();
        for (long subtask_id : this._subtask_ids) {
            subtask_ids.put(subtask_id);
        }
        return subtask_ids.toString();
    }
    public int getSubtaskCount() {
        return this._subtask_ids.length;
    }
    public String getTitle() {
        return this._title;
    }
    public Attachment[] getAttachment() {
        return this._attachments;
    }
    public History[] getHistory() {
        return this._history;
    }
    public Job[] getJob() {
        return this._jobs;
    }
    public void setAttachment(Attachment[] attachments) {
        this._attachments = attachments;
    }
    public void setHistory(History[] histories) {
        this._history = histories;
    }
    public void setJob(Job[] jobs) {
        this._jobs = jobs;
    }
    public Rabotnic getAuthor() {
        return this._author;
    }
    public void setAuthor(Rabotnic author) {
        this._author = author;
    }
    public boolean isOverdue() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Utils.mLocale);
        Date deadline;
        try {
            deadline = format.parse(this._deadline);
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
            deadline = format.parse(this._deadline);
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
    public void setAttachmentCount(int count) {
        this._attachment_count = count;
    }
    public void setHistoryCount(int count) {
        this._history_count = count;
    }
    public int getAttachmentCount() {
        return this._attachment_count;
    }
    public int getHistoryCount() {
        return this._history_count;
    }
    
    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {

        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this._action_list.toString());
        parcel.writeString(this._author_code);
        parcel.writeString(this._created);
        parcel.writeString(this._deadline);
        parcel.writeString(this._executed);
        parcel.writeLong(this._id);
        parcel.writeString(this._importance);
        parcel.writeString(this._route_name);
        parcel.writeString(this._state);
        parcel.writeString(_title);
        parcel.writeParcelable(this._author, flags);
        parcel.writeInt(this._attachment_count);
        parcel.writeInt(this._history_count);
        
        parcel.writeInt(this._attachments.length); // Длина списка. Нужно чтобы потом создать правильной длины список
        parcel.writeTypedArray(this._attachments, flags);
        
        parcel.writeInt(this._history.length); // Длина списка. Нужно чтобы потом создать правильной длины список
        parcel.writeTypedArray(this._history, flags);
        
        parcel.writeInt(this._jobs.length); // Длина списка. Нужно чтобы потом создать правильной длины список
        parcel.writeTypedArray(this._jobs, flags);
        
        parcel.writeInt(this._observers.length); // Длина списка. Нужно чтобы потом создать правильной длины список
        parcel.writeStringArray(this._observers);
        
        parcel.writeInt(this._participants.length); // Длина списка. Нужно чтобы потом создать правильной длины список
        parcel.writeStringArray(this._participants);
        
        parcel.writeInt(this._subtask_ids.length); // Длина списка. Нужно чтобы потом создать правильной длины список
        parcel.writeLongArray(this._subtask_ids);
    }
}