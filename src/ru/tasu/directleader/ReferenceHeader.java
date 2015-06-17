package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

//ReferenceHeader: [
//	{
//		DataType: "rdtString",
//		Id: 167967,
//		IsVisible: true,
//		Name: "Наименование",
//		Title: "Наименование",
//		TypeReference: "",
//		Value: "Поручение №1 от 15.06.2015. В работу (167967)"
//	},{
//		DataType: "rdtText",
//		Id: 167967,
//		IsVisible: true,
//		Name: "Текст",
//		Title: "Содержание",
//		TypeReference: "",
//		Value: "В работу"
//	},{
//		DataType: "rdtDate",
//		Id: 167967,
//		IsVisible: true,
//		Name: "Дата",
//		Title: "Дата поручения",
//		TypeReference: "",
//		Value: "15.06.2015"
//	},{
//		DataType: "rdtDate",
//		Id: 167967,
//		IsVisible: true,
//		Name: "Дата2",
//		Title: "Срок",
//		TypeReference: "",
//		Value: "15.07.2015"
//	},{
//		DataType: "rdtPick",
//		Id: 167967,
//		IsVisible: true,
//		Name: "ControlType",
//		Title: "На контроле",
//		TypeReference: "",
//		Value: "No"
//	},{
//		DataType: "rdtReference",
//		Id: 167967,
//		IsVisible: true,
//		Name: "Работник",
//		Title: "Контролер",
//		TypeReference: "РАБ",
//		Value: null
//	}
//]

public class ReferenceHeader extends Reference implements Parcelable {
	private static final String TAG = "ReferenceHeader";
	
	public ReferenceHeader(JSONArray json) {
		updateData(json);
	}
	public ReferenceHeader(Parcel in) {
		in.readList(_data, List.class.getClassLoader());
	}
	public String getResolutionTitle() {
//		Name: "Наименование",
		JSONObject obj = getAttributeByName("Наименование");
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
    	parcel.writeList(_data);
    }
}
