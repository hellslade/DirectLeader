package ru.tasu.directleader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoStartReceiver extends BroadcastReceiver {
    private static final String TAG = "AutoStartReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
      Log.v(TAG, "onReceive " + intent.getAction());
      context.startService(new Intent(context, UpdateService.class));
    }
}
