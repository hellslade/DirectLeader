package ru.tasu.directleader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class History implements Parcelable {
    private static final String TAG = "History";
    
    // Описание полей
    private String _author_code;
    private String _date;
    private String _message;
    private long _task_id;
    private Rabotnic _user;
    /*
    {
        AuthorCode: "Д000086",
        Date: "2013-12-05 13:08:05",
        Message: "Выполнено."
    }
    // */
    public History(JSONObject json) {
        updateData(json);
    }
    public History(String author_code, String date, String message) {
        this(author_code, date, message, 0);
    }
    public History(String author_code, String date, String message, long task_id) {
        this._author_code = author_code;
        this._date = date;
        this._message = message;
        this._task_id = task_id;
    }
    public History(Parcel in) {
        this._author_code = in.readString();
        this._date = in.readString();
        this._message = in.readString();
        this._task_id = in.readLong();
        this._user = in.readParcelable(Rabotnic.class.getClassLoader());
    }
    public void updateData(JSONObject data) {
        this._author_code = data.optString("AuthorCode");
        this._date = data.optString("Date");
        this._message = data.optString("Message");
    }
    public String getAuthorCode() {
        return this._author_code;
    }
    public void setTaskId(long task_id) {
        this._task_id = task_id;
    }
    public String getDate() {
        return getDate(false);
    }
    public String getDate(boolean formatted) {
        if (formatted) {
            SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Utils.mLocale);
            try {
                Date deadline = format.parse(this._date);
                SimpleDateFormat formatOutput =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return  formatOutput.format(deadline);
            } catch (ParseException e) {
                e.printStackTrace();
                return this._date;
            }
        } else {
            return this._date;
        }
    }
    public String getMessage() {
        return this._message;
    }
    public long getTaskId() {
        return this._task_id;
    }
    public void setUser(Rabotnic user) {
        this._user = user;
    }
    public Rabotnic getUser() {
        return this._user;
    }
    public static final Parcelable.Creator<History> CREATOR = new Parcelable.Creator<History>() {

        public History createFromParcel(Parcel in) {
            return new History(in);
        }

        public History[] newArray(int size) {
            return new History[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this._author_code);
        parcel.writeString(this._date);
        parcel.writeString(this._message);
        parcel.writeLong(this._task_id);
        parcel.writeParcelable(_user, flags);
    }
}
