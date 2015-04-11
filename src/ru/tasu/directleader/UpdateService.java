package ru.tasu.directleader;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class UpdateService extends Service {
    private final static String TAG = "UpdateService"; 
    private DirectLeaderApplication mDirect;
    
    private PendingIntent pintent;
    AlarmManager alarm;
    
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        UpdateService getService() {
            // Return this instance of LocalService so clients can call public methods
            return UpdateService.this;
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDirect = (DirectLeaderApplication)getApplication();
        Log.v(TAG, "onStartCommand " + mDirect);
        
        Intent i = new Intent(this, UpdateIntentService.class);
        pintent = PendingIntent.getService(this, 0, i, 0);
        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        setAlarm();
        return super.onStartCommand(intent, flags, startId);
    }
    public void cancelAlarm() {
        Log.v(TAG, "cancelAlarm()");
        try{
            AlarmManager am=(AlarmManager)mDirect.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pintent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void setAlarm() {
        Log.v(TAG, "setAlarm()");
        boolean enabled = mDirect.getSettings().getBoolean("update_enabled", false);
        long interval = 1000*60*30;
        try {
            String interv = mDirect.getSettings().getString("update_interval", "0");
            interval = Long.valueOf(interv)*1000; // В настройках интервал в секундах
        } catch (Exception e) {
            Log.v(TAG, " " + e.getLocalizedMessage());
        }
        // schedule for every 'interval' seconds
        Calendar cal = Calendar.getInstance();
        if (enabled) {
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, pintent);
        }
    }
}
