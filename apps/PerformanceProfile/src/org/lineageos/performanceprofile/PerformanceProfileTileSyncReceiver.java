package org.lineageos.performanceprofile;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemProperties;

public class PerformanceProfileTileSyncReceiver extends BroadcastReceiver {
    
    private static final String PROPERTY_PERFORMANCE_PROFILE = "sys.thermal.performance_profile";
    private static final String PROPERTY_PERFORMANCE_CPU = "sys.thermal.performance_cpu";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            String savedProfile = SystemProperties.get(PROPERTY_PERFORMANCE_PROFILE, "1");
            String savedCpu = SystemProperties.get(PROPERTY_PERFORMANCE_CPU, "1");
            
            try {
                SystemProperties.set(PROPERTY_PERFORMANCE_PROFILE, savedProfile);
                SystemProperties.set(PROPERTY_PERFORMANCE_CPU, savedCpu);
            } catch (Exception ignored) {}

            int profileIdx = 1;
            try {
                profileIdx = Integer.parseInt(savedProfile);
            } catch (NumberFormatException ignored) {}
            
            updateAppIcon(context, profileIdx);
        }
    }

    private void updateAppIcon(Context context, int profileIdx) {
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        String targetAlias;

        switch (profileIdx) {
            case 0:  targetAlias = packageName + ".LauncherLow"; break;
            case 2:  targetAlias = packageName + ".LauncherHigh"; break;
            case 1:
            default: targetAlias = packageName + ".LauncherMedium"; break;
        }

        String lowPath = packageName + ".LauncherLow";
        String medPath = packageName + ".LauncherMedium";
        String hiPath = packageName + ".LauncherHigh";

        try {
            if (!lowPath.equals(targetAlias)) {
                pm.setComponentEnabledSetting(new ComponentName(context, lowPath), 
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
            if (!medPath.equals(targetAlias)) {
                pm.setComponentEnabledSetting(new ComponentName(context, medPath), 
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
            if (!hiPath.equals(targetAlias)) {
                pm.setComponentEnabledSetting(new ComponentName(context, hiPath), 
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
            pm.setComponentEnabledSetting(new ComponentName(context, targetAlias), 
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            android.util.Log.e("TileSyncReceiver", "Asynchronous alias swap exception", e);
        }
    }
}
