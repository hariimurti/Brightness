package net.harimurti.brightness;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

@SuppressLint("DefaultLocale")
public class LuxService extends Service {
    private static final String TAG = "LuxService";

    Preferences pref;
    PowerManager pm;
    SensorManager sm;
    Sensor light;
    Brightness brightness;

    float last;
    float highest;
    int brightnessValue;
    boolean scheduled;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Service created...");

        brightness = new Brightness();
        pref = new Preferences();
        highest = pref.getHighestLuxValue();

        pm = (PowerManager) getSystemService(POWER_SERVICE);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        light = sm.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (pm.isInteractive()) RegisterSensorListener(true);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sm.unregisterListener(listener, light);
        unregisterReceiver(receiver);

        if (!pref.isOverrideBrightness()) {
            Log.d(TAG, "Service has ended...");
            return;
        }

        Log.d(TAG, "Schedule restarting service...");
        Intent restartServiceIntent = new Intent(this, this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent =
                PendingIntent.getService(getApplicationContext(), 1,
                        restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);
    }

    private void RegisterSensorListener(boolean state) {
        if (state) {
            if (pref.isOverrideBrightness()) {
                // set brightness to minimum
                LuxSensorChanged(0);
            }
            sm.registerListener(listener, light, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            sm.unregisterListener(listener, light);
        }
    }

    private void LuxSensorChanged(float value) {
        brightnessValue = Brightness.ConvertFromLux(value);

        if (value == last) return;
        else last = value;

        if (value > highest) {
            highest = value;
            pref.setHighestLuxValue(value);
            Log.i(TAG, String.format("Highest Lux: %d", (int)value));
        }

        Intent intent = new Intent("LuxDataReceiver");
        intent.putExtra("current", value);
        intent.putExtra("highest", highest);
        sendBroadcast(intent);

        Log.d(TAG, String.format("Lux value: %d", (int)value));

        if (!pref.isOverrideBrightness()) {
            sendBroadcast(new Intent("BrightnessReceiver"));
            return;
        }

        if (!scheduled) {
            scheduled = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scheduled = false;
                    if (!pm.isInteractive()) return;
                    if (brightness.get() == brightnessValue) return;
                    if (brightness.isAutomatic())
                        brightness.setAutomatic(false);

                    brightness.set(brightnessValue);
                }
            }, 2000);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (intent.getAction() == null) return;
            switch (intent.getAction()) {
                case Intent.ACTION_USER_PRESENT:
                    Log.d(TAG, "Screen [ UNLOCK ]");
                    RegisterSensorListener(true);
                    if (!App.MainActivityShowed) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(context, WakeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }, 1000);
                    }
                    break;

                case Intent.ACTION_SCREEN_OFF:
                    Log.d(TAG, "Screen [ OFF ]");
                    RegisterSensorListener(false);
                    break;
            }
        }
    };

    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //nothing
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            LuxSensorChanged(event.values[0]);
        }
    };
}
