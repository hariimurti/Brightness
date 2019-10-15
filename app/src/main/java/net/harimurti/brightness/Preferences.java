package net.harimurti.brightness;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
    private static final String HIGHEST_LUX_VALUE = "HIGHEST_LUX_VALUE";
    private static final String BRIGHTNESS_MODE_OVERRIDE = "BRIGHTNESS_MODE_OVERRIDE";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public Preferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
    }

    private boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    private void setBoolean(String key, boolean value) {
        editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private int getInteger(String key) {
        return preferences.getInt(key, 0);
    }

    private void setInteger(String key, int value) {
        editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    private float getFloat(String key) {
        return preferences.getFloat(key, 0);
    }

    private void setFloat(String key, float value) {
        editor = preferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    private String getString(String key) {
        return preferences.getString(key, "");
    }

    private void setString(String key, String value) {
        editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public boolean isOverrideBrightness()
    {
        return getBoolean(BRIGHTNESS_MODE_OVERRIDE);
    }

    public void setOverrideBrightness(boolean state) {
        setBoolean(BRIGHTNESS_MODE_OVERRIDE, state);
    }

    public float getHighestLuxValue() {
        return getFloat(Preferences.HIGHEST_LUX_VALUE);
    }

    public void setHighestLuxValue(float value) {
        setFloat(Preferences.HIGHEST_LUX_VALUE, value);
    }
}
