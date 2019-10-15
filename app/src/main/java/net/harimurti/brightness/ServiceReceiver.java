package net.harimurti.brightness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceReceiver extends BroadcastReceiver {
    private static final String TAG = "ServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive... " + intent.getAction());
        if (!new Preferences().isOverrideBrightness())
            return;

        context.startService(new Intent(context, LuxService.class));
    }
}
