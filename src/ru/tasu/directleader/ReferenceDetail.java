package ru.tasu.directleader;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

//ReferenceDetail: [
//	[
//		{
//			DataType: "rdtPick",
//			HeadId: 167967,
//			Id: 46261,
//			IsVisible: true,
//			Name: "������",
//			Title: "�������������",
//			TypeReference: "",
//			Value: "���"
//		},{
//			DataType: "rdtReference",
//			HeadId: 167967,
//			Id: 46261,
//			IsVisible: true,
//			Name: "PerformerT",
//			Title: "�����������",
//			TypeReference: "���",
//			Value: "��000023"
//		},{
//			DataType: "rdtDate",
//			HeadId: 167967,
//			Id: 46261,
//			IsVisible: true,
//			Name: "����2�",
//			Title: "���� ����������",
//			TypeReference: "",
//			Value: "16.06.2015"
//		},{
//			DataType: "rdtString",
//			HeadId: 167967,
//			Id: 46261,
//			IsVisible: true,
//			Name: "���2�",
//			Title: "�����",
//			TypeReference: "",
//			Value: "����������� �����"
//		}
//	]
//]

public class ReferenceDetail extends Reference implements Parcelable {
	private static final String TAG = "ReferenceDetail";
	
	public ReferenceDetail(ReferenceDetail source) {
		for (JSONObject json : source._data) {
			try {
				final JSONObject copy = new JSONObject(json.toString());
				this._data.add(copy);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	public ReferenceDetail(JSONArray json) {
		updateData(json);
	}
	public ReferenceDetail(Parcel in) {
		String jsonString = in.readString();
		try {
			JSONArray array = new JSONArray(jsonString);
			for (int i=0; i<array.length(); i++) {
				final JSONObject json = array.getJSONObject(i);
				_data.add(json);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
//		in.readList(_data, List.class.getClassLoader());
	}
	public void clearValues() {
		clearValues(false);
	}
	public void clearValues(boolean resetHeadId) {
		// �������� ��������, ���������� ��� �����������
		for (JSONObject attr : _data) {
			try {
				attr.put("Value", "");
				resetId(attr);
				if (resetHeadId) {
					resetHeadId(attr);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	public void resetId(JSONObject attr) {
		try {
			// �������� Id
			long id = attr.optLong("Id");
			// ����� �� ������, �.�. id ��� ����� ���� �������������
			id = Math.abs(id);
			// �����������
			id++;
			// ����������� ������������� ��������
			attr.put("Id", -id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public void resetHeadId(JSONObject attr) {
		try {
			// �������� HeadId
			long headId = attr.optLong("HeadId");
			// ����� �� ������, �.�. id ��� ����� ���� �������������
			headId = Math.abs(headId);
			// �����������
			//headId++;
			// ����������� ������������� ��������
			attr.put("HeadId", -headId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public static final Parcelable.Creator<ReferenceDetail> CREATOR = new Parcelable.Creator<ReferenceDetail>() {

        public ReferenceDetail createFromParcel(Parcel in) {
            return new ReferenceDetail(in);
        }

        public ReferenceDetail[] newArray(int size) {
            return new ReferenceDetail[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
    	Log.v(TAG, "writeToParcel");
    	// JSONObject is not parcellable, need to convert it in String object
    	String jsonString = "";
    	JSONArray array = new JSONArray();
    	for (JSONObject json : _data) {
    		array.put(json);
    	}
    	jsonString = array.toString();
//    	parcel.writeList(_data);
    	parcel.writeString(jsonString);
    }
}
