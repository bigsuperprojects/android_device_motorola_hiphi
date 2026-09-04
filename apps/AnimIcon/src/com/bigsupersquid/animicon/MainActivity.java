package com.bigsupersquid.animicon;

import android.app.Activity;
import android.os.Bundle;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

public final class MainActivity extends Activity {
    private static final String PREFS = "state";
    private static final String KEY = "value";
    private static final String[] ALIASES = {
        "com.bigsupersquid.animicon.State0",
        "com.bigsupersquid.animicon.State1",
        "com.bigsupersquid.animicon.State2"
    };
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        int oldState = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY, 0);
        int newState = (oldState + 1) % 3;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY, newState).apply();
        PackageManager pm = getPackageManager();
        for (int i = 0; i < ALIASES.length; i++) {
            pm.setComponentEnabledSetting(new ComponentName(this, ALIASES[i]),
                i == newState ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                              : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        }
        finish();
    }
}
