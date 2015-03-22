package ru.tasu.directleader;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpdateService extends Service {
    private final static String TAG = "UpdateService"; 
    private DirectLeaderApplication mDirect;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDirect = (DirectLeaderApplication)getApplication();
        Log.v(TAG, "onStartCommand " + mDirect);
        long interval = mDirect.getSettings().getInt("update_interval", 1000*60*60);
        Intent i = new Intent(this, UpdateIntentService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // schedule for every 'interval' seconds
        Calendar cal = Calendar.getInstance();
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, pintent); 
        return super.onStartCommand(intent, flags, startId);
    }
}
