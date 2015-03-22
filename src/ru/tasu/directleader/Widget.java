package ru.tasu.directleader;

import java.util.Arrays;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {
    private static final String TAG = "Widget";
    
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(TAG, "onEnabled");
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d(TAG, "onUpdate " + Arrays.toString(appWidgetIds));
        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, id);
        }
    }
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(TAG, "onDeleted " + Arrays.toString(appWidgetIds));
    }
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(TAG, "onDisabled");
    }
    static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetID) {
        Log.d(TAG, "updateWidget " + widgetID);
        // Получаем инфу из БД и выводим
        SharedPreferences mSettings = context.getSharedPreferences("DirectLeader", Context.MODE_PRIVATE);
        String userName = mSettings.getString("user_id_key", "");
        int[] counts = {0, 0, 0};
        if (!userName.equalsIgnoreCase("")) {
            JobDataSource jds = new JobDataSource(context);
            jds.open();
            counts = jds.getCountOfJobByPerformerCode(userName);
        }
        
        // Настраиваем внешний вид виджета
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
        widgetView.setTextViewText(R.id.overdueTextView, String.format("%s", counts[2]));
        
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        // Get the layout for the App Widget and attach an on-click listener to the button
        RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.widget);
        views.setOnClickPendingIntent(R.id.widgetImageView, pendingIntent);
        
        // Обновляем виджет
        appWidgetManager.updateAppWidget(widgetID, widgetView);
        
    }
}
