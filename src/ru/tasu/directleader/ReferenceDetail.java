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
//			Name: "ДаНетТ",
//			Title: "Ответственный",
//			TypeReference: "",
//			Value: "Нет"
//		},{
//			DataType: "rdtReference",
//			HeadId: 167967,
//			Id: 46261,
//			IsVisible: true,
//			Name: "PerformerT",
//			Title: "Исполнитель",
//			TypeReference: "РАБ",
//			Value: "НД000023"
//		},{
//			DataType: "rdtDate",
//			HeadId: 167967,
//			Id: 46261,
//			IsVisible: true,
//			Name: "Дата2Т",
//			Title: "Срок исполнения",
//			TypeReference: "",
//			Value: "16.06.2015"
//		},{
//			DataType: "rdtString",
//			HeadId: 167967,
//			Id: 46261,
//			IsVisible: true,
//			Name: "Доп2Т",
//			Title: "Текст",
//			TypeReference: "",
//			Value: "Подготовить отчет"
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
		// Очистить значения, необходимо при копировании
		for (JSONObject attr : _data) {
			try {
				attr.put("Value", "");
				attr.put("Id", -1);
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
