package ru.tasu.directleader;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

//ReferenceHeader: [
//	{
//		DataType: "rdtString",
//		Id: 167967,
//		IsVisible: true,
//		Name: "������������",
//		Title: "������������",
//		TypeReference: "",
//		Value: "��������� �1 �� 15.06.2015. � ������ (167967)"
//	},{
//		DataType: "rdtText",
//		Id: 167967,
//		IsVisible: true,
//		Name: "�����",
//		Title: "����������",
//		TypeReference: "",
//		Value: "� ������"
//	},{
//		DataType: "rdtReference",
//		Id: 167967,
//		IsVisible: true,
//		Name: "MainRRCAssignment",
//		Title: "������� ���������",
//		TypeReference: "RRCAssignments",
//		Value: null
//  },{
//		DataType: "rdtDate",
//		Id: 167967,
//		IsVisible: true,
//		Name: "����",
//		Title: "���� ���������",
//		TypeReference: "",
//		Value: "15.06.2015"
//	},{
//		DataType: "rdtDate",
//		Id: 167967,
//		IsVisible: true,
//		Name: "����2",
//		Title: "����",
//		TypeReference: "",
//		Value: "15.07.2015"
//	},{
//		DataType: "rdtPick",
//		Id: 167967,
//		IsVisible: true,
//		Name: "ControlType",
//		Title: "�� ��������",
//		TypeReference: "",
//		Value: "No"
//	},{
//		DataType: "rdtReference",
//		Id: 167967,
//		IsVisible: true,
//		Name: "��������",
//		Title: "���������",
//		TypeReference: "���",
//		Value: null
//	}
//]

public class ReferenceHeader extends Reference implements Parcelable {
	private static final String TAG = "ReferenceHeader";
	
	public ReferenceHeader(ReferenceHeader source) {
		for (JSONObject json : source._data) {
			try {
				final JSONObject copy = new JSONObject(json.toString());
				this._data.add(copy);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	public ReferenceHeader(JSONArray json) {
		updateData(json);
	}
	public ReferenceHeader(Parcel in) {
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
	}
	public void clearValues() {
		// �������� ��������, ���������� ��� �����������
		for (JSONObject attr : _data) {
			if (attr.optString("Name").equalsIgnoreCase("MainRRCAssignment")) {
				try {
					attr.put("Value", attr.optString("Id"));
					attr.put("Id", -1);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				continue;
			}
			try {
				attr.put("Value", "");
				attr.put("Id", -1);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	public String getResolutionTitle() {
//		Name: "������������",
		JSONObject obj = getAttributeByName("������������");
		if (obj == null) {
			return "";
		}
		return obj.optString("Value");
	}
	
	public static final Parcelable.Creator<ReferenceHeader> CREATOR = new Parcelable.Creator<ReferenceHeader>() {

        public ReferenceHeader createFromParcel(Parcel in) {
            return new ReferenceHeader(in);
        }

        public ReferenceHeader[] newArray(int size) {
            return new ReferenceHeader[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
    	// JSONObject is not parcellable, need to convert it in String object
    	String jsonString = "";
    	JSONArray array = new JSONArray();
    	for (JSONObject json : _data) {
    		array.put(json);
    	}
    	jsonString = array.toString();
    	parcel.writeString(jsonString);
    }
}
