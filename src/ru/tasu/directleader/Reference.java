package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Reference {
	private static final String TAG = "Reference";
	protected List<JSONObject> _data = new ArrayList<JSONObject>();
	
	public void setAttributeItemValue(String attrName, String attrValue) {
		JSONObject obj = getAttributeByName(attrName);
		try {
			obj.put("Value", attrValue);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public void updateData(JSONArray json) {
		for (int i=0 ; i<json.length() ; i++) {
			final JSONObject obj = json.optJSONObject(i);
			_data.add(obj);
		}
	}
	public List<JSONObject> getData() {
		return this._data;
	}
	public List<JSONObject> getVisibleAttributes() {
		List<JSONObject> result = new ArrayList<JSONObject>();
		for (JSONObject attr : _data) {
			final boolean isVisible = attr.optBoolean("IsVisible");
			if (isVisible) {
				result.add(attr);
			}
		}
		return result;
	}
	protected JSONObject getAttributeByName(String attrName) {
		for (JSONObject attr : _data) {
			final String name = attr.optString("Name");
			if (name.equalsIgnoreCase(attrName)) {
				return attr;
			}
		}
		return null;
	}
	public String getAttributeItemValue(String attrName) {
		JSONObject obj = getAttributeByName(attrName);
		if (obj == null) {
			return null;
		}
		return obj.optString("Value");
	}
}
