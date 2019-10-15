package net.harimurti.brightness;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    protected static Context context = null;
    public static boolean MainActivityShowed = false;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }

}
