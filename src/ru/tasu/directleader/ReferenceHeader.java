package ru.tasu.directleader;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

//ReferenceHeader: [
//    {
//	    Key: "ИД",
//	    Value: "167926"
//    },
//    {
//	    Key: "Вид",
//	    Value: "3342"
//    },
//    {
//	    Key: "Аналитика-оригинал",
//	    Value: "ОД000010"
//    },
//    {
//	    Key: "Наименование",
//	    Value: "Поручение №2 от 17.04.2015. Содержание (167926)"
//    },
//    {
//	    Key: "Текст",
//	    Value: "Содержание"
//    },
//    {
//	    Key: "Дата",
//	    Value: "17.04.2015"
//    },
//    {
//	    Key: "Employee",
//	    Value: "НД000023"
//    },
//    {
//	    Key: "Ссылка",
//	    Value: "2"
//    },
//    {
//	    Key: "Дата2",
//	    Value: "18.05.2015"
//    },
//    {
//	    Key: "Работник",
//	    Value: "НД000023"
//    },
//    {
//	    Key: "LongString",
//	    Value: "Григорьев А.А.; Баклаев С.И."
//    },
//  ]

public class ReferenceHeader implements Parcelable {
	private static final String TAG = "ReferenceHeader";
	
	private Map<String, String> _data = new HashMap<String, String>();
	// Default HashMap keys
	private static final String _defaultTitleKey = "Наименование";

	public ReferenceHeader(JSONArray json) {
		updateData(json);
	}
	public void updateData(JSONArray json) {
		String key, value;
		JSONObject obj;
		for (int i=0 ; i<json.length() ; i++) {
			obj = json.optJSONObject(i);
			key = obj.optString("Key");
			value = obj.optString("Value");
			_data.put(key, value);
		}
	}
	public ReferenceHeader(Parcel in) {
		_data = in.readHashMap(HashMap.class.getClassLoader());
	}
	public Map<String, String> getData() {
		return this._data;
	}
	public String getResolutionTitle() {
		return getResolutionTitle(_defaultTitleKey);
	}
	public String getResolutionTitle(String titleKey) {
		String title = "unspecified";
		if (_data.containsKey(titleKey)) {
			title = _data.get(titleKey);
		}
		return title;
	}
	public void setCodeRab(String code) {
		this._data.put("Работник", code);
	}
	public void setOnControl(String control) {
		this._data.put("ControlType", control);
	}
	public void setDate(String date) {
		this._data.put("Дата", date);
	}
	public void setDatePlan(String date) {
		this._data.put("Дата2", date);
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
    	parcel.writeMap(_data);
    }
}
