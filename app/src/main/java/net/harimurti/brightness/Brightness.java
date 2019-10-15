package net.harimurti.brightness;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class Brightness {
    private static final String TAG = "Brightness";
    private Context context;

    private static final int maxLux = 5000; // 25165 is maximum
    private static final int minBrightness = 8; // 0 - 244 (255 is maximum)

    public Brightness() {
        this.context = App.getContext();
    }

    public int get() {
        try {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        }
        catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            return 0;
        }
    }

    public void set(int value) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);
            context.sendBroadcast(new Intent("BrightnessReceiver"));
            Log.d(TAG, String.format("Set value: %d", value));
        }
            catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public boolean isAutomatic() {
        try {
            int mode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            return (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        }
        catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            return false;
        }
    }

    public void setAutomatic(boolean value) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                    value ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            Log.d(TAG, "Set mode to " + (value ? "Automatic" : "Manual"));
        }
        catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public static int ConvertFromLux(float value) {
        double range = maxLux / (255 - minBrightness);
        double math = (value / range) + minBrightness;
        if (math > 255) math = 255;
        return (int) Math.round(math);
    }

    public static int ConvertFromPercent(int value) {
        return Math.round(value * 255 / 100) + 1;
    }
}
