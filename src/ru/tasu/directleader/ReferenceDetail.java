package ru.tasu.directleader;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

//ReferenceDetail: [
//[
//    {
//	    Key: "��",
//	    Value: "46243"
//    },
//    {
//	    Key: "���",
//	    Value: "3342"
//    },
//    {
//	    Key: "��������������",
//	    Value: "�000003"
//    },
//    {
//	    Key: "PerformerT",
//	    Value: "��000030"
//    },
//    {
//	    Key: "�����",
//	    Value: null
//    },
//],

public class ReferenceDetail implements Parcelable {
	private static final String TAG = "ReferenceDetail";
	
	private Map<String, String> _data = new HashMap<String, String>();
	// Default HashMap keys
	private static final String _defaultTitleKey = "������������";
	private static final String _poruchenieKey = "���2�";
	private static final String _dataKey = "����2�";

	public ReferenceDetail(JSONArray json) {
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
	public ReferenceDetail(Parcel in) {
		_data = in.readHashMap(HashMap.class.getClassLoader());
	}
	public Map<String, String> getData() {
		return this._data;
	}
	/**
	 * ���������
	 * @return
	 */
	public String getPoruchenieText() {
		String result = "unspecified";
		if (_data.containsKey(_poruchenieKey)) {
			result = _data.get(_poruchenieKey);
		}
		return result;
	}
	/**
	 * ���� ����������
	 * @return
	 */
	public String getDataText() {
		String result = "unspecified";
		if (_data.containsKey(_dataKey)) {
			result = _data.get(_dataKey);
		}
		return result;
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
		this._data.put("PerformerT", code);
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
    	parcel.writeMap(_data);
    }
}
