package org.lineageos.performanceprofilebackend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;

public class ProfileCommandReceiver extends BroadcastReceiver {
    private static final String PROPERTY_PERFORMANCE_PROFILE = "persist.vendor.performance_profile";
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("org.lineageos.performanceprofile.ACTION_SET_PROFILE".equals(intent.getAction())) {
            int targetProfile = intent.getIntExtra("profile_index", 1);

            SharedPreferences prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE);
            prefs.edit().putInt("active_profile", targetProfile).apply();

            try {
                SystemProperties.set(PROPERTY_PERFORMANCE_PROFILE, Integer.toString(targetProfile));
            } catch (Exception e) {
            android.util.Log.e("PerfProfileBackend", "SELinux blocked property write!", e);
            }
        }
    }
}
