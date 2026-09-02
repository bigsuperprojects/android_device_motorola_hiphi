package org.lineageos.performanceprofile;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.service.quicksettings.TileService;

public class PerformanceProfileActivity extends Activity {

    private static final String PROPERTY_PERFORMANCE_PROFILE = "sys.thermal.performance_profile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String propValue = SystemProperties.get(PROPERTY_PERFORMANCE_PROFILE, "1");
        int currentProfile;
        try {
            currentProfile = Integer.parseInt(propValue);
        } catch (NumberFormatException e) {
            currentProfile = 1;
        }

        int nextProfile = currentProfile + 1;
        if (nextProfile > 2) {
            nextProfile = 0;
        }

        try {
            SystemProperties.set(PROPERTY_PERFORMANCE_PROFILE, Integer.toString(nextProfile));
        } catch (Exception e) {
            android.util.Log.e("PerfProfile", "System property modification failure", e);
        }

        Rect iconBounds = getIntent().getSourceBounds();
        if (iconBounds != null) {
            Intent serviceIntent = new Intent(this, OverlayService.class);
            serviceIntent.putExtra("current_profile", currentProfile);
            serviceIntent.putExtra("next_profile", nextProfile);
            serviceIntent.putExtra("bounds", iconBounds);
            startService(serviceIntent);
        }

        updateAppIcon(nextProfile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                ComponentName tileComponent = new ComponentName(getPackageName(), "org.lineageos.performanceprofile.PerformanceProfileTileService");
                TileService.requestListeningState(this, tileComponent);
            } catch (Exception ignored) {}
        }

        finish();
        overridePendingTransition(0, 0);
    }

    private void updateAppIcon(int profileIdx) {
        PackageManager pm = getPackageManager();
        String packageName = getPackageName();
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
                pm.setComponentEnabledSetting(new ComponentName(this, lowPath), 
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
            if (!medPath.equals(targetAlias)) {
                pm.setComponentEnabledSetting(new ComponentName(this, medPath), 
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
            if (!hiPath.equals(targetAlias)) {
                pm.setComponentEnabledSetting(new ComponentName(this, hiPath), 
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
            pm.setComponentEnabledSetting(new ComponentName(this, targetAlias), 
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            android.util.Log.e("PerfProfile", "Inline alias switch failure", e);
        }
    }
}
