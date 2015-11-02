package info.korzeniowski.rcontroller.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import info.korzeniowski.rcontroller.R;

public class Prefs {
    private Context context;
    private SharedPreferences sharedPreferences;

    public Prefs(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setBluetoothDevice(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.bluetooth_device_pref), name);
        editor.commit();
    }

    public String getBluetoothDevice() {
        return sharedPreferences.getString(context.getString(R.string.bluetooth_device_pref), "");
    }

    public void setServoFix(int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(context.getString(R.string.servo_fix_pref), value);
        editor.commit();
    }

    public int getServoFix() {
        return sharedPreferences.getInt(context.getString(R.string.servo_fix_pref), 0);
    }
}
