package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

//ReferenceDetail: [
//	[
//		{
//			DataType: "rdtInteger",
//			HeadId: 167967,
//			Id: 46261,
//			IsVisible: false,
//			Name: "�������������",
//			Title: "�� ������ �������� �������",
//			TypeReference: "",
//			Value: "167967"
//		},{
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
		in.readList(_data, List.class.getClassLoader());
	}
	public void clearValues() {
		// �������� ��������, ���������� ��� �����������
		String value;
		for (JSONObject attr : _data) {
			if (attr.optString("Name").equalsIgnoreCase("�������������")) {
				value = "-1"; // ��� �������� ������ ������ ������, ����� ���������� detail � �� ����� ��������� � �������� header
			} else {
				value = "";
			}
			try {
				attr.put("Value", value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
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
    	parcel.writeList(_data);
    }
}
