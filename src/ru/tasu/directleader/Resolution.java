package ru.tasu.directleader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Resolution implements Parcelable {
	private static final String TAG = "Resolution";
	
	private long _id;
	private JSONArray _detail;
	private JSONArray _header;
	private int _attach_job;
	private long _task_id;
	
	public Resolution(JSONObject reference) {
		if (reference != null) {
			this._detail = reference.optJSONArray("ReferenceDetail");
    		this._header = reference.optJSONArray("ReferenceHeader");
    		this._attach_job = reference.optInt("AttachToJob");
    		this._id = _header.optJSONObject(0).optLong("Id");
		}
	}
	public Resolution(String detail, String header, int attach_job, long task_id) {
		try {
			this._detail = new JSONArray(detail);
			this._header = new JSONArray(header);
			this._attach_job = attach_job;
			this._task_id = task_id;
			this._id = _header.optJSONObject(0).optLong("Id");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public Resolution(Parcel in) {
		try {
			this._detail = new JSONArray(in.readString());
			this._header = new JSONArray(in.readString());
			this._attach_job = in.readInt();
			this._task_id = in.readLong();
			this._id = in.readLong();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public void setTaskId(long task_id) {
		this._task_id = task_id;
	}
	public long getId() {
		return this._id;
	}
	public long getTaskId() {
		return this._task_id;
	}
	public void setReferenceHeader(JSONArray header) {
		this._header = header;
	}
	public void setReferenceDetail(JSONArray detail) {
		this._detail = detail;
	}
	public String getReferenceHeader() {
		return this._header.toString();
	}
	public String getReferenceDetail() {
		return this._detail.toString();
	}
	public JSONArray getReferenceHeaderJSON() {
		return this._header;
	}
	public JSONArray getReferenceDetailJSON() {
		return this._detail;
	}
	public static final Parcelable.Creator<Resolution> CREATOR = new Parcelable.Creator<Resolution>() {
        public Resolution createFromParcel(Parcel in) {
            return new Resolution(in);
        }
        public Resolution[] newArray(int size) {
            return new Resolution[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this._detail.toString());
        parcel.writeString(this._header.toString());
        parcel.writeInt(this._attach_job);
        parcel.writeLong(this._task_id);
        parcel.writeLong(this._id);
    }
}
