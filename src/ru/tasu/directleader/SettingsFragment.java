package ru.tasu.directleader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";
    
    private OnPreferenceChangeListener mListener; 

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Define the settings file to use by this settings fragment
        this.getPreferenceManager().setSharedPreferencesName("DirectLeader");
        
        getActivity().getSharedPreferences("DirectLeader", Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference);
        
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.v(TAG, "onSharedPreferenceChanged " + key);
        if (key.equalsIgnoreCase("update_enabled") || key.equalsIgnoreCase("update_interval")) {
            if (mListener != null) {
                mListener.onPreferenceChange();
            }
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPreferenceChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnPreferenceChangeListener");
        }
    }
}
abstract interface OnPreferenceChangeListener {
    public void onPreferenceChange();
}