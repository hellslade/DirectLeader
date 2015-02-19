package ru.tasu.directleader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Attachment implements Parcelable {
    private static final String TAG = "Attachment";
    
    // Описание полей
    private String _author_name;
    private String _ctitle;
    private String _created;
    private String _ext;
    private long _id;
    private String _modified;
    private String _name;
//    private _number; // не пойми что, описание отсутствует
    private boolean _signed;
//    private List<> _signed_by; // не пойми что, описание отсутствует
    private long _size;
    private int _version;
    private long _task_id;
    private String _task_title;
    /*
    {
        AuthorName: "Administrator",
        CTitle: "Письма входящие (официальные)",
        Created: "03.06.2013",
        Ext: "DOCX",
        Id: 514995,
        Modified: "03.06.2013",
        Name: "Письмо входящее № 12/Вх-1 от 03.06.2013 из Банк "Левобережный" ОАО г. Новосибирск, Об участии в выставке",
        Number: null,
        Signed: false,
        SignedBy: [ ],
        Size: 0,
        Version: 0
    }
    // */
    public Attachment(JSONObject json) {
        updateData(json);
    }
    public Attachment(String author_name, String ctitle, String created, String ext, long id, String modified, String name, boolean signed, long size, int version) {
        this(author_name, ctitle, created, ext, id, modified, name, signed, size, version, 0);
    }
    public Attachment(String author_name, String ctitle, String created, String ext, long id, String modified, String name, boolean signed, long size, int version, long task_id) {
        this._author_name = author_name;
        this._ctitle = ctitle;
        this._created = created;
        this._ext = ext;
        this._id = id;
        this._modified = modified;
        this._name = name;
//        this._number = data.optString("Number");
        this._signed = signed;
//        this._signed_by = data.optString("SignedBy");
        this._size = size;
        this._version = version;
        this._task_id = task_id;
    }
    public Attachment(Parcel in) {
        this._author_name = in.readString();
        this._ctitle = in.readString();
        this._created = in.readString();
        this._ext = in.readString();
        this._id = in.readLong();
        this._modified = in.readString();
        this._name = in.readString();
//        this._number = in.
        this._signed = in.readByte() != 0;
//        this._signed_by = in.
        this._size = in.readLong();
        this._version = in.readInt();
        this._task_id = in.readLong();
        this._task_title = in.readString();
    }
    public void updateData(JSONObject data) {
        this._author_name = data.optString("AuthorName");
        this._ctitle = data.optString("CTitle");
        this._created = data.optString("Created");
        this._ext = data.optString("Ext");
        this._id = data.optLong("Id");
        this._modified = data.optString("Modified");
        this._name = data.optString("Name");
//        this._number = data.optString("Number");
        this._signed = data.optBoolean("Signed");
//        this._signed_by = data.optString("SignedBy");
        this._size = data.optLong("Size");
        this._version = data.optInt("Version");
        this._task_id = 0;
    }
    public void setTaskId(long task_id) {
        this._task_id = task_id;
    }
    public long getTaskId() {
        return this._task_id;
    }
    public String getAuthorName() {
        return this._author_name;
    }
    public String getCTitle() {
        return this._ctitle;
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
    public String getExt() {
        return this._ext;
    }
    public long getId() {
        return this._id;
    }
    public String getModified() {
        return getModified(false);
    }
    public String getModified(boolean formatted) {
        if (formatted) {
            return Utils.formatDateTime(this._modified, "dd/MM/yyyy");
        } else {
            return this._modified;
        }
    }
    public String getName() {
        return this._name;
    }
    public boolean getSigned() {
        return this._signed;
    }
    public long getSize() {
        return this._size;
    }
    public int getVersion() {
        return this._version;
    }
    public void setTaskTitle(String title) {
        this._task_title = title;
    }
    public String getTaskTitle() {
        return this._task_title;
    }
    
    public static final Parcelable.Creator<Attachment> CREATOR = new Parcelable.Creator<Attachment>() {

        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }

        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this._author_name);
        parcel.writeString(this._ctitle);
        parcel.writeString(this._created);
        parcel.writeString(this._ext);
        parcel.writeLong(this._id);
        parcel.writeString(this._modified);
        parcel.writeString(this._name);
//        parcel.writeString(this._number);
        parcel.writeByte((byte) (this._signed ? 1 : 0));
//        parcel.writeString(this._signed_by);
        parcel.writeLong(this._size);
        parcel.writeInt(this._version);
        parcel.writeLong(this._task_id);
        parcel.writeString(this._task_title);
    }
}
