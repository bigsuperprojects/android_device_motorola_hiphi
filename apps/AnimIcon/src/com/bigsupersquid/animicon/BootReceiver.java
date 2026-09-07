package com.bigsupersquid.animicon;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.util.Log;

public final class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "AnimIcon";

    static final String PREFS = "state";
    static final String KEY = "value";
    static final String KEY_BOOT_TIME = "boot_time";

    static final long INITIALIZATION_DELAY_MS = 15_000L;

    private static final String THERMAL_PROFILE_PROPERTY =
            "sys.thermal.perf";

    private static final String[] ALIASES = {
        "com.bigsupersquid.animicon.State0",
        "com.bigsupersquid.animicon.State1",
        "com.bigsupersquid.animicon.State2"
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        Context appContext = context.getApplicationContext();

        int savedState = appContext
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY, 0);

        if (savedState < 0 || savedState >= ALIASES.length) {
            savedState = 0;
        }

        PackageManager packageManager =
                appContext.getPackageManager();

        for (int i = 0; i < ALIASES.length; i++) {
            int componentState = i == savedState
                    ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

            packageManager.setComponentEnabledSetting(
                    new ComponentName(appContext, ALIASES[i]),
                    componentState,
                    PackageManager.DONT_KILL_APP);
        }

        setThermalProfile(savedState);

        appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_BOOT_TIME, System.currentTimeMillis())
                .apply();
    }

    private static void setThermalProfile(int state) {
        try {
            SystemProperties.set(
                    THERMAL_PROFILE_PROPERTY,
                    Integer.toString(state));

            Log.i(
                    TAG,
                    "set " + THERMAL_PROFILE_PROPERTY
                            + "=" + state);
        } catch (RuntimeException exception) {
            Log.e(
                    TAG,
                    "unable to set "
                            + THERMAL_PROFILE_PROPERTY
                            + "=" + state,
                    exception);
        }
    }
}
