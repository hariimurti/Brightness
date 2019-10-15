package net.harimurti.brightness;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class MainActivity extends AppCompatActivity {
    Context context;
    Preferences pref;
    Intent intentLuxService;
    Brightness brightness;

    ImageView imgBrightness;
    TextView tvCurrent;
    TextView tvHighest;
    TextView tvBrightness;
    TextView tvBrightnessPercent;
    Button btnAutomatic;
    Button btnManual;
    Button btnOverride;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_title);

        context = getApplicationContext();
        pref = new Preferences();
        brightness = new Brightness();

        imgBrightness = findViewById(R.id.img_brightness);
        tvCurrent = findViewById(R.id.tv_current_lux);
        tvBrightness = findViewById(R.id.tv_brightness_value);
        tvBrightnessPercent = findViewById(R.id.tv_brightness_percent);
        tvHighest = findViewById(R.id.tv_highest_lux);
        tvHighest.setText(String.valueOf(pref.getHighestLuxValue()));
        updateBrightnessValue();

        btnAutomatic = findViewById(R.id.button_automatic);
        btnManual = findViewById(R.id.button_manual);
        btnOverride = findViewById(R.id.button_override);
        if (pref.isOverrideBrightness()) {
            btnAutomatic.setEnabled(true);
            btnManual.setEnabled(true);
            btnOverride.setEnabled(false);
            brightness.setAutomatic(false);
        }
        else {
            if (brightness.isAutomatic()) {
                btnAutomatic.setEnabled(false);
                btnManual.setEnabled(true);
                btnOverride.setEnabled(true);
            }
            else {
                btnAutomatic.setEnabled(true);
                btnManual.setEnabled(false);
                btnOverride.setEnabled(true);
            }
        }

        intentLuxService = new Intent(this, LuxService.class);
        startService(intentLuxService);
    }

    @Override
    protected  void onStart() {
        super.onStart();

        if (!Settings.System.canWrite(this)) {
            Toast.makeText(context, getString(R.string.allow_modifying_system_settings), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            startActivity(intent);
        }

        /*if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(context, getString(R.string.allow_draw_overlay_settings), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            startActivity(intent);
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        App.MainActivityShowed = true;
        registerReceiver(LuxDataReceiver, new IntentFilter("LuxDataReceiver"));
        registerReceiver(BrightnessReceiver, new IntentFilter("BrightnessReceiver"));
    }

    @Override
    protected void onPause() {
        super.onPause();

        App.MainActivityShowed = false;
        unregisterReceiver(LuxDataReceiver);
        unregisterReceiver(BrightnessReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!pref.isOverrideBrightness())
            stopService(intentLuxService);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_restart_service:
                stopService(intentLuxService);
                if (!pref.isOverrideBrightness())
                    startService(intentLuxService);
                break;

            case R.id.action_about:
                MenuAbout();
                break;

            case R.id.action_exit:
                stopService(intentLuxService);
                this.finish();
                break;
        }

        return true;
    }

    private BroadcastReceiver LuxDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float current = intent.getFloatExtra("current", 0);
            tvCurrent.setText(String.valueOf(current));

            float highest = intent.getFloatExtra("highest", 0);
            tvHighest.setText(String.valueOf(highest));
        }
    };

    private BroadcastReceiver BrightnessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBrightnessValue();
        }
    };

    private void updateBrightnessValue() {
        int current = brightness.get();
        int percent = (current * 100) / 255;
        tvBrightness.setText(String.valueOf(current));
        tvBrightnessPercent.setText(String.format("%d%%", percent));
        if (percent > 70)
            imgBrightness.setImageResource(R.drawable.ic_brightness_high);
        else if (percent > 30)
            imgBrightness.setImageResource(R.drawable.ic_brightness_medium);
        else
            imgBrightness.setImageResource(R.drawable.ic_brightness_low);
    }

    public void onButtonPresetClick(View v) {
        int percent = Integer.parseInt(v.getTag().toString());
        int value = Brightness.ConvertFromPercent(percent);
        brightness.set(value);

        Toast toast = Toast.makeText(context, String.format("Set brightness to %d%%", percent), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
        updateBrightnessValue();
    }

    public void onButtonModeClick(View v) {
        String text = "";
        switch (v.getTag().toString()) {
            case "manual":
                text = "Brightness Mode : Manual";

                brightness.setAutomatic(false);
                pref.setOverrideBrightness(false);

                btnAutomatic.setEnabled(true);
                btnManual.setEnabled(false);
                btnOverride.setEnabled(true);
                break;
            case "automatic":
                text = "Brightness Mode : Automatic";

                brightness.setAutomatic(true);
                pref.setOverrideBrightness(false);

                btnAutomatic.setEnabled(false);
                btnManual.setEnabled(true);
                btnOverride.setEnabled(true);
                break;
            case "override":
                text = "Brightness Mode : Override";

                brightness.setAutomatic(false);
                pref.setOverrideBrightness(true);

                btnAutomatic.setEnabled(true);
                btnManual.setEnabled(true);
                btnOverride.setEnabled(false);
                break;
        }

        if (text.isEmpty()) return;
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void MenuAbout() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getString(R.string.text_about))
                .setTitle(R.string.app_title)
                .create();
        dialog.setNeutralButton(getText(R.string.telegram), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://t.me/harimurti"));
                context.startActivity(intent);
            }
        });
        dialog.setNegativeButton(getText(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
